package com.ruoyi.web.controller.mc;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mc.domain.McGenerationTask;
import com.ruoyi.mc.service.IMcGenerationService;
import com.ruoyi.mc.service.IMcUserPointsService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;

/**
 * 材质生成Controller
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@RestController
@RequestMapping("/mc/generate")
public class McGenerationController extends BaseController
{
    @Autowired
    private IMcGenerationService generationService;

    @Autowired
    private IMcUserPointsService pointsService;

    /**
     * 查询生成任务列表（用户端：需要登录）
     */
    @GetMapping("/list")
    public TableDataInfo list(McGenerationTask mcGenerationTask)
    {
        startPage();
        // 用户必须登录才能访问
        // 管理员可以看所有任务，普通用户只能看自己的任务
        try {
            Long currentUserId = SecurityUtils.getUserId();
            if (!SecurityUtils.isAdmin())
            {
                mcGenerationTask.setUserId(currentUserId);
            }
        } catch (Exception e) {
            // 未登录，返回空列表并提示
            return getDataTable(new ArrayList<>());
        }
        List<McGenerationTask> list = generationService.selectMcGenerationTaskList(mcGenerationTask);
        return getDataTable(list);
    }

    /**
     * 查询所有用户的生成任务列表（管理员专用）
     */
    @PreAuthorize("@ss.hasPermi('mc:generate:list')")
    @GetMapping("/admin/list")
    public TableDataInfo adminList(McGenerationTask mcGenerationTask)
    {
        startPage();
        // 管理员查询所有任务，不做用户过滤
        List<McGenerationTask> list = generationService.selectMcGenerationTaskList(mcGenerationTask);
        return getDataTable(list);
    }

    /**
     * 关闭任务并返还积分（管理员专用）
     */
    @PreAuthorize("@ss.hasPermi('mc:generate:cancel')")
    @Log(title = "关闭任务并返还积分", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{taskId}")
    public AjaxResult cancelTask(@PathVariable Long taskId)
    {
        try
        {
            // 获取任务信息
            McGenerationTask task = generationService.selectMcGenerationTaskByTaskId(taskId);
            if (task == null)
            {
                return error("任务不存在");
            }

            // 只能关闭待处理、处理中、失败的任务
            if (task.getTaskStatus() == 4)
            {
                return error("已完成的任务无法关闭");
            }

            // 更新任务状态为已取消（使用状态5-失败）
            task.setTaskStatus(5);
            task.setErrorMsg("管理员关闭任务");
            generationService.updateMcGenerationTask(task);

            // 如果任务已扣除积分，返还积分
            if (task.getCostPoints() != null && task.getCostPoints() > 0 && task.getUserId() != null && task.getUserId() != 1L)
            {
                pointsService.refundPoints(task.getUserId(), task.getCostPoints(), taskId, "管理员关闭任务退还");
            }

            return success("任务已关闭，积分已返还");
        }
        catch (Exception e)
        {
            logger.error("关闭任务失败: {}", e.getMessage(), e);
            return error("关闭任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取生成任务详细信息（用户端：开放访问）
     */
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return success(generationService.selectMcGenerationTaskByTaskId(taskId));
    }

    /**
     * 新增生成任务（用户端：需要登录，需要积分）
     */
    @Log(title = "材质生成", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McGenerationTask mcGenerationTask)
    {
        Long userId;
        String userName;
        int requiredPoints;

        try {
            // 用户必须登录才能创建任务
            userId = SecurityUtils.getUserId();
            userName = SecurityUtils.getUsername();
        } catch (Exception e) {
            // 未登录，返回错误
            return error("请先登录后再创建生成任务");
        }

        // 根据生成类型计算所需积分
        String generationType = mcGenerationTask.getGenerationType();
        if (generationType == null || generationType.isEmpty()) {
            generationType = "item"; // 默认为物品类型
            mcGenerationTask.setGenerationType(generationType);
        }
        
        switch (generationType) {
            case "item":
                requiredPoints = 100;
                break;
            case "ui":
                requiredPoints = 200;
                break;
            case "complete":
                requiredPoints = 250;
                break;
            default:
                return error("无效的生成类型");
        }

        // 检查积分
        if (!pointsService.hasEnoughPoints(userId, requiredPoints))
        {
            return error("积分不足，当前操作需要" + requiredPoints + "积分");
        }

        // 创建任务 - 直接进入处理中状态
        mcGenerationTask.setTaskNo("TASK_" + System.currentTimeMillis());
        mcGenerationTask.setUserId(userId);
        mcGenerationTask.setUserName(userName);
        mcGenerationTask.setTaskStatus(1); // 直接设置为"处理中"，用户立即看到进度
        mcGenerationTask.setCostPoints(requiredPoints);
        mcGenerationTask.setCreateTime(new Date());
        mcGenerationTask.setStartTime(new Date()); // 设置开始时间

        int result = generationService.insertMcGenerationTask(mcGenerationTask);

        if (result > 0)
        {
            Long taskId = mcGenerationTask.getTaskId();

            // 立即扣除积分
            pointsService.deductPoints(userId, requiredPoints, taskId, mcGenerationTask.getTaskNo(), "生成材质包");

            // 立即异步执行生成任务
            final Long finalUserId = userId;
            final int finalRequiredPoints = requiredPoints;
            new Thread(() -> {
                try
                {
                    generationService.executeGeneration(taskId);
                }
                catch (Exception e)
                {
                    logger.error("生成任务执行失败: {}", e.getMessage(), e);
                    // 失败时退还积分
                    try {
                        pointsService.refundPoints(finalUserId, finalRequiredPoints, taskId, "生成失败退还");
                        logger.info("✅ 积分已返还给用户 {}, 返还 {} 积分", finalUserId, finalRequiredPoints);
                    } catch (Exception refundEx) {
                        logger.error("❌ 积分返还失败: {}", refundEx.getMessage(), refundEx);
                    }
                }
            }).start();

            return success(mcGenerationTask);
        }
        else
        {
            return error("创建任务失败");
        }
    }

    /**
     * 修改生成任务
     */
    @PreAuthorize("@ss.hasPermi('mc:generate:edit')")
    @Log(title = "材质生成", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McGenerationTask mcGenerationTask)
    {
        return toAjax(generationService.updateMcGenerationTask(mcGenerationTask));
    }

    /**
     * 删除生成任务
     */
    @PreAuthorize("@ss.hasPermi('mc:generate:remove')")
    @Log(title = "材质生成", businessType = BusinessType.DELETE)
	@DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(generationService.deleteMcGenerationTaskByTaskIds(taskIds));
    }

    /**
     * 重新生成（用户端：需要登录，需要积分）
     */
    @Log(title = "重新生成材质", businessType = BusinessType.INSERT)
    @PostMapping("/regenerate/{taskId}")
    public AjaxResult regenerate(@PathVariable Long taskId)
    {
        Long userId;
        int requiredPoints;

        try {
            userId = SecurityUtils.getUserId();
        } catch (Exception e) {
            return error("请先登录后再重新生成");
        }

        // 检查任务是否存在
        McGenerationTask task = generationService.selectMcGenerationTaskByTaskId(taskId);
        if (task == null)
        {
            return error("任务不存在");
        }

        // 验证任务所有权：只能重新生成自己的任务（管理员除外）
        if (!SecurityUtils.isAdmin() && !task.getUserId().equals(userId))
        {
            return error("无权限重新生成该任务");
        }

        // 根据任务的生成类型计算所需积分
        String generationType = task.getGenerationType();
        if (generationType == null || generationType.isEmpty()) {
            generationType = "item";
        }
        
        switch (generationType) {
            case "item":
                requiredPoints = 100;
                break;
            case "ui":
                requiredPoints = 200;
                break;
            case "complete":
                requiredPoints = 250;
                break;
            default:
                requiredPoints = 100;
        }

        // 检查积分
        if (!pointsService.hasEnoughPoints(userId, requiredPoints))
        {
            return error("积分不足，重新生成需要" + requiredPoints + "积分");
        }

        // 立即更新任务状态为"处理中"
        task.setTaskStatus(1); // 处理中
        task.setErrorMsg(null); // 清除旧的错误信息
        task.setStartTime(new Date());
        generationService.updateMcGenerationTask(task);

        // 扣除积分
        pointsService.deductPoints(userId, requiredPoints, taskId, null, "重新生成材质");

        // 异步重新生成
        final Long finalUserId = userId;
        final int finalRequiredPoints = requiredPoints;
        new Thread(() -> {
            try
            {
                generationService.executeGeneration(taskId);
            }
            catch (Exception e)
            {
                logger.error("重新生成失败: {}", e.getMessage(), e);
                // 失败退还积分
                try {
                    pointsService.refundPoints(finalUserId, finalRequiredPoints, taskId, "生成失败退还");
                    logger.info("✅ 积分已返还给用户 {}, 返还 {} 积分", finalUserId, finalRequiredPoints);
                } catch (Exception refundEx) {
                    logger.error("❌ 积分返还失败: {}", refundEx.getMessage(), refundEx);
                }
            }
        }).start();

        return success("重新生成任务已提交");
    }

    /**
     * 开始处理任务（管理员）
     * 手动触发AI生成流程
     */
    @PreAuthorize("@ss.hasPermi('mc:generate:process')")
    @Log(title = "开始处理材质生成任务", businessType = BusinessType.UPDATE)
    @PostMapping("/process/{taskId}")
    public AjaxResult processTask(@PathVariable Long taskId)
    {
        try
        {
            // 获取任务信息
            McGenerationTask task = generationService.selectMcGenerationTaskByTaskId(taskId);
            if (task == null)
            {
                return error("任务不存在");
            }

            // 检查任务状态
            if (task.getTaskStatus() != 0)
            {
                return error("只能处理待处理状态的任务");
            }

            // 检查用户积分（如果是登录用户且需要扣除积分）
            int requiredPoints = task.getCostPoints();
            if (requiredPoints > 0 && task.getUserId() != null && task.getUserId() != 1L)
            {
                if (!pointsService.hasEnoughPoints(task.getUserId(), requiredPoints))
                {
                    return error("用户积分不足，无法处理该任务");
                }
                // 扣除积分
                pointsService.deductPoints(task.getUserId(), requiredPoints, taskId, task.getTaskNo(), "生成材质包");
            }

            // 异步执行生成任务
            new Thread(() -> {
                try
                {
                    generationService.executeGeneration(taskId);
                }
                catch (Exception e)
                {
                    logger.error("生成任务执行失败: {}", e.getMessage(), e);
                    // 失败时退还积分
                    if (requiredPoints > 0 && task.getUserId() != null && task.getUserId() != 1L)
                    {
                        try {
                            pointsService.refundPoints(task.getUserId(), requiredPoints, taskId, "生成失败退还");
                            logger.info("✅ 积分已返还给用户 {}, 返还 {} 积分", task.getUserId(), requiredPoints);
                        } catch (Exception refundEx) {
                            logger.error("❌ 积分返还失败: {}", refundEx.getMessage(), refundEx);
                        }
                    }
                }
            }).start();

            return success("任务已开始处理");
        }
        catch (Exception e)
        {
            logger.error("处理任务失败: {}", e.getMessage(), e);
            return error("处理任务失败: " + e.getMessage());
        }
    }
}

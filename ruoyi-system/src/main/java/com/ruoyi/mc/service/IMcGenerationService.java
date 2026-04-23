package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McGenerationTask;
import java.util.List;

/**
 * 材质生成Service接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface IMcGenerationService
{
    /**
     * 查询生成任务
     *
     * @param taskId 生成任务ID
     * @return 生成任务
     */
    public McGenerationTask selectMcGenerationTaskByTaskId(Long taskId);

    /**
     * 查询生成任务列表
     *
     * @param mcGenerationTask 生成任务
     * @return 生成任务集合
     */
    public List<McGenerationTask> selectMcGenerationTaskList(McGenerationTask mcGenerationTask);

    /**
     * 新增生成任务
     *
     * @param mcGenerationTask 生成任务
     * @return 结果
     */
    public int insertMcGenerationTask(McGenerationTask mcGenerationTask);

    /**
     * 修改生成任务
     *
     * @param mcGenerationTask 生成任务
     * @return 结果
     */
    public int updateMcGenerationTask(McGenerationTask mcGenerationTask);

    /**
     * 批量删除生成任务
     *
     * @param taskIds 需要删除的生成任务ID
     * @return 结果
     */
    public int deleteMcGenerationTaskByTaskIds(Long[] taskIds);

    /**
     * 删除生成任务信息
     *
     * @param taskId 生成任务ID
     * @return 结果
     */
    public int deleteMcGenerationTaskByTaskId(Long taskId);

    /**
     * 执行完整的材质包生成流程
     * 1. 调用Gemini 2.5 Pro优化提示词
     * 2. 调用Gemini 3 Pro Image生成图片
     * 3. 图片预处理（抠白底）
     * 4. 切割成50张32x32图片
     *
     * @param taskId 任务ID
     * @return 任务对象
     */
    public McGenerationTask executeGeneration(Long taskId);

    /**
     * 统计用户当前处理中的任务数量
     *
     * @param userId 用户ID
     * @return 处理中的任务数量
     */
    public int countProcessingTasksByUserId(Long userId);
}

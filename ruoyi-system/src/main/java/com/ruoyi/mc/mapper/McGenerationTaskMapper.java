package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McGenerationTask;

/**
 * 生成任务Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface McGenerationTaskMapper
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
     * 删除生成任务
     *
     * @param taskId 生成任务ID
     * @return 结果
     */
    public int deleteMcGenerationTaskByTaskId(Long taskId);

    /**
     * 批量删除生成任务
     *
     * @param taskIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcGenerationTaskByTaskIds(Long[] taskIds);

    /**
     * 统计用户处理中的任务数量
     *
     * @param userId 用户ID
     * @return 处理中任务数量
     */
    public int countProcessingTasksByUserId(Long userId);

    /**
     * 查询超时的生成任务
     * 状态为1/2/3（处理中），且开始时间超过10分钟
     *
     * @return 超时任务列表
     */
    public List<McGenerationTask> selectTimeoutTasks();
}

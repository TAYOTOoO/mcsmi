package com.ruoyi.quartz.task;

import com.ruoyi.mc.domain.McGenerationTask;
import com.ruoyi.mc.mapper.McGenerationTaskMapper;
import com.ruoyi.mc.service.IMcUserPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 生成任务定时任务
 *
 * @author ruoyi
 * @date 2026-02-08
 */
@Component("generationTaskScheduledTask")
public class GenerationTaskScheduledTask
{
    private static final Logger log = LoggerFactory.getLogger(GenerationTaskScheduledTask.class);

    @Autowired
    private McGenerationTaskMapper generationTaskMapper;

    @Autowired
    private IMcUserPointsService userPointsService;

    /**
     * 检查并处理超时任务
     * 每10分钟执行一次
     * 将超过10分钟仍在处理中的任务标记为失败并退还积分
     */
    public void checkTimeoutTasks()
    {
        log.info("开始检查超时生成任务...");
        try
        {
            // 查询超时的任务（状态为1/2/3，且开始时间超过10分钟）
            List<McGenerationTask> timeoutTasks = generationTaskMapper.selectTimeoutTasks();

            int processedCount = 0;
            for (McGenerationTask task : timeoutTasks)
            {
                try
                {
                    log.warn("检测到超时任务: taskId={}, userId={}, status={}, startTime={}",
                             task.getTaskId(), task.getUserId(), task.getTaskStatus(), task.getStartTime());

                    // 更新任务状态为失败
                    task.setTaskStatus(5); // 5=失败
                    task.setCompleteTime(new Date());
                    task.setErrorMsg("任务超时（超过10分钟未完成），已自动取消");
                    generationTaskMapper.updateMcGenerationTask(task);

                    // 退还积分
                    if (task.getCostPoints() != null && task.getCostPoints() > 0)
                    {
                        userPointsService.refundPoints(
                            task.getUserId(),
                            task.getCostPoints(),
                            task.getTaskId(),
                            "生成超时退款"
                        );
                        log.info("超时任务 {} 已退还积分: {} 分", task.getTaskId(), task.getCostPoints());
                    }

                    processedCount++;
                }
                catch (Exception e)
                {
                    log.error("处理超时任务 {} 失败", task.getTaskId(), e);
                }
            }

            log.info("超时任务检查完成，共处理 {} 个任务", processedCount);
        }
        catch (Exception e)
        {
            log.error("检查超时任务失败", e);
        }
    }
}

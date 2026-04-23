package com.ruoyi.quartz.task;

import com.ruoyi.mc.service.IMcCroppedImageTempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 清理过期裁剪图片临时文件任务
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@Component("croppedImageCleanupTask")
public class CroppedImageCleanupTask
{
    private static final Logger log = LoggerFactory.getLogger(CroppedImageCleanupTask.class);

    @Autowired
    private IMcCroppedImageTempService croppedImageTempService;

    /**
     * 清理过期的裁剪图片临时文件
     * 默认每天凌晨2点执行
     */
    public void execute()
    {
        log.info("开始清理过期的裁剪图片临时文件...");
        try
        {
            int count = croppedImageTempService.cleanupExpiredBatches();
            log.info("清理完成，共清理{}个过期批次", count);
        }
        catch (Exception e)
        {
            log.error("清理过期文件失败", e);
        }
    }
}

package com.ruoyi.mc.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 裁剪图片临时存储Service接口
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public interface IMcCroppedImageTempService
{
    /**
     * 批量上传裁剪图片到临时存储
     *
     * @param files 裁剪后的图片文件数组
     * @param taskId 任务ID
     * @return 批次ID
     */
    String uploadCroppedImages(MultipartFile[] files, Long taskId);

    /**
     * 根据批次ID获取裁剪图片列表
     *
     * @param batchId 批次ID
     * @return 图片URL列表
     */
    List<String> getCroppedImages(String batchId);

    /**
     * 获取批次信息（包括图片列表）
     *
     * @param batchId 批次ID
     * @return 批次信息Map（包含imageUrls、taskId等）
     */
    Map<String, Object> getBatchInfo(String batchId);

    /**
     * 删除批次（用户完成编辑后清理）
     *
     * @param batchId 批次ID
     */
    void deleteBatch(String batchId);

    /**
     * 清理过期的临时文件（超过24小时）
     *
     * @return 清理的文件数量
     */
    int cleanupExpiredBatches();
}

package com.ruoyi.mc.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mc.service.IMcCroppedImageTempService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 裁剪图片临时存储Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@Service
public class McCroppedImageTempServiceImpl implements IMcCroppedImageTempService
{
    private static final Logger log = LoggerFactory.getLogger(McCroppedImageTempServiceImpl.class);

    @Value("${ruoyi.profile}")
    private String profilePath;

    private static final String TEMP_DIR = "/mc/cropped_temp/";
    private static final long EXPIRE_HOURS = 24; // 24小时过期

    /**
     * 批量上传裁剪图片到临时存储
     */
    @Override
    public String uploadCroppedImages(MultipartFile[] files, Long taskId)
    {
        if (files == null || files.length == 0)
        {
            throw new ServiceException("没有上传的文件");
        }

        try
        {
            // 生成批次ID：taskId_timestamp
            String batchId = taskId + "_" + System.currentTimeMillis();
            String batchDir = profilePath + TEMP_DIR + batchId;

            // 创建批次目录
            Files.createDirectories(Paths.get(batchDir));

            // 保存所有图片
            for (int i = 0; i < files.length; i++)
            {
                MultipartFile file = files[i];
                if (file.isEmpty())
                {
                    continue;
                }

                // 保留原文件名，或使用索引命名
                String fileName = file.getOriginalFilename();
                if (fileName == null || fileName.isEmpty())
                {
                    fileName = "cropped_" + i + ".png";
                }

                File destFile = new File(batchDir, fileName);
                file.transferTo(destFile);

                log.debug("保存裁剪图片: {}", destFile.getAbsolutePath());
            }

            log.info("批量上传完成，批次ID: {}, 文件数: {}", batchId, files.length);
            return batchId;
        }
        catch (IOException e)
        {
            log.error("上传裁剪图片失败", e);
            throw new ServiceException("上传裁剪图片失败: " + e.getMessage());
        }
    }

    /**
     * 根据批次ID获取裁剪图片列表
     */
    @Override
    public List<String> getCroppedImages(String batchId)
    {
        String batchDir = profilePath + TEMP_DIR + batchId;
        File dir = new File(batchDir);

        if (!dir.exists() || !dir.isDirectory())
        {
            throw new ServiceException("批次不存在或已过期");
        }

        File[] imageFiles = dir.listFiles((d, name) ->
            name.toLowerCase().endsWith(".png") ||
            name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg")
        );

        if (imageFiles == null || imageFiles.length == 0)
        {
            return new ArrayList<>();
        }

        // 转换为URL
        return Arrays.stream(imageFiles)
            .map(file -> TEMP_DIR + batchId + "/" + file.getName())
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * 获取批次信息
     */
    @Override
    public Map<String, Object> getBatchInfo(String batchId)
    {
        Map<String, Object> info = new HashMap<>();

        List<String> imageUrls = getCroppedImages(batchId);
        info.put("batchId", batchId);
        info.put("imageUrls", imageUrls);
        info.put("imageCount", imageUrls.size());

        // 从batchId解析taskId
        String[] parts = batchId.split("_");
        if (parts.length >= 1)
        {
            try
            {
                info.put("taskId", Long.parseLong(parts[0]));
            }
            catch (NumberFormatException e)
            {
                log.warn("无法解析taskId从batchId: {}", batchId);
            }
        }

        return info;
    }

    /**
     * 删除批次
     */
    @Override
    public void deleteBatch(String batchId)
    {
        String batchDir = profilePath + TEMP_DIR + batchId;
        File dir = new File(batchDir);

        if (!dir.exists())
        {
            return;
        }

        try
        {
            deleteDirectory(dir);
            log.info("删除批次: {}", batchId);
        }
        catch (IOException e)
        {
            log.error("删除批次失败: {}", batchId, e);
        }
    }

    /**
     * 清理过期的临时文件
     */
    @Override
    public int cleanupExpiredBatches()
    {
        String tempBaseDir = profilePath + TEMP_DIR;
        File baseDir = new File(tempBaseDir);

        if (!baseDir.exists() || !baseDir.isDirectory())
        {
            return 0;
        }

        Instant expireTime = Instant.now().minus(EXPIRE_HOURS, ChronoUnit.HOURS);
        int cleanedCount = 0;

        File[] batchDirs = baseDir.listFiles(File::isDirectory);
        if (batchDirs == null)
        {
            return 0;
        }

        for (File batchDir : batchDirs)
        {
            try
            {
                Path path = batchDir.toPath();
                BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                Instant createTime = attrs.creationTime().toInstant();

                if (createTime.isBefore(expireTime))
                {
                    deleteDirectory(batchDir);
                    cleanedCount++;
                    log.info("清理过期批次: {}", batchDir.getName());
                }
            }
            catch (IOException e)
            {
                log.warn("清理批次失败: {}", batchDir.getName(), e);
            }
        }

        log.info("清理过期批次完成，共清理{}个", cleanedCount);
        return cleanedCount;
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File directory) throws IOException
    {
        if (!directory.exists())
        {
            return;
        }

        if (directory.isDirectory())
        {
            File[] files = directory.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    deleteDirectory(file);
                }
            }
        }

        if (!directory.delete())
        {
            throw new IOException("Failed to delete: " + directory.getAbsolutePath());
        }
    }
}

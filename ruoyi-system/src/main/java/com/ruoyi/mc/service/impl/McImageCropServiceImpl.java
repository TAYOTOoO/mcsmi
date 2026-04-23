package com.ruoyi.mc.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mc.domain.*;
import com.ruoyi.mc.mapper.McGenerationTaskMapper;
import com.ruoyi.mc.service.IMcImageCropService;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * MC图片裁剪Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@Service
public class McImageCropServiceImpl implements IMcImageCropService
{
    private static final Logger log = LoggerFactory.getLogger(McImageCropServiceImpl.class);

    @Autowired
    private McGenerationTaskMapper generationTaskMapper;

    @Value("${ruoyi.profile}")
    private String profilePath;

    /**
     * 裁剪图片并按版本命名规则打包
     *
     * @param request 裁剪请求
     * @return 裁剪结果
     */
    @Override
    public CropResult cropAndPackage(CropRequest request)
    {
        log.info("开始裁剪任务: {}", request);

        try
        {
            // 1. 获取任务信息
            McGenerationTask task = generationTaskMapper.selectMcGenerationTaskByTaskId(request.getTaskId());
            if (task == null)
            {
                throw new ServiceException("任务不存在");
            }

            // 2. 获取图片路径（优先使用 generatedImageUrl，如果为空则使用 imageUrl）
            String imageUrl = task.getGeneratedImageUrl();
            if (StringUtils.isEmpty(imageUrl))
            {
                imageUrl = task.getImageUrl();
            }

            if (StringUtils.isEmpty(imageUrl))
            {
                throw new ServiceException("任务未生成图片");
            }

            // 转换为本地文件路径
            String imagePath = convertUrlToLocalPath(imageUrl);
            File imageFile = new File(imagePath);
            if (!imageFile.exists())
            {
                throw new ServiceException("图片文件不存在: " + imagePath);
            }

            log.info("图片路径: {}", imagePath);

            // 3. 裁剪图片
            List<File> croppedFiles = cropImage(imagePath, request.getHorizontalLines(), request.getVerticalLines());
            log.info("裁剪完成，共{}张图片", croppedFiles.size());

            // 4. 根据版本命名规则重命名
            int rows = request.getHorizontalLines().size() + 1;
            int cols = request.getVerticalLines().size() + 1;
            List<File> renamedFiles = renameByVersionRule(croppedFiles, request.getVersionName(), rows, cols);
            log.info("重命名完成");

            // 5. 打包ZIP
            String zipFileName = "textures_" + task.getTaskNo() + "_" + request.getVersionName() + "_" + System.currentTimeMillis() + ".zip";
            String zipPath = packToZip(renamedFiles, zipFileName);
            log.info("打包完成: {}", zipPath);

            // 6. 转换为下载URL
            String zipUrl = convertLocalPathToUrl(zipPath);

            // 7. 获取文件大小
            File zipFile = new File(zipPath);
            long fileSize = zipFile.length();

            // 8. 清理临时文件
            cleanupTempFiles(croppedFiles);
            cleanupTempFiles(renamedFiles);

            return new CropResult(zipUrl, renamedFiles.size(), fileSize);
        }
        catch (Exception e)
        {
            log.error("裁剪打包失败", e);
            throw new ServiceException("裁剪打包失败: " + e.getMessage());
        }
    }

    /**
     * 裁剪单张图片为多个小图
     *
     * @param imagePath 原图路径
     * @param hLines 横线Y坐标数组
     * @param vLines 竖线X坐标数组
     * @return 裁剪后的文件列表
     */
    @Override
    public List<File> cropImage(String imagePath, List<Integer> hLines, List<Integer> vLines)
    {
        List<File> result = new ArrayList<>();

        try
        {
            // 读取原图
            BufferedImage sourceImage = ImageIO.read(new File(imagePath));
            int imgWidth = sourceImage.getWidth();
            int imgHeight = sourceImage.getHeight();

            log.info("原图尺寸: {}x{}", imgWidth, imgHeight);

            // 计算所有矩形区域
            List<Rectangle> rectangles = calculateRectangles(hLines, vLines, imgWidth, imgHeight);
            log.info("需要裁剪{}个区域", rectangles.size());

            // 创建临时目录
            String tempDir = profilePath + "/mc/crop_temp/" + System.currentTimeMillis();
            Files.createDirectories(Paths.get(tempDir));

            // 裁剪每个区域
            for (int i = 0; i < rectangles.size(); i++)
            {
                Rectangle rect = rectangles.get(i);

                // 裁剪子图（保持透明通道）
                BufferedImage croppedImage = new BufferedImage(
                    rect.width, rect.height, BufferedImage.TYPE_INT_ARGB
                );
                croppedImage.getGraphics().drawImage(
                    sourceImage,
                    0, 0, rect.width, rect.height,
                    rect.x, rect.y, rect.x + rect.width, rect.y + rect.height,
                    null
                );

                // 保存为PNG
                File file = new File(tempDir, "temp_" + i + ".png");
                ImageIO.write(croppedImage, "PNG", file);
                result.add(file);
            }

            log.info("裁剪完成，保存至: {}", tempDir);
            return result;
        }
        catch (IOException e)
        {
            log.error("裁剪图片失败", e);
            throw new ServiceException("裁剪图片失败: " + e.getMessage());
        }
    }

    /**
     * 根据版本命名规则重命名文件
     * 注意：此方法已废弃，新方案中用户将在前端自行命名
     *
     * @param files 文件列表
     * @param versionName 版本号
     * @param rows 行数
     * @param cols 列数
     * @return 重命名后的文件列表
     */
    @Override
    public List<File> renameByVersionRule(List<File> files, String versionName, int rows, int cols)
    {
        List<File> result = new ArrayList<>();

        try
        {
            // 版本命名系统已废弃，使用简单的索引命名
            log.warn("版本命名系统已废弃，使用简单索引命名: texture_row_col.png");

            // 创建重命名目录
            String parentDir = files.get(0).getParent();
            String renameDir = parentDir + "_renamed";
            Files.createDirectories(Paths.get(renameDir));

            // 按行列顺序重命名
            for (int row = 0; row < rows; row++)
            {
                for (int col = 0; col < cols; col++)
                {
                    int index = row * cols + col;
                    if (index >= files.size())
                    {
                        log.warn("文件索引超出范围: {}", index);
                        break;
                    }

                    File sourceFile = files.get(index);
                    String fileName = "texture_" + row + "_" + col + ".png";

                    File targetFile = new File(renameDir, fileName);
                    Files.copy(sourceFile.toPath(), targetFile.toPath());
                    result.add(targetFile);

                    log.debug("重命名: {} -> {}", sourceFile.getName(), fileName);
                }
            }

            log.info("重命名完成，共{}个文件", result.size());
            return result;
        }
        catch (IOException e)
        {
            log.error("重命名文件失败", e);
            throw new ServiceException("重命名文件失败: " + e.getMessage());
        }
    }

    /**
     * 打包文件为ZIP
     *
     * @param files 文件列表
     * @param zipFileName ZIP文件名
     * @return ZIP文件路径
     */
    @Override
    public String packToZip(List<File> files, String zipFileName)
    {
        try
        {
            // 创建下载目录
            String downloadDir = profilePath + "/mc/downloads";
            Files.createDirectories(Paths.get(downloadDir));

            String zipPath = downloadDir + "/" + zipFileName;
            ZipFile zipFile = new ZipFile(zipPath);

            ZipParameters parameters = new ZipParameters();
            parameters.setIncludeRootFolder(false);

            // 添加所有文件
            for (File file : files)
            {
                zipFile.addFile(file, parameters);
            }

            log.info("ZIP打包完成: {}", zipPath);
            return zipPath;
        }
        catch (IOException e)
        {
            log.error("打包ZIP失败", e);
            throw new ServiceException("打包ZIP失败: " + e.getMessage());
        }
    }

    /**
     * 计算所有矩形区域
     */
    private List<Rectangle> calculateRectangles(List<Integer> hLines, List<Integer> vLines, int imgWidth, int imgHeight)
    {
        // 添加边界坐标
        List<Integer> xCoords = new ArrayList<>(vLines);
        xCoords.add(0, 0);
        xCoords.add(imgWidth);
        Collections.sort(xCoords);

        List<Integer> yCoords = new ArrayList<>(hLines);
        yCoords.add(0, 0);
        yCoords.add(imgHeight);
        Collections.sort(yCoords);

        // 生成所有矩形区域
        List<Rectangle> rectangles = new ArrayList<>();
        for (int j = 0; j < yCoords.size() - 1; j++)
        {
            for (int i = 0; i < xCoords.size() - 1; i++)
            {
                int x = xCoords.get(i);
                int y = yCoords.get(j);
                int width = xCoords.get(i + 1) - x;
                int height = yCoords.get(j + 1) - y;

                rectangles.add(new Rectangle(x, y, width, height));
            }
        }

        return rectangles;
    }

    /**
     * 矩形区域类
     */
    private static class Rectangle
    {
        int x, y, width, height;

        Rectangle(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * 将URL转换为本地文件路径
     */
    private String convertUrlToLocalPath(String url)
    {
        if (url.startsWith("http://") || url.startsWith("https://"))
        {
            return url; // 如果是HTTP URL，直接返回
        }

        // 去掉前缀 /mc/
        if (url.startsWith("/mc/"))
        {
            url = url.substring(4);
        }

        return profilePath + "/mc/" + url;
    }

    /**
     * 将本地路径转换为下载URL
     */
    private String convertLocalPathToUrl(String localPath)
    {
        // 提取相对路径
        String relativePath = localPath;
        if (localPath.contains("/mc/"))
        {
            int index = localPath.lastIndexOf("/mc/");
            relativePath = localPath.substring(index);
        }
        else if (localPath.contains("\\mc\\"))
        {
            int index = localPath.lastIndexOf("\\mc\\");
            relativePath = localPath.substring(index).replace("\\", "/");
        }

        return relativePath;
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(List<File> files)
    {
        if (files == null || files.isEmpty())
        {
            return;
        }

        try
        {
            // 删除所有文件
            for (File file : files)
            {
                if (file.exists())
                {
                    file.delete();
                }
            }

            // 删除空目录
            File parentDir = files.get(0).getParentFile();
            if (parentDir.exists() && parentDir.list().length == 0)
            {
                parentDir.delete();
            }
        }
        catch (Exception e)
        {
            log.warn("清理临时文件失败", e);
        }
    }
}

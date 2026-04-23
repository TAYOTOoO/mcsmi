package com.ruoyi.mc.service.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.service.IMcImageProcessService;
import com.ruoyi.common.exception.ServiceException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图片处理Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@Service
public class McImageProcessServiceImpl implements IMcImageProcessService
{
    private static final Logger log = LoggerFactory.getLogger(McImageProcessServiceImpl.class);

    @Value("${mc.temp.path:D:/mc/temp}")
    private String tempPath;

    @Value("/www/wwwroot/mc/textures")
    private String texturePath;

    /**
     * 下载图片到本地
     */
    @Override
    public String downloadImage(String imageUrl, String taskNo)
    {
        log.info("开始下载图片: {}", imageUrl);

        try
        {
            File tempDir = new File(tempPath);
            if (!tempDir.exists())
            {
                tempDir.mkdirs();
            }

            String fileName = "raw_" + taskNo + ".png";
            String filePath = tempPath + File.separator + fileName;

            if (imageUrl.startsWith("http"))
            {
                // 从URL下载
                URL url = new URL(imageUrl);
                FileUtils.copyURLToFile(url, new File(filePath));
            }
            else
            {
                // 已经是本地路径
                return imageUrl;
            }

            log.info("图片下载完成: {}", filePath);
            return filePath;
        }
        catch (Exception e)
        {
            log.error("图片下载失败: {}", e.getMessage(), e);
            throw new ServiceException("图片下载失败: " + e.getMessage());
        }
    }

    /**
     * 去除白色背景（边界洪水填充算法）
     */
    @Override
    public String removeWhiteBackground(String inputPath, String taskNo)
    {
        log.info("开始抠除白底: {}", inputPath);

        try
        {
            File inputFile = new File(inputPath);
            if (!inputFile.exists())
            {
                throw new ServiceException("输入文件不存在: " + inputPath);
            }

            BufferedImage source = ImageIO.read(inputFile);
            BufferedImage result = removeWhiteBackgroundAlgorithm(source);

            // 保存处理后的图片
            String outputPath = inputPath.replace("raw_", "processed_");
            ImageIO.write(result, "PNG", new File(outputPath));

            log.info("抠图完成: {}", outputPath);
            return outputPath;
        }
        catch (Exception e)
        {
            log.error("抠图失败: {}", e.getMessage(), e);
            throw new ServiceException("抠图失败: " + e.getMessage());
        }
    }

    /**
     * 抠白底核心算法
     */
    private BufferedImage removeWhiteBackgroundAlgorithm(BufferedImage source)
    {
        int width = source.getWidth();
        int height = source.getHeight();

        log.info("图片尺寸: {}×{}", width, height);

        // 创建ARGB格式的结果图
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();

        // 标记背景像素
        boolean[][] isBackground = new boolean[width][height];
        int whiteThreshold = 240; // RGB > 240 视为白色

        log.info("开始边界洪水填充...");

        // 从四条边开始洪水填充
        for (int x = 0; x < width; x++)
        {
            floodFill(result, isBackground, x, 0, whiteThreshold);
            floodFill(result, isBackground, x, height - 1, whiteThreshold);
        }

        for (int y = 0; y < height; y++)
        {
            floodFill(result, isBackground, 0, y, whiteThreshold);
            floodFill(result, isBackground, width - 1, y, whiteThreshold);
        }

        // 统计并透明化背景像素
        int bgCount = 0;
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (isBackground[x][y])
                {
                    result.setRGB(x, y, 0x00FFFFFF); // 完全透明
                    bgCount++;
                }
            }
        }

        log.info("已透明化{}个背景像素", bgCount);
        return result;
    }

    /**
     * 洪水填充算法（非递归，使用队列）
     */
    private void floodFill(BufferedImage image, boolean[][] isBackground, int startX, int startY, int threshold)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        if (startX < 0 || startX >= width || startY < 0 || startY >= height || isBackground[startX][startY])
        {
            return;
        }

        int startColor = image.getRGB(startX, startY);
        if (!isWhiteColor(startColor, threshold))
        {
            return;
        }

        Queue<Point> queue = new LinkedList<>();
        queue.offer(new Point(startX, startY));
        isBackground[startX][startY] = true;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        while (!queue.isEmpty())
        {
            Point p = queue.poll();

            for (int i = 0; i < 4; i++)
            {
                int nx = p.x + dx[i];
                int ny = p.y + dy[i];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !isBackground[nx][ny])
                {
                    int color = image.getRGB(nx, ny);
                    if (isWhiteColor(color, threshold))
                    {
                        isBackground[nx][ny] = true;
                        queue.offer(new Point(nx, ny));
                    }
                }
            }
        }
    }

    /**
     * 判断是否为白色
     */
    private boolean isWhiteColor(int rgb, int threshold)
    {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return r > threshold && g > threshold && b > threshold;
    }

    /**
     * 自动切割图片为10×5网格（50张32×32）
     */
    @Override
    public List<String> autoSplitGrid(String inputPath, Integer pixelSize, Integer rows, Integer cols, String taskNo)
    {
        log.info("开始切割图片: {}, 尺寸: {}×{}, 网格: {}行×{}列", inputPath, pixelSize, pixelSize, rows, cols);

        try
        {
            BufferedImage source = ImageIO.read(new File(inputPath));

            int expectedWidth = cols * pixelSize; // 320
            int expectedHeight = rows * pixelSize; // 160

            log.info("期望尺寸: {}×{}, 实际尺寸: {}×{}", expectedWidth, expectedHeight, source.getWidth(), source.getHeight());

            // 验证或调整图片尺寸
            if (source.getWidth() != expectedWidth || source.getHeight() != expectedHeight)
            {
                log.warn("图片尺寸不匹配，尝试缩放...");
                source = resizeImage(source, expectedWidth, expectedHeight);
            }

            List<String> texturePaths = new ArrayList<>(rows * cols);
            String outputDir = texturePath + File.separator + "task_" + taskNo;
            File outputDirFile = new File(outputDir);
            if (!outputDirFile.exists())
            {
                outputDirFile.mkdirs();
            }

            // 切割
            for (int row = 0; row < rows; row++)
            {
                for (int col = 0; col < cols; col++)
                {
                    int x = col * pixelSize;
                    int y = row * pixelSize;

                    BufferedImage cell = source.getSubimage(x, y, pixelSize, pixelSize);

                    // 保存
                    int position = row * cols + col;
                    String fileName = String.format("texture_%02d_r%d_c%d.png", position, row, col);
                    String filePath = outputDir + File.separator + fileName;
                    ImageIO.write(cell, "PNG", new File(filePath));

                    texturePaths.add(filePath);
                }
            }

            log.info("切割完成，共{}张图片", texturePaths.size());
            return texturePaths;
        }
        catch (Exception e)
        {
            log.error("图片切割失败: {}", e.getMessage(), e);
            throw new ServiceException("图片切割失败: " + e.getMessage());
        }
    }

    /**
     * 缩放图片
     */
    private BufferedImage resizeImage(BufferedImage source, int targetWidth, int targetHeight)
    {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }

    /**
     * 将本地文件路径转换为相对路径（供前端通过代理访问）
     */
    private String convertToHttpUrl(String localPath)
    {
        // D:/mc/textures/task_xxx/texture_01.png -> /mc/textures/task_xxx/texture_01.png
        // D:/mc/temp/generated_xxx.png -> /mc/temp/generated_xxx.png

        if (localPath.startsWith("http://") || localPath.startsWith("https://"))
        {
            return localPath; // 已经是HTTP URL
        }

        // 提取相对路径部分
        String relativePath = localPath;
        if (localPath.contains("\\mc\\") || localPath.contains("/mc/"))
        {
            int index = localPath.lastIndexOf("\\mc\\");
            if (index == -1)
            {
                index = localPath.lastIndexOf("/mc/");
            }
            if (index != -1)
            {
                relativePath = localPath.substring(index + 1).replace("\\", "/");
            }
        }

        // 返回相对路径，前端会通过 /api 代理访问
        String relativeUrl = "/" + relativePath;
        log.debug("转换路径: {} -> {}", localPath, relativeUrl);
        return relativeUrl;
    }
}

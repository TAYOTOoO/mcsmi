package com.ruoyi.mc.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MC材质图片处理工具类
 * 核心功能：
 * 1. 白色背景去除（转透明）
 * 2. 按固定网格切割大图为4个版本
 * 3. 调整尺寸为16×16或16×2^n
 *
 * @author ruoyi
 */
public class McImageProcessUtil {

    /**
     * 去除白色背景，转为透明
     * @param src 源图片
     * @param threshold 白色阈值(0-255)，默认240
     * @return 透明背景图片
     */
    public static BufferedImage removeWhiteBackground(BufferedImage src, int threshold) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // 判断是否为白色或接近白色
                if (red >= threshold && green >= threshold && blue >= threshold) {
                    // 设置为完全透明
                    dst.setRGB(x, y, 0x00FFFFFF);
                } else {
                    // 保持原色
                    dst.setRGB(x, y, rgb);
                }
            }
        }
        return dst;
    }

    /**
     * 按2x2网格切割图片为4个版本
     * 标准MC材质：32×32大图 -> 4个16×16小图
     *
     * @param src 源大图
     * @param gridConfig 网格配置 {rows:2, cols:2, cellWidth:16, cellHeight:16}
     * @return 切割后的图片列表[v1,v2,v3,v4]
     */
    public static List<BufferedImage> cutGrid2x2(BufferedImage src) {
        List<BufferedImage> results = new ArrayList<>();

        int cellWidth = 16;
        int cellHeight = 16;

        // 验证图片尺寸
        if (src.getWidth() < 32 || src.getHeight() < 32) {
            throw new IllegalArgumentException("源图尺寸不足32x32，无法按2x2网格切割");
        }

        // 切割4个版本
        // v1: 左上 (0, 0)
        results.add(src.getSubimage(0, 0, cellWidth, cellHeight));

        // v2: 右上 (16, 0)
        results.add(src.getSubimage(cellWidth, 0, cellWidth, cellHeight));

        // v3: 左下 (0, 16)
        results.add(src.getSubimage(0, cellHeight, cellWidth, cellHeight));

        // v4: 右下 (16, 16)
        results.add(src.getSubimage(cellWidth, cellHeight, cellWidth, cellHeight));

        return results;
    }

    /**
     * 按4x1网格切割图片为4个版本（横向排列）
     * 64×16大图 -> 4个16×16小图
     */
    public static List<BufferedImage> cutGrid4x1(BufferedImage src) {
        List<BufferedImage> results = new ArrayList<>();

        int cellWidth = 16;
        int cellHeight = 16;

        if (src.getWidth() < 64 || src.getHeight() < 16) {
            throw new IllegalArgumentException("源图尺寸不足64x16，无法按4x1网格切割");
        }

        for (int i = 0; i < 4; i++) {
            results.add(src.getSubimage(i * cellWidth, 0, cellWidth, cellHeight));
        }

        return results;
    }

    /**
     * 通用网格切割方法
     * @param src 源图片
     * @param rows 行数
     * @param cols 列数
     * @param cellWidth 单元格宽度
     * @param cellHeight 单元格高度
     * @return 切割后的图片列表（从左到右，从上到下）
     */
    public static List<BufferedImage> cutGridGeneric(BufferedImage src, int rows, int cols,
                                                      int cellWidth, int cellHeight) {
        List<BufferedImage> results = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * cellWidth;
                int y = row * cellHeight;

                // 边界检查
                if (x + cellWidth <= src.getWidth() && y + cellHeight <= src.getHeight()) {
                    results.add(src.getSubimage(x, y, cellWidth, cellHeight));
                }
            }
        }

        return results;
    }

    /**
     * 智能调整图片尺寸
     * 如果图片不是标准尺寸，自动缩放到最近的2的幂次
     *
     * @param src 源图片
     * @param targetWidth 目标宽度(必须是2的幂次)
     * @return 调整后的图片
     */
    public static BufferedImage resizeToPowerOfTwo(BufferedImage src, int targetWidth) {
        // 验证是否为2的幂次
        if ((targetWidth & (targetWidth - 1)) != 0) {
            throw new IllegalArgumentException("目标宽度必须是2的幂次: 16, 32, 64, 128...");
        }

        // 计算目标高度（保持宽高比）
        int targetHeight = src.getHeight() * targetWidth / src.getWidth();

        // 高度也调整为2的幂次
        targetHeight = nearestPowerOfTwo(targetHeight);

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();

        // 使用高质量缩放
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // 像素风格不要抗锯齿

        g2d.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resized;
    }

    /**
     * 找到最近的2的幂次
     */
    private static int nearestPowerOfTwo(int n) {
        int power = (int) Math.ceil(Math.log(n) / Math.log(2));
        return (int) Math.pow(2, power);
    }

    /**
     * 完整的材质预处理流程
     * Gemini生成图 -> 去白底 -> 切割 -> 调整尺寸 -> 保存
     *
     * @param rawImagePath Gemini生成的原始大图路径
     * @param outputDir 输出目录
     * @param textureName 材质名称(如wooden_pickaxe)
     * @param removeBackground 是否去除白色背景
     * @return 4个版本的文件路径列表
     */
    public static List<String> processTextureComplete(String rawImagePath, String outputDir,
                                                       String textureName, boolean removeBackground)
            throws IOException {

        List<String> savedPaths = new ArrayList<>();

        // 1. 读取原始图片
        BufferedImage rawImage = ImageIO.read(new File(rawImagePath));

        // 2. 去除白色背景（可选）
        if (removeBackground) {
            rawImage = removeWhiteBackground(rawImage, 240);
        }

        // 3. 调整大图尺寸（如果不是标准32x32，先调整）
        if (rawImage.getWidth() != 32 || rawImage.getHeight() != 32) {
            rawImage = resizeToPowerOfTwo(rawImage, 32);
        }

        // 4. 按2x2网格切割
        List<BufferedImage> versions = cutGrid2x2(rawImage);

        // 5. 保存4个版本
        for (int i = 0; i < versions.size(); i++) {
            BufferedImage version = versions.get(i);

            // 确保每个版本是16x16
            if (version.getWidth() != 16 || version.getHeight() != 16) {
                version = resizeToPowerOfTwo(version, 16);
            }

            // 生成文件名: wooden_pickaxe_v1.png
            String fileName = textureName + "_v" + (i + 1) + ".png";
            String filePath = outputDir + File.separator + fileName;

            // 保存为PNG
            File outputFile = new File(filePath);
            outputFile.getParentFile().mkdirs(); // 创建目录
            ImageIO.write(version, "PNG", outputFile);

            savedPaths.add(filePath);
        }

        return savedPaths;
    }

    /**
     * 批量处理多个材质
     * @param taskId 任务ID
     * @param rawImageDir Gemini生成的原始图片目录
     * @param outputBaseDir 输出基础目录
     * @param textureNames 材质名称列表
     * @return 处理结果统计
     */
    public static ProcessResult batchProcess(Long taskId, String rawImageDir,
                                              String outputBaseDir, List<String> textureNames) {
        ProcessResult result = new ProcessResult();
        result.setTotal(textureNames.size());

        for (String textureName : textureNames) {
            try {
                String rawImagePath = rawImageDir + File.separator + textureName + "_raw.png";
                String outputDir = outputBaseDir + File.separator + taskId + File.separator + "textures";

                List<String> savedPaths = processTextureComplete(rawImagePath, outputDir, textureName, true);

                result.addSuccess();
                result.addProcessedFiles(savedPaths);

            } catch (Exception e) {
                result.addFailed();
                result.addError(textureName + ": " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 处理结果统计类
     */
    public static class ProcessResult {
        private int total;
        private int success;
        private int failed;
        private List<String> processedFiles = new ArrayList<>();
        private List<String> errors = new ArrayList<>();

        // Getters and Setters
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getSuccess() { return success; }
        public void addSuccess() { this.success++; }
        public int getFailed() { return failed; }
        public void addFailed() { this.failed++; }
        public List<String> getProcessedFiles() { return processedFiles; }
        public void addProcessedFiles(List<String> files) { this.processedFiles.addAll(files); }
        public List<String> getErrors() { return errors; }
        public void addError(String error) { this.errors.add(error); }
    }
}

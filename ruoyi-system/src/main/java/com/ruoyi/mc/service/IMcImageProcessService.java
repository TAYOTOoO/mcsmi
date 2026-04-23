package com.ruoyi.mc.service;

import java.util.List;

/**
 * 图片处理Service接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface IMcImageProcessService
{
    /**
     * 下载图片到本地
     *
     * @param imageUrl 图片URL
     * @param taskNo 任务编号
     * @return 本地文件路径
     */
    String downloadImage(String imageUrl, String taskNo);

    /**
     * 去除白色背景（边界洪水填充算法）
     *
     * @param inputPath 输入图片路径
     * @param taskNo 任务编号
     * @return 处理后的图片路径
     */
    String removeWhiteBackground(String inputPath, String taskNo);

    /**
     * 自动切割图片为网格
     *
     * @param inputPath 输入图片路径
     * @param pixelSize 单个材质像素大小(32)
     * @param rows 行数(5)
     * @param cols 列数(10)
     * @param taskNo 任务编号
     * @return 切割后的图片路径列表
     */
    List<String> autoSplitGrid(String inputPath, Integer pixelSize, Integer rows, Integer cols, String taskNo);
}

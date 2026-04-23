package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McAiModel;
import java.util.List;
import java.util.Map;

/**
 * AI模型调用Service接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface IMcAiService
{
    /**
     * 调用Gemini 2.5 Pro文本模型
     * 用于优化提示词
     *
     * @param model 模型配置
     * @param prompt 输入提示词
     * @return 优化后的提示词
     */
    String callGeminiTextModel(McAiModel model, String prompt);

    /**
     * 调用Gemini 3 Pro Image画图模型
     * 生成320x160的PNG图片
     *
     * @param model 模型配置
     * @param prompt 完整提示词
     * @param referenceImageUrl 参考图片URL（可选）
     * @return 图片URL或本地路径
     */
    String callGeminiImageModel(McAiModel model, String prompt, String referenceImageUrl);

    /**
     * 测试模型连通性
     *
     * @param modelId 模型ID
     * @return 是否连通
     */
    boolean testModelConnection(Long modelId);

    /**
     * 根据API地址和密钥获取可用模型列表
     *
     * @param apiBaseUrl API基础地址（如：https://generativelanguage.googleapis.com/v1）
     * @param apiKey API密钥
     * @return 模型列表
     */
    List<Map<String, Object>> fetchAvailableModels(String apiBaseUrl, String apiKey);
}

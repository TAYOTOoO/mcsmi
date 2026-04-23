package com.ruoyi.mc.service.impl;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.domain.McAiModel;
import com.ruoyi.mc.mapper.McAiModelMapper;
import com.ruoyi.mc.service.IMcAiService;
import com.ruoyi.common.exception.ServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import java.net.URLConnection;

/**
 * AI模型调用Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@Service
public class McAiServiceImpl implements IMcAiService
{
    private static final Logger log = LoggerFactory.getLogger(McAiServiceImpl.class);

    @Autowired
    private McAiModelMapper modelMapper;

    @Value("${mc.temp.path:D:/mc/temp}")
    private String tempPath;

    @Value("${mc.upload.path:D:/mc/uploads}")
    private String uploadPath;

    /**
     * 调用文本生成模型（支持Gemini原生和OpenAI兼容格式）
     */
    @Override
    public String callGeminiTextModel(McAiModel model, String prompt)
    {
        log.info("========================================");
        log.info("开始调用文本模型: {}", model.getModelName());
        log.info("API URL: {}", model.getApiUrl());
        log.info("提示词长度: {}", prompt.length());
        log.info("========================================");

        try
        {
            // 判断API类型：优先检查URL特征，而不是域名
            boolean isGeminiNative = model.getApiUrl().contains("generateContent") ||
                                     model.getApiUrl().contains("/v1beta/models/");
            
            if (isGeminiNative)
            {
                log.info("使用Gemini原生格式调用");
                return callGeminiNativeTextModel(model, prompt);
            }
            else
            {
                log.info("使用OpenAI兼容格式调用");
                return callOpenAITextModel(model, prompt);
            }
        }
        catch (Exception e)
        {
            log.error("❌ 调用文本模型失败: {}", e.getMessage(), e);
            throw new ServiceException("文本模型调用失败: " + e.getMessage());
        }
    }

    /**
     * OpenAI兼容格式调用
     */
    private String callOpenAITextModel(McAiModel model, String prompt)
    {
        try
        {
            // 构建OpenAI格式请求
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model.getModelName());

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            // 添加参数 - 移除字数限制，让AI自由发挥
            requestBody.put("max_tokens", 16000); // 大幅提高限制，文本AI回复多少就用多少
            requestBody.put("temperature", 0.7);

            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + model.getApiKey());
            headers.put("Content-Type", "application/json");

            log.info("发送OpenAI格式请求到: {}", model.getApiUrl());
            log.info("请求体: {}", requestBody.toJSONString());

            String responseStr = null;
            try {
                responseStr = HttpUtils.sendPost(model.getApiUrl(), headers, requestBody.toJSONString());
            } catch (Exception e) {
                log.error("❌ HTTP请求失败: {}", e.getMessage(), e);
                throw new ServiceException("网络请求失败: " + e.getMessage());
            }

            if (StringUtils.isEmpty(responseStr))
            {
                log.error("❌ API返回为空，可能原因：1) API服务不可用 2) 网络问题 3) API密钥无效");
                throw new ServiceException("API返回为空，请检查API配置和网络连接");
            }

            log.info("✅ 收到响应，长度: {}", responseStr.length());
            log.info("响应内容: {}", responseStr);

            // 解析OpenAI格式响应
            JSONObject response = JSON.parseObject(responseStr);

            if (response.containsKey("error"))
            {
                String errorMsg = response.getJSONObject("error").getString("message");
                log.error("API返回错误: {}", errorMsg);
                throw new ServiceException("API错误: " + errorMsg);
            }

            if (!response.containsKey("choices"))
            {
                log.error("API响应格式错误: {}", responseStr);
                throw new ServiceException("API响应格式错误");
            }

            JSONArray choices = response.getJSONArray("choices");
            if (choices == null || choices.isEmpty())
            {
                throw new ServiceException("API未返回结果");
            }

            String result = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

            log.info("✅ 文本模型调用成功，返回长度: {}", result.length());
            return result;
        }
        catch (Exception e)
        {
            log.error("❌ OpenAI格式调用失败: {}", e.getMessage(), e);
            throw new ServiceException("OpenAI格式调用失败: " + e.getMessage());
        }
    }

    /**
     * Gemini原生格式调用
     */
    private String callGeminiNativeTextModel(McAiModel model, String prompt)
    {
        try
        {
            // 构建Gemini原生请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.add(part);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 添加生成配置
            if (StringUtils.isNotEmpty(model.getModelParams()))
            {
                JSONObject modelParams = JSON.parseObject(model.getModelParams());
                requestBody.put("generationConfig", modelParams);
            }

            // 判断是否为第三方代理API（非Google官方域名使用Bearer token认证）
            boolean isGoogleOfficial = model.getApiUrl().contains("googleapis.com");
            String url;
            Map<String, String> textHeaders = new HashMap<>();
            textHeaders.put("Content-Type", "application/json");

            if (isGoogleOfficial)
            {
                url = model.getApiUrl() + "?key=" + model.getApiKey();
            }
            else
            {
                url = model.getApiUrl();
                textHeaders.put("Authorization", "Bearer " + model.getApiKey());
                log.info("检测到第三方代理API，使用Bearer token认证");
            }

            String responseStr = HttpUtils.sendPost(url, textHeaders, requestBody.toJSONString());

            if (StringUtils.isEmpty(responseStr))
            {
                throw new ServiceException("API返回为空");
            }

            // 解析响应
            JSONObject response = JSON.parseObject(responseStr);

            if (!response.containsKey("candidates"))
            {
                log.error("API响应异常: {}", responseStr);
                throw new ServiceException("API响应格式错误: " + responseStr);
            }

            JSONArray candidates = response.getJSONArray("candidates");
            if (candidates == null || candidates.isEmpty())
            {
                throw new ServiceException("API未返回结果");
            }

            String optimizedPrompt = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");

            log.info("✅ Gemini原生格式调用成功，返回长度: {}", optimizedPrompt.length());
            return optimizedPrompt;
        }
        catch (Exception e)
        {
            log.error("❌ Gemini原生格式调用失败: {}", e.getMessage(), e);
            throw new ServiceException("Gemini原生格式调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用图片生成模型（支持Gemini原生和OpenAI兼容格式）
     * 智能识别API类型：支持只输入基础URL，自动补全端点路径
     */
    @Override
    public String callGeminiImageModel(McAiModel model, String prompt, String referenceImageUrl)
    {
        log.info("========================================");
        log.info("开始调用图片模型: {}", model.getModelName());
        log.info("API URL: {}", model.getApiUrl());
        log.info("提示词长度: {}", prompt.length());
        if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
            log.info("参考图片: ", referenceImageUrl);
        }
        log.info("========================================");

        // 智能修正API地址：如果只输入了基础URL，自动补全端点路径
        String fixedUrl = smartFixImageApiUrl(model.getApiUrl(), model.getModelName());
        if (!fixedUrl.equals(model.getApiUrl()))
        {
            log.info("智能修正API地址: {} -> {}", model.getApiUrl(), fixedUrl);
            model.setApiUrl(fixedUrl);
        }

        try
        {
            // 判断API类型：通过URL特征或模型名称判断
            boolean isGeminiNative = fixedUrl.contains("generateContent") ||
                                     fixedUrl.contains("/v1beta/models/") ||
                                     isGeminiModelName(model.getModelName());

            if (isGeminiNative)
            {
                log.info("使用Gemini原生格式调用图片生成");
                return callGeminiNativeImageModel(model, prompt, referenceImageUrl);
            }
            else if (fixedUrl.contains("/images/generations"))
            {
                log.info("使用OpenAI图片生成API格式");
                return callOpenAIImageGenerationModel(model, prompt);
            }
            else
            {
                log.info("使用OpenAI兼容聊天格式尝试图片生成");
                return callOpenAIImageModel(model, prompt);
            }
        }
        catch (Exception e)
        {
            log.error("❌ 调用图片模型失败: {}", e.getMessage(), e);
            throw new ServiceException("图片模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 下载或读取图片并返回字节数组
     */
    private byte[] downloadImage(String imageUrl) throws IOException {
        // 判断是HTTP URL、完整路径还是文件名
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // 从URL下载
            URL url = new URL(imageUrl);
            try (InputStream in = url.openStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return out.toByteArray();
            }
        } else if (imageUrl.contains("/") || imageUrl.contains("\\")) {
            // 完整路径
            File file = new File(imageUrl);
            if (!file.exists()) {
                throw new IOException("文件不存在: " + imageUrl);
            }
            return FileUtils.readFileToByteArray(file);
        } else {
            // 文件名，从上传目录读取
            File file = new File(uploadPath, imageUrl);
            if (!file.exists()) {
                throw new IOException("文件不存在: " + file.getAbsolutePath());
            }
            return FileUtils.readFileToByteArray(file);
        }
    }

    /**
     * 检测图片MIME类型
     */
    private String detectImageMimeType(String imageUrl, byte[] imageBytes) {
        // 先从URL扩展名判断
        String lowerUrl = imageUrl.toLowerCase();
        if (lowerUrl.endsWith(".png")) return "image/png";
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) return "image/jpeg";
        if (lowerUrl.endsWith(".gif")) return "image/gif";
        if (lowerUrl.endsWith(".webp")) return "image/webp";
        
        // 从文件头判断（魔数）
        if (imageBytes.length >= 8) {
            // PNG: 89 50 4E 47
            if (imageBytes[0] == (byte)0x89 && imageBytes[1] == 0x50 && 
                imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
                return "image/png";
            }
            // JPEG: FF D8 FF
            if (imageBytes[0] == (byte)0xFF && imageBytes[1] == (byte)0xD8 && 
                imageBytes[2] == (byte)0xFF) {
                return "image/jpeg";
            }
            // GIF: 47 49 46
            if (imageBytes[0] == 0x47 && imageBytes[1] == 0x49 && imageBytes[2] == 0x46) {
                return "image/gif";
            }
        }
        
        // 默认返回png
        return "image/png";
    }

    /**
     * 智能修正图片模型API地址
     * 如果用户只输入了基础URL（如 https://api.example.com），自动补全为完整端点
     * 类似Cherry Studio的智能识别逻辑
     */
    private String smartFixImageApiUrl(String apiUrl, String modelName)
    {
        if (apiUrl == null || apiUrl.isEmpty())
        {
            return apiUrl;
        }

        String cleanUrl = apiUrl.trim();
        while (cleanUrl.endsWith("/"))
        {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
        }

        // 已经是完整端点，不需要修正
        if (cleanUrl.contains("generateContent") || cleanUrl.contains("/chat/completions") ||
            cleanUrl.contains("/images/generations"))
        {
            return cleanUrl;
        }

        // 如果模型名包含gemini关键词，自动构建Gemini原生格式端点
        if (isGeminiModelName(modelName))
        {
            // 确保有 /v1beta 前缀
            if (!cleanUrl.contains("/v1beta"))
            {
                cleanUrl = cleanUrl + "/v1beta";
            }
            // 补全 /models/{modelName}:generateContent
            if (!cleanUrl.contains("/models/"))
            {
                cleanUrl = cleanUrl + "/models/" + modelName + ":generateContent";
            }
            else if (!cleanUrl.contains("generateContent"))
            {
                cleanUrl = cleanUrl + ":generateContent";
            }
            return cleanUrl;
        }

        // 非Gemini模型，补全为OpenAI聊天格式
        if (!cleanUrl.contains("/v1"))
        {
            cleanUrl = cleanUrl + "/v1";
        }
        if (!cleanUrl.contains("/chat/"))
        {
            cleanUrl = cleanUrl + "/chat/completions";
        }

        return cleanUrl;
    }

    /**
     * 判断模型名称是否为Gemini系列模型
     */
    private boolean isGeminiModelName(String modelName)
    {
        if (modelName == null) return false;
        String lower = modelName.toLowerCase();
        return lower.contains("gemini") || lower.contains("imagen");
    }

    /**
     * OpenAI兼容格式调用图片模型
     */
    private String callOpenAIImageModel(McAiModel model, String prompt)
    {
        try
        {
            // 构建OpenAI格式请求
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model.getModelName());

            // 图片生成模型需要特殊的content格式
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");

            // 对于支持图片生成的模型，使用content数组格式
            JSONArray content = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", prompt);
            content.add(textPart);
            message.put("content", content);

            messages.add(message);
            requestBody.put("messages", messages);

            // 添加参数
            requestBody.put("max_tokens", 4000); // 提高限制以支持更多元数据

            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + model.getApiKey());
            headers.put("Content-Type", "application/json");

            log.info("发送图片生成请求到: {}", model.getApiUrl());
            String responseStr = HttpUtils.sendPost(model.getApiUrl(), headers, requestBody.toJSONString());

            if (StringUtils.isEmpty(responseStr))
            {
                throw new ServiceException("API返回为空");
            }

            log.info("收到响应，长度: {}", responseStr.length());

            // 解析OpenAI格式响应
            JSONObject response = JSON.parseObject(responseStr);

            if (response.containsKey("error"))
            {
                String errorMsg = response.getJSONObject("error").getString("message");
                log.error("API返回错误: {}", errorMsg);
                throw new ServiceException("API错误: " + errorMsg);
            }

            if (!response.containsKey("choices"))
            {
                log.error("API响应格式错误: {}", responseStr);
                throw new ServiceException("API响应格式错误");
            }

            JSONArray choices = response.getJSONArray("choices");
            if (choices == null || choices.isEmpty())
            {
                throw new ServiceException("API未返回结果");
            }

            // 解析响应中的图片（可能是URL或Base64）
            JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");

            // 检查content数组中是否有图片
            if (messageObj.containsKey("content"))
            {
                Object contentObj = messageObj.get("content");

                // 如果是数组，查找图片
                if (contentObj instanceof JSONArray)
                {
                    JSONArray contentArray = (JSONArray) contentObj;
                    for (int i = 0; i < contentArray.size(); i++)
                    {
                        JSONObject item = contentArray.getJSONObject(i);

                        // 查找图片URL
                        if (item.containsKey("image_url"))
                        {
                            String imageUrl = item.getJSONObject("image_url").getString("url");
                            log.info("✅ 图片模型返回URL: {}", imageUrl);

                            // 如果是Base64，保存为文件
                            if (imageUrl.startsWith("data:image"))
                            {
                                return saveBase64Image(imageUrl.split(",")[1], "generated");
                            }
                            return imageUrl;
                        }

                        // 查找Base64数据
                        if (item.containsKey("data"))
                        {
                            String base64Data = item.getString("data");
                            return saveBase64Image(base64Data, "generated");
                        }
                    }
                }
                // 如果是字符串，可能直接是文本响应
                else if (contentObj instanceof String)
                {
                    String contentStr = (String) contentObj;
                    // 检查是否包含图片链接
                    if (contentStr.startsWith("http"))
                    {
                        log.info("✅ 图片模型返回URL: {}", contentStr);
                        return contentStr;
                    }
                }
            }

            throw new ServiceException("未能从响应中提取图片");
        }
        catch (Exception e)
        {
            log.error("❌ OpenAI格式图片调用失败: {}", e.getMessage(), e);
            throw new ServiceException("OpenAI格式图片调用失败: " + e.getMessage());
        }
    }

    /**
     * OpenAI标准图片生成API（/v1/images/generations端点）
     */
    private String callOpenAIImageGenerationModel(McAiModel model, String prompt)
    {
        try
        {
            // 构建DALL-E标准请求
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model.getModelName());
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1); // 生成1张图片
            requestBody.put("size", "1024x1024"); // 图片尺寸
            requestBody.put("response_format", "b64_json"); // 返回Base64格式

            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + model.getApiKey());
            headers.put("Content-Type", "application/json");

            log.info("发送OpenAI图片生成请求到: {}", model.getApiUrl());
            String responseStr = HttpUtils.sendPost(model.getApiUrl(), headers, requestBody.toJSONString());

            if (StringUtils.isEmpty(responseStr))
            {
                throw new ServiceException("API返回为空");
            }

            log.info("收到响应，长度: {}", responseStr.length());

            // 解析响应
            JSONObject response = JSON.parseObject(responseStr);

            if (response.containsKey("error"))
            {
                String errorMsg = response.getJSONObject("error").getString("message");
                log.error("API返回错误: {}", errorMsg);
                throw new ServiceException("API错误: " + errorMsg);
            }

            JSONArray data = response.getJSONArray("data");
            if (data == null || data.isEmpty())
            {
                throw new ServiceException("API未返回图片数据");
            }

            JSONObject imageData = data.getJSONObject(0);

            // 返回Base64或URL
            if (imageData.containsKey("b64_json"))
            {
                String base64Data = imageData.getString("b64_json");
                log.info("✅ 收到Base64图片数据");
                return saveBase64Image(base64Data, "generated");
            }
            else if (imageData.containsKey("url"))
            {
                String imageUrl = imageData.getString("url");
                log.info("✅ 收到图片URL: {}", imageUrl);
                return imageUrl;
            }

            throw new ServiceException("未能从响应中提取图片");
        }
        catch (Exception e)
        {
            log.error("❌ OpenAI图片生成API调用失败: {}", e.getMessage(), e);
            throw new ServiceException("OpenAI图片生成API调用失败: " + e.getMessage());
        }
    }

    /**
     * Gemini原生格式调用图片模型
     */
    private String callGeminiNativeImageModel(McAiModel model, String prompt, String referenceImageUrl)
    {
        try
        {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("role", "user");

            JSONArray parts = new JSONArray();
            
            // 如果有参考图片，先添加图片部分
            if (referenceImageUrl != null && !referenceImageUrl.isEmpty()) {
                log.info("📷 开始处理参考图片: {}", referenceImageUrl);
                try {
                    // 下载图片并转换为base64
                    byte[] imageBytes = downloadImage(referenceImageUrl);
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    
                    // 检测图片格式
                    String mimeType = detectImageMimeType(referenceImageUrl, imageBytes);
                    
                    JSONObject imagePart = new JSONObject();
                    JSONObject inlineData = new JSONObject();
                    inlineData.put("mime_type", mimeType);
                    inlineData.put("data", base64Image);
                    imagePart.put("inline_data", inlineData);
                    parts.add(imagePart);
                    
                    log.info("✅ 参考图片已添加到请求 (格式:{}, 大小:{}KB)", mimeType, imageBytes.length / 1024);
                } catch (Exception e) {
                    log.error("❌ 无法加载参考图片: {}", e.getMessage(), e);
                    throw new ServiceException("参考图片加载失败: " + e.getMessage());
                }
            }
            
            // 添加文本提示词
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.add(part);

            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            // 添加生成配置 - 关键：指定返回图片，并设置严格参数
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("responseModalities", new JSONArray().fluentAdd("TEXT").fluentAdd("IMAGE"));
            // 设置低temperature确保严格遵循参考图
            generationConfig.put("temperature", 0.1);
            generationConfig.put("candidateCount", 1);
            // 添加topK和topP参数，进一步提高精确度
            generationConfig.put("topK", 1);
            generationConfig.put("topP", 0.1);
            requestBody.put("generationConfig", generationConfig);

            // 判断是否为第三方代理API（非Google官方域名）
            // Google官方域名使用 ?key= 参数认证，第三方代理使用 Bearer token
            String url;
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            boolean isGoogleOfficial = model.getApiUrl().contains("googleapis.com");
            if (isGoogleOfficial)
            {
                // Google官方API：URL参数传递key
                url = model.getApiUrl() + "?key=" + model.getApiKey();
            }
            else
            {
                // 第三方代理API（如api.10dian-ai.top、aicanapi.com等）：Bearer token认证
                url = model.getApiUrl();
                headers.put("Authorization", "Bearer " + model.getApiKey());
                log.info("检测到第三方代理API，使用Bearer token认证");
            }

            log.info("发送Gemini原生图片生成请求到: {}", isGoogleOfficial ? url : model.getApiUrl());
            log.info("请求体: {}", requestBody.toJSONString());

            // 使用Apache HttpClient替代URLConnection，支持大响应体
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = null;
            String responseStr = null;
            
            try {
                HttpPost httpPost = new HttpPost(url);
                
                // 设置超时：连接30秒，读取300秒
                RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(30000)
                    .setSocketTimeout(300000)
                    .setConnectionRequestTimeout(30000)
                    .build();
                httpPost.setConfig(requestConfig);
                
                // 设置请求头
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
                
                // 设置请求体
                StringEntity entity = new StringEntity(requestBody.toJSONString(), "UTF-8");
                httpPost.setEntity(entity);
                
                // 执行请求
                httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                
                HttpEntity responseEntity = httpResponse.getEntity();
                responseStr = EntityUtils.toString(responseEntity, "UTF-8");
                
                if (statusCode < 200 || statusCode >= 300) {
                    log.error("HTTP错误 {}: {}", statusCode, responseStr.substring(0, Math.min(500, responseStr.length())));
                    throw new ServiceException("HTTP错误 " + statusCode + ": " + responseStr);
                }
                
            } finally {
                if (httpResponse != null) {
                    try { httpResponse.close(); } catch (Exception e) {}
                }
                try { httpClient.close(); } catch (Exception e) {}
            }

            if (StringUtils.isEmpty(responseStr))
            {
                throw new ServiceException("API返回为空");
            }

            log.info("收到响应，长度: {}", responseStr.length());
            log.info("响应内容: {}", responseStr);

            // 解析响应
            JSONObject response = JSON.parseObject(responseStr);

            if (response.containsKey("error"))
            {
                String errorMsg = response.getJSONObject("error").getString("message");
                log.error("API返回错误: {}", errorMsg);
                throw new ServiceException("API错误: " + errorMsg);
            }

            if (!response.containsKey("candidates"))
            {
                log.error("API响应异常: {}", responseStr);
                throw new ServiceException("API响应格式错误");
            }

            // 提取图片（可能是Base64或URL）
            JSONArray candidates = response.getJSONArray("candidates");
            if (candidates == null || candidates.isEmpty())
            {
                throw new ServiceException("API未返回结果");
            }

            JSONArray responseParts = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts");

            String imageResult = null;

            // 遍历parts查找图片
            for (int i = 0; i < responseParts.size(); i++)
            {
                JSONObject responsePart = responseParts.getJSONObject(i);

                // 格式1: inlineData (Base64)
                if (responsePart.containsKey("inlineData"))
                {
                    String base64Data = responsePart.getJSONObject("inlineData").getString("data");
                    log.info("✅ 找到Base64图片数据");
                    imageResult = saveBase64Image(base64Data, "generated");
                    break;
                }

                // 格式2: URL
                if (responsePart.containsKey("url"))
                {
                    imageResult = responsePart.getString("url");
                    log.info("✅ 找到图片URL: {}", imageResult);
                    break;
                }
            }

            if (imageResult == null)
            {
                throw new ServiceException("未能从响应中提取图片");
            }

            log.info("✅ Gemini原生格式图片调用成功: {}", imageResult);
            return imageResult;
        }
        catch (Exception e)
        {
            log.error("❌ Gemini原生格式图片调用失败: {}", e.getMessage(), e);
            throw new ServiceException("Gemini原生格式图片调用失败: " + e.getMessage());
        }
    }

    /**
     * 保存Base64图片并返回本地文件路径
     */
    private String saveBase64Image(String base64Data, String prefix) throws IOException
    {
        try
        {
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String fileName = prefix + "_" + System.currentTimeMillis() + ".png";
            File tempDir = new File(tempPath);

            // 确保目录存在
            if (!tempDir.exists())
            {
                boolean created = tempDir.mkdirs();
                log.info("创建临时目录: {}, 结果: {}", tempPath, created);
                if (!created && !tempDir.exists())
                {
                    throw new IOException("无法创建临时目录: " + tempPath);
                }
            }

            // 验证目录可写
            if (!tempDir.canWrite())
            {
                throw new IOException("临时目录不可写: " + tempPath);
            }

            String filePath = tempPath + File.separator + fileName;
            File imageFile = new File(filePath);

            FileUtils.writeByteArrayToFile(imageFile, imageBytes);

            // 验证文件是否成功保存
            if (!imageFile.exists() || imageFile.length() == 0)
            {
                throw new IOException("图片保存失败或文件为空: " + filePath);
            }

            log.info("✅ Base64图片已保存到本地: {}, 文件大小: {} bytes", filePath, imageFile.length());

            // 返回完整的本地文件路径，用于后续图片处理
            return filePath;
        }
        catch (Exception e)
        {
            log.error("❌ 保存Base64图片失败: {}", e.getMessage(), e);
            throw new IOException("保存图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 测试模型连通性
     */
    @Override
    public boolean testModelConnection(Long modelId)
    {
        McAiModel model = modelMapper.selectMcAiModelByModelId(modelId);
        if (model == null)
        {
            throw new ServiceException("模型不存在");
        }

        try
        {
            // 判断是否为OpenAI兼容格式（如艾可API）
            if (model.getApiUrl().contains("/chat/completions"))
            {
                return testOpenAICompatibleModel(model);
            }
            else if (model.getModelType() == 2)
            {
                // 测试Gemini原生文本模型
                String result = callGeminiTextModel(model, "Hello, this is a test.");
                return StringUtils.isNotEmpty(result);
            }
            else if (model.getModelType() == 1)
            {
                // 测试Gemini原生画图模型
                String result = callGeminiImageModel(model, "Generate a simple test image.", null);
                return StringUtils.isNotEmpty(result);
            }
            return false;
        }
        catch (Exception e)
        {
            log.error("模型连通性测试失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试OpenAI兼容格式的模型
     */
    private boolean testOpenAICompatibleModel(McAiModel model)
    {
        try
        {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model.getModelName());
            requestBody.put("max_tokens", 50);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");

            if (model.getModelType() == 1)
            {
                // 图片生成模型
                JSONArray content = new JSONArray();
                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                textPart.put("text", "Generate a simple red square");
                content.add(textPart);
                message.put("content", content);
            }
            else
            {
                // 文本生成模型
                message.put("content", "Hello");
            }

            messages.add(message);
            requestBody.put("messages", messages);

            // 构建请求URL
            String url = model.getApiUrl();

            // 构建请求头
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            headers.put("Authorization", "Bearer " + model.getApiKey());
            headers.put("Content-Type", "application/json");

            log.info("测试OpenAI兼容模型: {}", model.getModelName());

            // 发送请求
            // 使用Apache HttpClient替代URLConnection，支持大响应体
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = null;
            String responseStr = null;
            
            try {
                HttpPost httpPost = new HttpPost(url);
                
                // 设置超时：连接30秒，读取300秒
                RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(30000)
                    .setSocketTimeout(300000)
                    .setConnectionRequestTimeout(30000)
                    .build();
                httpPost.setConfig(requestConfig);
                
                // 设置请求头
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
                
                // 设置请求体
                StringEntity entity = new StringEntity(requestBody.toJSONString(), "UTF-8");
                httpPost.setEntity(entity);
                
                // 执行请求
                httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                
                HttpEntity responseEntity = httpResponse.getEntity();
                responseStr = EntityUtils.toString(responseEntity, "UTF-8");
                
                if (statusCode < 200 || statusCode >= 300) {
                    log.error("HTTP错误 {}: {}", statusCode, responseStr.substring(0, Math.min(500, responseStr.length())));
                    throw new ServiceException("HTTP错误 " + statusCode + ": " + responseStr);
                }
                
            } finally {
                if (httpResponse != null) {
                    try { httpResponse.close(); } catch (Exception e) {}
                }
                try { httpClient.close(); } catch (Exception e) {}
            }

            if (StringUtils.isEmpty(responseStr))
            {
                log.error("API返回为空");
                return false;
            }

            JSONObject response = JSON.parseObject(responseStr);

            // 检查是否有错误
            if (response.containsKey("error"))
            {
                log.error("API返回错误: {}", response.getJSONObject("error").getString("message"));
                return false;
            }

            // 检查是否有choices
            if (response.containsKey("choices") && !response.getJSONArray("choices").isEmpty())
            {
                log.info("模型测试成功");
                return true;
            }

            return false;
        }
        catch (Exception e)
        {
            log.error("测试OpenAI兼容模型失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据API地址和密钥获取可用模型列表
     */
    @Override
    public List<Map<String, Object>> fetchAvailableModels(String apiBaseUrl, String apiKey)
    {
        log.info("获取API可用模型列表: {}", apiBaseUrl);

        List<Map<String, Object>> modelList = new ArrayList<>();

        try
        {
            // 清理API基础地址，去掉末尾的斜杠
            String cleanBaseUrl = apiBaseUrl.trim();
            if (cleanBaseUrl.endsWith("/"))
            {
                cleanBaseUrl = cleanBaseUrl.substring(0, cleanBaseUrl.length() - 1);
            }

            // 判断API类型
            if (cleanBaseUrl.contains("generativelanguage.googleapis.com"))
            {
                // Google官方API - 调用models接口（使用?key=认证）
                modelList = fetchGoogleApiModels(cleanBaseUrl, apiKey);
            }
            else
            {
                // 所有第三方代理API统一使用OpenAI兼容格式获取模型列表（Bearer token认证）
                // 包括：aicanapi.com、api.10dian-ai.top 等
                modelList = fetchOpenAICompatibleModels(cleanBaseUrl, apiKey);
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("获取模型列表失败: {}", e.getMessage(), e);
            throw new ServiceException("获取模型列表失败: " + e.getMessage());
        }

        return modelList;
    }

    /**
     * 获取OpenAI兼容格式API的模型列表
     */
    private List<Map<String, Object>> fetchOpenAICompatibleModels(String baseUrl, String apiKey)
    {
        List<Map<String, Object>> modelList = new ArrayList<>();

        try
        {
            // 智能修正API地址
            baseUrl = fixApiBaseUrl(baseUrl);

            // 构建模型列表URL
            String modelsUrl = baseUrl + "/models";

            // 构建请求头
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            headers.put("Content-Type", "application/json");

            log.info("请求OpenAI兼容API模型列表: {}", modelsUrl);

            // 发送GET请求
            String responseStr = sendGetWithHeaders(modelsUrl, headers);

            if (StringUtils.isEmpty(responseStr))
            {
                throw new ServiceException("API返回为空，请检查API地址和密钥是否正确");
            }

            // 检查是否返回了HTML（说明地址错误）
            if (responseStr.trim().startsWith("<") || responseStr.contains("<!doctype"))
            {
                throw new ServiceException("API地址错误：返回了网页而不是JSON数据。请确保使用API地址而非管理界面地址。例如：https://your-domain.com/v1 而不是 https://your-domain.com");
            }

            // 解析响应
            JSONObject response = JSON.parseObject(responseStr);

            // 检查错误
            if (response.containsKey("error"))
            {
                JSONObject error = response.getJSONObject("error");
                String errorMessage = error.getString("message");
                log.error("API返回错误: {}", errorMessage);
                throw new ServiceException("API返回错误: " + errorMessage);
            }

            // 解析模型列表
            if (response.containsKey("data"))
            {
                JSONArray models = response.getJSONArray("data");

                if (models == null || models.isEmpty())
                {
                    throw new ServiceException("该API密钥下没有可用模型，请检查密钥权限");
                }

                for (int i = 0; i < models.size(); i++)
                {
                    JSONObject model = models.getJSONObject(i);
                    String modelId = model.getString("id");
                    String ownedBy = model.getString("owned_by");

                    if (StringUtils.isEmpty(modelId))
                    {
                        continue;
                    }

                    // 判断模型类型
                    Integer modelType = determineModelTypeByName(modelId);

                    // 根据模型类型生成正确的API地址
                    // Gemini系列模型使用 /v1beta/models/{model}:generateContent 端点
                    // 其他模型使用 /chat/completions 端点
                    String modelApiUrl;
                    if (isGeminiModelName(modelId))
                    {
                        // 从baseUrl提取域名部分，构建Gemini原生端点
                        String domain = baseUrl;
                        if (domain.contains("/v1"))
                        {
                            domain = domain.substring(0, domain.indexOf("/v1"));
                        }
                        modelApiUrl = domain + "/v1beta/models/" + modelId + ":generateContent";
                    }
                    else
                    {
                        modelApiUrl = baseUrl + "/chat/completions";
                    }

                    Map<String, Object> modelInfo = new HashMap<>();
                    modelInfo.put("modelName", modelId);
                    modelInfo.put("displayName", modelId);
                    modelInfo.put("description", "Provider: " + (ownedBy != null ? ownedBy : "unknown"));
                    modelInfo.put("modelType", modelType);
                    modelInfo.put("apiUrl", modelApiUrl);

                    modelList.add(modelInfo);
                    log.info("发现模型: {} (类型: {})", modelId, modelType == 1 ? "图片生成" : "文本生成");
                }

                log.info("共获取到 {} 个可用模型", modelList.size());
            }
            else
            {
                throw new ServiceException("API响应格式不正确，未找到模型列表。请确认API地址正确");
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("获取OpenAI兼容API模型列表失败: {}", e.getMessage(), e);
            throw new ServiceException("获取模型列表失败: " + e.getMessage());
        }

        return modelList;
    }

    /**
     * 智能修正API基础地址
     */
    private String fixApiBaseUrl(String baseUrl)
    {
        if (baseUrl == null || baseUrl.isEmpty())
        {
            return baseUrl;
        }

        // 去除末尾斜杠
        baseUrl = baseUrl.trim();
        while (baseUrl.endsWith("/"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 如果没有版本号，自动添加 /v1（适用于所有第三方代理API）
        // Google官方API不走这个逻辑
        if (!baseUrl.contains("/v1") && !baseUrl.contains("/v1beta") &&
            !baseUrl.contains("googleapis.com"))
        {
            log.info("自动添加 /v1 到API地址: {} -> {}/v1", baseUrl, baseUrl);
            baseUrl = baseUrl + "/v1";
        }

        return baseUrl;
    }

    /**
     * 发送带自定义请求头的GET请求
     */
    private String sendGetWithHeaders(String url, java.util.Map<String, String> headers)
    {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try
        {
            URL realUrl = new URL(url);
            java.net.URLConnection connection = realUrl.openConnection();

            // 设置请求头
            if (headers != null && !headers.isEmpty())
            {
                for (java.util.Map.Entry<String, String> entry : headers.entrySet())
                {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
        }
        catch (Exception e)
        {
            log.error("GET请求失败: {}", e.getMessage(), e);
            throw new ServiceException("请求失败: " + e.getMessage());
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                log.error("关闭流失败", ex);
            }
        }
        return result.toString();
    }

    /**
     * 根据模型名称判断类型
     */
    private Integer determineModelTypeByName(String modelName)
    {
        if (modelName == null)
        {
            return 2; // 默认文本生成
        }

        String lowerName = modelName.toLowerCase();

        // 包含image、vision、imagen、dall-e等关键词的为图片生成模型
        if (lowerName.contains("image") ||
            lowerName.contains("vision") ||
            lowerName.contains("imagen") ||
            lowerName.contains("dall-e") ||
            lowerName.contains("midjourney") ||
            lowerName.contains("stable-diffusion"))
        {
            return 1; // 图片生成
        }

        return 2; // 文本生成
    }

    /**
     * 从Google官方API获取模型列表
     */
    private List<Map<String, Object>> fetchGoogleApiModels(String baseUrl, String apiKey)
    {
        List<Map<String, Object>> modelList = new ArrayList<>();
        String modelsUrl = baseUrl + "/models?key=" + apiKey;

        log.info("请求Google API模型列表: {}", modelsUrl.replace(apiKey, "***"));

        String responseStr = HttpUtils.sendGet(modelsUrl);

        if (StringUtils.isEmpty(responseStr))
        {
            throw new ServiceException("API返回为空，请检查API地址和密钥是否正确");
        }

        JSONObject response = JSON.parseObject(responseStr);

        if (response.containsKey("error"))
        {
            JSONObject error = response.getJSONObject("error");
            String errorMessage = error.getString("message");
            log.error("API返回错误: {}", errorMessage);
            throw new ServiceException("API返回错误: " + errorMessage);
        }

        if (response.containsKey("models"))
        {
            JSONArray models = response.getJSONArray("models");

            for (int i = 0; i < models.size(); i++)
            {
                JSONObject model = models.getJSONObject(i);
                String modelName = model.getString("name");
                String displayName = model.getString("displayName");
                String description = model.getString("description");

                if (modelName != null && modelName.startsWith("models/"))
                {
                    modelName = modelName.substring(7);
                }

                Integer modelType = determineModelType(model);

                Map<String, Object> modelInfo = new HashMap<>();
                modelInfo.put("modelName", modelName);
                modelInfo.put("displayName", displayName != null ? displayName : modelName);
                modelInfo.put("description", description != null ? description : "");
                modelInfo.put("modelType", modelType);
                modelInfo.put("apiUrl", baseUrl + "/models/" + modelName + ":generateContent");

                modelList.add(modelInfo);
            }
        }

        return modelList;
    }

    /**
     * 从标准API获取模型列表
     */
    private List<Map<String, Object>> fetchStandardApiModels(String baseUrl, String apiKey)
    {
        List<Map<String, Object>> modelList = new ArrayList<>();

        // 去掉v1beta或v1后面的路径
        if (baseUrl.contains("/v1beta"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("/v1beta") + 7);
        }
        else if (baseUrl.contains("/v1"))
        {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("/v1") + 3);
        }

        // 判断认证方式：Google官方用?key=，第三方代理用Bearer token
        String modelsUrl;
        boolean isGoogleOfficial = baseUrl.contains("googleapis.com");
        if (isGoogleOfficial)
        {
            modelsUrl = baseUrl + "/models?key=" + apiKey;
        }
        else
        {
            modelsUrl = baseUrl + "/models";
        }
        log.info("请求模型列表URL: {}", modelsUrl);

        // 使用带header的GET请求
        Map<String, String> getHeaders = new HashMap<>();
        if (!isGoogleOfficial)
        {
            getHeaders.put("Authorization", "Bearer " + apiKey);
        }
        getHeaders.put("Content-Type", "application/json");

        String responseStr = sendGetWithHeaders(modelsUrl, getHeaders);

        if (StringUtils.isEmpty(responseStr))
        {
            throw new ServiceException("API返回为空，请检查API地址和密钥是否正确");
        }

        JSONObject response = JSON.parseObject(responseStr);

        if (response.containsKey("error"))
        {
            JSONObject error = response.getJSONObject("error");
            String errorMessage = error.getString("message");
            throw new ServiceException("API返回错误: " + errorMessage);
        }

        if (response.containsKey("models"))
        {
            JSONArray models = response.getJSONArray("models");

            for (int i = 0; i < models.size(); i++)
            {
                JSONObject model = models.getJSONObject(i);
                String modelName = model.getString("name");
                String displayName = model.getString("displayName");
                String description = model.getString("description");

                if (modelName != null && modelName.startsWith("models/"))
                {
                    modelName = modelName.substring(7);
                }

                Integer modelType = determineModelType(model);

                Map<String, Object> modelInfo = new HashMap<>();
                modelInfo.put("modelName", modelName);
                modelInfo.put("displayName", displayName != null ? displayName : modelName);
                modelInfo.put("description", description != null ? description : "");
                modelInfo.put("modelType", modelType);
                modelInfo.put("apiUrl", buildApiUrl(baseUrl, modelName));

                modelList.add(modelInfo);
            }
        }

        return modelList;
    }

    /**
     * 判断模型类型
     */
    private Integer determineModelType(JSONObject model)
    {
        String modelName = model.getString("name");

        // 包含image、vision、imagen等关键词的为图片生成模型
        if (modelName != null &&
            (modelName.toLowerCase().contains("image") ||
             modelName.toLowerCase().contains("vision") ||
             modelName.toLowerCase().contains("imagen")))
        {
            return 1; // 图片生成
        }

        // 检查supportedGenerationMethods
        if (model.containsKey("supportedGenerationMethods"))
        {
            JSONArray methods = model.getJSONArray("supportedGenerationMethods");
            if (methods != null)
            {
                for (int i = 0; i < methods.size(); i++)
                {
                    String method = methods.getString(i);
                    if (method != null && method.toLowerCase().contains("image"))
                    {
                        return 1; // 图片生成
                    }
                }
            }
        }

        // 默认为文本生成模型
        return 2;
    }

    /**
     * 构建完整的API地址
     */
    private String buildApiUrl(String baseUrl, String modelName)
    {
        if (baseUrl.contains("generativelanguage.googleapis.com"))
        {
            // Google官方API格式
            return baseUrl + "/models/" + modelName + ":generateContent";
        }
        else if (baseUrl.contains("/v1beta"))
        {
            // v1beta格式
            return baseUrl + "/models/" + modelName + ":generateContent";
        }
        else if (baseUrl.contains("/v1"))
        {
            // v1格式
            return baseUrl + "/models/" + modelName + ":generateContent";
        }
        else
        {
            // 默认格式
            return baseUrl + "/models/" + modelName + ":generateContent";
        }
    }
}

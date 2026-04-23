
package com.ruoyi.mc.service.impl;

import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mc.mapper.McGenerationTaskMapper;
import com.ruoyi.mc.mapper.McAiModelMapper;
import com.ruoyi.mc.mapper.McGenerationLogMapper;
import com.ruoyi.mc.mapper.McTemplateModelConfigMapper;
import com.ruoyi.mc.domain.McTemplateModelConfig;
import com.ruoyi.mc.domain.McGenerationTask;
import com.ruoyi.mc.domain.McAiModel;
import com.ruoyi.mc.domain.McGenerationLog;
import com.ruoyi.mc.domain.McPromptTemplate;
import com.ruoyi.mc.service.IMcGenerationService;
import com.ruoyi.mc.service.IMcAiService;
import com.ruoyi.mc.service.IMcImageProcessService;
import com.ruoyi.mc.service.IMcPromptTemplateService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 材质生成Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@Service
public class McGenerationServiceImpl implements IMcGenerationService
{
    private static final Logger log = LoggerFactory.getLogger(McGenerationServiceImpl.class);

    @Autowired
    private McGenerationTaskMapper taskMapper;

    @Autowired
    private McAiModelMapper aiModelMapper;

    @Autowired
    private McGenerationLogMapper generationLogMapper;

    @Autowired
    private IMcAiService aiService;

    @Autowired
    private IMcImageProcessService imageProcessService;

    @Autowired
    private IMcPromptTemplateService promptTemplateService;

    @Autowired
    private McTemplateModelConfigMapper templateModelConfigMapper;

    /**
     * 查询生成任务
     *
     * @param taskId 生成任务ID
     * @return 生成任务
     */
    @Override
    public McGenerationTask selectMcGenerationTaskByTaskId(Long taskId)
    {
        return taskMapper.selectMcGenerationTaskByTaskId(taskId);
    }

    /**
     * 查询生成任务列表
     *
     * @param mcGenerationTask 生成任务
     * @return 生成任务
     */
    @Override
    public List<McGenerationTask> selectMcGenerationTaskList(McGenerationTask mcGenerationTask)
    {
        return taskMapper.selectMcGenerationTaskList(mcGenerationTask);
    }

    /**
     * 新增生成任务
     *
     * @param mcGenerationTask 生成任务
     * @return 结果
     */
    @Override
    public int insertMcGenerationTask(McGenerationTask mcGenerationTask)
    {
        mcGenerationTask.setCreateTime(new Date());
        return taskMapper.insertMcGenerationTask(mcGenerationTask);
    }

    /**
     * 修改生成任务
     *
     * @param mcGenerationTask 生成任务
     * @return 结果
     */
    @Override
    public int updateMcGenerationTask(McGenerationTask mcGenerationTask)
    {
        return taskMapper.updateMcGenerationTask(mcGenerationTask);
    }

    /**
     * 批量删除生成任务
     *
     * @param taskIds 需要删除的生成任务ID
     * @return 结果
     */
    @Override
    public int deleteMcGenerationTaskByTaskIds(Long[] taskIds)
    {
        return taskMapper.deleteMcGenerationTaskByTaskIds(taskIds);
    }

    /**
     * 删除生成任务信息
     *
     * @param taskId 生成任务ID
     * @return 结果
     */
    @Override
    public int deleteMcGenerationTaskByTaskId(Long taskId)
    {
        return taskMapper.deleteMcGenerationTaskByTaskId(taskId);
    }

    /**
     * 执行完整的材质包生成流程
     * 注意：移除了@Transactional，改为在方法内部手动控制事务
     * 这样可以确保日志记录即使在任务失败时也能保存到数据库
     *
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Override
    public McGenerationTask executeGeneration(Long taskId)
    {
        log.info("################################################################################");
        log.info("🚀 开始执行材质生成任务: {}", taskId);
        log.info("################################################################################");
        long startTimestamp = System.currentTimeMillis();

        // 1. 获取任务信息
        log.info("步骤1️⃣: 获取任务信息");
        McGenerationTask task = taskMapper.selectMcGenerationTaskByTaskId(taskId);
        if (task == null)
        {
            log.error("❌ 任务不存在，ID: {}", taskId);
            throw new ServiceException("任务不存在，ID: " + taskId);
        }
        log.info("✅ 任务信息: 用户={}, 风格={}", task.getUserName(), task.getStyleDescription());

        // 立即创建生成日志记录（先插入数据库，后续更新）
        McGenerationLog generationLog = new McGenerationLog();
        generationLog.setTaskId(taskId);
        generationLog.setUserId(task.getUserId());
        generationLog.setUsername(task.getUserName());
        generationLog.setStyleDescription(task.getStyleDescription());
        generationLog.setStatus("0"); // 处理中
        generationLog.setCreateTime(new Date());

        // 立即插入日志到数据库
        try {
            generationLogMapper.insertMcGenerationLog(generationLog);
            log.info("✅ 生成日志已创建，日志ID: {}", generationLog.getLogId());
        } catch (Exception e) {
            log.error("❌ 创建生成日志失败: {}", e.getMessage(), e);
        }

        try
        {
            // 2. 根据任务中的模板ID获取对应的模板
            log.info("步骤2️⃣: 获取任务指定的预提示词模板");
            McPromptTemplate promptTemplate = null;
            if (task.getTemplateId() != null)
            {
                promptTemplate = promptTemplateService.selectMcPromptTemplateByTemplateId(task.getTemplateId());
            }
            if (promptTemplate == null)
            {
                throw new ServiceException("❌ 未找到指定的预提示词模板（模板ID: " + task.getTemplateId() + "），请选择一个有效的模板");
            }
            log.info("✅ 预提示词模板: {} (ID:{})", promptTemplate.getTemplateName(), promptTemplate.getTemplateId());

            // 检查是否已有优化后的提示词（重新生成时，文本模型已成功但图片模型失败的情况）
            String optimizedPrompt = task.getOptimizedPrompt();
            boolean skipTextModel = !StringUtils.isEmpty(optimizedPrompt);

            if (skipTextModel)
            {
                log.info("⏩ 检测到已有优化提示词（长度:{}），跳过文本模型步骤，直接使用已有结果", optimizedPrompt.length());
                log.info("📝 已有优化提示词前200字符: {}", optimizedPrompt.substring(0, Math.min(200, optimizedPrompt.length())));

                // 仍需设置文本模型信息用于日志记录
                if (promptTemplate.getTextModelId() != null)
                {
                    McAiModel textModel = aiModelMapper.selectMcAiModelByModelId(promptTemplate.getTextModelId());
                    if (textModel != null)
                    {
                        task.setTextModelId(textModel.getModelId());
                        task.setTextModelUrl(textModel.getApiUrl());
                        generationLog.setTextModelId(textModel.getModelId());
                        generationLog.setTextModelName(textModel.getModelName());
                    }
                }
                generationLog.setOptimizedPrompt(optimizedPrompt);
                generationLogMapper.updateMcGenerationLog(generationLog);
            }
            else
            {
                // 3. 从模板配置中获取文本生成模型列表（支持冗余切换）
                log.info("步骤3️⃣: 获取模板绑定的文本生成模型");
                
                // 从模板配置表中获取该模板的文本生成模型列表（已按优先级排序）
                List<McTemplateModelConfig> textModelConfigs = templateModelConfigMapper.selectEnabledModelsByTemplateAndType(
                    promptTemplate.getTemplateId(), 2); // 2=文本生成
                
                if (textModelConfigs == null || textModelConfigs.isEmpty()) {
                    throw new ServiceException("❌ 模板 [" + promptTemplate.getTemplateName() + "] 未配置可用的文本生成模型");
                }
                
                log.info("🔄 模板配置了 {} 个文本生成模型，将按优先级依次尝试", textModelConfigs.size());

                // 4. 替换模板中的[]占位符为用户输入的风格描述
                log.info("步骤4️⃣: 替换预提示词中的占位符");
                String templateContent = promptTemplate.getTemplateContent();
                String userStyle = task.getStyleDescription();
                if (StringUtils.isEmpty(userStyle))
                {
                    userStyle = "默认风格";
                }

                String promptWithStyle;
                if (!StringUtils.isEmpty(templateContent))
                {
                    log.info("🔄 使用用户输入 [{}] 替换模板占位符 []", userStyle);
                    promptWithStyle = templateContent.replace("[]", userStyle);
                    log.info("✅ 完整提示词长度: {}", promptWithStyle.length());
                }
                else
                {
                    // 没有模板内容，直接使用用户输入
                    promptWithStyle = userStyle;
                    log.info("✅ 直接使用用户输入作为提示词");
                }
                task.setPrePrompt(promptWithStyle);
                taskMapper.updateMcGenerationTask(task);

                // 5. 调用文本模型优化提示词（支持多模型冗余切换）
                log.info("步骤5️⃣: 调用文本AI优化提示词");
                log.info("📤 发送提示词到文本模型（长度:{}）", promptWithStyle.length());
                log.info("📝 提示词前100字符: {}", promptWithStyle.substring(0, Math.min(100, promptWithStyle.length())));

                optimizedPrompt = null;
                Exception lastException = null;
                
                // 依次尝试每个配置的文本模型
                for (int i = 0; i < textModelConfigs.size(); i++) {
                    McTemplateModelConfig config = textModelConfigs.get(i);
                    
                    // 获取完整的模型信息
                    McAiModel currentModel = aiModelMapper.selectMcAiModelByModelId(config.getModelId());
                    if (currentModel == null) {
                        log.warn("⚠️ 模型ID {} 不存在，跳过", config.getModelId());
                        continue;
                    }
                    
                    log.info("🔄 尝试文本模型 [{}/{}]: {} (ID:{}, 优先级:{})", 
                        i + 1, textModelConfigs.size(), 
                        currentModel.getModelName(), currentModel.getModelId(), config.getPriority());
                    
                    try {
                        // 强制要求文本AI生成英文提示词（提高图像AI识别率）
                        String promptForTextAI = promptWithStyle + "\n\n**IMPORTANT: You must respond in English only. Generate the complete prompt in English.**";
                        optimizedPrompt = aiService.callGeminiTextModel(currentModel, promptForTextAI);
                        log.info("✅ 文本AI优化完成！使用模型: {} (优先级:{}), 优化后长度: {}", 
                            currentModel.getModelName(), config.getPriority(), optimizedPrompt.length());
                        
                        // 更新任务中使用的模型信息
                        task.setTextModelId(currentModel.getModelId());
                        task.setTextModelUrl(currentModel.getApiUrl());
                        generationLog.setTextModelId(currentModel.getModelId());
                        generationLog.setTextModelName(currentModel.getModelName());
                        
                        break; // 成功则跳出循环
                        
                    } catch (Exception e) {
                        lastException = e;
                        log.warn("⚠️ 文本模型 {} (优先级:{}) 调用失败: {}", 
                            currentModel.getModelName(), config.getPriority(), e.getMessage());
                        
                        // 如果不是最后一个模型，继续尝试下一个
                        if (i < textModelConfigs.size() - 1) {
                            log.info("🔄 切换到下一个备用文本模型...");
                            continue;
                        } else {
                            // 所有模型都失败了
                            log.error("❌ 模板配置的所有 {} 个文本生成模型均调用失败", textModelConfigs.size());
                            
                            // 处理429错误
                            if (e.getMessage() != null && e.getMessage().contains("429")) {
                                String errorMsg = "所有文本模型API配额限制：请求过于频繁或配额已用完。建议：1) 等待1分钟后重试 2) 检查API密钥配额";
                                throw new ServiceException(errorMsg);
                            }
                            throw new ServiceException("所有文本生成模型均调用失败，最后错误: " + e.getMessage());
                        }
                    }
                }
                
                if (optimizedPrompt == null) {
                    throw new ServiceException("所有文本生成模型均调用失败" + (lastException != null ? "，最后错误: " + lastException.getMessage() : ""));
                }

                task.setOptimizedPrompt(optimizedPrompt);
                task.setFinalPrompt(optimizedPrompt);
                generationLog.setOptimizedPrompt(optimizedPrompt);
                taskMapper.updateMcGenerationTask(task);
            }

            // 更新日志
            generationLogMapper.updateMcGenerationLog(generationLog);

            // 6. 从模板配置中获取图片生成模型
            log.info("步骤6️⃣: 获取模板绑定的图片生成模型");
            if (promptTemplate.getImageModelId() == null)
            {
                throw new ServiceException("❌ 模板 [" + promptTemplate.getTemplateName() + "] 未配置图片生成模型");
            }
            McAiModel imageModel = aiModelMapper.selectMcAiModelByModelId(promptTemplate.getImageModelId());
            if (imageModel == null)
            {
                throw new ServiceException("❌ 模板绑定的图片生成模型不存在（模型ID: " + promptTemplate.getImageModelId() + "）");
            }
            log.info("✅ 图片模型: {} (ID:{})", imageModel.getModelName(), imageModel.getModelId());

            task.setImageModelId(imageModel.getModelId());
            task.setImageModelUrl(imageModel.getApiUrl());
            generationLog.setImageModelId(imageModel.getModelId());
            generationLog.setImageModelName(imageModel.getModelName());
            taskMapper.updateMcGenerationTask(task);

            // 更新日志
            generationLogMapper.updateMcGenerationLog(generationLog);

            // 7. 调用图片模型生成图片（支持模板配置的备用模型冗余切换）
            log.info("步骤7️⃣: 调用图片AI生成材质图");
            log.info("📤 发送优化后的提示词到图片模型（长度:{}）", optimizedPrompt.length());
            log.info("📝 优化提示词前200字符: {}", optimizedPrompt.substring(0, Math.min(200, optimizedPrompt.length())));

            // 从模板配置表中获取该模板的图片生成模型列表（已按优先级排序）
            List<McTemplateModelConfig> modelConfigs = templateModelConfigMapper.selectEnabledModelsByTemplateAndType(
                promptTemplate.getTemplateId(), 1); // 1=图片生成
            
            if (modelConfigs == null || modelConfigs.isEmpty()) {
                throw new ServiceException("❌ 模板 [" + promptTemplate.getTemplateName() + "] 未配置可用的图片生成模型");
            }
            
            log.info("🔄 模板配置了 {} 个图片生成模型，将按优先级依次尝试", modelConfigs.size());

            // 如果有参考图片，增强提示词以强制AI遵循参考图布局
            String finalPrompt = optimizedPrompt;
            if (promptTemplate.getReferenceImageUrl() != null && !promptTemplate.getReferenceImageUrl().isEmpty()) {
                String imageToImagePrefix = "**CRITICAL INSTRUCTIONS - MUST FOLLOW EXACTLY:**\n\n" +
                    "1. This is an IMAGE-TO-IMAGE generation task. The reference image is the ABSOLUTE source of truth.\n" +
                    "2. PRESERVE EXACT LAYOUT: Maintain pixel-perfect positions, sizes, spacing, and quantities of ALL elements.\n" +
                    "3. PRESERVE TRANSPARENCY: Keep all transparent/empty areas COMPLETELY EMPTY. Do NOT add any pixels, colors, or content to transparent regions.\n" +
                    "4. ONLY MODIFY: Apply the style ONLY to existing non-transparent pixels. Change colors, materials, textures, effects.\n" +
                    "5. STRICTLY FORBIDDEN: Do NOT add, remove, move, resize any elements. Do NOT fill transparent areas.\n\n";
                finalPrompt = imageToImagePrefix + optimizedPrompt;
                log.info("✅ 检测到参考图片，已添加强制布局和透明度保持指令");
            }

            String imageUrl = null;
            Exception lastException = null;
            
            // 依次尝试每个配置的模型
            for (int i = 0; i < modelConfigs.size(); i++) {
                McTemplateModelConfig config = modelConfigs.get(i);
                
                // 获取完整的模型信息
                McAiModel currentModel = aiModelMapper.selectMcAiModelByModelId(config.getModelId());
                if (currentModel == null) {
                    log.warn("⚠️ 模型ID {} 不存在，跳过", config.getModelId());
                    continue;
                }
                
                log.info("🔄 尝试模型 [{}/{}]: {} (ID:{}, 优先级:{})", 
                    i + 1, modelConfigs.size(), 
                    currentModel.getModelName(), currentModel.getModelId(), config.getPriority());
                
                try {
                    // 传递模板的参考图片URL（如果有）
                    imageUrl = aiService.callGeminiImageModel(currentModel, finalPrompt, promptTemplate.getReferenceImageUrl());
                    log.info("✅ 图片生成成功！使用模型: {} (优先级:{})", currentModel.getModelName(), config.getPriority());
                    
                    // 更新任务中使用的模型信息
                    task.setImageModelId(currentModel.getModelId());
                    task.setImageModelUrl(currentModel.getApiUrl());
                    generationLog.setImageModelId(currentModel.getModelId());
                    generationLog.setImageModelName(currentModel.getModelName());
                    
                    break; // 成功则跳出循环
                    
                } catch (Exception e) {
                    lastException = e;
                    log.warn("⚠️ 模型 {} (优先级:{}) 调用失败: {}", 
                        currentModel.getModelName(), config.getPriority(), e.getMessage());
                    
                    // 如果不是最后一个模型，继续尝试下一个
                    if (i < modelConfigs.size() - 1) {
                        log.info("🔄 切换到下一个备用模型...");
                        continue;
                    } else {
                        // 所有模型都失败了
                        log.error("❌ 模板配置的所有 {} 个图片生成模型均调用失败", modelConfigs.size());
                        
                        // 处理429错误
                        if (e.getMessage() != null && e.getMessage().contains("429")) {
                            String errorMsg = "所有图片模型API配额限制：请求过于频繁或配额已用完。建议：1) 等待1分钟后重试 2) 检查API密钥配额";
                            throw new ServiceException(errorMsg);
                        }
                        throw new ServiceException("所有图片生成模型均调用失败，最后错误: " + e.getMessage());
                    }
                }
            }
            
            if (imageUrl == null) {
                throw new ServiceException("图片生成失败，所有模型均不可用。最后错误: " + 
                    (lastException != null ? lastException.getMessage() : "未知错误"));
            }

            // 将本地文件路径转换为相对路径用于数据库存储和前端访问
            String imageUrlForDb = convertLocalPathToRelative(imageUrl);

            task.setImageUrl(imageUrlForDb);
            task.setGeneratedImageUrl(imageUrlForDb);
            generationLog.setGeneratedImageUrl(imageUrlForDb);
            taskMapper.updateMcGenerationTask(task);

            // 更新日志
            generationLogMapper.updateMcGenerationLog(generationLog);

            // 8. 下载并预处理图片（使用原始本地路径）
            log.info("步骤8️⃣: 下载并预处理图片");
            task.setTaskStatus(2); // 预处理中
            taskMapper.updateMcGenerationTask(task);

            String rawImagePath = imageProcessService.downloadImage(imageUrl, task.getTaskNo());
            log.info("✅ 图片下载完成: {}", rawImagePath);

            String processedImagePath = imageProcessService.removeWhiteBackground(rawImagePath, task.getTaskNo());
            log.info("✅ 白底抠除完成: {}", processedImagePath);

            // 注意：模板功能已废弃，用户将在前端自行裁剪和编辑材质包
            // 9. 获取材质模板（已废弃）
            /* 已注释：模板系统已移除
            if (task.getTemplateId() != null)
            {
                log.info("步骤9️⃣: 切割材质图");
                McTextureTemplate template = templateMapper.selectMcTextureTemplateByTemplateId(task.getTemplateId());
                if (template != null)
                {
                    // 切割图片
                    task.setTaskStatus(3); // 切割中
                    taskMapper.updateMcGenerationTask(task);

                    List<String> texturePaths = imageProcessService.autoSplitGrid(
                        processedImagePath,
                        template.getPixelSize(),
                        template.getGridRows(),
                        template.getGridCols(),
                        task.getTaskNo()
                    );
                    log.info("✅ 切割完成，共{}张材质", texturePaths.size());

                    // 保存材质记录
                    log.info("步骤🔟: 保存材质记录到数据库");
                    imageProcessService.saveTextureRecords(task.getTaskId(), template, texturePaths);
                    log.info("✅ 材质记录保存完成");

                    task.setCompletedTextures(template.getTotalCount());
                }
            }
            */

            // 10. 完成
            log.info("步骤✅: 任务完成");
            task.setTaskStatus(4); // 完成
            task.setCompleteTime(new Date());
            taskMapper.updateMcGenerationTask(task);

            // 计算生成耗时并更新日志为成功状态
            long endTimestamp = System.currentTimeMillis();
            int generationTime = (int) ((endTimestamp - startTimestamp) / 1000);
            generationLog.setGenerationTime(generationTime);
            generationLog.setStatus("1"); // 成功
            generationLogMapper.updateMcGenerationLog(generationLog);

            log.info("################################################################################");
            log.info("🎉 材质生成任务完成: {}, 耗时: {}秒", taskId, generationTime);
            log.info("################################################################################");
            return task;
        }
        catch (Exception e)
        {
            log.error("################################################################################");
            log.error("❌ 材质生成失败，任务ID: {}", taskId);
            log.error("❌ 错误类型: {}", e.getClass().getName());
            log.error("❌ 错误信息: {}", e.getMessage());
            log.error("❌ 错误堆栈:", e);
            log.error("################################################################################");

            // 更新任务状态为失败（保存真实错误信息到数据库用于调试）
            try {
                task.setTaskStatus(5); // 失败
                task.setErrorMsg(e.getMessage()); // 数据库保存真实错误信息
                taskMapper.updateMcGenerationTask(task);
            } catch (Exception updateEx) {
                log.error("更新任务状态失败: {}", updateEx.getMessage());
            }

            // 更新日志为失败状态（保存真实错误信息到数据库用于调试）
            try {
                long endTimestamp = System.currentTimeMillis();
                int generationTime = (int) ((endTimestamp - startTimestamp) / 1000);
                generationLog.setGenerationTime(generationTime);
                generationLog.setStatus("2"); // 失败
                generationLog.setErrorMessage(e.getMessage()); // 数据库保存真实错误信息
                generationLogMapper.updateMcGenerationLog(generationLog);
                log.info("✅ 失败日志已记录，日志ID: {}", generationLog.getLogId());
            } catch (Exception logEx) {
                log.error("❌ 更新失败日志失败: {}", logEx.getMessage(), logEx);
            }

            // 抛出通用错误信息给用户（不暴露真实错误）
            throw new ServiceException("GENERATION_FAILED");
        }
    }

    /**
     * 统计用户当前处理中的任务数量
     *
     * @param userId 用户ID
     * @return 处理中的任务数量
     */
    @Override
    public int countProcessingTasksByUserId(Long userId)
    {
        return taskMapper.countProcessingTasksByUserId(userId);
    }

    /**
     * 将本地文件路径转换为相对路径（用于数据库存储和前端访问）
     * 例如: D:/mc/temp/generated_xxx.png -> /mc/temp/generated_xxx.png
     */
    private String convertLocalPathToRelative(String localPath)
    {
        if (localPath == null)
        {
            return null;
        }

        // 如果已经是HTTP URL，直接返回
        if (localPath.startsWith("http://") || localPath.startsWith("https://"))
        {
            return localPath;
        }

        // 如果已经是正确的相对路径（/mc/ 开头），直接返回
        if (localPath.startsWith("/mc/"))
        {
            return localPath;
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

        // 返回相对路径（以 / 开头）
        String result = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        log.debug("路径转换: {} -> {}", localPath, result);
        return result;
    }
}





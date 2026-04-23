package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McTemplateModelConfig;
import com.ruoyi.mc.domain.McAiModel;

/**
 * 模板模型配置Service接口
 *
 * @author ruoyi
 * @date 2026-02-28
 */
public interface IMcTemplateModelConfigService 
{
    /**
     * 查询模板模型配置
     *
     * @param configId 配置ID
     * @return 模板模型配置
     */
    public McTemplateModelConfig selectMcTemplateModelConfigByConfigId(Long configId);

    /**
     * 查询模板模型配置列表
     *
     * @param mcTemplateModelConfig 模板模型配置
     * @return 模板模型配置集合
     */
    public List<McTemplateModelConfig> selectMcTemplateModelConfigList(McTemplateModelConfig mcTemplateModelConfig);

    /**
     * 获取模板的启用模型列表（按优先级排序）
     *
     * @param templateId 模板ID
     * @param modelType 模型类型：1=图片生成 2=文本生成
     * @return 模型列表
     */
    public List<McAiModel> getEnabledModels(Long templateId, Integer modelType);

    /**
     * 新增模板模型配置
     *
     * @param mcTemplateModelConfig 模板模型配置
     * @return 结果
     */
    public int insertMcTemplateModelConfig(McTemplateModelConfig mcTemplateModelConfig);

    /**
     * 修改模板模型配置
     *
     * @param mcTemplateModelConfig 模板模型配置
     * @return 结果
     */
    public int updateMcTemplateModelConfig(McTemplateModelConfig mcTemplateModelConfig);

    /**
     * 批量删除模板模型配置
     *
     * @param configIds 需要删除的配置ID
     * @return 结果
     */
    public int deleteMcTemplateModelConfigByConfigIds(Long[] configIds);

    /**
     * 删除模板模型配置信息
     *
     * @param configId 配置ID
     * @return 结果
     */
    public int deleteMcTemplateModelConfigByConfigId(Long configId);
}

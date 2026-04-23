package com.ruoyi.mc.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mc.domain.McTemplateModelConfig;

/**
 * 模板模型配置Mapper接口
 *
 * @author ruoyi
 * @date 2026-02-28
 */
public interface McTemplateModelConfigMapper 
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
     * 查询模板的启用模型列表（按优先级排序）
     *
     * @param templateId 模板ID
     * @param modelType 模型类型：1=图片生成 2=文本生成
     * @return 模型配置列表
     */
    public List<McTemplateModelConfig> selectEnabledModelsByTemplateAndType(@Param("templateId") Long templateId, @Param("modelType") Integer modelType);

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
     * 删除模板模型配置
     *
     * @param configId 配置ID
     * @return 结果
     */
    public int deleteMcTemplateModelConfigByConfigId(Long configId);

    /**
     * 批量删除模板模型配置
     *
     * @param configIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcTemplateModelConfigByConfigIds(Long[] configIds);
}



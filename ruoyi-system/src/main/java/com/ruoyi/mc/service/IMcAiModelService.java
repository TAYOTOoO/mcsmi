package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McAiModel;

/**
 * AI模型配置Service接口
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public interface IMcAiModelService
{
    /**
     * 查询AI模型配置
     *
     * @param modelId AI模型ID
     * @return AI模型配置
     */
    public McAiModel selectMcAiModelByModelId(Long modelId);

    /**
     * 查询AI模型配置列表
     *
     * @param mcAiModel AI模型配置
     * @return AI模型配置集合
     */
    public List<McAiModel> selectMcAiModelList(McAiModel mcAiModel);

    /**
     * 新增AI模型配置
     *
     * @param mcAiModel AI模型配置
     * @return 结果
     */
    public int insertMcAiModel(McAiModel mcAiModel);

    /**
     * 修改AI模型配置
     *
     * @param mcAiModel AI模型配置
     * @return 结果
     */
    public int updateMcAiModel(McAiModel mcAiModel);

    /**
     * 批量删除AI模型配置
     *
     * @param modelIds 需要删除的AI模型ID
     * @return 结果
     */
    public int deleteMcAiModelByModelIds(Long[] modelIds);

    /**
     * 删除AI模型配置信息
     *
     * @param modelId AI模型ID
     * @return 结果
     */
    public int deleteMcAiModelByModelId(Long modelId);

    /**
     * 禁用同类型的其他模型
     *
     * @param modelType 模型类型
     * @param excludeModelId 排除的模型ID
     * @return 结果
     */
    public int disableOtherModels(Integer modelType, Long excludeModelId);

    /**
     * 获取指定类型的启用模型
     *
     * @param modelType 模型类型(1=图片生成 2=文本生成)
     * @return 启用的模型
     */
    public McAiModel getActiveModelByType(Integer modelType);
}

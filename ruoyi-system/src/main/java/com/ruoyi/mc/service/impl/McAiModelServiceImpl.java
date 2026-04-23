package com.ruoyi.mc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McAiModelMapper;
import com.ruoyi.mc.domain.McAiModel;
import com.ruoyi.mc.service.IMcAiModelService;
import com.ruoyi.common.utils.DateUtils;

/**
 * AI模型配置Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@Service
public class McAiModelServiceImpl implements IMcAiModelService
{
    @Autowired
    private McAiModelMapper mcAiModelMapper;

    /**
     * 查询AI模型配置
     *
     * @param modelId AI模型ID
     * @return AI模型配置
     */
    @Override
    public McAiModel selectMcAiModelByModelId(Long modelId)
    {
        return mcAiModelMapper.selectMcAiModelByModelId(modelId);
    }

    /**
     * 查询AI模型配置列表
     *
     * @param mcAiModel AI模型配置
     * @return AI模型配置
     */
    @Override
    public List<McAiModel> selectMcAiModelList(McAiModel mcAiModel)
    {
        return mcAiModelMapper.selectMcAiModelList(mcAiModel);
    }

    /**
     * 新增AI模型配置
     *
     * @param mcAiModel AI模型配置
     * @return 结果
     */
    @Override
    public int insertMcAiModel(McAiModel mcAiModel)
    {
        mcAiModel.setCreateTime(DateUtils.getNowDate());
        return mcAiModelMapper.insertMcAiModel(mcAiModel);
    }

    /**
     * 修改AI模型配置
     *
     * @param mcAiModel AI模型配置
     * @return 结果
     */
    @Override
    public int updateMcAiModel(McAiModel mcAiModel)
    {
        mcAiModel.setUpdateTime(DateUtils.getNowDate());
        return mcAiModelMapper.updateMcAiModel(mcAiModel);
    }

    /**
     * 批量删除AI模型配置
     *
     * @param modelIds 需要删除的AI模型ID
     * @return 结果
     */
    @Override
    public int deleteMcAiModelByModelIds(Long[] modelIds)
    {
        return mcAiModelMapper.deleteMcAiModelByModelIds(modelIds);
    }

    /**
     * 删除AI模型配置信息
     *
     * @param modelId AI模型ID
     * @return 结果
     */
    @Override
    public int deleteMcAiModelByModelId(Long modelId)
    {
        return mcAiModelMapper.deleteMcAiModelByModelId(modelId);
    }

    /**
     * 禁用同类型的其他模型
     *
     * @param modelType 模型类型
     * @param excludeModelId 排除的模型ID
     * @return 结果
     */
    @Override
    public int disableOtherModels(Integer modelType, Long excludeModelId)
    {
        return mcAiModelMapper.disableOtherModels(modelType, excludeModelId);
    }

    /**
     * 获取指定类型的启用模型
     *
     * @param modelType 模型类型(1=图片生成 2=文本生成)
     * @return 启用的模型
     */
    @Override
    public McAiModel getActiveModelByType(Integer modelType)
    {
        return mcAiModelMapper.getActiveModelByType(modelType);
    }
}

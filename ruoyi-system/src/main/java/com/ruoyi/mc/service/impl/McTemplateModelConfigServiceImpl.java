package com.ruoyi.mc.service.impl;

import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McTemplateModelConfigMapper;
import com.ruoyi.mc.mapper.McAiModelMapper;
import com.ruoyi.mc.domain.McTemplateModelConfig;
import com.ruoyi.mc.domain.McAiModel;
import com.ruoyi.mc.service.IMcTemplateModelConfigService;

/**
 * 模板模型配置Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-28
 */
@Service
public class McTemplateModelConfigServiceImpl implements IMcTemplateModelConfigService 
{
    @Autowired
    private McTemplateModelConfigMapper configMapper;

    @Autowired
    private McAiModelMapper modelMapper;

    @Override
    public McTemplateModelConfig selectMcTemplateModelConfigByConfigId(Long configId)
    {
        return configMapper.selectMcTemplateModelConfigByConfigId(configId);
    }

    @Override
    public List<McTemplateModelConfig> selectMcTemplateModelConfigList(McTemplateModelConfig mcTemplateModelConfig)
    {
        return configMapper.selectMcTemplateModelConfigList(mcTemplateModelConfig);
    }

    @Override
    public List<McAiModel> getEnabledModels(Long templateId, Integer modelType)
    {
        List<McTemplateModelConfig> configs = configMapper.selectEnabledModelsByTemplateAndType(templateId, modelType);
        List<McAiModel> models = new ArrayList<>();
        for (McTemplateModelConfig config : configs)
        {
            McAiModel model = modelMapper.selectMcAiModelByModelId(config.getModelId());
            if (model != null)
            {
                models.add(model);
            }
        }
        return models;
    }

    @Override
    public int insertMcTemplateModelConfig(McTemplateModelConfig mcTemplateModelConfig)
    {
        return configMapper.insertMcTemplateModelConfig(mcTemplateModelConfig);
    }

    @Override
    public int updateMcTemplateModelConfig(McTemplateModelConfig mcTemplateModelConfig)
    {
        return configMapper.updateMcTemplateModelConfig(mcTemplateModelConfig);
    }

    @Override
    public int deleteMcTemplateModelConfigByConfigIds(Long[] configIds)
    {
        return configMapper.deleteMcTemplateModelConfigByConfigIds(configIds);
    }

    @Override
    public int deleteMcTemplateModelConfigByConfigId(Long configId)
    {
        return configMapper.deleteMcTemplateModelConfigByConfigId(configId);
    }
}

package com.ruoyi.mc.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McSystemConfigMapper;
import com.ruoyi.mc.domain.McSystemConfig;
import com.ruoyi.mc.service.IMcSystemConfigService;

/**
 * 系统配置Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class McSystemConfigServiceImpl implements IMcSystemConfigService
{
    @Autowired
    private McSystemConfigMapper mcSystemConfigMapper;

    /**
     * 查询系统配置
     *
     * @param configId 系统配置ID
     * @return 系统配置
     */
    @Override
    public McSystemConfig selectMcSystemConfigByConfigId(Long configId)
    {
        return mcSystemConfigMapper.selectMcSystemConfigByConfigId(configId);
    }

    /**
     * 根据配置键查询配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @Override
    public String selectConfigValueByKey(String configKey)
    {
        McSystemConfig config = mcSystemConfigMapper.selectMcSystemConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 根据配置键查询配置值(整数)
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Override
    public Integer selectConfigIntValue(String configKey, Integer defaultValue)
    {
        String value = selectConfigValueByKey(configKey);
        if (value == null || value.trim().isEmpty())
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    /**
     * 根据配置键查询配置值(布尔)
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    @Override
    public Boolean selectConfigBoolValue(String configKey, Boolean defaultValue)
    {
        String value = selectConfigValueByKey(configKey);
        if (value == null || value.trim().isEmpty())
        {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * 查询系统配置列表
     *
     * @param mcSystemConfig 系统配置
     * @return 系统配置
     */
    @Override
    public List<McSystemConfig> selectMcSystemConfigList(McSystemConfig mcSystemConfig)
    {
        return mcSystemConfigMapper.selectMcSystemConfigList(mcSystemConfig);
    }

    /**
     * 新增系统配置
     *
     * @param mcSystemConfig 系统配置
     * @return 结果
     */
    @Override
    public int insertMcSystemConfig(McSystemConfig mcSystemConfig)
    {
        return mcSystemConfigMapper.insertMcSystemConfig(mcSystemConfig);
    }

    /**
     * 修改系统配置
     *
     * @param mcSystemConfig 系统配置
     * @return 结果
     */
    @Override
    public int updateMcSystemConfig(McSystemConfig mcSystemConfig)
    {
        return mcSystemConfigMapper.updateMcSystemConfig(mcSystemConfig);
    }

    /**
     * 批量删除系统配置
     *
     * @param configIds 需要删除的系统配置ID
     * @return 结果
     */
    @Override
    public int deleteMcSystemConfigByConfigIds(Long[] configIds)
    {
        return mcSystemConfigMapper.deleteMcSystemConfigByConfigIds(configIds);
    }

    /**
     * 删除系统配置信息
     *
     * @param configId 系统配置ID
     * @return 结果
     */
    @Override
    public int deleteMcSystemConfigByConfigId(Long configId)
    {
        return mcSystemConfigMapper.deleteMcSystemConfigByConfigId(configId);
    }
}

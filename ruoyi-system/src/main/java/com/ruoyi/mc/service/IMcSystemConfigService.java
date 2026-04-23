package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McSystemConfig;

/**
 * 系统配置Service接口
 *
 * @author ruoyi
 */
public interface IMcSystemConfigService
{
    /**
     * 查询系统配置
     *
     * @param configId 系统配置ID
     * @return 系统配置
     */
    public McSystemConfig selectMcSystemConfigByConfigId(Long configId);

    /**
     * 根据配置键查询配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    public String selectConfigValueByKey(String configKey);

    /**
     * 根据配置键查询配置值(整数)
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Integer selectConfigIntValue(String configKey, Integer defaultValue);

    /**
     * 根据配置键查询配置值(布尔)
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public Boolean selectConfigBoolValue(String configKey, Boolean defaultValue);

    /**
     * 查询系统配置列表
     *
     * @param mcSystemConfig 系统配置
     * @return 系统配置集合
     */
    public List<McSystemConfig> selectMcSystemConfigList(McSystemConfig mcSystemConfig);

    /**
     * 新增系统配置
     *
     * @param mcSystemConfig 系统配置
     * @return 结果
     */
    public int insertMcSystemConfig(McSystemConfig mcSystemConfig);

    /**
     * 修改系统配置
     *
     * @param mcSystemConfig 系统配置
     * @return 结果
     */
    public int updateMcSystemConfig(McSystemConfig mcSystemConfig);

    /**
     * 批量删除系统配置
     *
     * @param configIds 需要删除的系统配置ID
     * @return 结果
     */
    public int deleteMcSystemConfigByConfigIds(Long[] configIds);

    /**
     * 删除系统配置信息
     *
     * @param configId 系统配置ID
     * @return 结果
     */
    public int deleteMcSystemConfigByConfigId(Long configId);
}

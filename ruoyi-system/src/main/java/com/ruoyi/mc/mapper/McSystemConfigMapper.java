package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McSystemConfig;

/**
 * 系统配置Mapper接口
 *
 * @author ruoyi
 */
public interface McSystemConfigMapper
{
    /**
     * 查询系统配置
     *
     * @param configId 系统配置ID
     * @return 系统配置
     */
    public McSystemConfig selectMcSystemConfigByConfigId(Long configId);

    /**
     * 根据配置键查询配置
     *
     * @param configKey 配置键
     * @return 系统配置
     */
    public McSystemConfig selectMcSystemConfigByKey(String configKey);

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
     * 删除系统配置
     *
     * @param configId 系统配置ID
     * @return 结果
     */
    public int deleteMcSystemConfigByConfigId(Long configId);

    /**
     * 批量删除系统配置
     *
     * @param configIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteMcSystemConfigByConfigIds(Long[] configIds);
}

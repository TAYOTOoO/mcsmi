package com.ruoyi.mc.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McPaymentConfigMapper;
import com.ruoyi.mc.domain.McPaymentConfig;
import com.ruoyi.mc.service.IMcPaymentConfigService;

/**
 * 支付配置Service业务层处理
 *
 * @author ruoyi
 * @date 2024
 */
@Service
public class McPaymentConfigServiceImpl implements IMcPaymentConfigService
{
    @Autowired
    private McPaymentConfigMapper mcPaymentConfigMapper;

    /**
     * 查询支付配置
     *
     * @param configId 支付配置主键
     * @return 支付配置
     */
    @Override
    public McPaymentConfig selectMcPaymentConfigByConfigId(Long configId)
    {
        return mcPaymentConfigMapper.selectMcPaymentConfigByConfigId(configId);
    }

    /**
     * 查询支付配置列表
     *
     * @param mcPaymentConfig 支付配置
     * @return 支付配置
     */
    @Override
    public List<McPaymentConfig> selectMcPaymentConfigList(McPaymentConfig mcPaymentConfig)
    {
        return mcPaymentConfigMapper.selectMcPaymentConfigList(mcPaymentConfig);
    }

    /**
     * 新增支付配置
     *
     * @param mcPaymentConfig 支付配置
     * @return 结果
     */
    @Override
    public int insertMcPaymentConfig(McPaymentConfig mcPaymentConfig)
    {
        mcPaymentConfig.setCreateTime(DateUtils.getNowDate());
        // 如果设置为默认，取消其他默认配置
        if ("1".equals(mcPaymentConfig.getIsDefault()))
        {
            mcPaymentConfigMapper.cancelAllDefaultConfig();
        }
        return mcPaymentConfigMapper.insertMcPaymentConfig(mcPaymentConfig);
    }

    /**
     * 修改支付配置
     *
     * @param mcPaymentConfig 支付配置
     * @return 结果
     */
    @Override
    public int updateMcPaymentConfig(McPaymentConfig mcPaymentConfig)
    {
        mcPaymentConfig.setUpdateTime(DateUtils.getNowDate());
        // 如果设置为默认，取消其他默认配置
        if ("1".equals(mcPaymentConfig.getIsDefault()))
        {
            mcPaymentConfigMapper.cancelAllDefaultConfig();
        }
        return mcPaymentConfigMapper.updateMcPaymentConfig(mcPaymentConfig);
    }

    /**
     * 批量删除支付配置
     *
     * @param configIds 需要删除的支付配置主键
     * @return 结果
     */
    @Override
    public int deleteMcPaymentConfigByConfigIds(Long[] configIds)
    {
        return mcPaymentConfigMapper.deleteMcPaymentConfigByConfigIds(configIds);
    }

    /**
     * 删除支付配置信息
     *
     * @param configId 支付配置主键
     * @return 结果
     */
    @Override
    public int deleteMcPaymentConfigByConfigId(Long configId)
    {
        return mcPaymentConfigMapper.deleteMcPaymentConfigByConfigId(configId);
    }

    /**
     * 查询默认支付配置
     *
     * @return 支付配置
     */
    @Override
    public McPaymentConfig selectDefaultPaymentConfig()
    {
        return mcPaymentConfigMapper.selectDefaultPaymentConfig();
    }
}

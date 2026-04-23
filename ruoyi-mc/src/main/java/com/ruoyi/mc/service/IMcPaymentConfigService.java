package com.ruoyi.mc.service;

import java.util.List;
import com.ruoyi.mc.domain.McPaymentConfig;

/**
 * 支付配置Service接口
 *
 * @author ruoyi
 * @date 2024
 */
public interface IMcPaymentConfigService
{
    /**
     * 查询支付配置
     *
     * @param configId 支付配置主键
     * @return 支付配置
     */
    public McPaymentConfig selectMcPaymentConfigByConfigId(Long configId);

    /**
     * 查询支付配置列表
     *
     * @param mcPaymentConfig 支付配置
     * @return 支付配置集合
     */
    public List<McPaymentConfig> selectMcPaymentConfigList(McPaymentConfig mcPaymentConfig);

    /**
     * 新增支付配置
     *
     * @param mcPaymentConfig 支付配置
     * @return 结果
     */
    public int insertMcPaymentConfig(McPaymentConfig mcPaymentConfig);

    /**
     * 修改支付配置
     *
     * @param mcPaymentConfig 支付配置
     * @return 结果
     */
    public int updateMcPaymentConfig(McPaymentConfig mcPaymentConfig);

    /**
     * 批量删除支付配置
     *
     * @param configIds 需要删除的支付配置主键集合
     * @return 结果
     */
    public int deleteMcPaymentConfigByConfigIds(Long[] configIds);

    /**
     * 删除支付配置信息
     *
     * @param configId 支付配置主键
     * @return 结果
     */
    public int deleteMcPaymentConfigByConfigId(Long configId);

    /**
     * 查询默认支付配置
     *
     * @return 支付配置
     */
    public McPaymentConfig selectDefaultPaymentConfig();
}

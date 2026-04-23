package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McSubscriptionPackage;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订阅套餐Service接口
 */
public interface IMcSubscriptionPackageService {

    /**
     * 查询订阅套餐列表
     */
    public List<McSubscriptionPackage> selectMcSubscriptionPackageList(McSubscriptionPackage mcSubscriptionPackage);

    /**
     * 根据套餐ID查询订阅套餐
     */
    public McSubscriptionPackage selectMcSubscriptionPackageByPackageId(Long packageId);

    /**
     * 查询所有可用套餐
     */
    public List<McSubscriptionPackage> selectAvailablePackages();

    /**
     * 新增订阅套餐
     */
    public int insertMcSubscriptionPackage(McSubscriptionPackage mcSubscriptionPackage);

    /**
     * 修改订阅套餐
     */
    public int updateMcSubscriptionPackage(McSubscriptionPackage mcSubscriptionPackage);

    /**
     * 删除订阅套餐
     */
    public int deleteMcSubscriptionPackageByPackageId(Long packageId);

    /**
     * 批量删除订阅套餐
     */
    public int deleteMcSubscriptionPackageByPackageIds(Long[] packageIds);

    /**
     * 计算实付金额（考虑订阅用户折扣）
     * @param amount 原价
     * @param isSubscriber 是否为订阅用户
     * @return 实付金额
     */
    public BigDecimal calculateActualAmount(BigDecimal amount, boolean isSubscriber);
}

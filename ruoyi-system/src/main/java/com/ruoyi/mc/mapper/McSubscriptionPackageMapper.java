package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McSubscriptionPackage;
import java.util.List;

/**
 * 订阅套餐Mapper接口
 */
public interface McSubscriptionPackageMapper {

    /**
     * 查询订阅套餐列表
     */
    public List<McSubscriptionPackage> selectMcSubscriptionPackageList(McSubscriptionPackage mcSubscriptionPackage);

    /**
     * 根据套餐ID查询订阅套餐
     */
    public McSubscriptionPackage selectMcSubscriptionPackageByPackageId(Long packageId);

    /**
     * 查询所有可用套餐（status='0'）
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
}

package com.ruoyi.mc.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McSubscriptionPackageMapper;
import com.ruoyi.mc.domain.McSubscriptionPackage;
import com.ruoyi.mc.service.IMcSubscriptionPackageService;
import com.ruoyi.common.utils.DateUtils;

/**
 * 订阅套餐Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-07
 */
@Service
public class McSubscriptionPackageServiceImpl implements IMcSubscriptionPackageService
{
    @Autowired
    private McSubscriptionPackageMapper mcSubscriptionPackageMapper;

    @Override
    public List<McSubscriptionPackage> selectMcSubscriptionPackageList(McSubscriptionPackage mcSubscriptionPackage)
    {
        return mcSubscriptionPackageMapper.selectMcSubscriptionPackageList(mcSubscriptionPackage);
    }

    @Override
    public McSubscriptionPackage selectMcSubscriptionPackageByPackageId(Long packageId)
    {
        return mcSubscriptionPackageMapper.selectMcSubscriptionPackageByPackageId(packageId);
    }

    @Override
    public List<McSubscriptionPackage> selectAvailablePackages()
    {
        return mcSubscriptionPackageMapper.selectAvailablePackages();
    }

    @Override
    public int insertMcSubscriptionPackage(McSubscriptionPackage mcSubscriptionPackage)
    {
        mcSubscriptionPackage.setCreateTime(DateUtils.getNowDate());
        return mcSubscriptionPackageMapper.insertMcSubscriptionPackage(mcSubscriptionPackage);
    }

    @Override
    public int updateMcSubscriptionPackage(McSubscriptionPackage mcSubscriptionPackage)
    {
        mcSubscriptionPackage.setUpdateTime(DateUtils.getNowDate());
        return mcSubscriptionPackageMapper.updateMcSubscriptionPackage(mcSubscriptionPackage);
    }

    @Override
    public int deleteMcSubscriptionPackageByPackageId(Long packageId)
    {
        return mcSubscriptionPackageMapper.deleteMcSubscriptionPackageByPackageId(packageId);
    }

    @Override
    public int deleteMcSubscriptionPackageByPackageIds(Long[] packageIds)
    {
        return mcSubscriptionPackageMapper.deleteMcSubscriptionPackageByPackageIds(packageIds);
    }

    @Override
    public BigDecimal calculateActualAmount(BigDecimal amount, boolean isSubscriber)
    {
        if (amount == null)
        {
            return BigDecimal.ZERO;
        }

        if (!isSubscriber)
        {
            return amount;
        }

        // 钻石订阅用户享受8折优惠（0.80）
        BigDecimal discountRate = new BigDecimal("0.80");
        return amount.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}

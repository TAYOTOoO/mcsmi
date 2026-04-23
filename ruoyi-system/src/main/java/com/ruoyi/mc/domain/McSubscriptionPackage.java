package com.ruoyi.mc.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订阅套餐对象 mc_subscription_package
 */
public class McSubscriptionPackage extends BaseEntity {

    private Long packageId;
    private String packageName;
    private Integer durationMonths;
    private BigDecimal amount;
    private Integer monthlyPoints;  // 每月赠送积分
    private BigDecimal discountRate; // 充值折扣率
    private Integer sort;
    private String status;

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getMonthlyPoints() {
        return monthlyPoints;
    }

    public void setMonthlyPoints(Integer monthlyPoints) {
        this.monthlyPoints = monthlyPoints;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取总赠送积分（按月数计算）
     */
    public Integer getTotalPoints() {
        if (monthlyPoints == null || durationMonths == null) {
            return 0;
        }
        return monthlyPoints * durationMonths;
    }

    /**
     * 获取折扣百分比显示（例如：0.80 -> "8折"）
     */
    public String getDiscountDisplay() {
        if (discountRate == null) {
            return "无折扣";
        }
        int discount = discountRate.multiply(new BigDecimal("10")).intValue();
        return discount + "折";
    }
}

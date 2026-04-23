package com.ruoyi.mc.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 充值套餐对象 mc_recharge_package
 */
public class McRechargePackage extends BaseEntity {

    private Long packageId;
    private String packageName;
    private BigDecimal amount;
    private Integer points;
    private Integer bonusPoints;
    private Integer sort;
    private String status;

    // Getters and Setters
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(Integer bonusPoints) {
        this.bonusPoints = bonusPoints;
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
     * 获取总积分（基础积分 + 赠送积分）
     */
    public Integer getTotalPoints() {
        return (points != null ? points : 0) + (bonusPoints != null ? bonusPoints : 0);
    }
}

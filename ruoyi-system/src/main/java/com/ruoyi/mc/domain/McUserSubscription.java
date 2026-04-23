package com.ruoyi.mc.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户订阅信息对象 mc_user_subscription
 */
public class McUserSubscription extends BaseEntity {

    private Long subscriptionId;
    private Long userId;
    private String subscriptionType; // normal普通用户 diamond钻石订阅用户
    private Long packageId; // 套餐ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private Integer isPermanent; // 0否 1是
    private String lastPointGrantMonth; // 格式：2026-02

    // 非数据库字段，用于前端展示
    private String userName;
    private String nickName;
    private Boolean isExpired; // 是否已到期
    private String packageName; // 套餐名称
    private java.math.BigDecimal discountRate; // 充值折扣率
    private Integer monthlyPoints; // 月赠积分

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getIsPermanent() {
        return isPermanent;
    }

    public void setIsPermanent(Integer isPermanent) {
        this.isPermanent = isPermanent;
    }

    public String getLastPointGrantMonth() {
        return lastPointGrantMonth;
    }

    public void setLastPointGrantMonth(String lastPointGrantMonth) {
        this.lastPointGrantMonth = lastPointGrantMonth;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Boolean getIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public java.math.BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(java.math.BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getMonthlyPoints() {
        return monthlyPoints;
    }

    public void setMonthlyPoints(Integer monthlyPoints) {
        this.monthlyPoints = monthlyPoints;
    }

    /**
     * 判断订阅是否有效
     */
    public boolean isActive() {
        if (isPermanent != null && isPermanent == 1) {
            return true;
        }
        if (expireTime == null) {
            return false;
        }
        return expireTime.after(new Date());
    }

    /**
     * 判断是否为钻石订阅用户
     */
    public boolean isDiamond() {
        return "diamond".equals(subscriptionType) && isActive();
    }
}

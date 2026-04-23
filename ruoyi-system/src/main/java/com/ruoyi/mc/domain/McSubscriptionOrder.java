package com.ruoyi.mc.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订阅订单对象 mc_subscription_order
 */
public class McSubscriptionOrder extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private Long packageId;
    private String packageName;
    private Integer durationMonths;
    private BigDecimal originalAmount;
    private BigDecimal actualAmount;
    private Integer paymentStatus; // 0待支付 1已支付 2已取消 3已退款
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date paymentTime;

    private String tradeNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    // 非数据库字段
    private String userName;
    private String nickName;
    private String paymentStatusName;
    private String status;  // 字符串类型status，用于前端兼容（"0"/"1"/"2"/"3"）
    private BigDecimal amount;  // 等同于actualAmount，用于前端兼容
    private String username;  // 等同于userName，用于前端兼容
    private Date payTime;  // 等同于paymentTime，用于前端兼容

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

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

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
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

    public String getPaymentStatusName() {
        return paymentStatusName;
    }

    public void setPaymentStatusName(String paymentStatusName) {
        this.paymentStatusName = paymentStatusName;
    }

    public String getStatus() {
        if (paymentStatus != null) {
            return String.valueOf(paymentStatus);
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if (status != null) {
            try {
                this.paymentStatus = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                // 忽略转换错误
            }
        }
    }

    public BigDecimal getAmount() {
        return actualAmount != null ? actualAmount : amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.actualAmount = amount;
    }

    public String getUsername() {
        return userName != null ? userName : username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.userName = username;
    }

    public Date getPayTime() {
        return paymentTime != null ? paymentTime : payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
        this.paymentTime = payTime;
    }

    /**
     * 获取支付状态文本
     */
    public String getPaymentStatusText() {
        if (paymentStatus == null) {
            return "未知";
        }
        switch (paymentStatus) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已取消";
            case 3: return "已退款";
            default: return "未知";
        }
    }
}

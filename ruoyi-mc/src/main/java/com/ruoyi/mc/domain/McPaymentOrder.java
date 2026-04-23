package com.ruoyi.mc.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 支付订单对象 mc_payment_order
 *
 * @author ruoyi
 * @date 2024
 */
public class McPaymentOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 商户订单号 */
    @Excel(name = "商户订单号")
    private String outTradeNo;

    /** 支付平台订单号 */
    @Excel(name = "支付平台订单号")
    private String tradeNo;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String username;

    /** 订单类型（recharge充值/subscription订阅） */
    @Excel(name = "订单类型", readConverterExp = "recharge=积分充值,subscription=订阅套餐")
    private String orderType;

    /** 套餐ID（订阅订单使用） */
    private Long packageId;

    /** 订阅时长（月） */
    @Excel(name = "订阅时长（月）")
    private Integer durationMonths;

    /** 订阅开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "订阅开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 订阅到期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "订阅到期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String productName;

    /** 支付金额 */
    @Excel(name = "支付金额")
    private BigDecimal amount;

    /** 充值积分数 */
    @Excel(name = "充值积分数")
    private Integer points;

    /** 支付方式 */
    @Excel(name = "支付方式")
    private String paymentType;

    /** 订单状态（0待支付 1已支付 2已取消 3已退款） */
    @Excel(name = "订单状态", readConverterExp = "0=待支付,1=已支付,2=已取消,3=已退款")
    private String status;

    /** 通知状态（0未通知 1已通知） */
    @Excel(name = "通知状态", readConverterExp = "0=未通知,1=已通知")
    private String notifyStatus;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 通知时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "通知时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date notifyTime;

    /** 支付配置ID */
    private Long configId;

    /** 业务扩展参数 */
    private String param;

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOutTradeNo(String outTradeNo)
    {
        this.outTradeNo = outTradeNo;
    }

    public String getOutTradeNo()
    {
        return outTradeNo;
    }

    public void setTradeNo(String tradeNo)
    {
        this.tradeNo = tradeNo;
    }

    public String getTradeNo()
    {
        return tradeNo;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public void setOrderType(String orderType)
    {
        this.orderType = orderType;
    }

    public String getOrderType()
    {
        return orderType;
    }

    public void setPackageId(Long packageId)
    {
        this.packageId = packageId;
    }

    public Long getPackageId()
    {
        return packageId;
    }

    public void setDurationMonths(Integer durationMonths)
    {
        this.durationMonths = durationMonths;
    }

    public Integer getDurationMonths()
    {
        return durationMonths;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPaymentType(String paymentType)
    {
        this.paymentType = paymentType;
    }

    public String getPaymentType()
    {
        return paymentType;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setNotifyStatus(String notifyStatus)
    {
        this.notifyStatus = notifyStatus;
    }

    public String getNotifyStatus()
    {
        return notifyStatus;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setNotifyTime(Date notifyTime)
    {
        this.notifyTime = notifyTime;
    }

    public Date getNotifyTime()
    {
        return notifyTime;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public Long getConfigId()
    {
        return configId;
    }

    public void setParam(String param)
    {
        this.param = param;
    }

    public String getParam()
    {
        return param;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("outTradeNo", getOutTradeNo())
            .append("tradeNo", getTradeNo())
            .append("userId", getUserId())
            .append("username", getUsername())
            .append("orderType", getOrderType())
            .append("packageId", getPackageId())
            .append("durationMonths", getDurationMonths())
            .append("startTime", getStartTime())
            .append("expireTime", getExpireTime())
            .append("productName", getProductName())
            .append("amount", getAmount())
            .append("points", getPoints())
            .append("paymentType", getPaymentType())
            .append("status", getStatus())
            .append("notifyStatus", getNotifyStatus())
            .append("payTime", getPayTime())
            .append("notifyTime", getNotifyTime())
            .append("configId", getConfigId())
            .append("param", getParam())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

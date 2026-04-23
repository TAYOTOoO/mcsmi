package com.ruoyi.mc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 支付配置对象 mc_payment_config
 *
 * @author ruoyi
 * @date 2024
 */
public class McPaymentConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 配置名称 */
    @Excel(name = "配置名称")
    private String configName;

    /** 支付API地址 */
    @Excel(name = "支付API地址")
    private String apiUrl;

    /** 商户ID */
    @Excel(name = "商户ID")
    private String merchantId;

    /** 商户密钥 */
    private String merchantKey;

    /** 异步通知地址 */
    @Excel(name = "异步通知地址")
    private String notifyUrl;

    /** 同步返回地址 */
    @Excel(name = "同步返回地址")
    private String returnUrl;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 是否默认（0否 1是） */
    @Excel(name = "是否默认", readConverterExp = "0=否,1=是")
    private String isDefault;

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigName(String configName)
    {
        this.configName = configName;
    }

    public String getConfigName()
    {
        return configName;
    }

    public void setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }

    public String getApiUrl()
    {
        return apiUrl;
    }

    public void setMerchantId(String merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantKey(String merchantKey)
    {
        this.merchantKey = merchantKey;
    }

    public String getMerchantKey()
    {
        return merchantKey;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = notifyUrl;
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configName", getConfigName())
            .append("apiUrl", getApiUrl())
            .append("merchantId", getMerchantId())
            .append("merchantKey", getMerchantKey())
            .append("notifyUrl", getNotifyUrl())
            .append("returnUrl", getReturnUrl())
            .append("status", getStatus())
            .append("isDefault", getIsDefault())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

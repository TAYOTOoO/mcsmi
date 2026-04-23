package com.ruoyi.mc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 系统配置对象 mc_system_config
 *
 * @author ruoyi
 */
public class McSystemConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 配置键 */
    @Excel(name = "配置键")
    private String configKey;

    /** 配置值 */
    @Excel(name = "配置值")
    private String configValue;

    /** 配置名称 */
    @Excel(name = "配置名称")
    private String configName;

    /** 配置类型(1=数字 2=字符串 3=布尔) */
    @Excel(name = "配置类型")
    private String configType;

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }

    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigName(String configName)
    {
        this.configName = configName;
    }

    public String getConfigName()
    {
        return configName;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    public String getConfigType()
    {
        return configType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configName", getConfigName())
            .append("configType", getConfigType())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

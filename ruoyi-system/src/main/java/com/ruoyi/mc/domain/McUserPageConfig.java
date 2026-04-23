package com.ruoyi.mc.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户端页面配置对象 mc_user_page_config
 *
 * @author ruoyi
 * @date 2026-02-04
 */
public class McUserPageConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 配置键（如home.title） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 配置类型：text文本/number数字/image图片/json复杂对象 */
    private String configType;

    /** 页面名称（home/generate/tasks/recharge/editor/profile等） */
    private String pageName;

    /** 分组名称（用于页面内分组） */
    private String groupName;

    /** 排序 */
    private Integer sortOrder;

    /** 配置说明 */
    private String description;

    /** 状态（0正常 1停用） */
    private String status;

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

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    public String getConfigType()
    {
        return configType;
    }

    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    public String getPageName()
    {
        return pageName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configType", getConfigType())
            .append("pageName", getPageName())
            .append("groupName", getGroupName())
            .append("sortOrder", getSortOrder())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

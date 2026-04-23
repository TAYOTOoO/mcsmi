package com.ruoyi.mc.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * MC版本对象 mc_version
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public class McVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long versionId;

    /** 版本号 */
    private String versionName;

    /** 版本代号 */
    private String versionCode;

    /** 资源包格式版本号 */
    private Integer packFormat;

    /** 版本描述 */
    private String description;

    /** 状态（0正常 1停用） */
    private String status;

    /** 创建者 */
    private String createBy;

    /** 更新者 */
    private String updateBy;

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }

    public String getVersionName()
    {
        return versionName;
    }

    public void setVersionCode(String versionCode)
    {
        this.versionCode = versionCode;
    }

    public String getVersionCode()
    {
        return versionCode;
    }

    public void setPackFormat(Integer packFormat)
    {
        this.packFormat = packFormat;
    }

    public Integer getPackFormat()
    {
        return packFormat;
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

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("versionId", getVersionId())
            .append("versionName", getVersionName())
            .append("versionCode", getVersionCode())
            .append("packFormat", getPackFormat())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

package com.ruoyi.mc.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

/**
 * 兑换码对象 mc_redemptions
 *
 * @author ruoyi
 * @date 2026-02-06
 */
public class McRedemption implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 创建者ID（管理员） */
    @Excel(name = "创建者ID")
    private Long userId;

    /** 兑换码（32位唯一） */
    @Excel(name = "兑换码")
    private String key;

    /** 状态：1未使用 2已使用 3已过期 */
    @Excel(name = "状态", readConverterExp = "1=未使用,2=已使用,3=已过期")
    private Long status;

    /** 批次名称 */
    @Excel(name = "批次名称")
    private String name;

    /** 额度（积分数） */
    @Excel(name = "额度")
    private Long quota;

    /** 创建时间（毫秒时间戳） */
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Long createdTime;

    /** 兑换时间（毫秒时间戳） */
    @Excel(name = "兑换时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Long redeemedTime;

    /** 兑换用户ID */
    @Excel(name = "兑换用户ID")
    private Long usedUserId;

    /** 软删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deletedAt;

    /** 过期时间（毫秒时间戳） */
    @Excel(name = "过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Long expiredTime;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public void setStatus(Long status)
    {
        this.status = status;
    }

    public Long getStatus()
    {
        return status;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setQuota(Long quota)
    {
        this.quota = quota;
    }

    public Long getQuota()
    {
        return quota;
    }

    public void setCreatedTime(Long createdTime)
    {
        this.createdTime = createdTime;
    }

    public Long getCreatedTime()
    {
        return createdTime;
    }

    public void setRedeemedTime(Long redeemedTime)
    {
        this.redeemedTime = redeemedTime;
    }

    public Long getRedeemedTime()
    {
        return redeemedTime;
    }

    public void setUsedUserId(Long usedUserId)
    {
        this.usedUserId = usedUserId;
    }

    public Long getUsedUserId()
    {
        return usedUserId;
    }

    public void setDeletedAt(Date deletedAt)
    {
        this.deletedAt = deletedAt;
    }

    public Date getDeletedAt()
    {
        return deletedAt;
    }

    public void setExpiredTime(Long expiredTime)
    {
        this.expiredTime = expiredTime;
    }

    public Long getExpiredTime()
    {
        return expiredTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("key", getKey())
            .append("status", getStatus())
            .append("name", getName())
            .append("quota", getQuota())
            .append("createdTime", getCreatedTime())
            .append("redeemedTime", getRedeemedTime())
            .append("usedUserId", getUsedUserId())
            .append("deletedAt", getDeletedAt())
            .append("expiredTime", getExpiredTime())
            .toString();
    }
}

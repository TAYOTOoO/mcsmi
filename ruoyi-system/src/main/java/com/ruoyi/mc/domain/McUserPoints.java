package com.ruoyi.mc.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户积分对象 mc_user_points
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public class McUserPoints extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 积分ID */
    private Long pointId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 总积分 */
    @Excel(name = "总积分")
    private Integer totalPoints;

    /** 已用积分 */
    @Excel(name = "已用积分")
    private Integer usedPoints;

    /** 剩余积分 */
    @Excel(name = "剩余积分")
    private Integer remainingPoints;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setPointId(Long pointId)
    {
        this.pointId = pointId;
    }

    public Long getPointId()
    {
        return pointId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setTotalPoints(Integer totalPoints)
    {
        this.totalPoints = totalPoints;
    }

    public Integer getTotalPoints()
    {
        return totalPoints;
    }

    public void setUsedPoints(Integer usedPoints)
    {
        this.usedPoints = usedPoints;
    }

    public Integer getUsedPoints()
    {
        return usedPoints;
    }

    public void setRemainingPoints(Integer remainingPoints)
    {
        this.remainingPoints = remainingPoints;
    }

    public Integer getRemainingPoints()
    {
        return remainingPoints;
    }

    @Override
    public Date getUpdateTime()
    {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("pointId", getPointId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("totalPoints", getTotalPoints())
            .append("usedPoints", getUsedPoints())
            .append("remainingPoints", getRemainingPoints())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

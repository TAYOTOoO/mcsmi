package com.ruoyi.mc.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 积分消费记录对象 mc_point_record
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public class McPointRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 变更类型(1增加 2消费 3退还) */
    @Excel(name = "变更类型", readConverterExp = "1=增加,2=消费,3=退还")
    private Integer changeType;

    /** 变更积分 */
    @Excel(name = "变更积分")
    private Integer changePoints;

    /** 变更前积分 */
    private Integer beforePoints;

    /** 变更后积分 */
    private Integer afterPoints;

    /** 关联任务ID */
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getRecordId()
    {
        return recordId;
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

    public void setChangeType(Integer changeType)
    {
        this.changeType = changeType;
    }

    public Integer getChangeType()
    {
        return changeType;
    }

    public void setChangePoints(Integer changePoints)
    {
        this.changePoints = changePoints;
    }

    public Integer getChangePoints()
    {
        return changePoints;
    }

    public void setBeforePoints(Integer beforePoints)
    {
        this.beforePoints = beforePoints;
    }

    public Integer getBeforePoints()
    {
        return beforePoints;
    }

    public void setAfterPoints(Integer afterPoints)
    {
        this.afterPoints = afterPoints;
    }

    public Integer getAfterPoints()
    {
        return afterPoints;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskNo(String taskNo)
    {
        this.taskNo = taskNo;
    }

    public String getTaskNo()
    {
        return taskNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("changeType", getChangeType())
            .append("changePoints", getChangePoints())
            .append("beforePoints", getBeforePoints())
            .append("afterPoints", getAfterPoints())
            .append("taskId", getTaskId())
            .append("taskNo", getTaskNo())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}

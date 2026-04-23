package com.ruoyi.mc.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 材质生成日志对象 mc_generation_log
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public class McGenerationLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String username;

    /** 用户输入的风格 */
    @Excel(name = "风格描述")
    private String styleDescription;

    /** 优化后的提示词 */
    private String optimizedPrompt;

    /** 生成的图片URL */
    @Excel(name = "图片URL")
    private String generatedImageUrl;

    /** 使用的文本模型ID */
    private Long textModelId;

    /** 使用的图片模型ID */
    private Long imageModelId;

    /** 文本模型名称 */
    @Excel(name = "文本模型")
    private String textModelName;

    /** 图片模型名称 */
    @Excel(name = "图片模型")
    private String imageModelName;

    /** 生成耗时(秒) */
    @Excel(name = "耗时(秒)")
    private Integer generationTime;

    /** 状态(0=处理中 1=成功 2=失败) */
    @Excel(name = "状态", readConverterExp = "0=处理中,1=成功,2=失败")
    private String status;

    /** 错误信息 */
    private String errorMessage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getLogId()
    {
        return logId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getTaskId()
    {
        return taskId;
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

    public void setStyleDescription(String styleDescription)
    {
        this.styleDescription = styleDescription;
    }

    public String getStyleDescription()
    {
        return styleDescription;
    }

    public void setOptimizedPrompt(String optimizedPrompt)
    {
        this.optimizedPrompt = optimizedPrompt;
    }

    public String getOptimizedPrompt()
    {
        return optimizedPrompt;
    }

    public void setGeneratedImageUrl(String generatedImageUrl)
    {
        this.generatedImageUrl = generatedImageUrl;
    }

    public String getGeneratedImageUrl()
    {
        return generatedImageUrl;
    }

    public void setTextModelId(Long textModelId)
    {
        this.textModelId = textModelId;
    }

    public Long getTextModelId()
    {
        return textModelId;
    }

    public void setImageModelId(Long imageModelId)
    {
        this.imageModelId = imageModelId;
    }

    public Long getImageModelId()
    {
        return imageModelId;
    }

    public void setTextModelName(String textModelName)
    {
        this.textModelName = textModelName;
    }

    public String getTextModelName()
    {
        return textModelName;
    }

    public void setImageModelName(String imageModelName)
    {
        this.imageModelName = imageModelName;
    }

    public String getImageModelName()
    {
        return imageModelName;
    }

    public void setGenerationTime(Integer generationTime)
    {
        this.generationTime = generationTime;
    }

    public Integer getGenerationTime()
    {
        return generationTime;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    @Override
    public Date getCreateTime()
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("taskId", getTaskId())
            .append("userId", getUserId())
            .append("username", getUsername())
            .append("styleDescription", getStyleDescription())
            .append("optimizedPrompt", getOptimizedPrompt())
            .append("generatedImageUrl", getGeneratedImageUrl())
            .append("textModelId", getTextModelId())
            .append("imageModelId", getImageModelId())
            .append("textModelName", getTextModelName())
            .append("imageModelName", getImageModelName())
            .append("generationTime", getGenerationTime())
            .append("status", getStatus())
            .append("errorMessage", getErrorMessage())
            .append("createTime", getCreateTime())
            .toString();
    }
}

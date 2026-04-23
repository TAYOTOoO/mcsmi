package com.ruoyi.mc.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生成任务对象 mc_generation_task
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public class McGenerationTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务编号 */
    @Excel(name = "任务编号")
    private String taskNo;

    /** 生成的原始大图URL */
    private String generatedImageUrl;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** MC版本ID */
    private Long versionId;

    /** MC版本名称 */
    @Excel(name = "MC版本")
    private String versionName;

    /** 模板ID */
    private Long templateId;

    /** 风格描述 */
    @Excel(name = "风格描述")
    private String styleDescription;

    /** 生成类型(item=物品,ui=UI,complete=完整) */
    @Excel(name = "生成类型", readConverterExp = "item=物品,ui=UI,complete=完整")
    private String generationType;

    /** 材质类型(1:物品材质 2:UI材质) */
    @Excel(name = "材质类型", readConverterExp = "1=物品材质,2=UI材质")
    private Integer materialType;

    /** 预提示词 */
    private String prePrompt;

    /** 优化后的提示词(Gemini 2.5 Pro生成) */
    private String optimizedPrompt;

    /** 最终提示词 */
    private String finalPrompt;

    /** 文本模型ID */
    private Long textModelId;

    /** 文本模型URL */
    private String textModelUrl;

    /** 画图模型ID */
    private Long imageModelId;

    /** 画图模型URL */
    private String imageModelUrl;

    /** AI生成的图片URL */
    private String imageUrl;

    /** 任务状态(0待处理 1生成中 2预处理中 3切割中 4完成 5失败) */
    @Excel(name = "任务状态", readConverterExp = "0=待处理,1=生成中,2=预处理中,3=切割中,4=完成,5=失败")
    private Integer taskStatus;

    /** 总材质数 */
    private Integer totalTextures;

    /** 已完成数 */
    private Integer completedTextures;

    /** 消耗积分 */
    @Excel(name = "消耗积分")
    private Integer costPoints;

    /** 错误信息 */
    private String errorMsg;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

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

    public void setGeneratedImageUrl(String generatedImageUrl)
    {
        this.generatedImageUrl = generatedImageUrl;
    }

    public String getGeneratedImageUrl()
    {
        return generatedImageUrl;
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

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setStyleDescription(String styleDescription)
    {
        this.styleDescription = styleDescription;
    }

    public String getStyleDescription()
    {
        return styleDescription;
    }

    public void setGenerationType(String generationType)
    {
        this.generationType = generationType;
    }

    public String getGenerationType()
    {
        return generationType;
    }

    public void setMaterialType(Integer materialType)
    {
        this.materialType = materialType;
    }

    public Integer getMaterialType()
    {
        return materialType;
    }

    public void setPrePrompt(String prePrompt)
    {
        this.prePrompt = prePrompt;
    }

    public String getPrePrompt()
    {
        return prePrompt;
    }

    public void setOptimizedPrompt(String optimizedPrompt)
    {
        this.optimizedPrompt = optimizedPrompt;
    }

    public String getOptimizedPrompt()
    {
        return optimizedPrompt;
    }

    public void setFinalPrompt(String finalPrompt)
    {
        this.finalPrompt = finalPrompt;
    }

    public String getFinalPrompt()
    {
        return finalPrompt;
    }

    public void setTextModelId(Long textModelId)
    {
        this.textModelId = textModelId;
    }

    public Long getTextModelId()
    {
        return textModelId;
    }

    public void setTextModelUrl(String textModelUrl)
    {
        this.textModelUrl = textModelUrl;
    }

    public String getTextModelUrl()
    {
        return textModelUrl;
    }

    public void setImageModelId(Long imageModelId)
    {
        this.imageModelId = imageModelId;
    }

    public Long getImageModelId()
    {
        return imageModelId;
    }

    public void setImageModelUrl(String imageModelUrl)
    {
        this.imageModelUrl = imageModelUrl;
    }

    public String getImageModelUrl()
    {
        return imageModelUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setTaskStatus(Integer taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public Integer getTaskStatus()
    {
        return taskStatus;
    }

    public void setTotalTextures(Integer totalTextures)
    {
        this.totalTextures = totalTextures;
    }

    public Integer getTotalTextures()
    {
        return totalTextures;
    }

    public void setCompletedTextures(Integer completedTextures)
    {
        this.completedTextures = completedTextures;
    }

    public Integer getCompletedTextures()
    {
        return completedTextures;
    }

    public void setCostPoints(Integer costPoints)
    {
        this.costPoints = costPoints;
    }

    public Integer getCostPoints()
    {
        return costPoints;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setCompleteTime(Date completeTime)
    {
        this.completeTime = completeTime;
    }

    public Date getCompleteTime()
    {
        return completeTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskNo", getTaskNo())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("versionId", getVersionId())
            .append("versionName", getVersionName())
            .append("templateId", getTemplateId())
            .append("styleDescription", getStyleDescription())
            .append("generationType", getGenerationType())
            .append("materialType", getMaterialType())
            .append("taskStatus", getTaskStatus())
            .append("totalTextures", getTotalTextures())
            .append("completedTextures", getCompletedTextures())
            .append("costPoints", getCostPoints())
            .append("createTime", getCreateTime())
            .append("startTime", getStartTime())
            .append("completeTime", getCompleteTime())
            .toString();
    }
}

package com.ruoyi.mc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * MC预提示词模板对象 mc_prompt_template
 *
 * @author ruoyi
 * @date 2026-01-27
 */
public class McPromptTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    @Excel(name = "模板名称")
    private String templateName;

    /** 模板内容（使用[]作为占位符） */
    private String templateContent;

    /** 是否启用(0=否 1=是) */
    @Excel(name = "是否启用", readConverterExp = "0=否,1=是")
    private String isActive;

    /** 模板类型(standard=标准,custom=自定义) */
    @Excel(name = "模板类型")
    private String templateType;

    /** 模板描述 */
    @Excel(name = "模板描述")
    private String description;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 文本模型ID */
    private Long textModelId;

    /** 图片模型ID */
    private Long imageModelId;

    /** 材质类型(1:物品材质 2:UI材质) */
    @Excel(name = "材质类型", readConverterExp = "1=物品材质,2=UI材质")
    private Integer materialType;

    /** 参考图片URL */
    private String referenceImageUrl;

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateContent(String templateContent)
    {
        this.templateContent = templateContent;
    }

    public String getTemplateContent()
    {
        return templateContent;
    }

    public void setIsActive(String isActive)
    {
        this.isActive = isActive;
    }

    public String getIsActive()
    {
        return isActive;
    }

    public void setTemplateType(String templateType)
    {
        this.templateType = templateType;
    }

    public String getTemplateType()
    {
        return templateType;
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

    public void setMaterialType(Integer materialType)
    {
        this.materialType = materialType;
    }

    public Integer getMaterialType()
    {
        return materialType;
    }

    public void setReferenceImageUrl(String referenceImageUrl)
    {
        this.referenceImageUrl = referenceImageUrl;
    }

    public String getReferenceImageUrl()
    {
        return referenceImageUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("templateId", getTemplateId())
            .append("templateName", getTemplateName())
            .append("templateContent", getTemplateContent())
            .append("isActive", getIsActive())
            .append("templateType", getTemplateType())
            .append("description", getDescription())
            .append("status", getStatus())
            .append("textModelId", getTextModelId())
            .append("imageModelId", getImageModelId())
            .append("materialType", getMaterialType())
            .append("referenceImageUrl", getReferenceImageUrl())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

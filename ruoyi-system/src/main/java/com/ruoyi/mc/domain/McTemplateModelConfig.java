package com.ruoyi.mc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 模板模型配置对象 mc_template_model_config
 *
 * @author ruoyi
 * @date 2026-02-28
 */
public class McTemplateModelConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 模板ID */
    @Excel(name = "模板ID")
    private Long templateId;

    /** 模型ID */
    @Excel(name = "模型ID")
    private Long modelId;

    /** 模型类型：1=图片生成 2=文本生成 */
    @Excel(name = "模型类型", readConverterExp = "1=图片生成,2=文本生成")
    private Integer modelType;

    /** 优先级，数字越小优先级越高（1最高） */
    @Excel(name = "优先级")
    private Integer priority;

    /** 状态：0=正常 1=停用 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    // 关联查询字段
    private String modelName;
    private String templateName;

    public void setConfigId(Long configId) 
    {
        this.configId = configId;
    }

    public Long getConfigId() 
    {
        return configId;
    }

    public void setTemplateId(Long templateId) 
    {
        this.templateId = templateId;
    }

    public Long getTemplateId() 
    {
        return templateId;
    }

    public void setModelId(Long modelId) 
    {
        this.modelId = modelId;
    }

    public Long getModelId() 
    {
        return modelId;
    }

    public void setModelType(Integer modelType) 
    {
        this.modelType = modelType;
    }

    public Integer getModelType() 
    {
        return modelType;
    }

    public void setPriority(Integer priority) 
    {
        this.priority = priority;
    }

    public Integer getPriority() 
    {
        return priority;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public String getModelName() 
    {
        return modelName;
    }

    public void setModelName(String modelName) 
    {
        this.modelName = modelName;
    }

    public String getTemplateName() 
    {
        return templateName;
    }

    public void setTemplateName(String templateName) 
    {
        this.templateName = templateName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("templateId", getTemplateId())
            .append("modelId", getModelId())
            .append("modelType", getModelType())
            .append("priority", getPriority())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

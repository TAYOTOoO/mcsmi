package com.ruoyi.mc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI模型配置对象 mc_ai_model
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public class McAiModel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模型ID */
    private Long modelId;

    /** 模型名称 */
    @Excel(name = "模型名称")
    private String modelName;

    /** 模型类型(1:图片生成 2:文本生成) */
    @Excel(name = "模型类型", readConverterExp = "1=图片生成,2=文本生成")
    private Integer modelType;

    /** API访问地址 */
    @Excel(name = "API地址")
    private String apiUrl;

    /** API密钥(加密存储) */
    private String apiKey;

    /** 模型参数配置(JSON格式) */
    private String modelParams;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 是否启用调用(0=否 1=是) */
    @Excel(name = "启用调用", readConverterExp = "0=否,1=是")
    private String isActive;

    public void setModelId(Long modelId)
    {
        this.modelId = modelId;
    }

    public Long getModelId()
    {
        return modelId;
    }

    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }

    public String getModelName()
    {
        return modelName;
    }

    public void setModelType(Integer modelType)
    {
        this.modelType = modelType;
    }

    public Integer getModelType()
    {
        return modelType;
    }

    public void setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }

    public String getApiUrl()
    {
        return apiUrl;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setModelParams(String modelParams)
    {
        this.modelParams = modelParams;
    }

    public String getModelParams()
    {
        return modelParams;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setIsActive(String isActive)
    {
        this.isActive = isActive;
    }

    public String getIsActive()
    {
        return isActive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("modelId", getModelId())
            .append("modelName", getModelName())
            .append("modelType", getModelType())
            .append("apiUrl", getApiUrl())
            .append("apiKey", getApiKey())
            .append("modelParams", getModelParams())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

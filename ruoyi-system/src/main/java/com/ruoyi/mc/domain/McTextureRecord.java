package com.ruoyi.mc.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 材质记录对象 mc_texture_record
 *
 * @author ruoyi
 * @date 2026-01-26
 */
public class McTextureRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 任务ID */
    private Long taskId;

    /** 模板ID */
    private Long templateId;

    /** 材质名称 */
    @Excel(name = "材质名称")
    private String textureName;

    /** 材质英文名 */
    private String textureNameEn;

    /** 文件名 */
    private String fileName;

    /** 材质包内路径 */
    private String filePath;

    /** 位置(0-49) */
    private Integer position;

    /** 图片本地路径 */
    private String imagePath;

    /** 状态 */
    private Integer status;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getRecordId()
    {
        return recordId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }

    public void setTextureName(String textureName)
    {
        this.textureName = textureName;
    }

    public String getTextureName()
    {
        return textureName;
    }

    public void setTextureNameEn(String textureNameEn)
    {
        this.textureNameEn = textureNameEn;
    }

    public String getTextureNameEn()
    {
        return textureNameEn;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public Integer getPosition()
    {
        return position;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getStatus()
    {
        return status;
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
            .append("recordId", getRecordId())
            .append("taskId", getTaskId())
            .append("templateId", getTemplateId())
            .append("textureName", getTextureName())
            .append("textureNameEn", getTextureNameEn())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("position", getPosition())
            .append("imagePath", getImagePath())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

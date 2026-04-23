package com.ruoyi.mc.domain;

import java.util.List;

/**
 * 图片裁剪请求DTO
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public class CropRequest
{
    /** 任务ID */
    private Long taskId;

    /** 版本号(如1.20.1) */
    private String versionName;

    /** 横线Y坐标数组 */
    private List<Integer> horizontalLines;

    /** 竖线X坐标数组 */
    private List<Integer> verticalLines;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getVersionName()
    {
        return versionName;
    }

    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }

    public List<Integer> getHorizontalLines()
    {
        return horizontalLines;
    }

    public void setHorizontalLines(List<Integer> horizontalLines)
    {
        this.horizontalLines = horizontalLines;
    }

    public List<Integer> getVerticalLines()
    {
        return verticalLines;
    }

    public void setVerticalLines(List<Integer> verticalLines)
    {
        this.verticalLines = verticalLines;
    }

    @Override
    public String toString()
    {
        return "CropRequest{" +
                "taskId=" + taskId +
                ", versionName='" + versionName + '\'' +
                ", horizontalLines=" + horizontalLines +
                ", verticalLines=" + verticalLines +
                '}';
    }
}

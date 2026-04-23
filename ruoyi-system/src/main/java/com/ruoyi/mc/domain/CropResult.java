package com.ruoyi.mc.domain;

/**
 * 图片裁剪结果DTO
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public class CropResult
{
    /** ZIP文件下载URL */
    private String zipUrl;

    /** 生成的文件数量 */
    private Integer fileCount;

    /** ZIP文件大小（字节） */
    private Long fileSize;

    public CropResult()
    {
    }

    public CropResult(String zipUrl, Integer fileCount)
    {
        this.zipUrl = zipUrl;
        this.fileCount = fileCount;
    }

    public CropResult(String zipUrl, Integer fileCount, Long fileSize)
    {
        this.zipUrl = zipUrl;
        this.fileCount = fileCount;
        this.fileSize = fileSize;
    }

    public String getZipUrl()
    {
        return zipUrl;
    }

    public void setZipUrl(String zipUrl)
    {
        this.zipUrl = zipUrl;
    }

    public Integer getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(Integer fileCount)
    {
        this.fileCount = fileCount;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    @Override
    public String toString()
    {
        return "CropResult{" +
                "zipUrl='" + zipUrl + '\'' +
                ", fileCount=" + fileCount +
                ", fileSize=" + fileSize +
                '}';
    }
}

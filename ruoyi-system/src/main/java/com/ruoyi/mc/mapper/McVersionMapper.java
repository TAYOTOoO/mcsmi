package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McVersion;

import java.util.List;

/**
 * MC版本Mapper接口
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public interface McVersionMapper
{
    /**
     * 查询所有正常状态的版本
     *
     * @return MC版本集合
     */
    List<McVersion> selectNormalVersionList();

    /**
     * 查询MC版本
     *
     * @param versionId MC版本主键
     * @return MC版本
     */
    McVersion selectMcVersionByVersionId(Long versionId);

    /**
     * 查询所有版本（包括停用的）
     *
     * @return MC版本集合
     */
    List<McVersion> selectAllVersions();

    /**
     * 批量插入版本
     *
     * @param versions 版本列表
     * @return 插入的行数
     */
    int batchInsertVersions(List<McVersion> versions);
}

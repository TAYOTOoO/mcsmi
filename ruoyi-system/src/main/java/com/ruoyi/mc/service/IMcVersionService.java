package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McVersion;

import java.util.List;

/**
 * MC版本Service接口
 *
 * @author ruoyi
 * @date 2026-02-03
 */
public interface IMcVersionService
{
    /**
     * 查询所有正常状态的版本（用户端）
     *
     * @return 版本列表
     */
    List<McVersion> selectNormalVersionList();

    /**
     * 查询MC版本
     *
     * @param versionId MC版本主键
     * @return MC版本
     */
    McVersion selectMcVersionByVersionId(Long versionId);
}

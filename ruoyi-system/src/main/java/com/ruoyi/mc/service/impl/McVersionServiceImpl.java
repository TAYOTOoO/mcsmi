package com.ruoyi.mc.service.impl;

import com.ruoyi.mc.domain.McVersion;
import com.ruoyi.mc.mapper.McVersionMapper;
import com.ruoyi.mc.service.IMcVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MC版本Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@Service
public class McVersionServiceImpl implements IMcVersionService
{
    @Autowired
    private McVersionMapper versionMapper;

    /**
     * 查询所有正常状态的版本
     */
    @Override
    public List<McVersion> selectNormalVersionList()
    {
        return versionMapper.selectNormalVersionList();
    }

    /**
     * 查询MC版本
     */
    @Override
    public McVersion selectMcVersionByVersionId(Long versionId)
    {
        return versionMapper.selectMcVersionByVersionId(versionId);
    }
}

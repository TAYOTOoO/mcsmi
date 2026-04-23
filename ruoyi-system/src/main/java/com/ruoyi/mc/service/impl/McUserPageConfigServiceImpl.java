package com.ruoyi.mc.service.impl;

import com.ruoyi.mc.domain.McUserPageConfig;
import com.ruoyi.mc.mapper.McUserPageConfigMapper;
import com.ruoyi.mc.service.IMcUserPageConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户端页面配置Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-04
 */
@Service
public class McUserPageConfigServiceImpl implements IMcUserPageConfigService
{
    @Autowired
    private McUserPageConfigMapper mcUserPageConfigMapper;

    /**
     * 查询用户端页面配置
     *
     * @param configId 用户端页面配置主键
     * @return 用户端页面配置
     */
    @Override
    public McUserPageConfig selectMcUserPageConfigByConfigId(Long configId)
    {
        return mcUserPageConfigMapper.selectMcUserPageConfigByConfigId(configId);
    }

    /**
     * 查询用户端页面配置列表
     *
     * @param mcUserPageConfig 用户端页面配置
     * @return 用户端页面配置
     */
    @Override
    public List<McUserPageConfig> selectMcUserPageConfigList(McUserPageConfig mcUserPageConfig)
    {
        return mcUserPageConfigMapper.selectMcUserPageConfigList(mcUserPageConfig);
    }

    /**
     * 根据配置键查询用户端页面配置
     *
     * @param configKey 配置键
     * @return 用户端页面配置
     */
    @Override
    public McUserPageConfig selectMcUserPageConfigByConfigKey(String configKey)
    {
        return mcUserPageConfigMapper.selectMcUserPageConfigByConfigKey(configKey);
    }

    /**
     * 根据页面名称查询配置列表
     *
     * @param pageName 页面名称
     * @return 用户端页面配置集合
     */
    @Override
    public List<McUserPageConfig> selectMcUserPageConfigByPageName(String pageName)
    {
        return mcUserPageConfigMapper.selectMcUserPageConfigByPageName(pageName);
    }

    /**
     * 新增用户端页面配置
     *
     * @param mcUserPageConfig 用户端页面配置
     * @return 结果
     */
    @Override
    public int insertMcUserPageConfig(McUserPageConfig mcUserPageConfig)
    {
        return mcUserPageConfigMapper.insertMcUserPageConfig(mcUserPageConfig);
    }

    /**
     * 修改用户端页面配置
     *
     * @param mcUserPageConfig 用户端页面配置
     * @return 结果
     */
    @Override
    public int updateMcUserPageConfig(McUserPageConfig mcUserPageConfig)
    {
        return mcUserPageConfigMapper.updateMcUserPageConfig(mcUserPageConfig);
    }

    /**
     * 批量删除用户端页面配置
     *
     * @param configIds 需要删除的用户端页面配置主键
     * @return 结果
     */
    @Override
    public int deleteMcUserPageConfigByConfigIds(Long[] configIds)
    {
        return mcUserPageConfigMapper.deleteMcUserPageConfigByConfigIds(configIds);
    }

    /**
     * 删除用户端页面配置信息
     *
     * @param configId 用户端页面配置主键
     * @return 结果
     */
    @Override
    public int deleteMcUserPageConfigByConfigId(Long configId)
    {
        return mcUserPageConfigMapper.deleteMcUserPageConfigByConfigId(configId);
    }
}

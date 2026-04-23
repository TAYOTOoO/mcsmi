package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McUserPageConfig;

import java.util.List;

/**
 * 用户端页面配置Service接口
 *
 * @author ruoyi
 * @date 2026-02-04
 */
public interface IMcUserPageConfigService
{
    /**
     * 查询用户端页面配置
     *
     * @param configId 用户端页面配置主键
     * @return 用户端页面配置
     */
    McUserPageConfig selectMcUserPageConfigByConfigId(Long configId);

    /**
     * 查询用户端页面配置列表
     *
     * @param mcUserPageConfig 用户端页面配置
     * @return 用户端页面配置集合
     */
    List<McUserPageConfig> selectMcUserPageConfigList(McUserPageConfig mcUserPageConfig);

    /**
     * 根据配置键查询用户端页面配置
     *
     * @param configKey 配置键
     * @return 用户端页面配置
     */
    McUserPageConfig selectMcUserPageConfigByConfigKey(String configKey);

    /**
     * 根据页面名称查询配置列表（仅返回正常状态的配置）
     *
     * @param pageName 页面名称
     * @return 用户端页面配置集合
     */
    List<McUserPageConfig> selectMcUserPageConfigByPageName(String pageName);

    /**
     * 新增用户端页面配置
     *
     * @param mcUserPageConfig 用户端页面配置
     * @return 结果
     */
    int insertMcUserPageConfig(McUserPageConfig mcUserPageConfig);

    /**
     * 修改用户端页面配置
     *
     * @param mcUserPageConfig 用户端页面配置
     * @return 结果
     */
    int updateMcUserPageConfig(McUserPageConfig mcUserPageConfig);

    /**
     * 批量删除用户端页面配置
     *
     * @param configIds 需要删除的用户端页面配置主键集合
     * @return 结果
     */
    int deleteMcUserPageConfigByConfigIds(Long[] configIds);

    /**
     * 删除用户端页面配置信息
     *
     * @param configId 用户端页面配置主键
     * @return 结果
     */
    int deleteMcUserPageConfigByConfigId(Long configId);
}

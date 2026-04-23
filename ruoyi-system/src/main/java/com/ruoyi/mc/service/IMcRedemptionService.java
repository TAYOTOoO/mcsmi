package com.ruoyi.mc.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mc.domain.McRedemption;

import java.util.List;

/**
 * 兑换码Service接口
 *
 * @author ruoyi
 * @date 2026-02-06
 */
public interface IMcRedemptionService
{
    /**
     * 查询兑换码
     *
     * @param id 兑换码主键
     * @return 兑换码
     */
    public McRedemption selectMcRedemptionById(Long id);

    /**
     * 根据key查询兑换码
     *
     * @param key 兑换码key
     * @return 兑换码
     */
    public McRedemption selectMcRedemptionByKey(String key);

    /**
     * 查询兑换码列表
     *
     * @param mcRedemption 兑换码
     * @return 兑换码集合
     */
    public List<McRedemption> selectMcRedemptionList(McRedemption mcRedemption);

    /**
     * 根据ID列表查询兑换码
     *
     * @param ids ID数组
     * @return 兑换码集合
     */
    public List<McRedemption> selectMcRedemptionByIds(Long[] ids);

    /**
     * 新增兑换码
     *
     * @param mcRedemption 兑换码
     * @return 结果
     */
    public int insertMcRedemption(McRedemption mcRedemption);

    /**
     * 批量生成兑换码
     *
     * @param name 批次名称
     * @param count 生成数量
     * @param quota 额度
     * @param expiredTime 过期时间（毫秒时间戳）
     * @param userId 创建者ID
     * @return 生成的兑换码key列表
     */
    public List<String> generateRedemptions(String name, Integer count, Long quota, Long expiredTime, Long userId);

    /**
     * 修改兑换码
     *
     * @param mcRedemption 兑换码
     * @return 结果
     */
    public int updateMcRedemption(McRedemption mcRedemption);

    /**
     * 批量删除兑换码
     *
     * @param ids 需要删除的兑换码主键集合
     * @return 结果
     */
    public int deleteMcRedemptionByIds(Long[] ids);

    /**
     * 删除兑换码信息
     *
     * @param id 兑换码主键
     * @return 结果
     */
    public int deleteMcRedemptionById(Long id);

    /**
     * 用户兑换兑换码
     *
     * @param key 兑换码
     * @param userId 用户ID
     * @throws ServiceException 业务异常
     */
    public void redeemCode(String key, Long userId) throws ServiceException;
}

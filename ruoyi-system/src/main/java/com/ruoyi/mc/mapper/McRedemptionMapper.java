package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McRedemption;

import java.util.List;

/**
 * 兑换码Mapper接口
 *
 * @author ruoyi
 * @date 2026-02-06
 */
public interface McRedemptionMapper
{
    /**
     * 查询兑换码
     *
     * @param id 兑换码主键
     * @return 兑换码
     */
    public McRedemption selectMcRedemptionById(Long id);

    /**
     * 根据兑换码key查询
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
     * 批量插入兑换码
     *
     * @param list 兑换码列表
     * @return 结果
     */
    public int batchInsertRedemptions(List<McRedemption> list);

    /**
     * 修改兑换码
     *
     * @param mcRedemption 兑换码
     * @return 结果
     */
    public int updateMcRedemption(McRedemption mcRedemption);

    /**
     * 删除兑换码
     *
     * @param id 兑换码主键
     * @return 结果
     */
    public int deleteMcRedemptionById(Long id);

    /**
     * 批量删除兑换码
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMcRedemptionByIds(Long[] ids);
}

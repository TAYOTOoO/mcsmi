package com.ruoyi.mc.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mc.domain.McRedemption;
import com.ruoyi.mc.mapper.McRedemptionMapper;
import com.ruoyi.mc.service.IMcRedemptionService;
import com.ruoyi.mc.service.IMcUserPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 兑换码Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-06
 */
@Service
public class McRedemptionServiceImpl implements IMcRedemptionService
{
    private static final Logger log = LoggerFactory.getLogger(McRedemptionServiceImpl.class);

    @Autowired
    private McRedemptionMapper mcRedemptionMapper;

    @Autowired
    private IMcUserPointsService userPointsService;

    /**
     * 查询兑换码
     *
     * @param id 兑换码主键
     * @return 兑换码
     */
    @Override
    public McRedemption selectMcRedemptionById(Long id)
    {
        return mcRedemptionMapper.selectMcRedemptionById(id);
    }

    /**
     * 根据key查询兑换码
     *
     * @param key 兑换码key
     * @return 兑换码
     */
    @Override
    public McRedemption selectMcRedemptionByKey(String key)
    {
        return mcRedemptionMapper.selectMcRedemptionByKey(key);
    }

    /**
     * 查询兑换码列表
     *
     * @param mcRedemption 兑换码
     * @return 兑换码
     */
    @Override
    public List<McRedemption> selectMcRedemptionList(McRedemption mcRedemption)
    {
        return mcRedemptionMapper.selectMcRedemptionList(mcRedemption);
    }

    /**
     * 根据ID列表查询兑换码
     *
     * @param ids ID数组
     * @return 兑换码集合
     */
    @Override
    public List<McRedemption> selectMcRedemptionByIds(Long[] ids)
    {
        return mcRedemptionMapper.selectMcRedemptionByIds(ids);
    }

    /**
     * 新增兑换码
     *
     * @param mcRedemption 兑换码
     * @return 结果
     */
    @Override
    public int insertMcRedemption(McRedemption mcRedemption)
    {
        if (mcRedemption.getCreatedTime() == null)
        {
            mcRedemption.setCreatedTime(System.currentTimeMillis());
        }
        if (mcRedemption.getStatus() == null)
        {
            mcRedemption.setStatus(1L); // 默认未使用
        }
        return mcRedemptionMapper.insertMcRedemption(mcRedemption);
    }

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> generateRedemptions(String name, Integer count, Long quota, Long expiredTime, Long userId)
    {
        log.info("开始批量生成兑换码: 批次名称={}, 数量={}, 额度={}, 过期时间={}", name, count, quota, expiredTime);

        if (count == null || count <= 0 || count > 1000)
        {
            throw new ServiceException("生成数量必须在1-1000之间");
        }

        List<McRedemption> list = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        Long now = System.currentTimeMillis();

        for (int i = 0; i < count; i++)
        {
            String key = generateUniqueKey();

            McRedemption redemption = new McRedemption();
            redemption.setKey(key);
            redemption.setName(name);
            redemption.setQuota(quota);
            redemption.setStatus(1L);  // 1未使用
            redemption.setCreatedTime(now);
            redemption.setExpiredTime(expiredTime);
            redemption.setUserId(userId);

            list.add(redemption);
            keys.add(key);
        }

        // 批量插入
        int result = mcRedemptionMapper.batchInsertRedemptions(list);
        log.info("批量生成兑换码完成: 成功插入{}条记录", result);

        return keys;
    }

    /**
     * 生成32位唯一兑换码
     * 使用大写字母和数字，排除易混淆字符（0/O、1/I/L）
     */
    private String generateUniqueKey()
    {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // 排除0O1IL
        StringBuilder key = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 32; i++)
        {
            key.append(chars.charAt(random.nextInt(chars.length())));
        }

        return key.toString();
    }

    /**
     * 修改兑换码
     *
     * @param mcRedemption 兑换码
     * @return 结果
     */
    @Override
    public int updateMcRedemption(McRedemption mcRedemption)
    {
        return mcRedemptionMapper.updateMcRedemption(mcRedemption);
    }

    /**
     * 批量删除兑换码
     *
     * @param ids 需要删除的兑换码主键
     * @return 结果
     */
    @Override
    public int deleteMcRedemptionByIds(Long[] ids)
    {
        return mcRedemptionMapper.deleteMcRedemptionByIds(ids);
    }

    /**
     * 删除兑换码信息
     *
     * @param id 兑换码主键
     * @return 结果
     */
    @Override
    public int deleteMcRedemptionById(Long id)
    {
        return mcRedemptionMapper.deleteMcRedemptionById(id);
    }

    /**
     * 用户兑换兑换码
     *
     * @param key 兑换码
     * @param userId 用户ID
     * @throws ServiceException 业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeemCode(String key, Long userId) throws ServiceException
    {
        log.info("用户{}尝试兑换兑换码: {}", userId, key);

        // 1. 查询兑换码
        McRedemption redemption = mcRedemptionMapper.selectMcRedemptionByKey(key);
        if (redemption == null)
        {
            log.warn("兑换码不存在: {}", key);
            throw new ServiceException("兑换码不存在");
        }

        // 2. 验证状态
        if (redemption.getStatus() != 1L)
        {
            log.warn("兑换码已被使用: {}, 状态={}", key, redemption.getStatus());
            throw new ServiceException("兑换码已被使用");
        }

        // 3. 验证过期时间
        Long now = System.currentTimeMillis();
        if (redemption.getExpiredTime() != null && redemption.getExpiredTime() < now)
        {
            log.warn("兑换码已过期: {}, 过期时间={}", key, redemption.getExpiredTime());
            // 更新为已过期
            redemption.setStatus(3L);
            mcRedemptionMapper.updateMcRedemption(redemption);
            throw new ServiceException("兑换码已过期");
        }

        // 4. 增加用户积分
        int points = redemption.getQuota().intValue();
        String remark = "兑换码兑换: " + key;
        userPointsService.addPoints(userId, points, remark);
        log.info("用户{}兑换成功，增加积分: {}", userId, points);

        // 5. 更新兑换码状态
        redemption.setStatus(2L);  // 已使用
        redemption.setUsedUserId(userId);
        redemption.setRedeemedTime(now);
        mcRedemptionMapper.updateMcRedemption(redemption);

        log.info("兑换码{}已标记为已使用，兑换用户: {}", key, userId);
    }
}

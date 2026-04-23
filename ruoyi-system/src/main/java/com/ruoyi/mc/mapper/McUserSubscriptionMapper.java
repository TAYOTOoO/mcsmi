package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McUserSubscription;
import java.util.List;

/**
 * 用户订阅信息Mapper接口
 */
public interface McUserSubscriptionMapper {

    /**
     * 查询用户订阅信息列表
     */
    public List<McUserSubscription> selectMcUserSubscriptionList(McUserSubscription mcUserSubscription);

    /**
     * 根据订阅ID查询用户订阅信息
     */
    public McUserSubscription selectMcUserSubscriptionBySubscriptionId(Long subscriptionId);

    /**
     * 根据用户ID查询用户订阅信息
     */
    public McUserSubscription selectMcUserSubscriptionByUserId(Long userId);

    /**
     * 新增用户订阅信息
     */
    public int insertMcUserSubscription(McUserSubscription mcUserSubscription);

    /**
     * 修改用户订阅信息
     */
    public int updateMcUserSubscription(McUserSubscription mcUserSubscription);

    /**
     * 删除用户订阅信息
     */
    public int deleteMcUserSubscriptionBySubscriptionId(Long subscriptionId);

    /**
     * 批量删除用户订阅信息
     */
    public int deleteMcUserSubscriptionBySubscriptionIds(Long[] subscriptionIds);

    /**
     * 查询需要发放本月积分的钻石用户
     * @param currentMonth 当前月份，格式：2026-02
     */
    public List<McUserSubscription> selectDiamondUsersForPointGrant(String currentMonth);

    /**
     * 查询即将到期的订阅（7天内）
     */
    public List<McUserSubscription> selectExpiringSoonSubscriptions();

    /**
     * 查询已到期的订阅
     */
    public List<McUserSubscription> selectExpiredSubscriptions();
}

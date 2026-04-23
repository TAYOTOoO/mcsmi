package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McUserSubscription;
import java.util.List;

/**
 * 用户订阅信息Service接口
 */
public interface IMcUserSubscriptionService {

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
     * 激活订阅（新购买）
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param months 订阅月数
     * @return 订阅信息
     */
    public McUserSubscription activateSubscription(Long userId, Long packageId, int months);

    /**
     * 续费订阅
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @param months 续费月数
     * @return 订阅信息
     */
    public McUserSubscription renewSubscription(Long userId, Long packageId, int months);

    /**
     * 订阅到期处理（降级为普通用户）
     * @param userId 用户ID
     */
    public void expireSubscription(Long userId);

    /**
     * 管理员编辑用户订阅
     * @param subscription 订阅信息
     * @return 更新结果
     */
    public int updateSubscriptionByAdmin(McUserSubscription subscription);

    /**
     * 发放月度积分
     * @param userId 用户ID
     * @param month 月份（2026-02）
     * @return 是否成功
     */
    public boolean grantMonthlyPoints(Long userId, String month);

    /**
     * 检查并处理所有到期订阅
     */
    public void checkAndUpdateExpiredSubscriptions();

    /**
     * 查询需要发放本月积分的钻石用户
     * @param currentMonth 当前月份
     * @return 用户列表
     */
    public List<McUserSubscription> selectDiamondUsersForPointGrant(String currentMonth);

    /**
     * 查询即将到期的订阅
     * @return 订阅列表
     */
    public List<McUserSubscription> selectExpiringSoonSubscriptions();

    /**
     * 批量发放月度积分
     * @param currentMonth 当前月份
     * @return 发放用户数量
     */
    public int batchGrantMonthlyPoints(String currentMonth);
}

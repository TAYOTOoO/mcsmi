package com.ruoyi.mc.utils;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.mc.domain.McUserSubscription;
import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订阅权限检查工具
 */
@Component
public class SubscriptionPermissionChecker {

    @Autowired
    private IMcUserSubscriptionService subscriptionService;

    @Autowired
    private ISysUserService userService;

    /**
     * 检查是否为有效钻石订阅用户
     * @param userId 用户ID
     * @return true=钻石用户 false=普通用户
     */
    public boolean isDiamondUser(Long userId) {
        if (userId == null) {
            return false;
        }

        // 方式1：从订阅表查询（优先使用，数据最准确）
        McUserSubscription subscription = subscriptionService.selectMcUserSubscriptionByUserId(userId);
        if (subscription != null && subscription.isDiamond()) {
            return true;
        }

        // 方式2：从sys_user表查询（备用）
        SysUser user = userService.selectUserById(userId);
        if (user != null && user.isDiamondUser()) {
            return true;
        }

        return false;
    }

    /**
     * 检查功能权限（预留扩展）
     * @param userId 用户ID
     * @param featureCode 功能代码
     * @return true=有权限 false=无权限
     */
    public boolean hasFeaturePermission(Long userId, String featureCode) {
        // 当前所有功能对钻石用户开放
        return isDiamondUser(userId);
    }

    /**
     * 获取用户充值折扣率
     * @param userId 用户ID
     * @return 折扣率（0.80表示8折，1.00表示无折扣）
     */
    public BigDecimal getDiscountRate(Long userId) {
        if (isDiamondUser(userId)) {
            return new BigDecimal("0.80"); // 钻石用户8折
        }
        return new BigDecimal("1.00"); // 普通用户原价
    }

    /**
     * 获取用户订阅信息（用于前端展示）
     * @param userId 用户ID
     * @return 订阅信息
     */
    public McUserSubscription getUserSubscription(Long userId) {
        if (userId == null) {
            return null;
        }
        return subscriptionService.selectMcUserSubscriptionByUserId(userId);
    }

    /**
     * 检查订阅是否即将到期（7天内）
     * @param userId 用户ID
     * @return true=即将到期 false=未到期或永久
     */
    public boolean isExpiringSoon(Long userId) {
        McUserSubscription subscription = getUserSubscription(userId);
        if (subscription == null || subscription.getIsPermanent() == 1) {
            return false;
        }

        if (subscription.getExpireTime() == null) {
            return false;
        }

        // 计算距离到期的天数
        long diff = subscription.getExpireTime().getTime() - new Date().getTime();
        long days = diff / (1000 * 60 * 60 * 24);

        return days >= 0 && days <= 7;
    }

    /**
     * 获取订阅剩余天数
     * @param userId 用户ID
     * @return 剩余天数，永久订阅返回-1，未订阅返回0
     */
    public long getRemainingDays(Long userId) {
        McUserSubscription subscription = getUserSubscription(userId);
        if (subscription == null || !"diamond".equals(subscription.getSubscriptionType())) {
            return 0;
        }

        if (subscription.getIsPermanent() == 1) {
            return -1; // 永久订阅
        }

        if (subscription.getExpireTime() == null) {
            return 0;
        }

        long diff = subscription.getExpireTime().getTime() - new Date().getTime();
        long days = diff / (1000 * 60 * 60 * 24);

        return days > 0 ? days : 0;
    }
}

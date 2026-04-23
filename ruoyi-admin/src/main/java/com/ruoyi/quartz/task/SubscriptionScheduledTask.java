package com.ruoyi.quartz.task;

import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.mc.service.IMcSubscriptionOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订阅系统定时任务
 *
 * @author ruoyi
 * @date 2026-02-07
 */
@Component("subscriptionScheduledTask")
public class SubscriptionScheduledTask
{
    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduledTask.class);

    @Autowired
    private IMcUserSubscriptionService subscriptionService;

    @Autowired
    private IMcSubscriptionOrderService orderService;

    /**
     * 检查并处理到期订阅
     * 每天凌晨1点执行
     */
    public void checkExpiredSubscriptions()
    {
        log.info("开始检查到期订阅...");
        try
        {
            subscriptionService.checkAndUpdateExpiredSubscriptions();
            log.info("到期订阅检查完成");
        }
        catch (Exception e)
        {
            log.error("检查到期订阅失败", e);
        }
    }

    /**
     * 发放月度积分
     * 每月1号凌晨2点执行
     */
    public void grantMonthlyPoints()
    {
        log.info("开始发放月度订阅积分...");
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String currentMonth = sdf.format(new Date());

            // 调用service层的批量发放方法
            // 这里需要遍历所有符合条件的用户
            int count = subscriptionService.batchGrantMonthlyPoints(currentMonth);
            log.info("月度积分发放完成，共发放{}个用户", count);
        }
        catch (Exception e)
        {
            log.error("发放月度积分失败", e);
        }
    }

    /**
     * 取消超时未支付订单
     * 每小时执行一次
     */
    public void cancelTimeoutOrders()
    {
        log.info("开始取消超时未支付订单...");
        try
        {
            orderService.cancelTimeoutOrders();
            log.info("超时订单取消完成");
        }
        catch (Exception e)
        {
            log.error("取消超时订单失败", e);
        }
    }
}

package com.ruoyi.mc.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mc.mapper.McSubscriptionOrderMapper;
import com.ruoyi.mc.mapper.McSubscriptionPackageMapper;
import com.ruoyi.mc.mapper.McUserSubscriptionMapper;
import com.ruoyi.mc.domain.McSubscriptionOrder;
import com.ruoyi.mc.domain.McSubscriptionPackage;
import com.ruoyi.mc.domain.McUserSubscription;
import com.ruoyi.mc.service.IMcSubscriptionOrderService;
import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.mc.service.IMcSubscriptionPackageService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.common.core.domain.entity.SysUser;

/**
 * 订阅订单Service业务层处理
 *
 * @author ruoyi
 * @date 2026-02-07
 */
@Service
public class McSubscriptionOrderServiceImpl implements IMcSubscriptionOrderService
{
    private static final Logger logger = LoggerFactory.getLogger(McSubscriptionOrderServiceImpl.class);

    @Autowired
    private McSubscriptionOrderMapper mcSubscriptionOrderMapper;

    @Autowired
    private McSubscriptionPackageMapper mcSubscriptionPackageMapper;

    @Autowired
    private McUserSubscriptionMapper mcUserSubscriptionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IMcUserSubscriptionService mcUserSubscriptionService;

    @Autowired
    private IMcSubscriptionPackageService mcSubscriptionPackageService;

    @Override
    public List<McSubscriptionOrder> selectMcSubscriptionOrderList(McSubscriptionOrder mcSubscriptionOrder)
    {
        return mcSubscriptionOrderMapper.selectMcSubscriptionOrderList(mcSubscriptionOrder);
    }

    @Override
    public McSubscriptionOrder selectMcSubscriptionOrderByOrderId(Long orderId)
    {
        return mcSubscriptionOrderMapper.selectMcSubscriptionOrderByOrderId(orderId);
    }

    @Override
    public McSubscriptionOrder selectMcSubscriptionOrderByOrderNo(String orderNo)
    {
        return mcSubscriptionOrderMapper.selectMcSubscriptionOrderByOrderNo(orderNo);
    }

    @Override
    public List<McSubscriptionOrder> selectMcSubscriptionOrderListByUserId(Long userId)
    {
        return mcSubscriptionOrderMapper.selectMcSubscriptionOrderListByUserId(userId);
    }

    @Override
    public int insertMcSubscriptionOrder(McSubscriptionOrder mcSubscriptionOrder)
    {
        mcSubscriptionOrder.setCreateTime(DateUtils.getNowDate());
        return mcSubscriptionOrderMapper.insertMcSubscriptionOrder(mcSubscriptionOrder);
    }

    @Override
    public int updateMcSubscriptionOrder(McSubscriptionOrder mcSubscriptionOrder)
    {
        mcSubscriptionOrder.setUpdateTime(DateUtils.getNowDate());
        return mcSubscriptionOrderMapper.updateMcSubscriptionOrder(mcSubscriptionOrder);
    }

    @Override
    public int deleteMcSubscriptionOrderByOrderId(Long orderId)
    {
        return mcSubscriptionOrderMapper.deleteMcSubscriptionOrderByOrderId(orderId);
    }

    @Override
    public int deleteMcSubscriptionOrderByOrderIds(Long[] orderIds)
    {
        return mcSubscriptionOrderMapper.deleteMcSubscriptionOrderByOrderIds(orderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public McSubscriptionOrder createOrder(Long userId, Long packageId)
    {
        // 查询用户信息
        SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null)
        {
            throw new ServiceException("用户不存在");
        }

        // 查询套餐信息
        McSubscriptionPackage subscriptionPackage = mcSubscriptionPackageMapper.selectMcSubscriptionPackageByPackageId(packageId);
        if (subscriptionPackage == null)
        {
            throw new ServiceException("套餐不存在");
        }

        if (!"0".equals(subscriptionPackage.getStatus()))
        {
            throw new ServiceException("套餐已下架");
        }

        // 订阅套餐价格固定，不打折（订阅折扣只适用于积分充值）
        BigDecimal actualAmount = subscriptionPackage.getAmount();

        // 生成订单号（时间戳 + 6位随机数）
        String orderNo = generateOrderNo();

        // 创建订单
        McSubscriptionOrder order = new McSubscriptionOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setPackageName(subscriptionPackage.getPackageName());
        order.setDurationMonths(subscriptionPackage.getDurationMonths());
        order.setOriginalAmount(subscriptionPackage.getAmount());
        order.setActualAmount(actualAmount);
        order.setPaymentStatus(0); // 待支付
        order.setCreateTime(DateUtils.getNowDate());

        mcSubscriptionOrderMapper.insertMcSubscriptionOrder(order);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processPaymentCallback(String orderNo, String tradeNo)
    {
        logger.info("开始处理订阅订单支付回调: orderNo={}, tradeNo={}", orderNo, tradeNo);

        // 查询订单
        McSubscriptionOrder order = mcSubscriptionOrderMapper.selectMcSubscriptionOrderByOrderNo(orderNo);
        if (order == null)
        {
            logger.error("订单不存在: {}", orderNo);
            return false;
        }

        // 检查订单状态（防止重复处理）
        if (order.getPaymentStatus() == 1)
        {
            logger.info("订单 {} 已支付，忽略重复回调", orderNo);
            return true; // 已支付，返回成功避免易支付重复回调
        }

        if (order.getPaymentStatus() != 0)
        {
            logger.warn("订单 {} 状态异常，当前状态：{}", orderNo, order.getPaymentStatus());
            return false;
        }

        try {
            Date now = DateUtils.getNowDate();

            // 计算订阅时间
            Date startTime = now;
            Date expireTime = calculateExpireTime(now, order.getDurationMonths());

            // 更新订单状态
            order.setPaymentStatus(1); // 已支付
            order.setPaymentTime(now);
            order.setTradeNo(tradeNo);
            order.setStartTime(startTime);
            order.setExpireTime(expireTime);
            order.setUpdateTime(now);
            mcSubscriptionOrderMapper.updateMcSubscriptionOrder(order);

            logger.info("订单 {} 状态已更新为已支付", orderNo);

            // 查询用户当前订阅状态
            McUserSubscription subscription = mcUserSubscriptionMapper.selectMcUserSubscriptionByUserId(order.getUserId());

            if (subscription == null || !"diamond".equals(subscription.getSubscriptionType()) || !subscription.isActive())
            {
                // 首次订阅或订阅已过期，激活订阅
                logger.info("激活用户 {} 的订阅", order.getUserId());
                mcUserSubscriptionService.activateSubscription(
                    order.getUserId(),
                    order.getPackageId(),
                    order.getDurationMonths()
                );
            }
            else
            {
                // 续费订阅
                logger.info("续费用户 {} 的订阅", order.getUserId());
                mcUserSubscriptionService.renewSubscription(
                    order.getUserId(),
                    order.getPackageId(),
                    order.getDurationMonths()
                );
            }

            logger.info("订阅订单 {} 支付成功，用户 {} 订阅已激活", orderNo, order.getUserId());
            return true;

        } catch (Exception e) {
            logger.error("处理订单 {} 回调失败", orderNo, e);
            throw e; // 抛出异常触发事务回滚
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders()
    {
        // 查询待支付超时订单（创建30分钟后仍未支付）
        List<McSubscriptionOrder> timeoutOrders = mcSubscriptionOrderMapper.selectTimeoutUnpaidOrders();

        Date now = DateUtils.getNowDate();
        for (McSubscriptionOrder order : timeoutOrders)
        {
            order.setPaymentStatus(2); // 已取消
            order.setUpdateTime(now);
            mcSubscriptionOrderMapper.updateMcSubscriptionOrder(order);
        }
    }

    /**
     * 生成订单号（时间戳 + 6位随机数）
     *
     * @return 订单号
     */
    private String generateOrderNo()
    {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        int randomNum = random.nextInt(900000) + 100000; // 生成6位随机数（100000-999999）
        return timestamp + String.valueOf(randomNum);
    }

    /**
     * 计算到期时间
     *
     * @param startTime 开始时间
     * @param months 月数
     * @return 到期时间
     */
    private Date calculateExpireTime(Date startTime, int months)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }
}

package com.ruoyi.mc.service.impl;

import com.ruoyi.mc.domain.McRechargeOrder;
import com.ruoyi.mc.domain.McRechargePackage;
import com.ruoyi.mc.mapper.McRechargeOrderMapper;
import com.ruoyi.mc.mapper.McRechargePackageMapper;
import com.ruoyi.mc.service.IMcRechargeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 充值订单服务实现
 */
@Service
public class McRechargeOrderServiceImpl implements IMcRechargeOrderService {

    @Autowired
    private McRechargeOrderMapper rechargeOrderMapper;

    @Autowired
    private McRechargePackageMapper rechargePackageMapper;

    @Override
    public List<McRechargeOrder> selectMcRechargeOrderList(McRechargeOrder mcRechargeOrder) {
        return rechargeOrderMapper.selectMcRechargeOrderList(mcRechargeOrder);
    }

    @Override
    public McRechargeOrder selectMcRechargeOrderByOrderId(Long orderId) {
        return rechargeOrderMapper.selectMcRechargeOrderByOrderId(orderId);
    }

    @Override
    public McRechargeOrder selectMcRechargeOrderByOrderNo(String orderNo) {
        return rechargeOrderMapper.selectMcRechargeOrderByOrderNo(orderNo);
    }

    @Override
    public McRechargeOrder createRechargeOrder(Long userId, Long packageId, String paymentType) {
        // 查询充值套餐
        McRechargePackage rechargePackage = rechargePackageMapper.selectMcRechargePackageByPackageId(packageId);
        if (rechargePackage == null) {
            throw new RuntimeException("充值套餐不存在");
        }

        if (!"0".equals(rechargePackage.getStatus())) {
            throw new RuntimeException("充值套餐已停用");
        }

        // 创建订单
        McRechargeOrder order = new McRechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setPackageId(packageId);
        order.setAmount(rechargePackage.getAmount());
        order.setPoints(rechargePackage.getTotalPoints()); // 基础积分 + 赠送积分
        order.setPaymentType(paymentType);
        order.setOrderStatus(0); // 待支付
        order.setNotifyStatus(0); // 未通知
        order.setExpireTime(getExpireTime(30)); // 30分钟后过期

        rechargeOrderMapper.insertMcRechargeOrder(order);

        return order;
    }

    @Override
    public McRechargeOrder createCustomRechargeOrder(Long userId, String amountStr, String paymentType) {
        // 解析金额
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            throw new RuntimeException("金额格式错误");
        }

        // 验证金额范围
        if (amount.compareTo(new BigDecimal("1")) < 0) {
            throw new RuntimeException("最低充值金额为1元");
        }
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            throw new RuntimeException("单笔充值不能超过10000元");
        }

        // 计算积分（10元 = 1000积分）
        int points = amount.multiply(new BigDecimal("100")).intValue();

        // 创建订单
        McRechargeOrder order = new McRechargeOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setAmount(amount);
        order.setPoints(points);
        order.setPaymentType(paymentType);
        order.setOrderStatus(0); // 待支付
        order.setNotifyStatus(0); // 未通知
        order.setExpireTime(getExpireTime(30)); // 30分钟后过期

        rechargeOrderMapper.insertMcRechargeOrder(order);

        return order;
    }

    @Override
    public int updateMcRechargeOrder(McRechargeOrder mcRechargeOrder) {
        return rechargeOrderMapper.updateMcRechargeOrder(mcRechargeOrder);
    }

    @Override
    public int deleteMcRechargeOrderByOrderId(Long orderId) {
        return rechargeOrderMapper.deleteMcRechargeOrderByOrderId(orderId);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "RC" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 获取过期时间
     */
    private Date getExpireTime(int minutes) {
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(minutes);
        return Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

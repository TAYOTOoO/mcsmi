package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McSubscriptionOrder;
import java.util.List;

/**
 * 订阅订单Service接口
 */
public interface IMcSubscriptionOrderService {

    /**
     * 查询订阅订单列表
     */
    public List<McSubscriptionOrder> selectMcSubscriptionOrderList(McSubscriptionOrder mcSubscriptionOrder);

    /**
     * 根据订单ID查询订阅订单
     */
    public McSubscriptionOrder selectMcSubscriptionOrderByOrderId(Long orderId);

    /**
     * 根据订单号查询订阅订单
     */
    public McSubscriptionOrder selectMcSubscriptionOrderByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订阅订单列表
     */
    public List<McSubscriptionOrder> selectMcSubscriptionOrderListByUserId(Long userId);

    /**
     * 新增订阅订单
     */
    public int insertMcSubscriptionOrder(McSubscriptionOrder mcSubscriptionOrder);

    /**
     * 修改订阅订单
     */
    public int updateMcSubscriptionOrder(McSubscriptionOrder mcSubscriptionOrder);

    /**
     * 删除订阅订单
     */
    public int deleteMcSubscriptionOrderByOrderId(Long orderId);

    /**
     * 批量删除订阅订单
     */
    public int deleteMcSubscriptionOrderByOrderIds(Long[] orderIds);

    /**
     * 创建订阅订单
     * @param userId 用户ID
     * @param packageId 套餐ID
     * @return 订单信息
     */
    public McSubscriptionOrder createOrder(Long userId, Long packageId);

    /**
     * 处理支付回调
     * @param orderNo 订单号
     * @param tradeNo 第三方交易号
     * @return 是否成功
     */
    public boolean processPaymentCallback(String orderNo, String tradeNo);

    /**
     * 取消超时未支付订单
     */
    public void cancelTimeoutOrders();
}

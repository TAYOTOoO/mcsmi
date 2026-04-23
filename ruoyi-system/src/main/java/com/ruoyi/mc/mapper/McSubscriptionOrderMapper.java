package com.ruoyi.mc.mapper;

import com.ruoyi.mc.domain.McSubscriptionOrder;
import java.util.List;

/**
 * 订阅订单Mapper接口
 */
public interface McSubscriptionOrderMapper {

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
     * 查询待支付超时订单（创建30分钟后仍未支付）
     */
    public List<McSubscriptionOrder> selectTimeoutUnpaidOrders();
}

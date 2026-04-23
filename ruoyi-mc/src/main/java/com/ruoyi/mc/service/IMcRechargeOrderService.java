package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McRechargeOrder;
import com.ruoyi.mc.domain.McRechargePackage;
import java.util.List;

/**
 * 充值订单服务接口
 */
public interface IMcRechargeOrderService {

    /**
     * 查询充值订单列表
     */
    public List<McRechargeOrder> selectMcRechargeOrderList(McRechargeOrder mcRechargeOrder);

    /**
     * 查询充值订单详细
     */
    public McRechargeOrder selectMcRechargeOrderByOrderId(Long orderId);

    /**
     * 根据订单号查询充值订单
     */
    public McRechargeOrder selectMcRechargeOrderByOrderNo(String orderNo);

    /**
     * 创建充值订单
     */
    public McRechargeOrder createRechargeOrder(Long userId, Long packageId, String paymentType);

    /**
     * 创建自定义金额充值订单
     */
    public McRechargeOrder createCustomRechargeOrder(Long userId, String amount, String paymentType);

    /**
     * 修改充值订单
     */
    public int updateMcRechargeOrder(McRechargeOrder mcRechargeOrder);

    /**
     * 删除充值订单
     */
    public int deleteMcRechargeOrderByOrderId(Long orderId);
}

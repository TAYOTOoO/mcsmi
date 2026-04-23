package com.ruoyi.mc.mapper;

import java.util.List;
import com.ruoyi.mc.domain.McPaymentOrder;

/**
 * 支付订单Mapper接口
 *
 * @author ruoyi
 * @date 2024
 */
public interface McPaymentOrderMapper
{
    /**
     * 查询支付订单
     *
     * @param orderId 支付订单主键
     * @return 支付订单
     */
    public McPaymentOrder selectMcPaymentOrderByOrderId(Long orderId);

    /**
     * 根据商户订单号查询支付订单
     *
     * @param outTradeNo 商户订单号
     * @return 支付订单
     */
    public McPaymentOrder selectMcPaymentOrderByOutTradeNo(String outTradeNo);

    /**
     * 查询支付订单列表
     *
     * @param mcPaymentOrder 支付订单
     * @return 支付订单集合
     */
    public List<McPaymentOrder> selectMcPaymentOrderList(McPaymentOrder mcPaymentOrder);

    /**
     * 新增支付订单
     *
     * @param mcPaymentOrder 支付订单
     * @return 结果
     */
    public int insertMcPaymentOrder(McPaymentOrder mcPaymentOrder);

    /**
     * 修改支付订单
     *
     * @param mcPaymentOrder 支付订单
     * @return 结果
     */
    public int updateMcPaymentOrder(McPaymentOrder mcPaymentOrder);

    /**
     * 删除支付订单
     *
     * @param orderId 支付订单主键
     * @return 结果
     */
    public int deleteMcPaymentOrderByOrderId(Long orderId);

    /**
     * 批量删除支付订单
     *
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMcPaymentOrderByOrderIds(Long[] orderIds);

    /**
     * 联合查询支付订单和订阅订单列表
     *
     * @param mcPaymentOrder 查询条件
     * @return 合并后的订单集合
     */
    public List<McPaymentOrder> selectAllOrdersList(McPaymentOrder mcPaymentOrder);
}

package com.ruoyi.mc.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mc.domain.McPaymentOrder;

/**
 * 支付订单Service接口
 *
 * @author ruoyi
 * @date 2024
 */
public interface IMcPaymentOrderService
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
     * 联合查询支付订单和订阅订单列表
     *
     * @param mcPaymentOrder 查询条件
     * @return 合并后的订单集合
     */
    public List<McPaymentOrder> selectAllOrdersList(McPaymentOrder mcPaymentOrder);

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
     * 批量删除支付订单
     *
     * @param orderIds 需要删除的支付订单主键集合
     * @return 结果
     */
    public int deleteMcPaymentOrderByOrderIds(Long[] orderIds);

    /**
     * 删除支付订单信息
     *
     * @param orderId 支付订单主键
     * @return 结果
     */
    public int deleteMcPaymentOrderByOrderId(Long orderId);

    /**
     * 创建支付订单并获取支付URL
     *
     * @param mcPaymentOrder 支付订单信息
     * @return 支付表单HTML或支付URL
     */
    public String createPaymentAndGetUrl(McPaymentOrder mcPaymentOrder) throws Exception;

    /**
     * 处理支付异步通知
     *
     * @param params 通知参数
     * @return 处理结果
     */
    public boolean handlePaymentNotify(Map<String, String> params);

    /**
     * 处理支付同步返回
     *
     * @param params 返回参数
     * @return 订单信息
     */
    public McPaymentOrder handlePaymentReturn(Map<String, String> params);

    /**
     * 查询易支付订单状态并自动补单
     *
     * @param outTradeNo 商户订单号
     * @return 补单结果描述
     */
    public String queryAndReconcileOrder(String outTradeNo);
}

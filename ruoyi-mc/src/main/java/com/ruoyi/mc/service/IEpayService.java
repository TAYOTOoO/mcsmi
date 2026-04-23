package com.ruoyi.mc.service;

import com.ruoyi.mc.domain.McRechargeOrder;
import com.ruoyi.mc.domain.McSubscriptionOrder;
import java.util.Map;

/**
 * 易支付服务接口
 */
public interface IEpayService {

    /**
     * 生成支付表单HTML
     *
     * @param order 充值订单
     * @return 支付表单HTML
     */
    String generatePayForm(McRechargeOrder order);

    /**
     * 生成支付URL
     *
     * @param order 充值订单
     * @return 支付URL
     */
    String generatePayUrl(McRechargeOrder order);

    /**
     * 生成订阅订单支付URL
     *
     * @param order 订阅订单
     * @return 支付URL
     */
    String generateSubscriptionPayUrl(McSubscriptionOrder order);

    /**
     * 创建订阅订单并调用易支付API获取支付数据
     *
     * @param order 订阅订单
     * @return 支付数据（JSON字符串）
     */
    String createSubscriptionPayment(McSubscriptionOrder order);

    /**
     * 验证回调签名
     *
     * @param params 回调参数
     * @return 是否验证通过
     */
    boolean verifySign(Map<String, String> params);

    /**
     * 处理支付回调
     *
     * @param params 回调参数
     * @return 处理结果
     */
    String handleNotify(Map<String, String> params);

    /**
     * 处理同步跳转
     *
     * @param params 跳转参数
     * @return 订单号
     */
    String handleReturn(Map<String, String> params);
}

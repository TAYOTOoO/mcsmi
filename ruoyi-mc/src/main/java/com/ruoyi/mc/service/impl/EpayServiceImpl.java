package com.ruoyi.mc.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.mc.domain.McPaymentConfig;
import com.ruoyi.mc.domain.McRechargeOrder;
import com.ruoyi.mc.domain.McSubscriptionOrder;
import com.ruoyi.mc.mapper.McPaymentConfigMapper;
import com.ruoyi.mc.mapper.McRechargeOrderMapper;
import com.ruoyi.mc.service.IEpayService;
import com.ruoyi.mc.service.IMcUserPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 易支付服务实现
 */
@Service
public class EpayServiceImpl implements IEpayService {

    private static final Logger log = LoggerFactory.getLogger(EpayServiceImpl.class);

    @Autowired
    private McPaymentConfigMapper paymentConfigMapper;

    @Autowired
    private McRechargeOrderMapper rechargeOrderMapper;

    @Autowired
    private IMcUserPointsService userPointsService;

    /**
     * 生成支付表单HTML
     */
    @Override
    public String generatePayForm(McRechargeOrder order) {
        McPaymentConfig config = paymentConfigMapper.selectDefaultPaymentConfig();
        if (config == null) {
            throw new RuntimeException("支付配置未启用");
        }

        Map<String, String> params = buildPayParams(order, config);
        String sign = generateSign(params, config.getMerchantKey());
        params.put("sign", sign);
        params.put("sign_type", "MD5");

        // 生成表单HTML
        StringBuilder formHtml = new StringBuilder();
        formHtml.append("<form id='payForm' action='").append(config.getApiUrl()).append("' method='post'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formHtml.append("<input type='hidden' name='").append(entry.getKey())
                    .append("' value='").append(entry.getValue()).append("'>");
        }
        formHtml.append("</form>");
        formHtml.append("<script>document.getElementById('payForm').submit();</script>");

        return formHtml.toString();
    }

    /**
     * 生成支付URL
     */
    @Override
    public String generatePayUrl(McRechargeOrder order) {
        McPaymentConfig config = paymentConfigMapper.selectDefaultPaymentConfig();
        if (config == null) {
            throw new RuntimeException("支付配置未启用");
        }

        Map<String, String> params = buildPayParams(order, config);
        String sign = generateSign(params, config.getMerchantKey());
        params.put("sign", sign);
        params.put("sign_type", "MD5");

        // 生成URL
        try {
            StringBuilder url = new StringBuilder(config.getApiUrl()).append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(entry.getKey()).append("=")
                   .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()))
                   .append("&");
            }
            return url.substring(0, url.length() - 1);
        } catch (Exception e) {
            throw new RuntimeException("生成支付URL失败", e);
        }
    }

    /**
     * 生成订阅订单支付URL
     */
    @Override
    public String generateSubscriptionPayUrl(McSubscriptionOrder order) {
        log.info("=== 开始生成订阅订单支付URL ===");
        log.info("订单号: {}, 套餐: {}, 金额: {}", order.getOrderNo(), order.getPackageName(), order.getActualAmount());

        McPaymentConfig config = paymentConfigMapper.selectDefaultPaymentConfig();
        if (config == null) {
            throw new RuntimeException("支付配置未启用");
        }

        log.info("支付配置: API={}, 商户ID={}", config.getApiUrl(), config.getMerchantId());

        Map<String, String> params = buildSubscriptionPayParams(order, config);
        log.info("构建的支付参数: {}", params);

        String sign = generateSign(params, config.getMerchantKey());
        params.put("sign", sign);
        params.put("sign_type", "MD5");

        log.info("生成的签名: {}", sign);

        // 生成URL
        try {
            StringBuilder url = new StringBuilder(config.getApiUrl()).append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                url.append(entry.getKey()).append("=")
                   .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()))
                   .append("&");
            }
            String finalUrl = url.substring(0, url.length() - 1);
            log.info("最终支付URL: {}", finalUrl);
            log.info("=== 订阅订单支付URL生成完成 ===");
            return finalUrl;
        } catch (Exception e) {
            log.error("生成支付URL失败", e);
            throw new RuntimeException("生成支付URL失败", e);
        }
    }

    /**
     * 创建订阅订单并调用易支付API获取支付数据
     */
    @Override
    public String createSubscriptionPayment(McSubscriptionOrder order) {
        log.info("=== 开始创建订阅支付 ===");
        log.info("订单号: {}, 套餐: {}, 金额: {}", order.getOrderNo(), order.getPackageName(), order.getActualAmount());

        McPaymentConfig config = paymentConfigMapper.selectDefaultPaymentConfig();
        if (config == null) {
            throw new RuntimeException("支付配置未启用");
        }

        if (!"0".equals(config.getStatus())) {
            throw new RuntimeException("支付配置已停用");
        }

        log.info("支付配置: API={}, 商户ID={}", config.getApiUrl(), config.getMerchantId());

        // 构建请求参数
        Map<String, String> params = buildSubscriptionPayParams(order, config);

        // 添加设备类型
        params.put("device", "pc");
        params.put("clientip", "127.0.0.1");

        // 生成签名
        String sign = generateSign(params, config.getMerchantKey());
        params.put("sign", sign);
        params.put("sign_type", "MD5");

        log.info("请求易支付API: {}", config.getApiUrl());
        log.info("请求参数: {}", params);

        // 构建POST请求参数
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (postData.length() > 0) {
                postData.append("&");
            }
            postData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // 调用易支付API
        String responseStr = HttpUtils.sendPost(config.getApiUrl(), postData.toString());
        log.info("易支付API响应: {}", responseStr);

        if (StringUtils.isEmpty(responseStr)) {
            throw new RuntimeException("支付接口无响应");
        }

        // 解析响应
        JSONObject response = JSON.parseObject(responseStr);
        Integer code = response.getInteger("code");

        if (code == null || code != 1) {
            String msg = response.getString("msg");
            log.error("易支付API返回错误: code={}, msg={}", code, msg);
            throw new RuntimeException("支付接口错误: " + msg);
        }

        // 返回支付数据
        log.info("=== 订阅支付创建成功 ===");
        return responseStr;
    }

    /**
     * 构建支付参数
     */
    private Map<String, String> buildPayParams(McRechargeOrder order, McPaymentConfig config) {
        Map<String, String> params = new TreeMap<>();
        params.put("pid", config.getMerchantId());
        params.put("type", order.getPaymentType());
        params.put("out_trade_no", order.getOrderNo());
        params.put("notify_url", config.getNotifyUrl());
        params.put("return_url", config.getReturnUrl());
        params.put("name", "材质工坊充值-" + order.getPoints() + "积分");
        params.put("money", order.getAmount().toString());
        params.put("param", order.getOrderId().toString());
        params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        return params;
    }

    /**
     * 构建订阅订单支付参数
     */
    private Map<String, String> buildSubscriptionPayParams(McSubscriptionOrder order, McPaymentConfig config) {
        Map<String, String> params = new TreeMap<>();
        params.put("pid", config.getMerchantId());
        params.put("type", order.getPaymentMethod() != null ? order.getPaymentMethod() : "alipay");
        params.put("out_trade_no", order.getOrderNo());

        // 构建订阅回调URL：从充值回调URL替换路径部分
        // 数据库中的notify_url: http://mcsmi.com/mc/paymentOrder/notify
        // 订阅回调URL: http://mcsmi.com/mc/subscription/order/pay/notify
        String notifyUrl = config.getNotifyUrl();
        if (notifyUrl.contains("/mc/paymentOrder/notify")) {
            notifyUrl = notifyUrl.replace("/mc/paymentOrder/notify", "/mc/subscription/order/pay/notify");
        } else if (notifyUrl.contains("/mc/")) {
            // 兼容其他URL格式：取域名+/mc/前缀，拼接订阅回调路径
            int mcIndex = notifyUrl.indexOf("/mc/");
            notifyUrl = notifyUrl.substring(0, mcIndex) + "/mc/subscription/order/pay/notify";
        } else {
            log.warn("回调URL格式无法识别，使用默认订阅回调路径: {}", notifyUrl);
            notifyUrl = notifyUrl.substring(0, notifyUrl.indexOf("/", 8)) + "/mc/subscription/order/pay/notify";
        }
        params.put("notify_url", notifyUrl);

        params.put("return_url", config.getReturnUrl());
        params.put("name", "钻石订阅-" + order.getPackageName());
        params.put("money", order.getActualAmount().toString());
        params.put("param", order.getOrderId().toString());
        params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        log.info("订阅订单回调URL: {}", notifyUrl);

        return params;
    }

    /**
     * 生成签名（MD5方式）
     */
    private String generateSign(Map<String, String> params, String key) {
        try {
            // 按key排序并拼接参数（跳过空值，与易支付签名规则一致）
            String signStr = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            // 添加密钥
            signStr = signStr + key;

            log.info("签名原串: {}", signStr);

            // MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(signStr.getBytes(StandardCharsets.UTF_8));

            // 转为16进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 验证回调签名
     */
    @Override
    public boolean verifySign(Map<String, String> params) {
        McPaymentConfig config = paymentConfigMapper.selectDefaultPaymentConfig();
        if (config == null) {
            return false;
        }

        String sign = params.get("sign");
        if (sign == null || sign.isEmpty()) {
            return false;
        }

        // 移除sign参数
        Map<String, String> verifyParams = new TreeMap<>(params);
        verifyParams.remove("sign");
        verifyParams.remove("sign_type");

        // 生成签名并比对
        String generatedSign = generateSign(verifyParams, config.getMerchantKey());
        boolean result = sign.equalsIgnoreCase(generatedSign);

        log.info("签名验证: 传入sign={}, 计算sign={}, 结果={}", sign, generatedSign, result);

        return result;
    }

    /**
     * 处理支付回调
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleNotify(Map<String, String> params) {
        log.info("收到支付回调: {}", params);

        // 验证签名
        if (!verifySign(params)) {
            log.error("签名验证失败");
            return "fail";
        }

        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.warn("支付状态异常: {}", tradeStatus);
            return "fail";
        }

        // 查询订单
        McRechargeOrder order = rechargeOrderMapper.selectMcRechargeOrderByOrderNo(outTradeNo);
        if (order == null) {
            log.error("订单不存在: {}", outTradeNo);
            return "fail";
        }

        // 防止重复回调
        if (order.getOrderStatus() == 1) {
            log.info("订单已支付，忽略重复回调: {}", outTradeNo);
            return "success";
        }

        // 更新订单状态
        order.setOrderStatus(1);
        order.setNotifyStatus(1);
        order.setTradeNo(tradeNo);
        order.setPayTime(new Date());
        order.setCallbackData(params.toString());
        rechargeOrderMapper.updateMcRechargeOrder(order);

        // 增加用户积分
        userPointsService.addPoints(order.getUserId(), order.getPoints(),
                order.getOrderId(), order.getOrderNo(), "充值到账");

        log.info("订单支付成功: {}, 积分: {}", outTradeNo, order.getPoints());

        return "success";
    }

    /**
     * 处理同步跳转
     */
    @Override
    public String handleReturn(Map<String, String> params) {
        log.info("收到同步跳转: {}", params);

        if (!verifySign(params)) {
            log.error("同步跳转签名验证失败");
            return null;
        }

        return params.get("out_trade_no");
    }
}

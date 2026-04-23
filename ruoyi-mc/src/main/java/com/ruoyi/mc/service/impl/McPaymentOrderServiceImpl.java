package com.ruoyi.mc.service.impl;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mc.domain.McPaymentConfig;
import com.ruoyi.mc.service.IMcPaymentConfigService;
import com.ruoyi.mc.service.IMcUserPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mc.mapper.McPaymentOrderMapper;
import com.ruoyi.mc.domain.McPaymentOrder;
import com.ruoyi.mc.service.IMcPaymentOrderService;

/**
 * 支付订单Service业务层处理
 *
 * @author ruoyi
 * @date 2024
 */
@Service
public class McPaymentOrderServiceImpl implements IMcPaymentOrderService
{
    private static final Logger log = LoggerFactory.getLogger(McPaymentOrderServiceImpl.class);

    @Autowired
    private McPaymentOrderMapper mcPaymentOrderMapper;

    @Autowired
    private IMcPaymentConfigService paymentConfigService;

    @Autowired
    private IMcUserPointsService userPointsService;

    @Override
    public McPaymentOrder selectMcPaymentOrderByOrderId(Long orderId)
    {
        return mcPaymentOrderMapper.selectMcPaymentOrderByOrderId(orderId);
    }

    @Override
    public McPaymentOrder selectMcPaymentOrderByOutTradeNo(String outTradeNo)
    {
        return mcPaymentOrderMapper.selectMcPaymentOrderByOutTradeNo(outTradeNo);
    }

    @Override
    public List<McPaymentOrder> selectMcPaymentOrderList(McPaymentOrder mcPaymentOrder)
    {
        return mcPaymentOrderMapper.selectMcPaymentOrderList(mcPaymentOrder);
    }

    @Override
    public List<McPaymentOrder> selectAllOrdersList(McPaymentOrder mcPaymentOrder)
    {
        return mcPaymentOrderMapper.selectAllOrdersList(mcPaymentOrder);
    }

    @Override
    public int insertMcPaymentOrder(McPaymentOrder mcPaymentOrder)
    {
        mcPaymentOrder.setCreateTime(DateUtils.getNowDate());
        return mcPaymentOrderMapper.insertMcPaymentOrder(mcPaymentOrder);
    }

    @Override
    public int updateMcPaymentOrder(McPaymentOrder mcPaymentOrder)
    {
        mcPaymentOrder.setUpdateTime(DateUtils.getNowDate());
        return mcPaymentOrderMapper.updateMcPaymentOrder(mcPaymentOrder);
    }

    @Override
    public int deleteMcPaymentOrderByOrderIds(Long[] orderIds)
    {
        return mcPaymentOrderMapper.deleteMcPaymentOrderByOrderIds(orderIds);
    }

    @Override
    public int deleteMcPaymentOrderByOrderId(Long orderId)
    {
        return mcPaymentOrderMapper.deleteMcPaymentOrderByOrderId(orderId);
    }

    @Override
    public String createPaymentAndGetUrl(McPaymentOrder mcPaymentOrder) throws Exception
    {
        McPaymentConfig config;
        if (mcPaymentOrder.getConfigId() != null)
        {
            config = paymentConfigService.selectMcPaymentConfigByConfigId(mcPaymentOrder.getConfigId());
        }
        else
        {
            config = paymentConfigService.selectDefaultPaymentConfig();
        }

        if (config == null)
        {
            throw new Exception("支付配置不存在");
        }

        if (!"0".equals(config.getStatus()))
        {
            throw new Exception("支付配置已停用");
        }

        if (mcPaymentOrder.getOutTradeNo() == null || mcPaymentOrder.getOutTradeNo().isEmpty())
        {
            String outTradeNo = "MC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
            mcPaymentOrder.setOutTradeNo(outTradeNo);
        }

        mcPaymentOrder.setStatus("0");
        mcPaymentOrder.setNotifyStatus("0");
        mcPaymentOrder.setConfigId(config.getConfigId());
        mcPaymentOrder.setCreateTime(new Date());

        insertMcPaymentOrder(mcPaymentOrder);

        log.info("=== 创建支付订单 ===");
        log.info("订单号: {}, 用户ID: {}, 金额: {}, 积分: {}",
                mcPaymentOrder.getOutTradeNo(), mcPaymentOrder.getUserId(),
                mcPaymentOrder.getAmount(), mcPaymentOrder.getPoints());

        // 构建请求参数（按照易支付API要求）
        TreeMap<String, String> params = new TreeMap<>();
        params.put("pid", config.getMerchantId());
        params.put("type", mcPaymentOrder.getPaymentType() != null ? mcPaymentOrder.getPaymentType() : "alipay");
        params.put("out_trade_no", mcPaymentOrder.getOutTradeNo());
        params.put("notify_url", config.getNotifyUrl());
        params.put("return_url", config.getReturnUrl());
        params.put("name", mcPaymentOrder.getProductName());
        params.put("money", mcPaymentOrder.getAmount().toString());

        // 添加用户IP（必填）
        String clientIp = mcPaymentOrder.getParam();
        if (StringUtils.isEmpty(clientIp))
        {
            clientIp = "127.0.0.1";
        }
        params.put("clientip", clientIp);

        // 添加设备类型（可选，根据设备判断）
        params.put("device", "pc");

        // 生成签名
        String sign = generateSign(params, config.getMerchantKey());
        params.put("sign", sign);
        params.put("sign_type", "MD5");

        log.info("请求易支付API: {}", config.getApiUrl());
        log.info("请求参数: {}", params);

        // 构建POST请求参数
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (postData.length() > 0)
            {
                postData.append("&");
            }
            postData.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // 调用易支付API
        String responseStr = HttpUtils.sendPost(config.getApiUrl(), postData.toString());
        log.info("易支付API响应: {}", responseStr);

        if (StringUtils.isEmpty(responseStr))
        {
            throw new Exception("支付接口无响应");
        }

        // 解析响应
        JSONObject response = JSON.parseObject(responseStr);
        Integer code = response.getInteger("code");

        if (code == null || code != 1)
        {
            String msg = response.getString("msg");
            log.error("支付接口返回错误: {}", msg);
            throw new Exception("创建支付失败: " + (msg != null ? msg : "未知错误"));
        }

        // 返回结果给前端
        JSONObject result = new JSONObject();
        result.put("outTradeNo", mcPaymentOrder.getOutTradeNo());
        result.put("tradeNo", response.getString("trade_no"));

        // 根据返回字段判断支付方式
        if (response.containsKey("qrcode"))
        {
            // 二维码支付
            result.put("payType", "qrcode");
            result.put("payData", response.getString("qrcode"));
            log.info("返回二维码支付链接");
        }
        else if (response.containsKey("payurl"))
        {
            // 跳转支付
            result.put("payType", "payurl");
            result.put("payData", response.getString("payurl"));
            log.info("返回跳转支付链接");
        }
        else if (response.containsKey("urlscheme"))
        {
            // 小程序支付
            result.put("payType", "urlscheme");
            result.put("payData", response.getString("urlscheme"));
            log.info("返回小程序支付链接");
        }
        else
        {
            throw new Exception("支付接口返回数据格式异常");
        }

        return result.toJSONString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentNotify(Map<String, String> params)
    {
        try
        {
            log.info("=== 开始处理支付回调 ===");
            log.info("回调参数: {}", params);

            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String money = params.get("money");
            String tradeStatus = params.get("trade_status");
            String sign = params.get("sign");

            log.info("订单号: {}, 交易号: {}, 金额: {}, 状态: {}", outTradeNo, tradeNo, money, tradeStatus);

            McPaymentOrder order = selectMcPaymentOrderByOutTradeNo(outTradeNo);
            if (order == null)
            {
                log.error("订单不存在: {}", outTradeNo);
                return false;
            }

            log.info("查询到订单: orderId={}, userId={}, amount={}, currentStatus={}",
                    order.getOrderId(), order.getUserId(), order.getAmount(), order.getStatus());

            if ("1".equals(order.getStatus()))
            {
                log.info("订单已处理过，直接返回成功");
                return true;
            }

            McPaymentConfig config = paymentConfigService.selectMcPaymentConfigByConfigId(order.getConfigId());
            if (config == null)
            {
                log.error("支付配置不存在: {}", order.getConfigId());
                return false;
            }

            TreeMap<String, String> signParams = new TreeMap<>();
            for (Map.Entry<String, String> entry : params.entrySet())
            {
                if (!"sign".equals(entry.getKey()) && !"sign_type".equals(entry.getKey()))
                {
                    signParams.put(entry.getKey(), entry.getValue());
                }
            }
            String localSign = generateSign(signParams, config.getMerchantKey());
            log.info("签名验证: 接收签名={}, 本地签名={}", sign, localSign);

            if (!localSign.equals(sign))
            {
                log.error("签名验证失败，订单号: {}", outTradeNo);
                return false;
            }

            BigDecimal notifyAmount = new BigDecimal(money);
            if (order.getAmount().compareTo(notifyAmount) != 0)
            {
                log.error("订单金额不匹配，订单号: {}, 订单金额: {}, 通知金额: {}",
                        outTradeNo, order.getAmount(), notifyAmount);
                return false;
            }

            if ("TRADE_SUCCESS".equals(tradeStatus))
            {
                log.info("交易状态为TRADE_SUCCESS，开始更新订单状态");

                order.setTradeNo(tradeNo);
                order.setStatus("1");
                order.setNotifyStatus("1");
                order.setPayTime(new Date());
                order.setNotifyTime(new Date());
                updateMcPaymentOrder(order);

                log.info("订单状态更新成功");

                // 增加用户积分
                if (order.getUserId() != null && order.getPoints() != null && order.getPoints() > 0)
                {
                    log.info("开始增加用户积分: userId={}, points={}", order.getUserId(), order.getPoints());
                    userPointsService.addPoints(order.getUserId(), order.getPoints(),
                            order.getOrderId(), order.getOutTradeNo(), "充值到账");
                    log.info("用户 {} 充值积分成功: {}", order.getUserId(), order.getPoints());
                }
                else
                {
                    log.warn("积分为空或为0，不进行积分增加: userId={}, points={}", order.getUserId(), order.getPoints());
                }

                log.info("订单支付成功，订单号: {}, 金额: {}", outTradeNo, money);
                return true;
            }
            else
            {
                log.warn("交易状态不是TRADE_SUCCESS: {}", tradeStatus);
            }

            return false;
        }
        catch (Exception e)
        {
            log.error("处理支付通知异常", e);
            return false;
        }
    }

    @Override
    public McPaymentOrder handlePaymentReturn(Map<String, String> params)
    {
        try
        {
            String outTradeNo = params.get("out_trade_no");
            if (outTradeNo == null || outTradeNo.isEmpty())
            {
                return null;
            }

            return selectMcPaymentOrderByOutTradeNo(outTradeNo);
        }
        catch (Exception e)
        {
            log.error("处理支付返回异常", e);
            return null;
        }
    }

    private String generateSign(TreeMap<String, String> params, String key)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (entry.getValue() != null && !entry.getValue().isEmpty())
            {
                if (!first)
                {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }
        sb.append(key);

        log.info("签名原串: {}", sb.toString());

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(sb.toString().getBytes("UTF-8"));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes)
            {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
        catch (Exception e)
        {
            log.error("生成签名异常", e);
            return "";
        }
    }

    @Override
    public String queryAndReconcileOrder(String outTradeNo)
    {
        try
        {
            log.info("=== 开始查询易支付订单状态: {} ===", outTradeNo);

            McPaymentOrder order = selectMcPaymentOrderByOutTradeNo(outTradeNo);
            if (order == null)
            {
                return "订单不存在: " + outTradeNo;
            }

            if ("1".equals(order.getStatus()))
            {
                return "订单已支付，无需补单";
            }

            McPaymentConfig config = paymentConfigService.selectMcPaymentConfigByConfigId(order.getConfigId());
            if (config == null)
            {
                config = paymentConfigService.selectDefaultPaymentConfig();
            }
            if (config == null)
            {
                return "支付配置不存在";
            }

            // 构建易支付查询API URL
            // 易支付查询接口: apiUrl域名/api.php?act=order&pid=XXX&key=XXX&out_trade_no=XXX
            String apiBase = config.getApiUrl();
            if (apiBase.contains("/mapi.php"))
            {
                apiBase = apiBase.replace("/mapi.php", "/api.php");
            }
            else if (apiBase.contains("/submit.php"))
            {
                apiBase = apiBase.replace("/submit.php", "/api.php");
            }
            else if (!apiBase.contains("/api.php"))
            {
                apiBase = apiBase.substring(0, apiBase.lastIndexOf("/") + 1) + "api.php";
            }

            String queryUrl = apiBase + "?act=order&pid=" + config.getMerchantId()
                    + "&key=" + config.getMerchantKey()
                    + "&out_trade_no=" + outTradeNo;

            log.info("查询易支付订单: {}", queryUrl);

            String responseStr = HttpUtils.sendGet(queryUrl);
            log.info("易支付查询响应: {}", responseStr);

            if (StringUtils.isEmpty(responseStr))
            {
                return "易支付查询接口无响应";
            }

            JSONObject response = JSON.parseObject(responseStr);
            Integer code = response.getInteger("code");

            if (code == null || code != 1)
            {
                return "易支付查询失败: " + response.getString("msg");
            }

            String tradeStatus = response.getString("status");
            String tradeNo = response.getString("trade_no");
            String money = response.getString("money");

            log.info("易支付订单状态: status={}, trade_no={}, money={}", tradeStatus, tradeNo, money);

            if (!"1".equals(tradeStatus))
            {
                return "易支付订单未支付 (status=" + tradeStatus + ")，用户可能未完成付款";
            }

            // 易支付显示已支付，执行补单
            log.info("易支付确认已支付，开始补单: outTradeNo={}", outTradeNo);

            order.setTradeNo(tradeNo);
            order.setStatus("1");
            order.setNotifyStatus("1");
            order.setPayTime(new Date());
            order.setNotifyTime(new Date());
            updateMcPaymentOrder(order);

            // 增加用户积分
            if (order.getUserId() != null && order.getPoints() != null && order.getPoints() > 0)
            {
                userPointsService.addPoints(order.getUserId(), order.getPoints(),
                        order.getOrderId(), order.getOutTradeNo(), "补单到账");
                log.info("补单成功: userId={}, points={}", order.getUserId(), order.getPoints());
                return "补单成功！已为用户 " + order.getUsername() + " 补充 " + order.getPoints() + " 积分";
            }
            else
            {
                return "订单状态已更新为已支付，但积分信息为空无法补充积分";
            }
        }
        catch (Exception e)
        {
            log.error("查询易支付订单异常", e);
            return "查询异常: " + e.getMessage();
        }
    }
}

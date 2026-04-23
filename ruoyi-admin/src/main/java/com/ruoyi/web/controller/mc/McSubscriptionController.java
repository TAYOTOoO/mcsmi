package com.ruoyi.web.controller.mc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mc.domain.McSubscriptionOrder;
import com.ruoyi.mc.domain.McSubscriptionPackage;
import com.ruoyi.mc.domain.McUserSubscription;
import com.ruoyi.mc.domain.McPaymentConfig;
import com.ruoyi.mc.service.IMcSubscriptionOrderService;
import com.ruoyi.mc.service.IMcSubscriptionPackageService;
import com.ruoyi.mc.service.IMcUserSubscriptionService;
import com.ruoyi.mc.service.IEpayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订阅管理Controller（管理端）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mc/subscription")
public class McSubscriptionController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(McSubscriptionController.class);

    @Autowired
    private IMcSubscriptionPackageService subscriptionPackageService;

    @Autowired
    private IMcSubscriptionOrderService subscriptionOrderService;

    @Autowired
    private IMcUserSubscriptionService userSubscriptionService;

    @Autowired
    private IEpayService epayService;

    // ==================== 套餐管理 ====================

    /**
     * 查询订阅套餐列表
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:list')")
    @GetMapping("/package/list")
    public TableDataInfo packageList(McSubscriptionPackage mcSubscriptionPackage) {
        startPage();
        List<McSubscriptionPackage> list = subscriptionPackageService.selectMcSubscriptionPackageList(mcSubscriptionPackage);
        return getDataTable(list);
    }

    /**
     * 获取订阅套餐详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:query')")
    @GetMapping("/package/{packageId}")
    public AjaxResult getPackageInfo(@PathVariable("packageId") Long packageId) {
        return success(subscriptionPackageService.selectMcSubscriptionPackageByPackageId(packageId));
    }

    /**
     * 新增订阅套餐
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:add')")
    @Log(title = "订阅套餐", businessType = BusinessType.INSERT)
    @PostMapping("/package")
    public AjaxResult addPackage(@RequestBody McSubscriptionPackage mcSubscriptionPackage) {
        return toAjax(subscriptionPackageService.insertMcSubscriptionPackage(mcSubscriptionPackage));
    }

    /**
     * 修改订阅套餐
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:edit')")
    @Log(title = "订阅套餐", businessType = BusinessType.UPDATE)
    @PutMapping("/package")
    public AjaxResult editPackage(@RequestBody McSubscriptionPackage mcSubscriptionPackage) {
        return toAjax(subscriptionPackageService.updateMcSubscriptionPackage(mcSubscriptionPackage));
    }

    /**
     * 删除订阅套餐
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:remove')")
    @Log(title = "订阅套餐", businessType = BusinessType.DELETE)
    @DeleteMapping("/package/{packageIds}")
    public AjaxResult removePackage(@PathVariable Long[] packageIds) {
        return toAjax(subscriptionPackageService.deleteMcSubscriptionPackageByPackageIds(packageIds));
    }

    // ==================== 订单管理 ====================

    /**
     * 查询订阅订单列表
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:list')")
    @GetMapping("/order/list")
    public TableDataInfo orderList(McSubscriptionOrder mcSubscriptionOrder) {
        startPage();
        List<McSubscriptionOrder> list = subscriptionOrderService.selectMcSubscriptionOrderList(mcSubscriptionOrder);
        return getDataTable(list);
    }

    /**
     * 获取订阅订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:query')")
    @GetMapping("/order/{orderId}")
    public AjaxResult getOrderInfo(@PathVariable("orderId") Long orderId) {
        return success(subscriptionOrderService.selectMcSubscriptionOrderByOrderId(orderId));
    }

    /**
     * 退款订单（管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:edit')")
    @Log(title = "订阅订单退款", businessType = BusinessType.UPDATE)
    @PostMapping("/order/refund")
    public AjaxResult refundOrder(@RequestBody Map<String, Object> params) {
        try {
            Long orderId = Long.valueOf(params.get("orderId").toString());
            String remark = params.getOrDefault("remark", "管理员退款").toString();

            McSubscriptionOrder order = subscriptionOrderService.selectMcSubscriptionOrderByOrderId(orderId);
            if (order == null) {
                return error("订单不存在");
            }

            if (order.getPaymentStatus() != 1) {
                return error("只能退款已支付的订单");
            }

            // TODO: 实现退款逻辑
            // 1. 修改订单状态为已退款
            // 2. 取消用户订阅
            // 3. 记录退款日志

            logger.info("管理员 {} 对订单 {} 执行退款，原因：{}", SecurityUtils.getUsername(), order.getOrderNo(), remark);

            return error("退款功能待实现");
        } catch (Exception e) {
            logger.error("订单退款失败", e);
            return error("退款失败：" + e.getMessage());
        }
    }

    // ==================== 用户订阅管理 ====================

    /**
     * 查询用户订阅列表
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:list')")
    @GetMapping("/user/list")
    public TableDataInfo userSubscriptionList(McUserSubscription mcUserSubscription) {
        startPage();
        List<McUserSubscription> list = userSubscriptionService.selectMcUserSubscriptionList(mcUserSubscription);
        return getDataTable(list);
    }

    /**
     * 获取用户订阅详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:subscription:query')")
    @GetMapping("/user/{subscriptionId}")
    public AjaxResult getUserSubscriptionInfo(@PathVariable("subscriptionId") Long subscriptionId) {
        return success(userSubscriptionService.selectMcUserSubscriptionBySubscriptionId(subscriptionId));
    }

    // ==================== 用户端接口（无权限要求） ====================

    /**
     * 获取可用订阅套餐列表（用户端）
     */
    @GetMapping("/packages")
    public AjaxResult getAvailablePackages() {
        List<McSubscriptionPackage> packages = subscriptionPackageService.selectAvailablePackages();
        return success(packages);
    }

    /**
     * 创建订阅订单（用户端）
     */
    @PostMapping("/order/create")
    public AjaxResult createOrder(@RequestBody Map<String, Object> params) {
        try {
            Long userId = SecurityUtils.getUserId();
            Long packageId = Long.valueOf(params.get("packageId").toString());
            String paymentType = params.getOrDefault("paymentType", "alipay").toString();

            logger.info("用户 {} 创建订阅订单，套餐ID：{}，支付方式：{}", userId, packageId, paymentType);

            // 创建订单
            McSubscriptionOrder order = subscriptionOrderService.createOrder(userId, packageId);

            // 设置支付方式并更新数据库
            order.setPaymentMethod(paymentType);
            subscriptionOrderService.updateMcSubscriptionOrder(order);

            logger.info("订单创建成功，订单号：{}，支付方式：{}", order.getOrderNo(), order.getPaymentMethod());

            // 调用易支付API获取支付数据（与充值订单保持一致）
            String paymentResponse = epayService.createSubscriptionPayment(order);

            logger.info("易支付API响应: {}", paymentResponse);

            // 解析响应
            JSONObject response = JSON.parseObject(paymentResponse);
            Integer code = response.getInteger("code");

            if (code == null || code != 1) {
                String msg = response.getString("msg");
                logger.error("易支付返回错误: {}", msg);
                return error("支付接口错误: " + msg);
            }

            // 构建返回数据（与充值订单格式一致）
            Map<String, Object> result = new HashMap<>();
            result.put("outTradeNo", order.getOrderNo());
            result.put("tradeNo", response.getString("trade_no"));

            // 根据返回字段判断支付方式（与充值订单逻辑一致）
            if (response.containsKey("qrcode")) {
                // 二维码支付
                result.put("payType", "qrcode");
                result.put("payData", response.getString("qrcode"));
                logger.info("返回二维码支付链接");
            } else if (response.containsKey("payurl")) {
                // 跳转支付
                result.put("payType", "payurl");
                result.put("payData", response.getString("payurl"));
                logger.info("返回跳转支付链接");
            } else if (response.containsKey("urlscheme")) {
                // 小程序支付
                result.put("payType", "urlscheme");
                result.put("payData", response.getString("urlscheme"));
                logger.info("返回小程序支付链接");
            } else {
                logger.error("支付接口返回数据格式异常: {}", paymentResponse);
                return error("支付接口返回数据格式异常");
            }

            logger.info("订阅支付创建成功: orderNo={}, payType={}", order.getOrderNo(), result.get("payType"));

            return success(result);
        } catch (Exception e) {
            logger.error("创建订阅订单失败", e);
            return error("创建订单失败：" + e.getMessage());
        }
    }

    /**
     * 查询订单状态（用户端）
     */
    @GetMapping("/order/status/{orderNo}")
    public AjaxResult getOrderStatus(@PathVariable String orderNo) {
        try {
            Long userId = SecurityUtils.getUserId();
            McSubscriptionOrder order = subscriptionOrderService.selectMcSubscriptionOrderByOrderNo(orderNo);

            if (order == null) {
                return error("订单不存在");
            }

            if (!order.getUserId().equals(userId)) {
                return error("无权查看此订单");
            }

            return success(order);
        } catch (Exception e) {
            logger.error("查询订单状态失败", e);
            return error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 支付回调（易支付 - POST）
     */
    @PostMapping("/order/pay/notify")
    @ResponseBody
    public String paymentNotifyPost(HttpServletRequest request) {
        return handlePaymentNotify(request);
    }

    /**
     * 支付回调（易支付 - GET，部分支付网关使用GET回调）
     */
    @GetMapping("/order/pay/notify")
    @ResponseBody
    public String paymentNotifyGet(HttpServletRequest request) {
        return handlePaymentNotify(request);
    }

    /**
     * 处理支付回调的统一方法
     */
    private String handlePaymentNotify(HttpServletRequest request) {
        try {
            // 获取所有回调参数
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    params.put(key, values[0]);
                }
            });

            logger.info("收到订阅订单支付回调，参数：{}", params);

            // 使用EpayService验证签名
            if (!epayService.verifySign(params)) {
                logger.error("签名验证失败");
                return "fail";
            }

            // 检查支付状态
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                logger.warn("支付状态异常：{}", tradeStatus);
                return "fail";
            }

            // 获取订单号和交易号
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");

            logger.info("订阅订单 {} 支付成功，交易号：{}", outTradeNo, tradeNo);

            // 处理订单
            boolean result = subscriptionOrderService.processPaymentCallback(outTradeNo, tradeNo);
            return result ? "success" : "fail";
        } catch (Exception e) {
            logger.error("处理支付回调失败", e);
            return "fail";
        }
    }

    /**
     * 获取我的订阅信息（用户端）
     */
    @GetMapping("/my/info")
    public AjaxResult getMySubscription() {
        try {
            Long userId = SecurityUtils.getUserId();
            McUserSubscription subscription = userSubscriptionService.selectMcUserSubscriptionByUserId(userId);

            if (subscription == null) {
                return success(null);
            }

            return success(subscription);
        } catch (Exception e) {
            logger.error("查询订阅信息失败", e);
            return error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取我的订阅订单列表（用户端）
     */
    @GetMapping("/my/orders")
    public AjaxResult getMyOrders() {
        try {
            Long userId = SecurityUtils.getUserId();
            List<McSubscriptionOrder> orders = subscriptionOrderService.selectMcSubscriptionOrderListByUserId(userId);
            return success(orders);
        } catch (Exception e) {
            logger.error("查询订阅订单失败", e);
            return error("查询失败：" + e.getMessage());
        }
    }
}

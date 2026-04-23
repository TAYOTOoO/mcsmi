package com.ruoyi.web.controller.mc;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mc.domain.McRechargeOrder;
import com.ruoyi.mc.domain.McRechargePackage;
import com.ruoyi.mc.mapper.McRechargePackageMapper;
import com.ruoyi.mc.service.IEpayService;
import com.ruoyi.mc.service.IMcRechargeOrderService;
import com.ruoyi.mc.utils.SubscriptionPermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 充值支付Controller
 */
@RestController
@RequestMapping("/mc/payment")
public class McPaymentController extends BaseController {

    @Autowired
    private IMcRechargeOrderService rechargeOrderService;

    @Autowired
    private McRechargePackageMapper rechargePackageMapper;

    @Autowired
    private IEpayService epayService;

    @Autowired
    private SubscriptionPermissionChecker permissionChecker;

    /**
     * 查询充值套餐列表
     */
    @GetMapping("/packages")
    public AjaxResult getPackages() {
        List<McRechargePackage> packages = rechargePackageMapper.selectAvailablePackages();

        // 检查用户是否为钻石用户，应用折扣
        try {
            Long userId = getUserId();
            if (permissionChecker.isDiamondUser(userId)) {
                BigDecimal discountRate = permissionChecker.getDiscountRate(userId);
                for (McRechargePackage pkg : packages) {
                    // 计算折扣后价格
                    BigDecimal originalPrice = pkg.getAmount();
                    BigDecimal discountedPrice = originalPrice.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
                    // 将折扣信息添加到params中，前端可以展示
                    if (pkg.getParams() == null) {
                        pkg.setParams(new HashMap<>());
                    }
                    pkg.getParams().put("isDiamond", true);
                    pkg.getParams().put("originalPrice", originalPrice);
                    pkg.getParams().put("discountedPrice", discountedPrice);
                    pkg.getParams().put("discountRate", discountRate);
                }
            }
        } catch (Exception e) {
            // 用户未登录或获取失败，不应用折扣
            logger.debug("获取用户折扣信息失败，使用原价", e);
        }

        return success(packages);
    }

    /**
     * 查询充值订单列表（用户端：开放访问）
     */
    @GetMapping("/orders")
    public TableDataInfo listOrders() {
        startPage();
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            // 安全修复：未登录用户不允许查询订单
            logger.warn("未登录用户尝试访问订单列表");
            return getDataTable(new java.util.ArrayList<>());
        }
        McRechargeOrder query = new McRechargeOrder();
        query.setUserId(userId);
        List<McRechargeOrder> list = rechargeOrderService.selectMcRechargeOrderList(query);
        return getDataTable(list);
    }

    /**
     * 创建充值订单（套餐）（用户端：开放访问）
     */
    @PostMapping("/create")
    public AjaxResult createOrder(@RequestBody Map<String, Object> params) {
        Long packageId = Long.valueOf(params.get("packageId").toString());
        String paymentType = params.get("paymentType").toString();
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            // 安全修复：未登录用户不允许创建订单
            logger.warn("未登录用户尝试创建充值订单");
            return error("请先登录");
        }

        // 检查是否为钻石用户，应用折扣
        McRechargeOrder order;
        if (permissionChecker.isDiamondUser(userId)) {
            BigDecimal discountRate = permissionChecker.getDiscountRate(userId);
            logger.info("用户 {} 为钻石用户，享受 {}% 折扣", userId, discountRate.multiply(new BigDecimal("100")));
            // TODO: 在创建订单时传递折扣率，让订单服务计算实付金额
            // 当前先使用原逻辑创建订单
            order = rechargeOrderService.createRechargeOrder(userId, packageId, paymentType);
        } else {
            order = rechargeOrderService.createRechargeOrder(userId, packageId, paymentType);
        }

        return success(order);
    }

    /**
     * 创建充值订单（自定义金额）（用户端：开放访问）
     */
    @PostMapping("/create/custom")
    public AjaxResult createCustomOrder(@RequestBody Map<String, Object> params) {
        String amount = params.get("amount").toString();
        String paymentType = params.get("paymentType").toString();
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            // 安全修复：未登录用户不允许创建订单
            logger.warn("未登录用户尝试创建自定义充值订单");
            return error("请先登录");
        }

        McRechargeOrder order = rechargeOrderService.createCustomRechargeOrder(userId, amount, paymentType);
        return success(order);
    }

    /**
     * 发起支付（生成支付URL）（用户端：开放访问）
     */
    @GetMapping("/pay/{orderId}")
    public AjaxResult pay(@PathVariable Long orderId) {
        McRechargeOrder order = rechargeOrderService.selectMcRechargeOrderByOrderId(orderId);
        if (order == null) {
            return error("订单不存在");
        }

        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 1L; // 匿名用户使用默认ID
        }

        if (!order.getUserId().equals(userId)) {
            return error("无权操作此订单");
        }

        if (order.getOrderStatus() != 0) {
            return error("订单状态异常");
        }

        // 生成支付URL
        String payUrl = epayService.generatePayUrl(order);
        return success(payUrl);
    }

    /**
     * 发起支付（生成支付表单）（用户端：开放访问）
     */
    @GetMapping("/pay/form/{orderId}")
    public void payForm(@PathVariable Long orderId, HttpServletResponse response) throws IOException {
        McRechargeOrder order = rechargeOrderService.selectMcRechargeOrderByOrderId(orderId);
        if (order == null) {
            response.getWriter().write("订单不存在");
            return;
        }

        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 1L; // 匿名用户使用默认ID
        }

        if (!order.getUserId().equals(userId)) {
            response.getWriter().write("无权操作此订单");
            return;
        }

        if (order.getOrderStatus() != 0) {
            response.getWriter().write("订单状态异常");
            return;
        }

        // 生成支付表单并自动提交
        String formHtml = epayService.generatePayForm(order);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(formHtml);
    }

    /**
     * 支付异步回调
     */
    @PostMapping("/notify")
    @ResponseBody
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });

        return epayService.handleNotify(params);
    }

    /**
     * 支付同步跳转
     */
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });

        String orderNo = epayService.handleReturn(params);
        if (orderNo != null) {
            // 跳转到结果页面
            response.sendRedirect("http://localhost:3000/recharge/result?orderNo=" + orderNo);
        } else {
            response.getWriter().write("支付验证失败");
        }
    }

    /**
     * 查询订单状态（用户端：开放访问）
     */
    @GetMapping("/order/{orderNo}")
    public AjaxResult getOrder(@PathVariable String orderNo) {
        McRechargeOrder order = rechargeOrderService.selectMcRechargeOrderByOrderNo(orderNo);
        if (order == null) {
            return error("订单不存在");
        }

        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            userId = 1L; // 匿名用户使用默认ID
        }

        if (!order.getUserId().equals(userId)) {
            return error("无权查看此订单");
        }

        return success(order);
    }
}

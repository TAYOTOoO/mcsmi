package com.ruoyi.web.controller.mc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.mc.domain.McPaymentOrder;
import com.ruoyi.mc.domain.McSubscriptionOrder;
import com.ruoyi.mc.service.IMcPaymentOrderService;
import com.ruoyi.mc.service.IMcSubscriptionOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 支付订单Controller
 *
 * @author ruoyi
 * @date 2024
 */
@RestController
@RequestMapping("/mc/paymentOrder")
public class McPaymentOrderController extends BaseController
{
    @Autowired
    private IMcPaymentOrderService mcPaymentOrderService;

    @Autowired
    private IMcSubscriptionOrderService mcSubscriptionOrderService;

    /**
     * 查询支付订单列表（包含订阅订单）
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(McPaymentOrder mcPaymentOrder)
    {
        startPage();
        List<McPaymentOrder> list = mcPaymentOrderService.selectAllOrdersList(mcPaymentOrder);
        return getDataTable(list);
    }

    /**
     * 导出支付订单列表（包含订阅订单）
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:export')")
    @Log(title = "支付订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McPaymentOrder mcPaymentOrder)
    {
        List<McPaymentOrder> list = mcPaymentOrderService.selectAllOrdersList(mcPaymentOrder);
        ExcelUtil<McPaymentOrder> util = new ExcelUtil<McPaymentOrder>(McPaymentOrder.class);
        util.exportExcel(response, list, "支付订单数据");
    }

    /**
     * 获取支付订单详细信息（支持订阅订单）
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        // 订阅订单的orderId = 原始orderId + 10000000
        if (orderId > 10000000L)
        {
            Long realOrderId = orderId - 10000000L;
            McSubscriptionOrder subOrder = mcSubscriptionOrderService.selectMcSubscriptionOrderByOrderId(realOrderId);
            if (subOrder != null)
            {
                // 转换为McPaymentOrder格式以复用前端展示
                McPaymentOrder paymentOrder = new McPaymentOrder();
                paymentOrder.setOrderId(orderId);
                paymentOrder.setOutTradeNo(subOrder.getOrderNo());
                paymentOrder.setTradeNo(subOrder.getTradeNo());
                paymentOrder.setUserId(subOrder.getUserId());
                paymentOrder.setUsername(subOrder.getUserName());
                paymentOrder.setOrderType("subscription");
                paymentOrder.setPackageId(subOrder.getPackageId());
                paymentOrder.setDurationMonths(subOrder.getDurationMonths());
                paymentOrder.setStartTime(subOrder.getStartTime());
                paymentOrder.setExpireTime(subOrder.getExpireTime());
                paymentOrder.setProductName(subOrder.getPackageName());
                paymentOrder.setAmount(subOrder.getActualAmount());
                paymentOrder.setPaymentType(subOrder.getPaymentMethod());
                paymentOrder.setStatus(subOrder.getPaymentStatus() != null ? String.valueOf(subOrder.getPaymentStatus()) : null);
                paymentOrder.setPayTime(subOrder.getPaymentTime());
                paymentOrder.setCreateTime(subOrder.getCreateTime());
                paymentOrder.setUpdateTime(subOrder.getUpdateTime());
                paymentOrder.setRemark(subOrder.getRemark());
                return success(paymentOrder);
            }
            return error("订单不存在");
        }
        return success(mcPaymentOrderService.selectMcPaymentOrderByOrderId(orderId));
    }

    /**
     * 新增支付订单
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:add')")
    @Log(title = "支付订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McPaymentOrder mcPaymentOrder)
    {
        return toAjax(mcPaymentOrderService.insertMcPaymentOrder(mcPaymentOrder));
    }

    /**
     * 修改支付订单
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:edit')")
    @Log(title = "支付订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McPaymentOrder mcPaymentOrder)
    {
        return toAjax(mcPaymentOrderService.updateMcPaymentOrder(mcPaymentOrder));
    }

    /**
     * 删除支付订单（支持订阅订单）
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:remove')")
    @Log(title = "支付订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        // 分离支付订单和订阅订单
        java.util.List<Long> paymentIds = new java.util.ArrayList<>();
        java.util.List<Long> subscriptionIds = new java.util.ArrayList<>();
        for (Long id : orderIds)
        {
            if (id > 10000000L)
            {
                subscriptionIds.add(id - 10000000L);
            }
            else
            {
                paymentIds.add(id);
            }
        }

        int count = 0;
        if (!paymentIds.isEmpty())
        {
            count += mcPaymentOrderService.deleteMcPaymentOrderByOrderIds(paymentIds.toArray(new Long[0]));
        }
        if (!subscriptionIds.isEmpty())
        {
            count += mcSubscriptionOrderService.deleteMcSubscriptionOrderByOrderIds(subscriptionIds.toArray(new Long[0]));
        }

        return toAjax(count);
    }

    /**
     * 创建支付订单（用户端调用）
     */
    @PostMapping("/create")
    public AjaxResult createPayment(@RequestBody McPaymentOrder mcPaymentOrder, HttpServletRequest request)
    {
        try
        {
            // 从登录信息获取用户ID和用户名
            mcPaymentOrder.setUserId(getUserId());
            mcPaymentOrder.setUsername(getUsername());

            // 获取用户IP地址（存储到param字段临时传递）
            String clientIp = IpUtils.getIpAddr(request);
            mcPaymentOrder.setParam(clientIp);

            logger.info("用户 {} 创建支付订单，IP: {}, 金额: {}, 积分: {}",
                    mcPaymentOrder.getUsername(), clientIp,
                    mcPaymentOrder.getAmount(), mcPaymentOrder.getPoints());

            String paymentData = mcPaymentOrderService.createPaymentAndGetUrl(mcPaymentOrder);
            return AjaxResult.success("创建订单成功", paymentData);
        }
        catch (Exception e)
        {
            logger.error("创建支付订单失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 测试支付（管理员后台测试用）
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:edit')")
    @PostMapping("/test")
    public AjaxResult testPayment(@RequestBody McPaymentOrder mcPaymentOrder)
    {
        try
        {
            mcPaymentOrder.setUsername("测试用户");
            String paymentForm = mcPaymentOrderService.createPaymentAndGetUrl(mcPaymentOrder);
            return AjaxResult.success("创建测试订单成功", paymentForm);
        }
        catch (Exception e)
        {
            logger.error("测试支付失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 支付异步通知（易支付回调）
     * 注意：易支付可能使用POST或GET方法，所以同时支持两种方法
     */
    @PostMapping("/notify")
    public String paymentNotifyPost(HttpServletRequest request)
    {
        return handlePaymentNotify(request);
    }

    /**
     * 支付异步通知（易支付回调 - GET方法）
     */
    @GetMapping("/notify")
    public String paymentNotifyGet(HttpServletRequest request)
    {
        return handlePaymentNotify(request);
    }

    /**
     * 处理支付回调的统一方法
     */
    private String handlePaymentNotify(HttpServletRequest request)
    {
        logger.info("=== 收到支付回调请求 ===");
        logger.info("请求来源IP: {}", request.getRemoteAddr());
        logger.info("请求URL: {}", request.getRequestURL());

        try
        {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet())
            {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++)
                {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            logger.info("回调参数: {}", params);

            boolean result = mcPaymentOrderService.handlePaymentNotify(params);
            if (result)
            {
                logger.info("支付回调处理成功");
                return "success";
            }
            else
            {
                logger.error("支付回调处理失败");
                return "fail";
            }
        }
        catch (Exception e)
        {
            logger.error("处理支付通知异常", e);
            return "fail";
        }
    }

    /**
     * 支付同步返回
     */
    @GetMapping("/return")
    public AjaxResult paymentReturn(HttpServletRequest request)
    {
        try
        {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet())
            {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++)
                {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            McPaymentOrder order = mcPaymentOrderService.handlePaymentReturn(params);
            return success(order);
        }
        catch (Exception e)
        {
            logger.error("处理支付返回异常", e);
            return error("处理支付返回失败");
        }
    }

    /**
     * 查询订单状态（用户端轮询）
     */
    @GetMapping("/status/{outTradeNo}")
    public AjaxResult checkOrderStatus(@PathVariable String outTradeNo)
    {
        McPaymentOrder order = mcPaymentOrderService.selectMcPaymentOrderByOutTradeNo(outTradeNo);
        if (order == null)
        {
            return error("订单不存在");
        }
        return success(order);
    }

    /**
     * 查询易支付订单状态并补单（管理员操作）
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentOrder:edit')")
    @Log(title = "支付订单补单", businessType = BusinessType.UPDATE)
    @PostMapping("/reconcile/{outTradeNo}")
    public AjaxResult reconcileOrder(@PathVariable String outTradeNo)
    {
        String result = mcPaymentOrderService.queryAndReconcileOrder(outTradeNo);
        if (result.startsWith("补单成功"))
        {
            return success(result);
        }
        return error(result);
    }
}

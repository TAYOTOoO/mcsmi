package com.ruoyi.web.controller.mc;

import java.util.List;
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
import com.ruoyi.mc.domain.McPaymentConfig;
import com.ruoyi.mc.service.IMcPaymentConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 支付配置Controller
 *
 * @author ruoyi
 * @date 2024
 */
@RestController
@RequestMapping("/mc/paymentConfig")
public class McPaymentConfigController extends BaseController
{
    @Autowired
    private IMcPaymentConfigService mcPaymentConfigService;

    /**
     * 查询支付配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(McPaymentConfig mcPaymentConfig)
    {
        startPage();
        List<McPaymentConfig> list = mcPaymentConfigService.selectMcPaymentConfigList(mcPaymentConfig);
        return getDataTable(list);
    }

    /**
     * 导出支付配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:export')")
    @Log(title = "支付配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McPaymentConfig mcPaymentConfig)
    {
        List<McPaymentConfig> list = mcPaymentConfigService.selectMcPaymentConfigList(mcPaymentConfig);
        ExcelUtil<McPaymentConfig> util = new ExcelUtil<McPaymentConfig>(McPaymentConfig.class);
        util.exportExcel(response, list, "支付配置数据");
    }

    /**
     * 获取支付配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(mcPaymentConfigService.selectMcPaymentConfigByConfigId(configId));
    }

    /**
     * 获取默认支付配置
     */
    @GetMapping("/default")
    public AjaxResult getDefault()
    {
        return success(mcPaymentConfigService.selectDefaultPaymentConfig());
    }

    /**
     * 新增支付配置
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:add')")
    @Log(title = "支付配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McPaymentConfig mcPaymentConfig)
    {
        return toAjax(mcPaymentConfigService.insertMcPaymentConfig(mcPaymentConfig));
    }

    /**
     * 修改支付配置
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:edit')")
    @Log(title = "支付配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McPaymentConfig mcPaymentConfig)
    {
        return toAjax(mcPaymentConfigService.updateMcPaymentConfig(mcPaymentConfig));
    }

    /**
     * 删除支付配置
     */
    @PreAuthorize("@ss.hasPermi('mc:paymentConfig:remove')")
    @Log(title = "支付配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(mcPaymentConfigService.deleteMcPaymentConfigByConfigIds(configIds));
    }
}

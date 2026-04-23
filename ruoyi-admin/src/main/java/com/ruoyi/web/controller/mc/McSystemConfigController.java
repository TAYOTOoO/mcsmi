package com.ruoyi.web.controller.mc;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.framework.web.service.EmailService;
import com.ruoyi.mc.domain.McSystemConfig;
import com.ruoyi.mc.service.IMcSystemConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 系统配置Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mc/system/config")
public class McSystemConfigController extends BaseController
{
    @Autowired
    private IMcSystemConfigService mcSystemConfigService;

    @Autowired
    private EmailService emailService;

    /**
     * 查询系统配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(McSystemConfig mcSystemConfig)
    {
        startPage();
        List<McSystemConfig> list = mcSystemConfigService.selectMcSystemConfigList(mcSystemConfig);
        return getDataTable(list);
    }

    /**
     * 导出系统配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:config:export')")
    @Log(title = "系统配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McSystemConfig mcSystemConfig)
    {
        List<McSystemConfig> list = mcSystemConfigService.selectMcSystemConfigList(mcSystemConfig);
        ExcelUtil<McSystemConfig> util = new ExcelUtil<McSystemConfig>(McSystemConfig.class);
        util.exportExcel(response, list, "系统配置数据");
    }

    /**
     * 获取系统配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(mcSystemConfigService.selectMcSystemConfigByConfigId(configId));
    }

    /**
     * 根据配置键获取配置值
     */
    @GetMapping(value = "/key/{configKey}")
    public AjaxResult getConfigByKey(@PathVariable("configKey") String configKey)
    {
        return success(mcSystemConfigService.selectConfigValueByKey(configKey));
    }

    /**
     * 新增系统配置
     */
    @PreAuthorize("@ss.hasPermi('mc:config:add')")
    @Log(title = "系统配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McSystemConfig mcSystemConfig)
    {
        return toAjax(mcSystemConfigService.insertMcSystemConfig(mcSystemConfig));
    }

    /**
     * 修改系统配置
     */
    @PreAuthorize("@ss.hasPermi('mc:config:edit')")
    @Log(title = "系统配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McSystemConfig mcSystemConfig)
    {
        return toAjax(mcSystemConfigService.updateMcSystemConfig(mcSystemConfig));
    }

    /**
     * 删除系统配置
     */
    @PreAuthorize("@ss.hasPermi('mc:config:remove')")
    @Log(title = "系统配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(mcSystemConfigService.deleteMcSystemConfigByConfigIds(configIds));
    }

    /**
     * 发送测试邮件
     */
    @PreAuthorize("@ss.hasPermi('mc:config:edit')")
    @Log(title = "发送测试邮件", businessType = BusinessType.OTHER)
    @PostMapping("/testEmail")
    public AjaxResult testEmail(@RequestBody Map<String, String> params)
    {
        String toEmail = params.get("email");
        if (toEmail == null || toEmail.trim().isEmpty())
        {
            return error("请输入收件人邮箱");
        }

        try
        {
            emailService.sendTestEmail(toEmail);
            return success("测试邮件已发送，请检查邮箱");
        }
        catch (Exception e)
        {
            return error("发送失败：" + e.getMessage());
        }
    }
}

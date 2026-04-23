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
import com.ruoyi.mc.domain.McUserPageConfig;
import com.ruoyi.mc.service.IMcUserPageConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 用户端页面配置Controller
 *
 * @author ruoyi
 * @date 2026-02-04
 */
@RestController
@RequestMapping("/mc/userpage")
public class McUserPageConfigController extends BaseController
{
    @Autowired
    private IMcUserPageConfigService mcUserPageConfigService;

    /**
     * 查询用户端页面配置列表（管理端）
     */
    @PreAuthorize("@ss.hasPermi('mc:userpage:list')")
    @GetMapping("/list")
    public TableDataInfo list(McUserPageConfig mcUserPageConfig)
    {
        startPage();
        List<McUserPageConfig> list = mcUserPageConfigService.selectMcUserPageConfigList(mcUserPageConfig);
        return getDataTable(list);
    }

    /**
     * 导出用户端页面配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:userpage:export')")
    @Log(title = "用户端页面配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McUserPageConfig mcUserPageConfig)
    {
        List<McUserPageConfig> list = mcUserPageConfigService.selectMcUserPageConfigList(mcUserPageConfig);
        ExcelUtil<McUserPageConfig> util = new ExcelUtil<McUserPageConfig>(McUserPageConfig.class);
        util.exportExcel(response, list, "用户端页面配置数据");
    }

    /**
     * 获取用户端页面配置详细信息（管理端）
     */
    @PreAuthorize("@ss.hasPermi('mc:userpage:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(mcUserPageConfigService.selectMcUserPageConfigByConfigId(configId));
    }

    /**
     * 新增用户端页面配置
     */
    @PreAuthorize("@ss.hasPermi('mc:userpage:add')")
    @Log(title = "用户端页面配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McUserPageConfig mcUserPageConfig)
    {
        return toAjax(mcUserPageConfigService.insertMcUserPageConfig(mcUserPageConfig));
    }

    /**
     * 修改用户端页面配置
     */
    @PreAuthorize("@ss.hasPermi('mc:userpage:edit')")
    @Log(title = "用户端页面配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McUserPageConfig mcUserPageConfig)
    {
        return toAjax(mcUserPageConfigService.updateMcUserPageConfig(mcUserPageConfig));
    }

    /**
     * 删除用户端页面配置
     */
    @PreAuthorize("@ss.hasPermi('mc:userpage:remove')")
    @Log(title = "用户端页面配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(mcUserPageConfigService.deleteMcUserPageConfigByConfigIds(configIds));
    }

    /**
     * 根据配置键获取配置值（用户端API - 无需权限）
     */
    @GetMapping("/public/key/{configKey}")
    public AjaxResult getConfigByKey(@PathVariable("configKey") String configKey)
    {
        McUserPageConfig config = mcUserPageConfigService.selectMcUserPageConfigByConfigKey(configKey);
        if (config != null && "0".equals(config.getStatus()))
        {
            return success(config);
        }
        return success(null);
    }

    /**
     * 根据页面名称获取配置列表（用户端API - 无需权限）
     */
    @GetMapping("/public/page/{pageName}")
    public AjaxResult getConfigByPageName(@PathVariable("pageName") String pageName)
    {
        List<McUserPageConfig> list = mcUserPageConfigService.selectMcUserPageConfigByPageName(pageName);
        return success(list);
    }
}

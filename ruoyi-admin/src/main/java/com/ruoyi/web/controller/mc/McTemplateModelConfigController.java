package com.ruoyi.web.controller.mc;

import java.util.List;
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
import com.ruoyi.mc.domain.McTemplateModelConfig;
import com.ruoyi.mc.service.IMcTemplateModelConfigService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 模板模型配置Controller
 *
 * @author ruoyi
 * @date 2026-02-28
 */
@RestController
@RequestMapping("/mc/template-model-config")
public class McTemplateModelConfigController extends BaseController
{
    @Autowired
    private IMcTemplateModelConfigService templateModelConfigService;

    /**
     * 查询模板模型配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(McTemplateModelConfig mcTemplateModelConfig)
    {
        startPage();
        List<McTemplateModelConfig> list = templateModelConfigService.selectMcTemplateModelConfigList(mcTemplateModelConfig);
        return getDataTable(list);
    }

    /**
     * 获取模板的模型配置列表（按类型和优先级排序）
     */
    @PreAuthorize("@ss.hasPermi('mc:template:list')")
    @GetMapping("/template/{templateId}")
    public AjaxResult getByTemplate(@PathVariable Long templateId)
    {
        McTemplateModelConfig query = new McTemplateModelConfig();
        query.setTemplateId(templateId);
        List<McTemplateModelConfig> list = templateModelConfigService.selectMcTemplateModelConfigList(query);
        return success(list);
    }

    /**
     * 获取模板模型配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:template:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(templateModelConfigService.selectMcTemplateModelConfigByConfigId(configId));
    }

    /**
     * 新增模板模型配置
     */
    @PreAuthorize("@ss.hasPermi('mc:template:add')")
    @Log(title = "模板模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McTemplateModelConfig mcTemplateModelConfig)
    {
        return toAjax(templateModelConfigService.insertMcTemplateModelConfig(mcTemplateModelConfig));
    }

    /**
     * 修改模板模型配置
     */
    @PreAuthorize("@ss.hasPermi('mc:template:edit')")
    @Log(title = "模板模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McTemplateModelConfig mcTemplateModelConfig)
    {
        return toAjax(templateModelConfigService.updateMcTemplateModelConfig(mcTemplateModelConfig));
    }

    /**
     * 删除模板模型配置
     */
    @PreAuthorize("@ss.hasPermi('mc:template:remove')")
    @Log(title = "模板模型配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(templateModelConfigService.deleteMcTemplateModelConfigByConfigIds(configIds));
    }

    /**
     * 批量更新优先级（用于拖拽排序）
     */
    @PreAuthorize("@ss.hasPermi('mc:template:edit')")
    @Log(title = "更新模型优先级", businessType = BusinessType.UPDATE)
    @PutMapping("/priority")
    public AjaxResult updatePriority(@RequestBody List<McTemplateModelConfig> configs)
    {
        for (McTemplateModelConfig config : configs)
        {
            templateModelConfigService.updateMcTemplateModelConfig(config);
        }
        return success();
    }
}

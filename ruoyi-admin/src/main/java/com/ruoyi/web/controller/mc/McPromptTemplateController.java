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
import com.ruoyi.mc.domain.McPromptTemplate;
import com.ruoyi.mc.service.IMcPromptTemplateService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * MC预提示词模板Controller
 *
 * @author ruoyi
 * @date 2026-01-27
 */
@RestController
@RequestMapping("/mc/prompt")
public class McPromptTemplateController extends BaseController
{
    @Autowired
    private IMcPromptTemplateService mcPromptTemplateService;

    /**
     * 查询MC预提示词模板列表
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:list')")
    @GetMapping("/list")
    public TableDataInfo list(McPromptTemplate mcPromptTemplate)
    {
        startPage();
        List<McPromptTemplate> list = mcPromptTemplateService.selectMcPromptTemplateList(mcPromptTemplate);
        return getDataTable(list);
    }

    /**
     * 导出MC预提示词模板列表
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:export')")
    @Log(title = "MC预提示词模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McPromptTemplate mcPromptTemplate)
    {
        List<McPromptTemplate> list = mcPromptTemplateService.selectMcPromptTemplateList(mcPromptTemplate);
        ExcelUtil<McPromptTemplate> util = new ExcelUtil<McPromptTemplate>(McPromptTemplate.class);
        util.exportExcel(response, list, "MC预提示词模板数据");
    }

    /**
     * 获取MC预提示词模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:query')")
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId)
    {
        return success(mcPromptTemplateService.selectMcPromptTemplateByTemplateId(templateId));
    }

    /**
     * 获取当前启用的模板
     */
    @GetMapping("/active")
    public AjaxResult getActiveTemplate()
    {
        McPromptTemplate template = mcPromptTemplateService.getActiveTemplate();
        if (template == null)
        {
            return error("未找到启用的预提示词模板，请先启用一个模板");
        }
        return success(template);
    }

    /**
     * 获取所有启用的模板列表（用户端使用，无需权限）
     * 支持按材质类型筛选
     */
    @GetMapping("/activeList")
    public AjaxResult getActiveTemplates(McPromptTemplate mcPromptTemplate)
    {
        // 设置查询条件：只查询启用的模板
        mcPromptTemplate.setIsActive("1");
        mcPromptTemplate.setStatus("0");
        List<McPromptTemplate> list = mcPromptTemplateService.selectMcPromptTemplateList(mcPromptTemplate);
        return success(list);
    }

    /**
     * 新增MC预提示词模板
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:add')")
    @Log(title = "MC预提示词模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McPromptTemplate mcPromptTemplate)
    {
        return toAjax(mcPromptTemplateService.insertMcPromptTemplate(mcPromptTemplate));
    }

    /**
     * 修改MC预提示词模板
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:edit')")
    @Log(title = "MC预提示词模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McPromptTemplate mcPromptTemplate)
    {
        return toAjax(mcPromptTemplateService.updateMcPromptTemplate(mcPromptTemplate));
    }

    /**
     * 删除MC预提示词模板
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:remove')")
    @Log(title = "MC预提示词模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds)
    {
        return toAjax(mcPromptTemplateService.deleteMcPromptTemplateByTemplateIds(templateIds));
    }

    /**
     * 启用模板
     */
    @PreAuthorize("@ss.hasPermi('mc:prompt:activate')")
    @Log(title = "启用MC预提示词模板", businessType = BusinessType.UPDATE)
    @PutMapping("/activate/{templateId}")
    public AjaxResult activate(@PathVariable Long templateId)
    {
        return toAjax(mcPromptTemplateService.activateTemplate(templateId));
    }
}

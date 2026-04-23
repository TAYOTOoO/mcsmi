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
import com.ruoyi.mc.domain.McAiModel;
import com.ruoyi.mc.service.IMcAiModelService;
import com.ruoyi.mc.service.IMcAiService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI模型配置Controller
 *
 * @author ruoyi
 * @date 2026-01-26
 */
@RestController
@RequestMapping("/mc/model")
public class McAiModelController extends BaseController
{
    @Autowired
    private IMcAiModelService mcAiModelService;

    @Autowired
    private IMcAiService aiService;

    /**
     * 查询AI模型配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(McAiModel mcAiModel)
    {
        startPage();
        List<McAiModel> list = mcAiModelService.selectMcAiModelList(mcAiModel);
        return getDataTable(list);
    }

    /**
     * 导出AI模型配置列表
     */
    @PreAuthorize("@ss.hasPermi('mc:model:export')")
    @Log(title = "AI模型配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McAiModel mcAiModel)
    {
        List<McAiModel> list = mcAiModelService.selectMcAiModelList(mcAiModel);
        ExcelUtil<McAiModel> util = new ExcelUtil<McAiModel>(McAiModel.class);
        util.exportExcel(response, list, "AI模型配置数据");
    }

    /**
     * 获取AI模型配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:model:query')")
    @GetMapping(value = "/{modelId}")
    public AjaxResult getInfo(@PathVariable("modelId") Long modelId)
    {
        return success(mcAiModelService.selectMcAiModelByModelId(modelId));
    }

    /**
     * 新增AI模型配置
     */
    @PreAuthorize("@ss.hasPermi('mc:model:add')")
    @Log(title = "AI模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McAiModel mcAiModel)
    {
        return toAjax(mcAiModelService.insertMcAiModel(mcAiModel));
    }

    /**
     * 修改AI模型配置
     */
    @PreAuthorize("@ss.hasPermi('mc:model:edit')")
    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McAiModel mcAiModel)
    {
        return toAjax(mcAiModelService.updateMcAiModel(mcAiModel));
    }

    /**
     * 删除AI模型配置
     */
    @PreAuthorize("@ss.hasPermi('mc:model:remove')")
    @Log(title = "AI模型配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable Long[] modelIds)
    {
        return toAjax(mcAiModelService.deleteMcAiModelByModelIds(modelIds));
    }

    /**
     * 测试模型连通性
     */
    @PreAuthorize("@ss.hasPermi('mc:model:test')")
    @GetMapping("/test/{modelId}")
    public AjaxResult testConnection(@PathVariable Long modelId)
    {
        boolean result = aiService.testModelConnection(modelId);
        return result ? success("模型连通性正常") : error("模型连通性测试失败");
    }

    /**
     * 获取可用模型列表
     */
    @PreAuthorize("@ss.hasPermi('mc:model:add')")
    @GetMapping("/fetchModels")
    public AjaxResult fetchModels(String apiBaseUrl, String apiKey)
    {
        if (StringUtils.isEmpty(apiBaseUrl) || StringUtils.isEmpty(apiKey))
        {
            return error("API地址和密钥不能为空");
        }

        try
        {
            return success(aiService.fetchAvailableModels(apiBaseUrl, apiKey));
        }
        catch (Exception e)
        {
            return error(e.getMessage());
        }
    }

    /**
     * 设置启用的模型
     */
    @PreAuthorize("@ss.hasPermi('mc:model:edit')")
    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PutMapping("/setActive/{modelId}")
    public AjaxResult setActiveModel(@PathVariable Long modelId, @RequestBody McAiModel mcAiModel)
    {
        McAiModel model = mcAiModelService.selectMcAiModelByModelId(modelId);
        if (model == null)
        {
            return error("模型不存在");
        }

        // 更新当前模型状态（不再自动禁用同类型其他模型）
        model.setIsActive(mcAiModel.getIsActive());
        return toAjax(mcAiModelService.updateMcAiModel(model));
    }

    /**
     * 获取当前启用的模型
     */
    @GetMapping("/getActiveModels")
    public AjaxResult getActiveModels()
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        // 获取启用的文本模型
        McAiModel textModel = mcAiModelService.getActiveModelByType(2);
        result.put("textModel", textModel);

        // 获取启用的图片模型
        McAiModel imageModel = mcAiModelService.getActiveModelByType(1);
        result.put("imageModel", imageModel);

        return success(result);
    }
}

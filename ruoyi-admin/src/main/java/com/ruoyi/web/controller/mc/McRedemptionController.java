package com.ruoyi.web.controller.mc;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mc.domain.McRedemption;
import com.ruoyi.mc.service.IMcRedemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * 兑换码Controller
 *
 * @author ruoyi
 * @date 2026-02-06
 */
@RestController
@RequestMapping("/mc/redemption")
public class McRedemptionController extends BaseController
{
    @Autowired
    private IMcRedemptionService mcRedemptionService;

    /**
     * 查询兑换码列表
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:list')")
    @GetMapping("/list")
    public TableDataInfo list(McRedemption mcRedemption)
    {
        startPage();
        List<McRedemption> list = mcRedemptionService.selectMcRedemptionList(mcRedemption);
        return getDataTable(list);
    }

    /**
     * 获取兑换码详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(mcRedemptionService.selectMcRedemptionById(id));
    }

    /**
     * 新增兑换码
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:add')")
    @Log(title = "兑换码", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McRedemption mcRedemption)
    {
        mcRedemption.setUserId(SecurityUtils.getUserId());
        return toAjax(mcRedemptionService.insertMcRedemption(mcRedemption));
    }

    /**
     * 批量生成兑换码
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:generate')")
    @Log(title = "批量生成兑换码", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody Map<String, Object> params)
    {
        try
        {
            String name = (String) params.get("name");
            Integer count = Integer.valueOf(params.get("count").toString());
            Long quota = Long.valueOf(params.get("quota").toString());
            Long expiredTime = params.get("expiredTime") != null ? Long.valueOf(params.get("expiredTime").toString()) : null;
            Long userId = SecurityUtils.getUserId();

            List<String> keys = mcRedemptionService.generateRedemptions(name, count, quota, expiredTime, userId);
            return AjaxResult.success("生成成功", keys);
        }
        catch (Exception e)
        {
            logger.error("批量生成兑换码失败", e);
            return AjaxResult.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 修改兑换码
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:edit')")
    @Log(title = "兑换码", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McRedemption mcRedemption)
    {
        return toAjax(mcRedemptionService.updateMcRedemption(mcRedemption));
    }

    /**
     * 删除兑换码
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:remove')")
    @Log(title = "兑换码", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(mcRedemptionService.deleteMcRedemptionByIds(ids));
    }

    /**
     * 导出兑换码为TXT文件
     */
    @PreAuthorize("@ss.hasPermi('mc:redemption:export')")
    @Log(title = "导出兑换码", businessType = BusinessType.EXPORT)
    @PostMapping("/exportTxt")
    public void exportTxt(@RequestBody Long[] ids, HttpServletResponse response)
    {
        try
        {
            List<McRedemption> list = mcRedemptionService.selectMcRedemptionByIds(ids);

            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=redemption_codes_" + System.currentTimeMillis() + ".txt");

            PrintWriter writer = response.getWriter();
            for (McRedemption redemption : list)
            {
                writer.println(redemption.getKey());
            }
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            logger.error("导出兑换码失败", e);
        }
    }

    /**
     * 用户兑换兑换码（用户端接口）
     */
    @PostMapping("/redeem")
    public AjaxResult redeem(@RequestBody Map<String, String> params)
    {
        try
        {
            String key = params.get("key");
            if (key == null || key.trim().isEmpty())
            {
                return AjaxResult.error("请输入兑换码");
            }

            Long userId = SecurityUtils.getUserId();
            mcRedemptionService.redeemCode(key.trim(), userId);
            return AjaxResult.success("兑换成功");
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        catch (Exception e)
        {
            logger.error("兑换失败", e);
            return AjaxResult.error("兑换失败，请稍后重试");
        }
    }
}

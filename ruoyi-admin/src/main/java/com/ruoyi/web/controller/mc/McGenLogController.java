package com.ruoyi.web.controller.mc;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mc.domain.McGenerationLog;
import com.ruoyi.mc.mapper.McGenerationLogMapper;

/**
 * 生成日志Controller
 *
 * @author ruoyi
 * @date 2026-01-27
 */
@RestController
@RequestMapping("/mc/genlog")
public class McGenLogController extends BaseController
{
    @Autowired
    private McGenerationLogMapper generationLogMapper;

    /**
     * 查询生成日志列表
     */
    @PreAuthorize("@ss.hasPermi('mc:genlog:list')")
    @GetMapping("/list")
    public TableDataInfo list(McGenerationLog mcGenerationLog)
    {
        startPage();
        List<McGenerationLog> list = generationLogMapper.selectMcGenerationLogList(mcGenerationLog);
        return getDataTable(list);
    }

    /**
     * 获取生成日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:genlog:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(generationLogMapper.selectMcGenerationLogByLogId(logId));
    }

    /**
     * 获取生成日志统计信息
     */
    @PreAuthorize("@ss.hasPermi('mc:genlog:list')")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        Map<String, Object> stats = new HashMap<>();

        // 查询总调用次数
        Integer totalCalls = generationLogMapper.countTotalCalls();
        stats.put("totalCalls", totalCalls != null ? totalCalls : 0);

        // 查询成功次数
        Integer successCalls = generationLogMapper.countSuccessCalls();
        stats.put("successCalls", successCalls != null ? successCalls : 0);

        // 计算成功率
        if (totalCalls != null && totalCalls > 0) {
            double successRate = (double) successCalls / totalCalls * 100;
            stats.put("successRate", String.format("%.1f", successRate));
        } else {
            stats.put("successRate", "0.0");
        }

        // 查询平均耗时
        Double avgTime = generationLogMapper.avgGenerationTime();
        stats.put("avgTime", avgTime != null ? String.format("%.1f", avgTime) : "0.0");

        // Token消耗（暂时设为0，后续可扩展）
        stats.put("totalTokens", 0);

        return success(stats);
    }
}

package com.ruoyi.web.controller.mc;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mc.domain.McVersion;
import com.ruoyi.mc.service.IMcVersionService;
import com.ruoyi.web.task.MinecraftVersionSyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MC版本Controller
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@RestController
@RequestMapping("/mc/version")
public class McVersionController extends BaseController
{
    @Autowired
    private IMcVersionService versionService;

    @Autowired
    private MinecraftVersionSyncTask versionSyncTask;

    /**
     * 查询版本列表（用户端 - 只返回正常状态的版本）
     */
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<McVersion> list = versionService.selectNormalVersionList();
        return AjaxResult.success(list);
    }

    /**
     * 获取版本详细信息
     */
    @GetMapping(value = "/{versionId}")
    public AjaxResult getInfo(@PathVariable("versionId") Long versionId)
    {
        return AjaxResult.success(versionService.selectMcVersionByVersionId(versionId));
    }

    /**
     * 手动同步Minecraft官方版本列表（仅管理员）
     */
    @PreAuthorize("@ss.hasPermi('mc:version:sync')")
    @PostMapping("/sync")
    public AjaxResult syncVersions()
    {
        try {
            versionSyncTask.manualSync();
            return AjaxResult.success("版本同步任务已触发，请稍后查看结果");
        } catch (Exception e) {
            return AjaxResult.error("同步失败：" + e.getMessage());
        }
    }
}

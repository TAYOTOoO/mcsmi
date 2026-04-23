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
import com.ruoyi.mc.domain.McAnnouncement;
import com.ruoyi.mc.service.IMcAnnouncementService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 公告Controller
 *
 * @author ruoyi
 * @date 2026-02-04
 */
@RestController
@RequestMapping("/mc/announcement")
public class McAnnouncementController extends BaseController
{
    @Autowired
    private IMcAnnouncementService mcAnnouncementService;

    /**
     * 查询公告列表
     */
    @PreAuthorize("@ss.hasPermi('mc:announcement:list')")
    @GetMapping("/list")
    public TableDataInfo list(McAnnouncement mcAnnouncement)
    {
        startPage();
        List<McAnnouncement> list = mcAnnouncementService.selectMcAnnouncementList(mcAnnouncement);
        return getDataTable(list);
    }

    /**
     * 获取用户端有效公告
     */
    @GetMapping("/active")
    public AjaxResult getActive() {
        return AjaxResult.success(mcAnnouncementService.selectActiveAnnouncements());
    }

    /**
     * 导出公告列表
     */
    @PreAuthorize("@ss.hasPermi('mc:announcement:export')")
    @Log(title = "公告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McAnnouncement mcAnnouncement)
    {
        List<McAnnouncement> list = mcAnnouncementService.selectMcAnnouncementList(mcAnnouncement);
        ExcelUtil<McAnnouncement> util = new ExcelUtil<McAnnouncement>(McAnnouncement.class);
        util.exportExcel(response, list, "公告数据");
    }

    /**
     * 获取公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:announcement:query')")
    @GetMapping(value = "/{announcementId}")
    public AjaxResult getInfo(@PathVariable("announcementId") Long announcementId)
    {
        return AjaxResult.success(mcAnnouncementService.selectMcAnnouncementByAnnouncementId(announcementId));
    }

    /**
     * 新增公告
     */
    @PreAuthorize("@ss.hasPermi('mc:announcement:add')")
    @Log(title = "公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McAnnouncement mcAnnouncement)
    {
        mcAnnouncement.setCreateBy(getUsername());
        return toAjax(mcAnnouncementService.insertMcAnnouncement(mcAnnouncement));
    }

    /**
     * 修改公告
     */
    @PreAuthorize("@ss.hasPermi('mc:announcement:edit')")
    @Log(title = "公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McAnnouncement mcAnnouncement)
    {
        mcAnnouncement.setUpdateBy(getUsername());
        return toAjax(mcAnnouncementService.updateMcAnnouncement(mcAnnouncement));
    }

    /**
     * 删除公告
     */
    @PreAuthorize("@ss.hasPermi('mc:announcement:remove')")
    @Log(title = "公告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{announcementIds}")
    public AjaxResult remove(@PathVariable Long[] announcementIds)
    {
        return toAjax(mcAnnouncementService.deleteMcAnnouncementByAnnouncementIds(announcementIds));
    }
}

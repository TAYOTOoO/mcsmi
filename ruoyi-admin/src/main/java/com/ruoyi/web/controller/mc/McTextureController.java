package com.ruoyi.web.controller.mc;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mc.domain.McTextureRecord;
import com.ruoyi.mc.mapper.McTextureRecordMapper;

/**
 * 材质记录Controller
 *
 * @author ruoyi
 * @date 2026-01-28
 */
@RestController
@RequestMapping("/mc/texture")
public class McTextureController extends BaseController
{
    @Autowired
    private McTextureRecordMapper textureRecordMapper;

    /**
     * 查询任务的材质列表（用户端：开放访问）
     */
    @GetMapping("/list/{taskId}")
    public TableDataInfo listByTaskId(@PathVariable Long taskId)
    {
        startPage();
        List<McTextureRecord> list = textureRecordMapper.selectMcTextureRecordByTaskId(taskId);
        return getDataTable(list);
    }

    /**
     * 获取材质记录详细信息（用户端：开放访问）
     */
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId)
    {
        return success(textureRecordMapper.selectMcTextureRecordByRecordId(recordId));
    }
}

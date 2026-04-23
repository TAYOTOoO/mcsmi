package com.ruoyi.web.controller.mc;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mc.domain.CropRequest;
import com.ruoyi.mc.domain.CropResult;
import com.ruoyi.mc.service.IMcImageCropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * MC图片裁剪Controller
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@RestController
@RequestMapping("/mc/crop")
public class McImageCropController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(McImageCropController.class);

    @Autowired
    private IMcImageCropService imageCropService;

    /**
     * 裁剪图片并打包下载
     *
     * @param request 裁剪请求
     * @return 裁剪结果（包含ZIP下载地址）
     */
    @Log(title = "图片裁剪打包", businessType = BusinessType.OTHER)
    @PostMapping("/split-and-pack")
    public AjaxResult splitAndPack(@RequestBody CropRequest request)
    {
        log.info("收到裁剪请求: {}", request);

        // 参数校验
        if (request.getTaskId() == null)
        {
            return AjaxResult.error("任务ID不能为空");
        }

        if (request.getVersionName() == null || request.getVersionName().isEmpty())
        {
            return AjaxResult.error("版本号不能为空");
        }

        if (request.getHorizontalLines() == null)
        {
            request.setHorizontalLines(new java.util.ArrayList<>());
        }

        if (request.getVerticalLines() == null)
        {
            request.setVerticalLines(new java.util.ArrayList<>());
        }

        // 限制切片数量
        int rows = request.getHorizontalLines().size() + 1;
        int cols = request.getVerticalLines().size() + 1;
        int totalCount = rows * cols;

        if (totalCount > 100)
        {
            return AjaxResult.error("切片数量过多（最多100张），当前: " + totalCount);
        }

        try
        {
            // 执行裁剪打包
            CropResult result = imageCropService.cropAndPackage(request);

            log.info("裁剪打包完成: {}", result);
            return AjaxResult.success("裁剪打包成功", result);
        }
        catch (Exception e)
        {
            log.error("裁剪打包失败", e);
            return AjaxResult.error("裁剪打包失败: " + e.getMessage());
        }
    }
}

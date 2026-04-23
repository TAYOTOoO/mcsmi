package com.ruoyi.web.controller.mc;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mc.service.IMcCroppedImageTempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 裁剪图片临时存储Controller
 *
 * @author ruoyi
 * @date 2026-02-03
 */
@RestController
@RequestMapping("/mc/cropped-temp")
public class McCroppedImageTempController extends BaseController
{
    @Autowired
    private IMcCroppedImageTempService croppedImageTempService;

    /**
     * 批量上传裁剪图片
     *
     * @param files 图片文件数组
     * @param taskId 任务ID
     * @return batchId
     */
    @PostMapping("/upload")
    public AjaxResult uploadCroppedImages(
        @RequestParam("files") MultipartFile[] files,
        @RequestParam("taskId") Long taskId
    )
    {
        String batchId = croppedImageTempService.uploadCroppedImages(files, taskId);
        return AjaxResult.success("上传成功", batchId);
    }

    /**
     * 获取批次信息
     *
     * @param batchId 批次ID
     * @return 批次信息（包含图片列表）
     */
    @GetMapping("/batch/{batchId}")
    public AjaxResult getBatchInfo(@PathVariable String batchId)
    {
        Map<String, Object> batchInfo = croppedImageTempService.getBatchInfo(batchId);
        return AjaxResult.success(batchInfo);
    }

    /**
     * 获取批次的图片列表
     *
     * @param batchId 批次ID
     * @return 图片URL列表
     */
    @GetMapping("/images/{batchId}")
    public AjaxResult getCroppedImages(@PathVariable String batchId)
    {
        List<String> imageUrls = croppedImageTempService.getCroppedImages(batchId);
        return AjaxResult.success(imageUrls);
    }

    /**
     * 删除批次（用户完成编辑后调用）
     *
     * @param batchId 批次ID
     * @return 成功信息
     */
    @DeleteMapping("/{batchId}")
    public AjaxResult deleteBatch(@PathVariable String batchId)
    {
        croppedImageTempService.deleteBatch(batchId);
        return AjaxResult.success("删除成功");
    }

    /**
     * 手动触发清理过期批次（管理员用）
     *
     * @return 清理数量
     */
    @PostMapping("/cleanup")
    public AjaxResult cleanupExpired()
    {
        int count = croppedImageTempService.cleanupExpiredBatches();
        return AjaxResult.success("清理完成", count);
    }
}

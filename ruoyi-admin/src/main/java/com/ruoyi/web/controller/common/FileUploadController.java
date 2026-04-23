package com.ruoyi.web.controller.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@RestController
@RequestMapping("/common/upload")
public class FileUploadController {
    
    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
    
    @Value("${mc.upload.path:D:/mc/uploads}")
    private String uploadPath;
    
    @PostMapping("/referenceImage")
    public AjaxResult uploadReferenceImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择文件");
        }
        
        try {
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return AjaxResult.error("只支持图片文件");
            }
            
            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            File destFile = new File(uploadDir, filename);
            file.transferTo(destFile);
            
            log.info("参考图片上传成功: {}", destFile.getAbsolutePath());
            
            // 返回文件名（不是完整路径）
            return AjaxResult.success("上传成功", filename);
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return AjaxResult.error("上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 访问上传的参考图片
     */
    @GetMapping("/referenceImage/{filename}")
    public void getReferenceImage(@PathVariable String filename, HttpServletResponse response) {
        try {
            File file = new File(uploadPath, filename);
            if (!file.exists()) {
                response.setStatus(404);
                return;
            }
            
            // 设置响应类型
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            String contentType = "image/" + extension;
            if (extension.equals("jpg")) {
                contentType = "image/jpeg";
            }
            response.setContentType(contentType);
            
            // 输出文件
            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            log.error("读取图片失败", e);
            response.setStatus(500);
        }
    }
}

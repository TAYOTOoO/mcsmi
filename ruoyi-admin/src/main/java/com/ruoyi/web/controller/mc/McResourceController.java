package com.ruoyi.web.controller.mc;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.mc.domain.McGenerationTask;
import com.ruoyi.mc.service.IMcGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * MC资源访问控制器（带鉴权）
 * 用于访问用户生成的材质包、图片等私有资源
 *
 * @author ruoyi
 * @date 2026-02-09
 */
@RestController
@RequestMapping("/mc/resource")
public class McResourceController
{
    private static final Logger log = LoggerFactory.getLogger(McResourceController.class);

    @Autowired
    private IMcGenerationService generationService;

    @Value("${mc.temp.path:/www/wwwroot/mc/temp}")
    private String mcTempPath;

    @Value("${mc.textures.path:/www/wwwroot/mc/textures}")
    private String mcTexturesPath;

    @Value("${mc.output.path:/www/wwwroot/mc/output}")
    private String mcOutputPath;

    /**
     * 访问生成的临时图片（带鉴权）
     * 路径格式：/mc/resource/temp?path=xxx.png&taskId=123
     */
    @GetMapping("/temp")
    public void accessTempImage(@RequestParam String path, @RequestParam Long taskId, HttpServletResponse response)
    {
        accessResource(mcTempPath, path, taskId, response, false);
    }

    /**
     * 访问生成的材质图片（带鉴权）
     * 路径格式：/mc/resource/texture?path=xxx.png&taskId=123
     */
    @GetMapping("/texture")
    public void accessTexture(@RequestParam String path, @RequestParam Long taskId, HttpServletResponse response)
    {
        accessResource(mcTexturesPath, path, taskId, response, false);
    }

    /**
     * 下载材质包ZIP（带鉴权）
     * 路径格式：/mc/resource/download?path=xxx.zip&taskId=123
     */
    @GetMapping("/download")
    public void downloadPackage(@RequestParam String path, @RequestParam Long taskId, HttpServletResponse response)
    {
        accessResource(mcOutputPath, path, taskId, response, true);
    }

    /**
     * 统一的资源访问逻辑（带权限验证）
     */
    private void accessResource(String basePath, String path, Long taskId, HttpServletResponse response, boolean isDownload)
    {
        try
        {
            // 1. 验证参数
            if (StringUtils.isEmpty(path) || taskId == null)
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 2. 防止路径穿越攻击
            if (path.contains("..") || path.contains("\\"))
            {
                log.warn("检测到路径穿越攻击: {}", path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 3. 查询任务信息
            McGenerationTask task = generationService.selectMcGenerationTaskByTaskId(taskId);
            if (task == null)
            {
                log.warn("任务不存在: taskId={}", taskId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 4. 权限验证：必须是任务所有者或管理员
            Long currentUserId = SecurityUtils.getUserId();
            if (!task.getUserId().equals(currentUserId) && !SecurityUtils.isAdmin(currentUserId))
            {
                log.warn("用户{}无权访问任务{}的资源", currentUserId, taskId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // 5. 构建文件完整路径
            String filePath = basePath + File.separator + path;
            File file = new File(filePath);

            // 6. 验证文件存在
            if (!file.exists() || !file.isFile())
            {
                log.warn("文件不存在: {}", filePath);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 7. 设置响应头
            String fileName = file.getName();
            String extension = StringUtils.substringAfterLast(fileName, ".");

            if (isDownload)
            {
                // 下载模式：设置为附件
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            }
            else
            {
                // 预览模式：根据文件类型设置Content-Type
                if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension))
                {
                    response.setContentType("image/jpeg");
                }
                else if ("png".equalsIgnoreCase(extension))
                {
                    response.setContentType("image/png");
                }
                else if ("gif".equalsIgnoreCase(extension))
                {
                    response.setContentType("image/gif");
                }
                else if ("webp".equalsIgnoreCase(extension))
                {
                    response.setContentType("image/webp");
                }
                else
                {
                    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                }
            }

            // 8. 允许跨域访问（用于前端预览）
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Authorization");

            // 9. 设置文件大小
            response.setContentLengthLong(file.length());

            // 10. 读取文件并写入响应流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream())
            {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1)
                {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            log.info("用户{}成功访问任务{}的资源: {}", currentUserId, taskId, path);
        }
        catch (IOException e)
        {
            log.error("读取文件失败: path={}", path, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (Exception e)
        {
            log.error("访问资源失败: path={}", path, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

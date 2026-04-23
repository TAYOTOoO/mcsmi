package com.ruoyi.web.controller.mc;

import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Minecraft 下载代理控制器
 */
@RestController
@RequestMapping("/mc/download")
public class McDownloadProxyController {

    /**
     * 代理下载 client.jar
     * 避免前端 CORS 问题
     */
    @PostMapping("/proxy")
    public ResponseEntity<byte[]> proxyDownload(@RequestBody Map<String, String> params) {
        String downloadUrl = params.get("url");
        String versionId = params.get("versionId");

        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 创建 HTTP 连接
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000); // 30秒连接超时
            connection.setReadTimeout(300000);   // 5分钟读取超时
            connection.setRequestProperty("User-Agent", "MCTextureWorkshop/1.0");

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return ResponseEntity.status(responseCode).build();
            }

            // 获取文件大小
            long contentLength = connection.getContentLengthLong();

            // 读取文件内容
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] data = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    // 每 1MB 打印一次进度（用于调试）
                    if (totalBytesRead % (1024 * 1024) == 0) {
                        System.out.println("Downloaded: " + (totalBytesRead / (1024 * 1024)) + " MB");
                    }
                }
            }

            byte[] fileContent = buffer.toByteArray();

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(fileContent.length);
            headers.setContentDispositionFormData("attachment", versionId + ".jar");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取下载进度（可选，用于大文件）
     */
    @GetMapping("/progress/{taskId}")
    public AjaxResult getProgress(@PathVariable String taskId) {
        // TODO: 实现下载进度跟踪
        return AjaxResult.success();
    }
}

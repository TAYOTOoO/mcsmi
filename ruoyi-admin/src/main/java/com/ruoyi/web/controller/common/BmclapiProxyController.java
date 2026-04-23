package com.ruoyi.web.controller.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * BMCLAPI 反向代理控制器
 * 解决前端跨域问题
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bmclapi")
public class BmclapiProxyController {
    
    private static final Logger log = LoggerFactory.getLogger(BmclapiProxyController.class);
    
    private static final String BMCLAPI_BASE_URL = "https://bmclapi2.bangbang93.com";
    private static final int MAX_REDIRECTS = 5;
    
    /**
     * 代理所有 BMCLAPI 请求
     */
    @GetMapping("/**")
    public void proxyGet(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI().replace("/bmclapi", "");
        String queryString = request.getQueryString();
        
        String targetUrl = BMCLAPI_BASE_URL + path;
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl += "?" + queryString;
        }
        
        log.info("代理请求: {} -> {}", request.getRequestURI(), targetUrl);
        
        try {
            proxyRequest(targetUrl, response, 0);
        } catch (Exception e) {
            log.error("代理请求失败: {}", targetUrl, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 执行代理请求，支持自动跟随重定向
     */
    private void proxyRequest(String targetUrl, HttpServletResponse response, int redirectCount) throws IOException {
        if (redirectCount >= MAX_REDIRECTS) {
            throw new IOException("重定向次数过多");
        }
        
        URL url = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setInstanceFollowRedirects(false);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                
                String location = connection.getHeaderField("Location");
                if (location == null) {
                    throw new IOException("重定向响应缺少 Location 头");
                }
                
                if (location.startsWith("/")) {
                    location = BMCLAPI_BASE_URL + location;
                }
                
                log.info("跟随重定向: {} -> {}", targetUrl, location);
                connection.disconnect();
                proxyRequest(location, response, redirectCount + 1);
                return;
            }
            
            response.setStatus(responseCode);
            
            String contentType = connection.getContentType();
            if (contentType != null) {
                response.setContentType(contentType);
            }
            
            long contentLength = connection.getContentLengthLong();
            if (contentLength > 0) {
                response.setContentLengthLong(contentLength);
            }
            
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            try (InputStream in = connection.getInputStream();
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } finally {
            connection.disconnect();
        }
    }
}

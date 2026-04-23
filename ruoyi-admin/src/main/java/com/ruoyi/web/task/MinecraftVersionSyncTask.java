package com.ruoyi.web.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.mc.domain.McVersion;
import com.ruoyi.mc.mapper.McVersionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Minecraft版本同步定时任务
 * 每天凌晨2点从Minecraft官方API同步版本列表
 *
 * @author ruoyi
 */
@Component("minecraftVersionSyncTask")
public class MinecraftVersionSyncTask {

    private static final Logger log = LoggerFactory.getLogger(MinecraftVersionSyncTask.class);

    @Autowired
    private McVersionMapper versionMapper;

    /**
     * Minecraft官方版本清单API
     */
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    /**
     * 资源包格式版本映射表（根据Minecraft官方wiki）
     * https://minecraft.fandom.com/wiki/Pack_format
     */
    private static final Map<String, Integer> PACK_FORMAT_MAP = new HashMap<>();

    static {
        // 1.20.2 - 1.20.4
        PACK_FORMAT_MAP.put("1.20.2", 18);
        PACK_FORMAT_MAP.put("1.20.3", 18);
        PACK_FORMAT_MAP.put("1.20.4", 18);

        // 1.20.5+
        PACK_FORMAT_MAP.put("1.20.5", 32);
        PACK_FORMAT_MAP.put("1.20.6", 32);

        // 1.21+
        PACK_FORMAT_MAP.put("1.21", 34);
        PACK_FORMAT_MAP.put("1.21.1", 34);
        PACK_FORMAT_MAP.put("1.21.2", 41);
        PACK_FORMAT_MAP.put("1.21.3", 41);
        PACK_FORMAT_MAP.put("1.21.4", 42);

        // 1.19系列
        PACK_FORMAT_MAP.put("1.19", 9);
        PACK_FORMAT_MAP.put("1.19.1", 9);
        PACK_FORMAT_MAP.put("1.19.2", 9);
        PACK_FORMAT_MAP.put("1.19.3", 12);
        PACK_FORMAT_MAP.put("1.19.4", 13);

        // 1.18系列
        PACK_FORMAT_MAP.put("1.18", 8);
        PACK_FORMAT_MAP.put("1.18.1", 8);
        PACK_FORMAT_MAP.put("1.18.2", 8);

        // 1.17系列
        PACK_FORMAT_MAP.put("1.17", 7);
        PACK_FORMAT_MAP.put("1.17.1", 7);

        // 1.16系列
        PACK_FORMAT_MAP.put("1.16", 5);
        PACK_FORMAT_MAP.put("1.16.1", 5);
        PACK_FORMAT_MAP.put("1.16.2", 6);
        PACK_FORMAT_MAP.put("1.16.3", 6);
        PACK_FORMAT_MAP.put("1.16.4", 6);
        PACK_FORMAT_MAP.put("1.16.5", 6);
    }

    /**
     * 每天凌晨2点执行同步任务
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncMinecraftVersions() {
        log.info("开始同步Minecraft版本列表...");

        try {
            // 1. 从官方API获取版本列表
            JSONObject manifest = fetchVersionManifest();
            if (manifest == null) {
                log.error("获取Minecraft版本清单失败");
                return;
            }

            JSONArray versions = manifest.getJSONArray("versions");
            if (versions == null || versions.isEmpty()) {
                log.warn("版本清单为空");
                return;
            }

            // 2. 获取数据库中已有的版本
            List<McVersion> existingVersions = versionMapper.selectAllVersions();
            Set<String> existingVersionNames = existingVersions.stream()
                    .map(McVersion::getVersionName)
                    .collect(Collectors.toSet());

            // 3. 筛选release版本并过滤已存在的版本
            List<McVersion> newVersions = new ArrayList<>();
            for (int i = 0; i < versions.size(); i++) {
                JSONObject versionObj = versions.getJSONObject(i);
                String versionType = versionObj.getString("type");
                String versionName = versionObj.getString("id");

                // 只同步正式版本（release），不同步快照版本（snapshot）
                if (!"release".equals(versionType)) {
                    continue;
                }

                // 过滤已存在的版本
                if (existingVersionNames.contains(versionName)) {
                    continue;
                }

                // 只同步1.16及以上版本
                if (!isVersionSupported(versionName)) {
                    continue;
                }

                // 创建新版本对象
                McVersion version = new McVersion();
                version.setVersionName(versionName);
                version.setVersionCode(versionName.replace(".", "_"));
                version.setPackFormat(getPackFormat(versionName));
                version.setDescription("Minecraft " + versionName + " 正式版");
                version.setStatus("0"); // 0=正常，1=停用
                version.setCreateBy("system");

                newVersions.add(version);
            }

            // 4. 批量插入新版本
            if (!newVersions.isEmpty()) {
                int insertCount = versionMapper.batchInsertVersions(newVersions);
                log.info("同步完成，新增 {} 个版本", insertCount);

                // 记录新增的版本
                newVersions.forEach(v ->
                        log.info("新增版本: {} (pack_format={})", v.getVersionName(), v.getPackFormat())
                );
            } else {
                log.info("没有新版本需要同步");
            }

        } catch (Exception e) {
            log.error("同步Minecraft版本列表失败", e);
        }
    }

    /**
     * 从Minecraft官方API获取版本清单
     */
    private JSONObject fetchVersionManifest() {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(VERSION_MANIFEST_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("User-Agent", "MinecraftTextureWorkshop/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("请求失败，HTTP状态码: {}", responseCode);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return JSON.parseObject(response.toString());

        } catch (Exception e) {
            log.error("请求Minecraft官方API失败", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 判断版本是否支持（1.16及以上）
     */
    private boolean isVersionSupported(String versionName) {
        try {
            String[] parts = versionName.split("\\.");
            if (parts.length < 2) {
                return false;
            }

            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);

            // 1.16及以上版本
            if (major > 1) {
                return true;
            }
            if (major == 1 && minor >= 16) {
                return true;
            }

            return false;
        } catch (Exception e) {
            log.warn("版本号格式异常: {}", versionName);
            return false;
        }
    }

    /**
     * 获取资源包格式版本号
     */
    private Integer getPackFormat(String versionName) {
        // 精确匹配
        if (PACK_FORMAT_MAP.containsKey(versionName)) {
            return PACK_FORMAT_MAP.get(versionName);
        }

        // 尝试匹配主版本号（例如：1.21.5 匹配 1.21）
        String[] parts = versionName.split("\\.");
        if (parts.length >= 2) {
            String majorMinor = parts[0] + "." + parts[1];
            if (PACK_FORMAT_MAP.containsKey(majorMinor)) {
                return PACK_FORMAT_MAP.get(majorMinor);
            }
        }

        // 默认返回最新的pack format
        log.warn("未找到版本 {} 的pack_format映射，使用默认值 42", versionName);
        return 42;
    }

    /**
     * 手动触发同步（用于测试）
     */
    public void manualSync() {
        log.info("手动触发版本同步");
        syncMinecraftVersions();
    }
}

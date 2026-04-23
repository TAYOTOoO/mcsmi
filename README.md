# 材质工坊 - Minecraft 材质包 AI 生成平台

> 📝 **项目说明**
>
> **Important**
>
> - 本项目仅供个人学习使用，不保证稳定性，且不提供任何技术支持
> - 使用者必须在遵循相关模型服务提供商的使用条款以及法律法规的情况下使用，不得用于非法用途
> - 根据《生成式人工智能服务管理暂行办法》的要求，请勿对中国地区公众提供一切未经备案的生成式人工智能服务

基于 AI 的 Minecraft 材质包一站式生成与编辑平台。输入一段文字描述，自动生成完整材质包；内置画布编辑器、多版本格式转换器和完整的订阅管理系统。
QQ交流群：796956602

## 功能介绍

### AI 智能生成
- 输入文字描述，调用 AI 图像模型生成材质大图
- 自动切割为 50 张 32×32 像素贴图，直接可用于 Minecraft 材质包
- 支持多种生成风格与提示词模板

### 材质编辑器
- 画布编辑：框选、图层管理、撤销/重做
- 智能去除毛边（订阅功能）
- 清除文字水印（订阅功能，基于图像特征分析）
- 一键导出标准 `.zip` 格式材质包

### 版本转换器
- 支持 Minecraft 1.6 ~ 1.21.4（pack_format 1 ~ 46）双向转换
- 自动处理贴图重命名、目录结构变更等兼容性问题

### 用户与商业系统
- 积分充值、订阅套餐、兑换码
- 用户任务中心，实时查看生成进度
- 完整管理后台：AI 模型配置、用户管理、订单管理、公告管理

---

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 2.5.15 · Java 8 · MyBatis · 若依框架 3.9.1 |
| 数据库 | MySQL 8.0+ · Redis 6+ |
| 用户前端 | Vue 3 · Vite · Element Plus · Pinia |
| 管理后台 | Vue 2 · Element UI · Vue CLI |

---

## 项目结构

```
.
├── ruoyi-admin/        # 后台主入口（Spring Boot 启动模块，含所有 Controller）
├── ruoyi-mc/           # MC 核心业务模块（支付/订单 domain/mapper/service）
├── ruoyi-framework/    # 框架核心（Security、过滤器、拦截器）
├── ruoyi-system/       # 若依系统模块（用户、角色、菜单）
├── ruoyi-common/       # 通用工具
├── ruoyi-generator/    # 代码生成器
├── ruoyi-quartz/       # 定时任务
├── mc-user-web/        # 用户端前端（Vue 3）
├── ruoyi-ui/           # 管理后台前端（Vue 2）
├── sql/                # 数据库初始化脚本
└── nginx.conf          # Nginx 参考配置
```

---

## 本地开发部署

### 环境要求

| 工具 | 版本要求 |
|---|---|
| JDK | 1.8+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 16+ |
| npm | 8+ |

### 第一步：准备数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE mc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入基础数据
mysql -u root -p mc < sql/mc.sql
```

### 第二步：配置后端

编辑 `ruoyi-admin/src/main/resources/application-dev.yml`，修改数据库和 Redis 连接：

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://127.0.0.1:3306/mc?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
        username: root
        password: 你的数据库密码
  redis:
    host: 127.0.0.1
    port: 6379
    password: 你的Redis密码（无密码则留空）

mc:
  temp:
    path: /tmp/mc/temp        # 生成临时文件目录
  textures:
    path: /tmp/mc/textures    # 材质文件目录
  output:
    path: /tmp/mc/output      # 导出目录

ruoyi:
  profile: /tmp/mc            # 上传文件根目录
```

创建所需目录：

```bash
mkdir -p /tmp/mc/temp /tmp/mc/textures /tmp/mc/output
```

### 第三步：启动后端

```bash
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar --spring.profiles.active=dev
```

后端运行在 `http://localhost:8080`

### 第四步：启动用户前端

```bash
cd mc-user-web
npm install
npm run dev
# 访问 http://localhost:3000
```

### 第五步：启动管理后台

```bash
cd ruoyi-ui
npm install
npm run dev
# 访问 http://localhost:80
# 默认账号：admin / admin123
```

### 第六步：配置 AI 模型

1. 登录管理后台（`/admin`）
2. 进入「材质工坊」→「AI 模型配置」
3. 填入 AI 服务的 API 地址和密钥（兼容 OpenAI 图像 API 格式）

---

## 服务器生产部署

### 环境要求

- Linux 服务器（推荐 Ubuntu 22.04 / CentOS 7+）
- JDK 8、Maven、Node.js 16+、Nginx、MySQL 8、Redis

### 第一步：数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE mc CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p mc < sql/mc.sql
```

### 第二步：配置生产环境

编辑 `ruoyi-admin/src/main/resources/application-prod.yml`，填写生产环境数据库、Redis 连接信息。

**安全注意事项：**
- 不要将含有真实密码的配置文件提交到代码仓库
- 推荐使用环境变量或配置中心管理敏感信息
- 建议更换 `application.yml` 中的 Token 密钥为强随机字符串

### 第三步：构建后端 jar 包

```bash
mvn clean package -DskipTests -P prod
```

产物位于 `ruoyi-admin/target/ruoyi-admin.jar`，将其上传到服务器。

### 第四步：构建前端静态文件

**构建用户端：**

```bash
cd mc-user-web
npm install
npm run build
# 产物：mc-user-web/dist/
```

**构建管理后台：**

```bash
cd ruoyi-ui
npm install
npm run build:prod
# 产物：ruoyi-ui/dist-admin/
```

**合并到部署目录（Linux）：**

```bash
mkdir -p /www/wwwroot/mc/admin

# 用户端放根目录
cp -r mc-user-web/dist/* /www/wwwroot/mc/

# 管理端放 /admin 子目录
cp -r ruoyi-ui/dist-admin/* /www/wwwroot/mc/admin/
```

### 第五步：创建文件存储目录

```bash
mkdir -p /data/mc/temp /data/mc/textures /data/mc/output /data/mc/upload
chown -R www:www /data/mc  # 根据实际运行用户调整
```

在 `application-prod.yml` 中将路径改为上述目录：

```yaml
mc:
  temp:
    path: /data/mc/temp
  textures:
    path: /data/mc/textures
  output:
    path: /data/mc/output
ruoyi:
  profile: /data/mc/upload
```

### 第六步：配置 Nginx

将项目根目录的 `nginx.conf` 复制到服务器，将其中的 `your-domain.com` 替换为实际域名后启用。

**申请 SSL 证书（Let's Encrypt）：**

```bash
# Ubuntu
apt install certbot python3-certbot-nginx
# CentOS
yum install certbot python3-certbot-nginx

# 申请证书并自动写入 Nginx 配置
certbot --nginx -d your-domain.com -d www.your-domain.com
```

**Nginx 配置说明：**

```nginx
# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    root /www/wwwroot/mc/dist;   # 替换为前端构建产物路径

    ssl_certificate     /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    client_max_body_size 20m;

    # MC 材质资源代理（优先级最高，放最前）
    location /mc/ {
        proxy_pass http://127.0.0.1:8080/mc/;
        proxy_connect_timeout 600s;
        proxy_read_timeout 600s;
    }

    # 用户端 API
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
    }

    # 管理端 API
    location ^~ /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;
    }

    # 上传文件访问
    location ^~ /profile/ {
        alias /data/mc/upload/;   # 替换为实际上传目录
        autoindex off;
    }

    # 管理后台路由
    location /admin/ {
        try_files $uri $uri/ /admin/index.html;
    }

    # 用户端路由
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

完整配置（含静态资源缓存、HSTS、日志等）见项目根目录 `nginx.conf`。

```bash
nginx -t && nginx -s reload
```

### 第七步：启动后端服务

```bash
# 前台运行（测试用）
java -jar ruoyi-admin.jar --spring.profiles.active=prod

# 后台运行（推荐配合 systemd）
nohup java -jar ruoyi-admin.jar --spring.profiles.active=prod > /var/log/mc-app.log 2>&1 &
```

**推荐使用 systemd 管理服务：**

```ini
# /etc/systemd/system/mc-app.service
[Unit]
Description=MC材质工坊后端
After=network.target mysql.service redis.service

[Service]
User=www
ExecStart=/usr/bin/java -jar /opt/mc/ruoyi-admin.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable mc-app
systemctl start mc-app
systemctl status mc-app
```

### 第八步：验证部署

| 地址 | 说明 |
|---|---|
| `http://your-domain.com/` | 用户端 |
| `http://your-domain.com/admin` | 管理后台 |
| `http://your-domain.com/api/login` | 后端接口（应返回 JSON） |

---

## 版本转换支持范围

| pack_format | Minecraft 版本 |
|---|---|
| 1 | 1.6 – 1.8 |
| 2 | 1.9 – 1.10 |
| 3 | 1.11 – 1.12 |
| 4 | 1.13 – 1.14 |
| 5 | 1.15 – 1.16.1 |
| 6 | 1.16.2 – 1.16.5 |
| 7 | 1.17 |
| 8 | 1.18 |
| 9 | 1.19 – 1.19.2 |
| 12 | 1.19.3 |
| 13 | 1.19.4 |
| 15 | 1.20 – 1.20.1 |
| 18 | 1.20.2 |
| 32 | 1.20.5 – 1.20.6 |
| 34 | 1.21 – 1.21.1 |
| 42 | 1.21.2 – 1.21.3 |
| 46 | 1.21.4 |

---

## 常见问题

**Q: 启动后访问页面空白？**  
检查 Nginx 的 `root` 路径是否正确指向前端构建产物目录，以及 `try_files` 配置是否生效。

**Q: 图片无法显示/上传失败？**  
检查 `ruoyi.profile` 配置的目录是否存在，运行 Java 进程的用户是否有写入权限。

**Q: AI 生成任务一直等待？**  
检查「AI 模型配置」中的 API 地址和密钥是否填写正确，以及服务器能否访问该 AI 服务。

**Q: 管理后台登录失败？**  
默认账号 `admin`，密码 `admin123`。如已修改，请查询 `sys_user` 表中的账号信息。

---

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可证

| 内容 | 许可证 |
|---|---|
| 程序代码 | [GPL-3.0](https://www.gnu.org/licenses/gpl-3.0.html) |
| 皮肤素材 / 图片资源 | [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/) |

本项目基于[若依框架](https://gitee.com/y_project/RuoYi)二次开发，代码遵循 GPL-3.0，图片资源遵循 CC BY-NC-SA 4.0。详见 [LICENSE](LICENSE)。

---

Copyright © 2024 MCSMI (Minecraft Character Skin Medical Institute). All rights reserved.

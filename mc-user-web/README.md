# MC材质工坊 - 用户端前端

> 基于 Vue 3 + Vite + Element Plus 的Minecraft材质包AI生成平台用户端

## ✨ 特性

- 🎨 **AI智能生成** - 使用Gemini AI根据描述生成独特材质包
- 🖼️ **精准切割** - 自动将大图切割成50张32×32像素材质
- ⚡ **快速处理** - 智能背景去除，保留完美边缘
- 📦 **一键导出** - 支持导出ZIP格式材质包
- 📱 **移动适配** - 完美支持移动端访问
- 🎯 **实时更新** - 任务状态自动刷新

## 🚀 快速开始

### 环境要求

- Node.js 14+
- npm 或 yarn

### 安装依赖

```bash
cd mc-user-web
npm install
```

### 开发运行

```bash
npm run dev
```

访问: http://localhost:3000

### 生产构建

```bash
npm run build
```

构建产物在 `dist` 目录

## 📁 项目结构

```
mc-user-web/
├── src/
│   ├── api/               # API接口
│   │   └── generation.js  # 材质生成API
│   ├── assets/           # 静态资源
│   │   └── styles/       # 全局样式
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── utils/            # 工具函数
│   │   └── request.js    # axios封装
│   ├── views/            # 页面组件
│   │   ├── Home.vue      # 首页
│   │   ├── Generate.vue  # 生成页面
│   │   ├── MyTasks.vue   # 任务列表
│   │   ├── Result.vue    # 结果查看
│   │   └── Login.vue     # 登录页
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── vite.config.js        # Vite配置
└── package.json
```

## 🔗 后端接口

本项目通过Vite代理连接到后端服务（默认 http://localhost:8080）

主要接口：
- `GET /mc/generate/list` - 查询任务列表
- `POST /mc/generate` - 创建生成任务
- `GET /mc/generate/:id` - 查询任务详情
- `POST /mc/generate/regenerate/:id` - 重新生成
- `GET /mc/points/my` - 查询用户积分
- `GET /mc/texture/list/:taskId` - 查询任务材质列表

## 🎯 主要功能

### 1. 首页 (/)
- Hero区域展示
- 特性介绍
- 使用流程说明

### 2. 生成页面 (/generate)
- 风格描述输入
- MC版本选择
- 材质模板选择
- AI模型选择
- 积分显示
- 风格示例快速应用

### 3. 任务列表 (/my-tasks)
- 任务状态实时显示
- 自动刷新（10秒）
- 支持重新生成
- 查看结果跳转

### 4. 结果查看 (/result/:taskId)
- 材质网格预览（10×5）
- 单张材质放大查看
- ZIP导出功能

## 🎨 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 4
- **UI组件**: Element Plus 2.3
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **样式**: SCSS

## 🔧 配置说明

### API代理配置

编辑 `vite.config.js`:

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 📝 注意事项

1. **后端服务**: 确保后端服务（RuoYi）已启动在 http://localhost:8080
2. **登录认证**: 目前使用localStorage存储token
3. **文件访问**: 材质图片通过 `/api/file/texture/` 路径访问
4. **移动端**: 已适配移动端，网格自动调整列数

## 🆚 与管理后台的区别

| 项目 | 用户端 (本项目) | 管理后台 (ruoyi-ui) |
|------|----------------|-------------------|
| 技术栈 | Vue 3 + Vite | Vue 2 + Webpack |
| 端口 | 3000 | 80 |
| 用途 | 普通用户生成材质 | 管理员配置系统 |
| 功能 | 生成、查看、导出 | AI模型配置、用户管理 |
| 登录 | 简化登录 | 完整权限管理 |

## 🔐 部署

### 开发环境

```bash
npm run dev
```

### 生产环境

1. 构建项目：
```bash
npm run build
```

2. 将 `dist` 目录部署到Web服务器（Nginx/Apache）

3. 配置反向代理，将 `/api` 请求转发到后端：

**Nginx示例**:
```nginx
location /api/ {
    proxy_pass http://localhost:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

## 📄 License

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

**材质工坊** - 让Minecraft材质创作更简单 🎮✨

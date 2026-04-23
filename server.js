const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
const PORT = 80;

// API代理到后端
app.use('/api', createProxyMiddleware({
  target: 'http://localhost:8080',
  changeOrigin: true,
  pathRewrite: {
    '^/api': '' // 移除/api前缀
  }
}));

// 管理端路由
app.use('/admin', express.static(path.join(__dirname, 'dist/admin'), {
  index: false
}));
app.get('/admin/*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/admin/index.html'));
});

// 用户端路由（默认）
app.use(express.static(path.join(__dirname, 'dist')));
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log('========================================');
  console.log('服务器启动成功！');
  console.log('========================================');
  console.log('');
  console.log(`访问地址:`);
  console.log(`  用户端: http://localhost/`);
  console.log(`  管理端: http://localhost/admin`);
  console.log(`  后端API: http://localhost:8080`);
  console.log('');
  console.log('按 Ctrl+C 停止服务器');
  console.log('');
});

<template>
  <div class="login-page minecraft-page">
    <div class="login-container">
      <form class="login-form" @submit.prevent="handleLogin">
        <h1 class="form-header">材质工坊 - 登录</h1>

        <div class="form-group">
          <label for="username">用户名:</label>
          <input
            type="text"
            id="username"
            v-model="loginForm.username"
            required
            autocomplete="off"
          >
        </div>

        <div class="form-group">
          <label for="password">密码:</label>
          <input
            type="password"
            id="password"
            v-model="loginForm.password"
            required
            autocomplete="off"
          >
        </div>

        <div class="form-group remember-group">
          <label></label>
          <div class="checkbox-wrapper">
            <input
              type="checkbox"
              id="rememberPassword"
              v-model="rememberPassword"
            >
            <label for="rememberPassword" class="checkbox-label">记住密码</label>
          </div>
        </div>

        <div class="login-actions">
          <button type="submit" class="mc-button" :disabled="loading">
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <router-link to="/register" class="mc-button secondary">注册账号</router-link>
        </div>

        <div class="extra-links">
          <a href="#" @click.prevent="handleForgotPassword">忘记密码?</a>
          <router-link to="/admin/login" target="_blank">管理员后台</router-link>
        </div>
      </form>

      <!-- 返回首页按钮 -->
      <router-link to="/" class="mc-button back-btn">返回首页</router-link>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/mc/auth'

export default {
  name: 'UserLogin',
  data() {
    return {
      loading: false,
      rememberPassword: false,
      loginForm: {
        username: '',
        password: ''
      }
    }
  },
  mounted() {
    // 页面加载时检查是否有保存的用户名和密码
    const savedUsername = localStorage.getItem('savedUsername')
    const savedPassword = localStorage.getItem('savedPassword')

    if (savedUsername && savedPassword) {
      this.loginForm.username = savedUsername
      this.loginForm.password = savedPassword
      this.rememberPassword = true
    }
  },
  methods: {
    async handleLogin() {
      if (this.loading) return

      if (!this.loginForm.username || !this.loginForm.password) {
        this.$message.warning('请输入用户名和密码')
        return
      }

      this.loading = true
      try {
        const res = await login(this.loginForm)

        // 保存Token和用户信息
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', this.loginForm.username)

        // 根据"记住密码"选项保存或清除密码
        if (this.rememberPassword) {
          localStorage.setItem('savedUsername', this.loginForm.username)
          localStorage.setItem('savedPassword', this.loginForm.password)
        } else {
          localStorage.removeItem('savedUsername')
          localStorage.removeItem('savedPassword')
        }

        this.$message.success('登录成功')

        // 跳转回原页面或首页
        const redirect = this.$route.query.redirect || '/'
        this.$router.push(redirect)
      } catch (error) {
        console.error('登录失败:', error)
        this.$message.error(error.message || '登录失败，请检查用户名和密码')
      } finally {
        this.loading = false
      }
    },
    handleForgotPassword() {
      this.$message.info('请联系管理员重置密码，或发送邮件至 support@example.com')
    }
  }
}
</script>

<style scoped>
/* --- 基础变量 --- */
:root {
  --mc-border-dark: #3a3a3a;
  --mc-border-light: #ffffff;
  --mc-text-shadow: 2px 2px 0 #3a3a3a;
}

.login-page {
  /* 外部背景材质 (圆石) */
  background-image: url('~@/assets/images/minecraft/new-textures/default_cobble.png');
  background-repeat: repeat;
  background-size: 64px 64px;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  image-rendering: pixelated;
  padding: 20px;
}

/* --- 登录面板容器 --- */
.login-container {
  width: 100%;
  max-width: 420px;
  transform: scale(1.25);
}

.login-form {
  background: rgba(30, 30, 30, 0.85);
  backdrop-filter: blur(4px);
  border: 3px solid #000;
  box-shadow: inset 0 0 0 2px #555;
  padding: 25px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
  color: #fff;
}

.form-header {
  font-size: 20px;
  color: #fff;
  text-shadow: 2px 2px 0 #000;
  text-align: center;
  margin-bottom: 10px;
}

/* --- 表单组 --- */
.form-group {
  display: grid;
  grid-template-columns: 80px 1fr;
  align-items: center;
  gap: 10px;
}

.form-group label {
  font-size: 14px;
  color: #ddd;
  text-shadow: 1px 1px 0 #000;
  text-align: right;
}

.form-group input {
  background-color: rgba(0, 0, 0, 0.5);
  border: 2px solid #555;
  box-shadow: inset 2px 2px 0 #000;
  padding: 10px;
  font-family: inherit;
  font-size: 14px;
  color: #fff;
  outline: none;
  transition: outline 0.1s;
  width: 100%;
}

.form-group input:focus {
  outline: 2px solid #FFD700;
  border-color: #fff;
}

/* --- 记住密码复选框 --- */
.remember-group {
  margin-top: -5px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.checkbox-wrapper input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  margin: 0;
  accent-color: #FFD700;
}

.checkbox-label {
  font-size: 13px;
  color: #ddd;
  text-shadow: 1px 1px 0 #000;
  cursor: pointer;
  user-select: none;
  text-align: left !important;
}

.checkbox-label:hover {
  color: #fff;
}

/* --- 按钮和链接 --- */
.mc-button {
  background-color: #7d7d7d;
  color: #fff;
  text-shadow: 2px 2px 0 #3a3a3a;
  border: 2px solid;
  border-color: #3a3a3a #fff #fff #3a3a3a;
  padding: 12px;
  font-family: inherit;
  font-size: 16px;
  cursor: pointer;
  text-align: center;
  text-decoration: none;
  display: block;
  width: 100%;
  box-sizing: border-box;
}

.mc-button:hover {
  background-color: #9d9d9d;
}

.mc-button:disabled {
  background-color: #555;
  color: #aaa;
  cursor: not-allowed;
}

.login-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 10px;
}

.back-btn {
  margin-top: 0;
}

.extra-links {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
}

.extra-links a {
  font-size: 12px;
  color: #aaa;
  text-shadow: 1px 1px 0 #000;
  text-decoration: none;
}

.extra-links a:hover {
  color: #fff;
  text-decoration: underline;
}

/* 响应式 */
@media (max-width: 480px) {
  .form-group {
    grid-template-columns: 1fr;
    gap: 5px;
  }

  .form-group label {
    text-align: left;
  }

  .login-actions {
    grid-template-columns: 1fr;
  }
}
</style>

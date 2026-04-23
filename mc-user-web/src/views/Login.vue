<template>
  <div class="login-page">
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
          <a href="#" @click.prevent="showForgotDialog = true">忘记密码?</a>
<!--          <a href="http://localhost:80" target="_blank">管理员后台</a>-->
        </div>
      </form>

      <!-- 返回首页按钮 -->
      <router-link to="/" class="mc-button back-btn">返回首页</router-link>
    </div>

    <!-- 忘记密码对话框 -->
    <div v-if="showForgotDialog" class="mc-dialog-overlay" @click.self="closeForgotDialog">
      <div class="mc-dialog mc-dialog-wide">
        <div class="dialog-content mc-box">
          <h3 class="dialog-title">重置密码</h3>
          <div class="dialog-body-horizontal">
            <div class="form-column">
              <div class="form-group-dialog">
                <label>用户名</label>
                <input v-model="forgotForm.username" type="text" class="mc-input" placeholder="请输入用户名">
              </div>
              <div class="form-group-dialog">
                <label>绑定邮箱</label>
                <input v-model="forgotForm.email" type="email" class="mc-input" placeholder="请输入绑定的邮箱">
              </div>
              <div class="form-group-dialog">
                <label>验证码</label>
                <div class="code-input-group">
                  <input v-model="forgotForm.code" type="text" class="mc-input" placeholder="请输入验证码" maxlength="6">
                  <button
                    type="button"
                    class="mc-box btn-send-code"
                    @click="sendForgotCode"
                    :disabled="forgotCodeCountdown > 0 || !forgotForm.username || !forgotForm.email"
                  >
                    {{ forgotCodeCountdown > 0 ? `${forgotCodeCountdown}秒后重试` : '发送验证码' }}
                  </button>
                </div>
              </div>
            </div>
            <div class="form-column">
              <div class="form-group-dialog">
                <label>新密码</label>
                <input v-model="forgotForm.newPassword" type="password" class="mc-input" placeholder="6-20位字符">
              </div>
              <div class="form-group-dialog">
                <label>确认密码</label>
                <input v-model="forgotForm.confirmPassword" type="password" class="mc-input" placeholder="再次输入新密码">
              </div>
            </div>
          </div>
          <div class="dialog-actions">
            <button type="button" class="mc-box btn-cancel" @click="closeForgotDialog">取消</button>
            <button type="button" class="mc-box btn-confirm" @click="handleResetPassword" :disabled="forgotLoading">
              {{ forgotLoading ? '提交中...' : '重置密码' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, sendEmailCode } from '@/api/auth'
import { clearSubscriptionCache } from '@/composables/useSubscription'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const rememberPassword = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 忘记密码对话框
const showForgotDialog = ref(false)
const forgotLoading = ref(false)
const forgotCodeCountdown = ref(0)
const forgotForm = reactive({
  username: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 页面加载时检查是否有保存的用户名和密码
onMounted(() => {
  const savedUsername = localStorage.getItem('savedUsername')
  const savedPassword = localStorage.getItem('savedPassword')

  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername
    loginForm.password = savedPassword
    rememberPassword.value = true
  }
})

const handleLogin = async () => {
  if (loading.value) return

  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await login(loginForm)

    // 保存Token和用户信息
    localStorage.setItem('token', res.token)
    localStorage.setItem('username', loginForm.username)

    // 根据"记住密码"选项保存或清除密码
    if (rememberPassword.value) {
      localStorage.setItem('savedUsername', loginForm.username)
      localStorage.setItem('savedPassword', loginForm.password)
    } else {
      localStorage.removeItem('savedUsername')
      localStorage.removeItem('savedPassword')
    }

    ElMessage.success('登录成功')

    // 清除订阅缓存，确保登录后重新加载订阅信息
    clearSubscriptionCache()

    // 跳转回原页面或首页
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 发送忘记密码验证码
const sendForgotCode = async () => {
  if (!forgotForm.username) {
    ElMessage.error('请输入用户名')
    return
  }

  if (!forgotForm.email) {
    ElMessage.error('请输入邮箱')
    return
  }

  // 验证邮箱格式
  if (!forgotForm.email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
    ElMessage.error('邮箱格式不正确')
    return
  }

  try {
    // 验证用户名和邮箱是否匹配
    await request({
      url: '/verifyUserEmail',
      method: 'post',
      data: {
        username: forgotForm.username,
        email: forgotForm.email
      }
    })

    // 发送验证码
    await sendEmailCode({ email: forgotForm.email })
    ElMessage.success('验证码已发送，请查收邮件')

    // 开始倒计时
    forgotCodeCountdown.value = 60
    const timer = setInterval(() => {
      forgotCodeCountdown.value--
      if (forgotCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 重置密码
const handleResetPassword = async () => {
  if (!forgotForm.username || !forgotForm.email || !forgotForm.code || !forgotForm.newPassword || !forgotForm.confirmPassword) {
    ElMessage.warning('请填写完整')
    return
  }

  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }

  if (forgotForm.newPassword.length < 6 || forgotForm.newPassword.length > 20) {
    ElMessage.warning('新密码长度必须在6-20位之间')
    return
  }

  forgotLoading.value = true
  try {
    await request({
      url: '/resetPassword',
      method: 'post',
      data: {
        username: forgotForm.username,
        email: forgotForm.email,
        code: forgotForm.code,
        newPassword: forgotForm.newPassword
      }
    })

    ElMessage.success('密码重置成功，请使用新密码登录')
    closeForgotDialog()
  } catch (error) {
    ElMessage.error(error.message || '重置密码失败')
  } finally {
    forgotLoading.value = false
  }
}

// 关闭忘记密码对话框
const closeForgotDialog = () => {
  showForgotDialog.value = false
  // 重置表单
  Object.assign(forgotForm, {
    username: '',
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: ''
  })
  forgotCodeCountdown.value = 0
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
  background-image: url('@/assets/images/new-textures/default_cobble.png');
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

/* --- 忘记密码对话框样式 --- */
.mc-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.mc-dialog {
  max-width: 500px;
  width: 90%;
}

.mc-dialog-wide {
  max-width: 800px;
  width: 90%;
}

.dialog-content {
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('@/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  padding: 25px;
  color: #fff;
}

.dialog-title {
  font-size: 20px;
  color: #fff;
  text-shadow: 2px 2px 0 #000;
  text-align: center;
  margin: 0 0 20px 0;
}

.dialog-body-horizontal {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 20px;
}

.form-column {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group-dialog {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group-dialog label {
  font-size: 14px;
  color: #ddd;
  text-shadow: 1px 1px 0 #000;
}

.mc-input {
  background-color: rgba(0, 0, 0, 0.5);
  border: 2px solid #555;
  box-shadow: inset 2px 2px 0 #000;
  padding: 10px;
  font-family: inherit;
  font-size: 14px;
  color: #fff;
  outline: none;
  transition: all 0.2s;
  width: 100%;
}

.mc-input:focus {
  outline: 2px solid #FFD700;
  border-color: #fff;
  background-color: rgba(0, 0, 0, 0.7);
}

.mc-input:read-only {
  background-color: rgba(60, 60, 60, 0.5);
  cursor: not-allowed;
  color: #aaa;
}

.code-input-group {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.code-input-group .mc-input {
  flex: 1;
}

.btn-send-code {
  background-color: #7d7d7d;
  color: #fff;
  text-shadow: 2px 2px 0 #3a3a3a;
  border: 2px solid;
  border-color: #3a3a3a #fff #fff #3a3a3a;
  padding: 10px 12px;
  font-family: inherit;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
  width: 110px;
  box-sizing: border-box;
}

.btn-send-code:hover:not(:disabled) {
  background-color: #9d9d9d;
}

.btn-send-code:disabled {
  background-color: #555;
  color: #aaa;
  cursor: not-allowed;
  opacity: 0.5;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
}

.btn-cancel,
.btn-confirm {
  background-color: #7d7d7d;
  color: #fff;
  text-shadow: 2px 2px 0 #3a3a3a;
  border: 2px solid;
  border-color: #3a3a3a #fff #fff #3a3a3a;
  padding: 12px 24px;
  font-family: inherit;
  font-size: 16px;
  cursor: pointer;
  width: 120px;
  box-sizing: border-box;
}

.btn-cancel:hover {
  background-color: #9d9d9d;
}

.btn-confirm {
  background-color: #4a9ec1;
  border-color: #2a5e7a #8ecfef #8ecfef #2a5e7a;
}

.btn-confirm:hover:not(:disabled) {
  background-color: #6ab8d8;
}

.btn-confirm:disabled {
  background-color: #555;
  color: #aaa;
  cursor: not-allowed;
  opacity: 0.5;
}

/* 响应式 - 对话框 */
@media (max-width: 768px) {
  .dialog-body-horizontal {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .mc-dialog-wide {
    max-width: 95%;
  }

  .dialog-content {
    padding: 20px;
  }
}
</style>

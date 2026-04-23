<template>
  <div class="register-page">
    <div class="register-container">
      <form class="register-form" @submit.prevent="handleRegister">
        <h1 class="form-header">材质工坊 - 注册</h1>

        <div class="form-group">
          <label for="username">用户名:</label>
          <input 
            type="text" 
            id="username" 
            v-model="registerForm.username" 
            required
            autocomplete="off"
          >
        </div>

        <div class="form-group">
          <label for="password">密码:</label>
          <input 
            type="password" 
            id="password" 
            v-model="registerForm.password" 
            required
            autocomplete="off"
          >
        </div>
        
        <div class="form-group">
          <label for="confirm-password">再次输入:</label>
          <input 
            type="password" 
            id="confirm-password" 
            v-model="registerForm.confirmPassword" 
            required
            autocomplete="off"
          >
        </div>
        
        <div v-if="emailVerifyEnabled" class="form-group">
          <label for="email">邮箱:</label>
          <input 
            type="email" 
            id="email" 
            v-model="registerForm.email" 
            :required="emailVerifyEnabled"
            autocomplete="off"
          >
        </div>

        <div v-if="emailVerifyEnabled" class="form-group">
          <label for="verify-code">验证码:</label>
          <div class="input-with-button">
            <input 
              type="text" 
              id="verify-code" 
              v-model="registerForm.emailCode" 
              :required="emailVerifyEnabled"
              autocomplete="off"
            >
            <button 
              type="button" 
              class="mc-button btn-send-code" 
              :disabled="countdown > 0 || sendingCode"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '发送' }}
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="invite-code">邀请码:</label>
          <input 
            type="text" 
            id="invite-code" 
            v-model="registerForm.inviteCode" 
            placeholder="选填"
            autocomplete="off"
          >
        </div>
        
        <button type="submit" class="mc-button submit-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <div class="extra-links">
          <router-link to="/login">已有账号？立即登录</router-link>
        </div>
      </form>
      
      <!-- 返回首页按钮 -->
      <router-link to="/" class="mc-button back-btn">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendEmailCode, getEmailVerifyConfig } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const emailVerifyEnabled = ref(true)
let countdownTimer = null

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  emailCode: '',
  inviteCode: ''
})

const validateEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

// 获取邮箱验证配置
const loadEmailVerifyConfig = async () => {
  try {
    const response = await getEmailVerifyConfig()
    // 后端返回的是配置值字符串，需要转换为布尔值
    const configValue = response.data || response.msg || 'true'
    emailVerifyEnabled.value = configValue === 'true'
    console.log('邮箱验证配置:', emailVerifyEnabled.value)
  } catch (error) {
    // 接口调用失败时，默认启用邮箱验证（静默处理，不抛出错误）
    emailVerifyEnabled.value = true
    console.log('使用默认邮箱验证配置')
  }
}

onMounted(() => {
  loadEmailVerifyConfig()
})

const handleSendCode = async () => {
  if (!registerForm.email) {
    ElMessage.warning('请输入邮箱地址')
    return
  }

  if (!validateEmail(registerForm.email)) {
    ElMessage.warning('邮箱格式不正确')
    return
  }

  sendingCode.value = true
  try {
    await sendEmailCode({ email: registerForm.email })
    ElMessage.success('验证码已发送到您的邮箱')
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error(error.message || '验证码发送失败')
  } finally {
    sendingCode.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleRegister = async () => {
  if (loading.value) return

  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  // 如果启用了邮箱验证，则验证邮箱字段
  if (emailVerifyEnabled.value) {
    if (!registerForm.email) {
      ElMessage.warning('请输入邮箱地址')
      return
    }
    if (!validateEmail(registerForm.email)) {
      ElMessage.warning('邮箱格式不正确')
      return
    }
    if (!registerForm.emailCode) {
      ElMessage.warning('请输入邮箱验证码')
      return
    }
  }

  loading.value = true
  try {
    // 根据配置决定传递的参数
    const params = {
      username: registerForm.username,
      password: registerForm.password,
      inviteCode: registerForm.inviteCode
    }
    
    if (emailVerifyEnabled.value) {
      params.email = registerForm.email
      params.emailCode = registerForm.emailCode
    }
    
    await register(params)
    ElMessage.success('注册成功！请登录')
    
    if (countdownTimer) {
      clearInterval(countdownTimer)
    }
    
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
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

.register-page {
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

/* --- 注册面板容器 --- */
.register-container {
  width: 100%;
  max-width: 480px;
  transform: scale(1.25);
}

.register-form {
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
  grid-template-columns: 100px 1fr;
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
  box-sizing: border-box;
}

.form-group input:focus {
  outline: 2px solid #FFD700;
  border-color: #fff;
}

/* 验证码输入框布局 */
.input-with-button {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  width: 100%;
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

.btn-send-code {
  font-size: 12px;
  padding: 10px 15px;
  width: auto;
}

.submit-btn {
  margin-top: 10px;
}

.back-btn {
  margin-top: 0;
}

.extra-links {
  text-align: center;
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
}
</style>
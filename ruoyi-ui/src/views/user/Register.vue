<template>
  <div class="register-page minecraft-page">
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

        <div class="form-group">
          <label for="email">邮箱:</label>
          <input
            type="email"
            id="email"
            v-model="registerForm.email"
            required
            autocomplete="off"
          >
        </div>

        <div class="form-group">
          <label for="verify-code">验证码:</label>
          <div class="input-with-button">
            <input
              type="text"
              id="verify-code"
              v-model="registerForm.emailCode"
              required
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

<script>
import { register, sendEmailCode } from '@/api/mc/auth'

export default {
  name: 'UserRegister',
  data() {
    return {
      loading: false,
      sendingCode: false,
      countdown: 0,
      countdownTimer: null,
      registerForm: {
        username: '',
        password: '',
        confirmPassword: '',
        email: '',
        emailCode: '',
        inviteCode: ''
      }
    }
  },
  beforeDestroy() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
    }
  },
  methods: {
    validateEmail(email) {
      return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
    },
    async handleSendCode() {
      if (!this.registerForm.email) {
        this.$message.warning('请输入邮箱地址')
        return
      }

      if (!this.validateEmail(this.registerForm.email)) {
        this.$message.warning('邮箱格式不正确')
        return
      }

      this.sendingCode = true
      try {
        await sendEmailCode({ email: this.registerForm.email })
        this.$message.success('验证码已发送到您的邮箱')
        this.startCountdown()
      } catch (error) {
        console.error('发送验证码失败:', error)
        this.$message.error(error.message || '验证码发送失败')
      } finally {
        this.sendingCode = false
      }
    },
    startCountdown() {
      this.countdown = 60
      this.countdownTimer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(this.countdownTimer)
        }
      }, 1000)
    },
    async handleRegister() {
      if (this.loading) return

      // 表单验证
      if (!this.registerForm.username || !this.registerForm.password || !this.registerForm.email) {
        this.$message.warning('请填写所有必填项')
        return
      }

      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        this.$message.warning('两次输入的密码不一致')
        return
      }

      if (!this.validateEmail(this.registerForm.email)) {
        this.$message.warning('邮箱格式不正确')
        return
      }

      if (!this.registerForm.emailCode) {
        this.$message.warning('请输入邮箱验证码')
        return
      }

      this.loading = true
      try {
        await register(this.registerForm)
        this.$message.success('注册成功，请登录')
        this.$router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
        this.$message.error(error.message || '注册失败，请重试')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register-page {
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

.register-container {
  width: 100%;
  max-width: 450px;
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
  width: 100%;
}

.form-group input:focus {
  outline: 2px solid #FFD700;
  border-color: #fff;
}

.input-with-button {
  display: flex;
  gap: 10px;
}

.btn-send-code {
  flex-shrink: 0;
  padding: 10px 15px;
  font-size: 12px;
  background-color: #7d7d7d;
  color: #fff;
  border: 2px solid;
  border-color: #3a3a3a #fff #fff #3a3a3a;
  cursor: pointer;
  white-space: nowrap;
  min-width: 60px;
}

.btn-send-code:hover {
  background-color: #9d9d9d;
}

.btn-send-code:disabled {
  background-color: #555;
  color: #aaa;
  cursor: not-allowed;
}

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
}

.mc-button:hover {
  background-color: #9d9d9d;
}

.mc-button:disabled {
  background-color: #555;
  color: #aaa;
  cursor: not-allowed;
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

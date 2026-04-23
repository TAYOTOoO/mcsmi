<template>
  <div class="page-wrapper">
    <div class="top-border"></div>
    <div class="content-body">
      <div class="side-border left"></div>
      <main>
        <!-- 页头 -->
        <header class="page-header">
          <div class="mc-box logo">材质工坊</div>
          <nav class="main-nav">
            <ul>
              <li><router-link to="/" class="mc-box">首页</router-link></li>
              <li><router-link to="/generate" class="mc-box">开始生成</router-link></li>
              <li><router-link to="/my-tasks" class="mc-box">我的任务</router-link></li>
              <li><router-link to="/texture-pack-editor" class="mc-box">材质编辑</router-link></li>
              <li><router-link to="/recharge" class="mc-box">积分充值</router-link></li>
            </ul>
          </nav>
          
          <div class="user-actions">
            <template v-if="username">
              <span class="mc-box username-display" @click="goToProfile" title="个人中心">
                {{ username }}
                <span v-if="subscriptionInfo.isSubscribed" class="diamond-icon">💎</span>
              </span>
              <button class="mc-box logout-btn" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <button class="mc-box login-btn" @click="goToLogin">登录</button>
            </template>
          </div>
        </header>

        <!-- 用户信息面板 -->
        <div class="center-panel">
          <h2 class="panel-title">用户信息</h2>
          <div class="user-info-grid">
            <div class="inventory-display">
              <div class="profile-slot main-slot">
                <img src="/assets/images/new-textures/default_mese_crystal.png" alt="Avatar">
                <p class="slot-name">{{ userInfo.userName || '未登录' }}</p>
                <div class="subscription-badge-container">
                  <span v-if="subscriptionInfo.isSubscribed" class="diamond-badge">
                    💎 钻石订阅用户
                  </span>
                  <span v-else class="normal-badge">
                    普通用户
                  </span>
                </div>
                <p v-if="subscriptionInfo.isSubscribed" class="subscription-time">
                  {{ subscriptionInfo.isPermanent ? '永久有效' : `到期时间: ${formatTime(subscriptionInfo.expireTime)}` }}
                </p>
                <p class="slot-id">{{ userInfo.email || '未绑定邮箱' }}</p>
              </div>
              <div class="inventory-slots">
                <div class="empty-slot" v-for="n in 18" :key="n"></div>
              </div>
            </div>
            
            <div class="points-balance-bar mc-box">
              <img src="/assets/images/new-textures/default_gold_ingot.png" alt="积分">
              <span class="points-value">{{ pointsInfo.currentPoints || 0 }}</span>
            </div>

            <!-- 兑换码输入 -->
            <div class="redeem-section mc-box">
              <input
                type="text"
                class="redeem-input"
                v-model="redeemCode"
                placeholder="输入兑换码领取积分"
                maxlength="39"
                @keyup.enter="handleRedeem"
              >
              <button class="mc-box btn-redeem" @click="handleRedeem" :disabled="redeemLoading">
                {{ redeemLoading ? '兑换中...' : '兑换' }}
              </button>
            </div>

            <button class="mc-box btn-recharge-center" @click="router.push('/recharge')">积分充值</button>
            
            <div class="stats-grid">
              <div class="stat-box mc-box">
                <span class="label">累计消费</span>
                <span class="value">{{ pointsInfo.totalSpent || 0 }}</span>
              </div>
              <div class="stat-box mc-box">
                <span class="label">累计充值</span>
                <span class="value">{{ pointsInfo.totalRecharged || 0 }}</span>
              </div>
              <div class="stat-box mc-box">
                <span class="label">生成次数</span>
                <span class="value">{{ userInfo.taskCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 账户设置 -->
        <div class="center-panel">
          <h2 class="panel-title">账户设置</h2>
          <div class="settings-actions">
            <button class="mc-box btn-setting" @click="showPasswordDialog = true">修改密码</button>
            <button class="mc-box btn-setting" @click="showEmailDialog = true">修改邮箱</button>
          </div>
        </div>

        <!-- 积分记录面板 -->
        <div class="center-panel">
          <h2 class="panel-title">积分记录</h2>
          <div v-if="loading" class="loading-state">加载中...</div>
          <div v-else-if="pointsRecords.length === 0" class="empty-state">暂无记录</div>
          <div v-else class="history-list">
            <div v-for="record in pointsRecords" :key="record.id" class="history-item mc-box">
              <span :class="['tag', getTagClass(record.changeType)]">
                {{ getPointsTypeText(record.changeType) }}
              </span>
              <span class="description">{{ record.remark }}</span>
              <span :class="['points', isPositiveChange(record.changeType) ? 'points-positive' : 'points-negative']">
                {{ isPositiveChange(record.changeType) ? '+' : '-' }}{{ Math.abs(record.changePoints) }}
              </span>
              <span class="timestamp">{{ formatTime(record.createTime) }}</span>
            </div>
          </div>
          
          <!-- 分页 -->
          <div v-if="pointsTotal > pointsPageSize" class="pagination">
            <button class="mc-box btn-page" :disabled="pointsPage === 1" @click="prevPage">上一页</button>
            <span class="page-info">{{ pointsPage }} / {{ Math.ceil(pointsTotal / pointsPageSize) }}</span>
            <button class="mc-box btn-page" :disabled="pointsPage >= Math.ceil(pointsTotal / pointsPageSize)" @click="nextPage">下一页</button>
          </div>
        </div>

        <footer class="page-footer">
            © 2026 材质工坊
        </footer>
      </main>
      <div class="side-border right"></div>
    </div>

    <!-- 修改密码对话框 -->
    <div v-if="showPasswordDialog" class="mc-dialog-overlay" @click.self="showPasswordDialog = false">
      <div class="mc-dialog mc-dialog-wide">
        <div class="dialog-content mc-box">
          <h3 class="dialog-title">修改密码</h3>
          <div class="dialog-body-horizontal">
            <div class="form-column">
              <div class="form-group">
                <label>新密码</label>
                <input v-model="passwordForm.newPassword" type="password" class="mc-input" placeholder="6-20位字符">
              </div>
              <div class="form-group">
                <label>确认密码</label>
                <input v-model="passwordForm.confirmPassword" type="password" class="mc-input" placeholder="再次输入新密码">
              </div>
            </div>
            <div class="form-column">
              <div class="form-group">
                <label>邮箱验证</label>
                <input v-model="passwordForm.email" type="email" class="mc-input" :placeholder="userInfo.email || '请输入邮箱'" :value="userInfo.email" readonly>
              </div>
              <div class="form-group">
                <label>验证码</label>
                <div class="code-input-group">
                  <input v-model="passwordForm.code" type="text" class="mc-input" placeholder="请输入验证码" maxlength="6">
                  <button
                    class="mc-box btn-send-code"
                    @click="sendPasswordCode"
                    :disabled="passwordCodeCountdown > 0"
                  >
                    {{ passwordCodeCountdown > 0 ? `${passwordCodeCountdown}秒后重试` : '发送验证码' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="dialog-actions">
            <button class="mc-box btn-cancel" @click="showPasswordDialog = false">取消</button>
            <button class="mc-box btn-confirm" @click="updatePassword" :disabled="passwordLoading">
              {{ passwordLoading ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改邮箱对话框 -->
    <div v-if="showEmailDialog" class="mc-dialog-overlay" @click.self="showEmailDialog = false">
      <div class="mc-dialog mc-dialog-wide">
        <div class="dialog-content mc-box">
          <h3 class="dialog-title">修改邮箱</h3>
          <div class="dialog-body-horizontal">
            <div class="form-column">
              <div class="form-group">
                <label>当前邮箱</label>
                <input v-model="emailForm.oldEmail" type="email" class="mc-input" :placeholder="userInfo.email || '当前邮箱'" :value="userInfo.email" readonly>
              </div>
              <div class="form-group">
                <label>旧邮箱验证码</label>
                <div class="code-input-group">
                  <input v-model="emailForm.oldCode" type="text" class="mc-input" placeholder="请输入验证码" maxlength="6">
                  <button
                    class="mc-box btn-send-code"
                    @click="sendOldEmailCode"
                    :disabled="oldEmailCodeCountdown > 0"
                  >
                    {{ oldEmailCodeCountdown > 0 ? `${oldEmailCodeCountdown}秒后重试` : '发送验证码' }}
                  </button>
                </div>
              </div>
            </div>
            <div class="form-column">
              <div class="form-group">
                <label>新邮箱</label>
                <input v-model="emailForm.newEmail" type="email" class="mc-input" placeholder="请输入新邮箱">
              </div>
              <div class="form-group">
                <label>新邮箱验证码</label>
                <div class="code-input-group">
                  <input v-model="emailForm.newCode" type="text" class="mc-input" placeholder="请输入验证码" maxlength="6">
                  <button
                    class="mc-box btn-send-code"
                    @click="sendNewEmailCode"
                    :disabled="newEmailCodeCountdown > 0 || !emailForm.newEmail"
                  >
                    {{ newEmailCodeCountdown > 0 ? `${newEmailCodeCountdown}秒后重试` : '发送验证码' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="dialog-actions">
            <button class="mc-box btn-cancel" @click="showEmailDialog = false">取消</button>
            <button class="mc-box btn-confirm" @click="handleUpdateEmail" :disabled="emailLoading">
              {{ emailLoading ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserProfile, getPointsInfo, getPointsRecords, updatePassword as updatePasswordApi, updateEmail as updateEmailApi } from '@/api/user'
import { logout } from '@/api/auth'
import { sendEmailCode } from '@/api/auth'
import { redeemCode as redeemCodeApi } from '@/api/redemption'
import { getMySubscription } from '@/api/subscription'

const router = useRouter()
const loading = ref(false)
const username = ref('')

const userInfo = ref({
  userName: '',
  email: '',
  createTime: '',
  taskCount: 0
})

// 订阅信息
const subscriptionInfo = ref({
  isSubscribed: false,
  subscriptionType: '',
  expireTime: '',
  isPermanent: false
})

const pointsInfo = ref({
  currentPoints: 0,
  totalSpent: 0,
  totalRecharged: 0
})

const pointsRecords = ref([])
const pointsPage = ref(1)
const pointsPageSize = ref(10)
const pointsTotal = ref(0)

// 兑换码
const redeemCode = ref('')
const redeemLoading = ref(false)

// 密码修改
const showPasswordDialog = ref(false)
const passwordLoading = ref(false)
const passwordCodeCountdown = ref(0)
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: '',
  email: '',
  code: ''
})

// 邮箱修改
const showEmailDialog = ref(false)
const emailLoading = ref(false)
const oldEmailCodeCountdown = ref(0)
const newEmailCodeCountdown = ref(0)
const emailForm = reactive({
  oldEmail: '',
  oldCode: '',
  newEmail: '',
  newCode: ''
})

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  loadUserProfile()
  loadPointsInfo()
  loadPointsRecords()
  loadSubscriptionInfo()
})

const loadUserProfile = async () => {
  try {
    const res = await getUserProfile()
    userInfo.value = res.data
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

const loadSubscriptionInfo = async () => {
  try {
    const res = await getMySubscription()
    if (res.code === 200 && res.data) {
      subscriptionInfo.value = {
        isSubscribed: res.data.subscriptionType === 'diamond',
        subscriptionType: res.data.subscriptionType || 'normal',
        expireTime: res.data.expireTime || '',
        isPermanent: res.data.isPermanent === 1
      }
    }
  } catch (error) {
    console.error('加载订阅信息失败', error)
  }
}

const loadPointsInfo = async () => {
  try {
    const res = await getPointsInfo()
    pointsInfo.value = res.data || {}
  } catch (error) {
    console.error('加载积分信息失败', error)
  }
}

const loadPointsRecords = async () => {
  loading.value = true
  try {
    const res = await getPointsRecords({
      pageNum: pointsPage.value,
      pageSize: pointsPageSize.value
    })
    pointsRecords.value = res.rows || []
    pointsTotal.value = res.total || 0
  } catch (error) {
    console.error('加载积分记录失败', error)
  } finally {
    loading.value = false
  }
}

const getPointsTypeText = (type) => {
  const types = { 1: '充值', 2: '消费', 3: '退款', 4: '赠送', 5: '签到' }
  return types[type] || '其他'
}

const getTagClass = (type) => {
  // 1=充值, 2=消费, 3=退款, 4=赠送, 5=签到
  if (type === 1 || type === 4 || type === 5) return 'tag-recharge'
  if (type === 3) return 'tag-refund'
  return 'tag-consume'
}

const isPositiveChange = (type) => {
  // 充值(1)、退款(3)、赠送(4)、签到(5) 都是增加积分
  return type === 1 || type === 3 || type === 4 || type === 5
}

const formatTime = (timeStr) => timeStr ? timeStr.replace('T', ' ') : ''

const prevPage = () => {
  if (pointsPage.value > 1) {
    pointsPage.value--
    loadPointsRecords()
  }
}

const nextPage = () => {
  if (pointsPage.value < Math.ceil(pointsTotal.value / pointsPageSize.value)) {
    pointsPage.value++
    loadPointsRecords()
  }
}

// 发送修改密码验证码
const sendPasswordCode = async () => {
  if (!userInfo.value.email) {
    ElMessage.error('请先绑定邮箱')
    return
  }

  try {
    await sendEmailCode({ email: userInfo.value.email })
    ElMessage.success('验证码已发送，请查收邮件')

    // 开始倒计时
    passwordCodeCountdown.value = 60
    const timer = setInterval(() => {
      passwordCodeCountdown.value--
      if (passwordCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 发送旧邮箱验证码
const sendOldEmailCode = async () => {
  if (!userInfo.value.email) {
    ElMessage.error('当前账号未绑定邮箱')
    return
  }

  try {
    await sendEmailCode({ email: userInfo.value.email })
    ElMessage.success('验证码已发送到当前邮箱，请查收')

    // 开始倒计时
    oldEmailCodeCountdown.value = 60
    const timer = setInterval(() => {
      oldEmailCodeCountdown.value--
      if (oldEmailCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 发送新邮箱验证码
const sendNewEmailCode = async () => {
  if (!emailForm.newEmail) {
    ElMessage.error('请先输入新邮箱')
    return
  }

  // 验证邮箱格式
  if (!emailForm.newEmail.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
    ElMessage.error('邮箱格式不正确')
    return
  }

  try {
    await sendEmailCode({ email: emailForm.newEmail })
    ElMessage.success('验证码已发送到新邮箱，请查收')

    // 开始倒计时
    newEmailCodeCountdown.value = 60
    const timer = setInterval(() => {
      newEmailCodeCountdown.value--
      if (newEmailCodeCountdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

const updatePassword = async () => {
  if (!passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    ElMessage.warning('新密码长度必须在6-20位之间')
    return
  }
  if (!passwordForm.code) {
    ElMessage.warning('请输入邮箱验证码')
    return
  }

  passwordLoading.value = true
  try {
    await updatePasswordApi({
      newPassword: passwordForm.newPassword,
      code: passwordForm.code
    })
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false

    // 重置表单
    Object.assign(passwordForm, {
      newPassword: '',
      confirmPassword: '',
      email: '',
      code: ''
    })

    handleLogout()
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleUpdateEmail = async () => {
  if (!emailForm.oldCode || !emailForm.newEmail || !emailForm.newCode) {
    ElMessage.warning('请填写完整')
    return
  }

  // 验证新邮箱格式
  if (!emailForm.newEmail.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
    ElMessage.error('新邮箱格式不正确')
    return
  }

  emailLoading.value = true
  try {
    await updateEmailApi({
      oldCode: emailForm.oldCode,
      newEmail: emailForm.newEmail,
      newCode: emailForm.newCode
    })
    ElMessage.success('邮箱修改成功')
    showEmailDialog.value = false

    // 重置表单
    Object.assign(emailForm, {
      oldEmail: '',
      oldCode: '',
      newEmail: '',
      newCode: ''
    })

    loadUserProfile()
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    emailLoading.value = false
  }
}

// 兑换兑换码
const handleRedeem = async () => {
  if (!redeemCode.value || !redeemCode.value.trim()) {
    ElMessage.warning('请输入兑换码')
    return
  }

  redeemLoading.value = true
  try {
    await redeemCodeApi(redeemCode.value.trim())
    ElMessage.success('兑换成功！积分已到账')
    redeemCode.value = ''
    // 刷新积分信息和记录
    await loadPointsInfo()
    await loadPointsRecords()
  } catch (error) {
    ElMessage.error(error.message || '兑换失败')
  } finally {
    redeemLoading.value = false
  }
}

const goToLogin = () => router.push('/login')

const goToProfile = () => {
  // 已经在个人中心页面，无需跳转
  // router.push('/profile')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消' })
    try { await logout() } catch (e) {}
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    username.value = ''
    router.push('/login')
  } catch {}
}
</script>

<style scoped>
/* --- 基础布局 --- */
.page-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px 20px 20px;
  transform: scale(1.5);
  transform-origin: top center;
  margin-bottom: 200px;
}

.top-border {
  background-image: url('/assets/images/new-textures/default_obsidian_brick.png');
  background-repeat: repeat-x;
  background-size: 64px 64px;
  height: 48px;
  width: 100%;
  max-width: 996px;
  border: 4px solid var(--mc-border-black);
  border-bottom: 0;
  flex-shrink: 0;
  image-rendering: pixelated;
}

.content-body {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 1004px;
}

.side-border {
  background-image: url('/assets/images/new-textures/default_obsidian_block.png');
  background-repeat: repeat-y;
  background-size: 64px 64px;
  width: 48px;
  flex-shrink: 0;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  border-bottom: 0;
  image-rendering: pixelated;
}
.side-border.left { border-right: 4px solid var(--mc-border-black); }
.side-border.right { border-left: 4px solid var(--mc-border-black); }

main {
  max-width: 900px;
  width: 100%;
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('/assets/images/new-textures/default_pine_wood.png');
  background-size: auto, 64px 64px;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  padding: 20px;
  image-rendering: pixelated;
  color: #fff;
  text-shadow: 2px 2px 0 #000;
}

/* --- UI组件 --- */
.mc-box {
  background-color: rgba(40, 40, 40, 0.85);
  backdrop-filter: blur(4px);
  border: 2px solid var(--mc-border-black);
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark);
  padding: 8px 12px;
  text-align: center;
  display: inline-block;
  color: var(--mc-text-color);
  text-decoration: none;
  font-family: inherit;
  cursor: pointer;
}
a.mc-box:hover, button.mc-box:hover {
  background-color: rgba(60, 60, 60, 0.9);
  box-shadow: inset 2px 2px 0px #666, inset -2px -2px 0px #222;
}
button.mc-box { font-size: 14px; }

/* --- 头部 --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 0 20px 0;
  flex-wrap: wrap;
  gap: 10px;
}
.logo { font-size: 24px; }
.main-nav ul { display: flex; gap: 10px; list-style: none; padding: 0; margin: 0; }
.main-nav a.mc-box { padding: 6px 10px; font-size: 14px; }
.main-nav a.mc-box.active {
  background-color: #585858;
  box-shadow: inset 2px 2px 0px #3a3a3a, inset -2px -2px 0px #a0a0a0;
  color: #ffffa0;
}

.user-actions { display: flex; align-items: center; gap: 10px; }
.username-display {
  font-size: 12px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.username-display:hover {
  background: #4a4a4a;
  transform: translateY(-2px);
}

.diamond-icon {
  margin-left: 4px;
  animation: sparkle 2s infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.login-btn, .logout-btn { padding: 6px 10px; font-size: 14px; }

/* --- 个人中心 --- */
.center-panel {
  background: rgba(30, 30, 30, 0.6);
  border: 3px solid #000;
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.panel-title {
  font-size: 16px;
  color: #fff;
  text-shadow: 1px 1px 0 #000;
  margin-top: 0;
  margin-bottom: 15px;
}

.inventory-display {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.profile-slot {
  width: 100px;
  height: 100px;
  background: #8b8b8b; /* Stone color */
  border: 4px solid #373737; /* Dark border */
  border-right-color: #ffffff; /* Highlight */
  border-bottom-color: #ffffff; /* Highlight */
  border-top-color: #373737; /* Shadow */
  border-left-color: #373737; /* Shadow */
  box-shadow: inset 2px 2px 0px #5e5e5e, inset -2px -2px 0px #a0a0a0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5px;
  transition: transform 0.1s;
  image-rendering: pixelated;
}
.profile-slot:hover { transform: scale(1.02); }

/* Main slot specific styles */
.profile-slot.main-slot {
  width: 180px;
  height: auto;
  min-height: 180px;
  padding: 15px 10px;
  gap: 8px;
  background-image: url('/assets/images/new-textures/default_stone.png');
  background-size: 64px 64px;
}

.profile-slot img { width: 48px; height: 48px; image-rendering: pixelated; margin-bottom: 2px; }
.profile-slot.main-slot img { width: 64px; height: 64px; margin-bottom: 5px; filter: drop-shadow(4px 4px 0 rgba(0,0,0,0.5)); }

.slot-name { font-size: 12px; margin: 0; color: #fff; text-shadow: 2px 2px 0 #000; }
.profile-slot.main-slot .slot-name { font-size: 16px; color: #ffffa0; margin-bottom: 5px; }

.slot-id { font-size: 8px; margin: 0; color: #ccc; text-shadow: 1px 1px 0 #000; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 90px; }
.profile-slot.main-slot .slot-id { font-size: 10px; max-width: 100%; color: #ddd; }

.subscription-badge-container {
  margin: 5px 0;
}

.diamond-badge {
  background-color: rgba(0, 40, 40, 0.8);
  border: 2px solid #55FFFF;
  color: #55FFFF;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: bold;
  text-shadow: 2px 2px 0 #002222;
  box-shadow: 0 4px 0 rgba(0,0,0,0.5);
  display: inline-block;
  image-rendering: pixelated;
}

.normal-badge {
  background-color: rgba(40, 40, 40, 0.8);
  border: 2px solid #AAAAAA;
  color: #AAAAAA;
  padding: 6px 10px;
  font-size: 12px;
  text-shadow: 2px 2px 0 #000;
  box-shadow: 0 4px 0 rgba(0,0,0,0.5);
  display: inline-block;
}

.subscription-time {
  font-size: 10px;
  margin: 5px 0 0 0;
  color: #FFD700;
  text-shadow: 1px 1px 0 #000;
  background: rgba(0,0,0,0.4);
  padding: 2px 4px;
}

@keyframes shine {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.2); }
}

.inventory-slots {
  display: grid;
  grid-template-columns: repeat(auto-fill, 54px);
  gap: 4px;
  flex: 1;
}
.empty-slot {
  width: 54px;
  height: 54px;
  background: #444;
  border: 2px solid #222;
  box-shadow: inset 2px 2px #0006;
}

.points-balance-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 25px;
  margin-bottom: 20px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
}
.points-balance-bar img { width: 48px; height: 48px; image-rendering: pixelated; }
.points-value { font-size: 32px; color: #FFD700; flex-grow: 1; text-align: right; text-shadow: 2px 2px 0 #3a3a3a; }

/* 兑换码输入框 */
.redeem-section {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 15px;
  margin-bottom: 20px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
}

.redeem-input {
  flex: 1;
  padding: 8px 12px;
  font-size: 14px;
  font-family: 'Courier New', monospace;
  background-color: rgba(0, 0, 0, 0.5);
  border: 2px solid #666;
  box-shadow: inset 2px 2px 0px #222, inset -2px -2px 0px #888;
  color: #fff;
  letter-spacing: 1px;
  text-transform: uppercase;
  outline: none;
}

.redeem-input::placeholder {
  color: #999;
  text-transform: none;
  letter-spacing: normal;
}

.redeem-input:focus {
  border-color: #FFD700;
  background-color: rgba(0, 0, 0, 0.7);
}

.btn-redeem {
  padding: 8px 20px;
  background-color: #2a632b;
  color: #fff;
  text-shadow: 1px 1px 0 #000;
  border-color: #000;
  font-size: 14px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-redeem:hover:not(:disabled) {
  background-color: #3c8f3e;
}

.btn-redeem:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-recharge-center {
  display: block;
  width: 200px;
  margin: 0 auto 20px;
  background-color: #2a632b;
  color: #fff;
  text-shadow: 1px 1px 0 #000;
  border-color: #000;
  font-size: 16px;
  padding: 10px;
}
.btn-recharge-center:hover { background-color: #3c8f3e; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}
.stat-box {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  padding: 15px;
  text-align: center;
}
.stat-box .label { font-size: 12px; margin-bottom: 8px; display: block; color: #ddd; text-shadow: 1px 1px 0 #000; }
.stat-box .value { font-size: 20px; color: #fff; text-shadow: 1px 1px 0 #000; }

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
}
.history-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  align-items: center;
  gap: 15px;
  padding: 8px 12px;
  font-size: 12px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
}
.tag { padding: 3px 6px; font-size: 10px; border: 2px solid #000; }
.tag-consume { background-color: #c14a4a; color: #fff; }
.tag-recharge { background-color: #4ac156; color: #fff; }
.tag-refund { background-color: #4a9ec1; color: #fff; }

.description { color: #fff; text-shadow: 1px 1px 0 #000; }
.points { font-weight: bold; justify-self: end; text-shadow: 1px 1px 0 #000; }
.points-negative { color: #FF5555; }
.points-positive { color: #55FF55; }
.timestamp { color: #ddd; justify-self: end; text-shadow: 1px 1px 0 #000; }

.pagination { display: flex; justify-content: center; gap: 15px; margin-top: 20px; }
.btn-page { padding: 6px 12px; font-size: 12px; }
.page-info { padding: 6px 12px; font-size: 12px; cursor: default; }

.loading-state, .empty-state { text-align: center; padding: 20px; color: #ddd; text-shadow: 1px 1px 0 #000; }

.page-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #ccc;
  text-shadow: 1px 1px 0 #000;
}

.settings-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
}
.btn-setting { width: 150px; }

/* 弹窗样式 */
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
}

.mc-dialog {
  width: 90%;
  max-width: 450px;
}

.mc-dialog-wide {
  max-width: 800px; /* 横版宽度 */
}

.dialog-content {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  padding: 30px;
  border: 4px solid var(--mc-border-black);
}

.dialog-title {
  font-size: 20px;
  color: white;
  text-shadow: 2px 2px 0 #000;
  margin-top: 0;
  margin-bottom: 20px;
  text-align: center;
}

/* 横版布局 */
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

.form-group {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #ddd;
  margin-bottom: 8px;
  text-shadow: 1px 1px 0 #000;
}

.mc-input {
  width: 100%;
  background: rgba(0, 0, 0, 0.5);
  border: 2px solid #aaa;
  color: white;
  padding: 10px;
  font-family: inherit;
  font-size: 14px;
  box-sizing: border-box;
}

.mc-input::placeholder {
  color: #888;
}

.mc-input:focus {
  outline: none;
  border-color: #fff;
  background: rgba(0, 0, 0, 0.7);
}

.mc-input:read-only {
  background: rgba(60, 60, 60, 0.5);
  cursor: not-allowed;
  color: #aaa;
}

/* 验证码输入组 */
.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group .mc-input {
  flex: 1;
}

.btn-send-code {
  padding: 10px 16px;
  font-size: 12px;
  white-space: nowrap;
  min-width: 110px;
}

.btn-send-code:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.dialog-actions {
  display: flex;
  gap: 15px;
  margin-top: 25px;
  justify-content: center;
}

.btn-cancel {
  background-color: #c14a4a;
  flex: 0 0 120px;
  padding: 12px;
  font-size: 16px;
}

.btn-cancel:hover {
  background-color: #d15555;
}

.btn-confirm {
  background-color: #4ac156;
  flex: 0 0 120px;
  padding: 12px;
  font-size: 16px;
}

.btn-confirm:hover {
  background-color: #5dd169;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .mc-header .header-content { flex-direction: column; }
  .history-item { grid-template-columns: 1fr; gap: 5px; text-align: left; }
  .stats-grid { grid-template-columns: 1fr; }

  .mc-dialog-wide {
    max-width: 95%;
  }

  .dialog-body-horizontal {
    grid-template-columns: 1fr;
    gap: 15px;
  }
}
</style>
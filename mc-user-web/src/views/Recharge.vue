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
              <li><router-link to="/recharge" class="mc-box active">积分充值</router-link></li>
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

        <!-- 充值主面板 -->
        <div class="recharge-panel">
          <h2 class="panel-header">积分与订阅</h2>
          <div class="info-bar mc-box">
            <img src="/assets/images/new-textures/coins_bucket_scrap_gold.png" alt="金锭">
            <span>1元 = 100积分</span>
          </div>

          <!-- 主选项卡切换 -->
          <div class="main-tab-switcher">
            <button
              :class="['mc-box tab-btn', { active: activeTab === 'points' }]"
              @click="activeTab = 'points'"
            >
              积分充值
            </button>
            <button
              :class="['mc-box tab-btn', { active: activeTab === 'diamond' }]"
              @click="activeTab = 'diamond'"
            >
              💎 钻石订阅
            </button>
          </div>

          <!-- 积分充值标签页 -->
          <div v-if="activeTab === 'points'" class="points-recharge-section">
            <div class="tab-switcher">
              <button
                :class="['mc-box tab-btn', { active: rechargeTab === 'package' }]"
                @click="rechargeTab = 'package'"
              >
                套餐充值
              </button>
              <button
                :class="['mc-box tab-btn', { active: rechargeTab === 'custom' }]"
                @click="rechargeTab = 'custom'"
              >
                自定义金额
              </button>
            </div>

            <!-- 套餐充值 -->
            <div v-if="rechargeTab === 'package'" class="package-grid">
              <button
                v-for="pkg in packages"
                :key="pkg.packageId"
                :class="['package-card', { selected: selectedPackage === pkg.packageId, 'stone-bg': selectedPackage !== pkg.packageId }]"
                @click="selectPackage(pkg)"
              >
                <img src="/assets/images/new-textures/coins_bucket_scrap_gold.png" alt="金锭">
                <h4>{{ pkg.packageName }}</h4>
                <div v-if="subscriptionInfo.isSubscribed" class="discount-price-container">
                  <p class="price original-price">¥{{ pkg.amount }}</p>
                  <p class="price discounted-price">¥{{ getDiscountedPrice(pkg.amount) }}</p>
                  <p class="discount-tag">钻石会员专享</p>
                </div>
                <p v-else class="price">¥{{ pkg.amount }}</p>
                <p class="points">{{ pkg.points }}积分 <span v-if="pkg.bonusPoints > 0" class="bonus">+{{ pkg.bonusPoints }}</span></p>
                <p class="total-points">共{{ pkg.points + pkg.bonusPoints }}积分</p>
                <div v-if="selectedPackage === pkg.packageId" class="selected-mark">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="选定" class="check-icon">
                </div>
              </button>
            </div>

            <!-- 自定义金额 -->
            <div v-else class="custom-section mc-box">
              <div class="custom-form-row">
                <label>充值金额:</label>
                <input
                  v-model.number="customForm.amount"
                  type="number"
                  class="mc-input"
                  placeholder="最低1元"
                  min="1"
                >
                <span>元</span>
              </div>
              <div v-if="subscriptionInfo.isSubscribed" class="custom-form-row">
                <label>折后金额:</label>
                <span class="calculated-points">¥{{ (customForm.amount * subscriptionInfo.discountRate).toFixed(2) }}</span>
              </div>
              <div class="custom-form-row">
                <label>获得积分:</label>
                <span class="calculated-points">{{ customForm.amount * 100 }} 积分</span>
              </div>
              <div class="form-hint">最低充值1元，最高单笔10000元</div>
            </div>

            <h3 class="section-title">选择支付方式</h3>
            <div class="payment-grid">
              <button
                :class="['payment-card', { selected: paymentType === 'alipay', 'stone-bg': paymentType !== 'alipay' }]"
                @click="paymentType = 'alipay'"
              >
                <img src="/assets/images/new-textures/default_diamond.png" alt="支付宝">
                <p>支付宝</p>
                <div v-if="paymentType === 'alipay'" class="selected-mark">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="选定" class="check-icon">
                </div>
              </button>
              <button
                :class="['payment-card', { selected: paymentType === 'wxpay', 'stone-bg': paymentType !== 'wxpay' }]"
                @click="paymentType = 'wxpay'"
              >
                <img src="/assets/images/new-textures/ethereal_illumishroom_green.png" alt="微信支付">
                <p>微信支付</p>
                <div v-if="paymentType === 'wxpay'" class="selected-mark">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="选定" class="check-icon">
                </div>
              </button>
            </div>

            <button class="btn-recharge mc-box" @click="handleRecharge" :disabled="loading">
              {{ loading ? '处理中...' : '立即充值' }}
            </button>
          </div>

          <!-- 钻石订阅标签页 -->
          <div v-else class="diamond-subscription-section">
            <div class="subscription-benefits mc-box">
              <h3>钻石会员特权</h3>
              <ul class="benefits-list">
                <li v-if="subscriptionInfo.isSubscribed">✨ 充值享受{{ (subscriptionInfo.discountRate * 10).toFixed(0) }}折优惠</li>
                <li v-else>✨ 充值享受折扣优惠</li>
                <li v-if="subscriptionInfo.isSubscribed">✨ 每月赠送{{ formatPoints(subscriptionInfo.monthlyPoints) }}积分</li>
                <li v-else>✨ 每月赠送积分</li>
                <li>✨ 专属钻石标识</li>
                <li>✨ 图片商用授权</li>
                <li>✨ 图片分割工厂</li>
                <li>✨ 容差调节功能</li>
                <li>✨ 毛边处理功能</li>
                <li>✨ 文字消除功能</li>
              </ul>
            </div>

            <h3 class="section-title">选择订阅套餐</h3>
            <div class="package-grid">
              <button
                v-for="pkg in subscriptionPackages"
                :key="pkg.packageId"
                :class="['package-card', { selected: selectedSubscription === pkg.packageId, 'stone-bg': selectedSubscription !== pkg.packageId }]"
                @click="selectSubscription(pkg.packageId)"
              >
                <img src="/assets/images/new-textures/default_diamond.png" alt="钻石" class="package-icon">
                <h4>{{ pkg.packageName }}</h4>
                <p class="price">¥{{ pkg.amount }}</p>
                <p class="duration">{{ getDurationText(pkg.durationMonths) }}</p>
                <p class="benefits">充值{{ (pkg.discountRate * 10).toFixed(0) }}折 + 月赠{{ formatPoints(pkg.monthlyPoints) }}积分</p>
                <div v-if="selectedSubscription === pkg.packageId" class="selected-mark">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="选定" class="check-icon">
                </div>
              </button>
            </div>

            <h3 class="section-title">选择支付方式</h3>
            <div class="payment-grid">
              <button
                :class="['payment-card', { selected: paymentType === 'alipay', 'stone-bg': paymentType !== 'alipay' }]"
                @click="paymentType = 'alipay'"
              >
                <img src="/assets/images/new-textures/default_diamond.png" alt="支付宝">
                <p>支付宝</p>
                <div v-if="paymentType === 'alipay'" class="selected-mark">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="选定" class="check-icon">
                </div>
              </button>
              <button
                :class="['payment-card', { selected: paymentType === 'wxpay', 'stone-bg': paymentType !== 'wxpay' }]"
                @click="paymentType = 'wxpay'"
              >
                <img src="/assets/images/new-textures/ethereal_illumishroom_green.png" alt="微信支付">
                <p>微信支付</p>
                <div v-if="paymentType === 'wxpay'" class="selected-mark">
                  <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="选定" class="check-icon">
                </div>
              </button>
            </div>

            <button class="btn-recharge mc-box" @click="handleBuySelectedSubscription" :disabled="loading || !selectedSubscription">
              {{ loading ? '处理中...' : '立即订阅' }}
            </button>
          </div>
        </div>

        <!-- 充值历史 -->
        <div class="history-section">
          <h2>充值历史</h2>
          <div v-if="orders.length === 0" class="empty-history mc-box">暂无充值记录</div>
          <div v-else class="history-list">
            <div v-for="order in orders" :key="order.orderId" class="history-item mc-box">
              <div class="history-row">
                <span><img src="/assets/images/new-textures/default_tin_lump.png" alt="ID">{{ order.orderNo }}</span>
                <span><img src="/assets/images/new-textures/coins_bucket_scrap_gold.png" alt="金额">金额: ¥{{ order.amount }}</span>
              </div>
              <div class="history-row">
                <span><img src="/assets/images/new-textures/default_mese_crystal.png" alt="积分">积分: {{ order.points }}</span>
                <span><img src="/assets/images/new-textures/default_diamond.png" alt="支付">方式: {{ getPaymentTypeText(order.paymentType) }}</span>
              </div>
              <div class="history-row">
                <span><img src="/assets/images/new-textures/default_obsidian_shard.png" alt="时间">{{ formatTime(order.createTime) }}</span>
                <button v-if="order.orderStatus === 0" class="btn-pay mc-box" @click="handlePay(order)">去支付</button>
                <button v-else class="btn-pay mc-box paid">{{ getOrderStatusText(order.orderStatus) }}</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 订阅订单历史 -->
        <div v-if="subscriptionOrders.length > 0" class="history-section">
          <h2>订阅订单</h2>
          <div class="history-list">
            <div v-for="order in subscriptionOrders" :key="order.orderId" class="history-item mc-box">
              <div class="history-row">
                <span><img src="/assets/images/new-textures/default_tin_lump.png" alt="ID">{{ order.orderNo }}</span>
                <span><img src="/assets/images/new-textures/default_diamond.png" alt="套餐">{{ order.packageName }}</span>
              </div>
              <div class="history-row">
                <span><img src="/assets/images/new-textures/coins_bucket_scrap_gold.png" alt="金额">金额: ¥{{ order.actualAmount }}</span>
                <span>时长: {{ getDurationText(order.durationMonths) }}</span>
              </div>
              <div class="history-row">
                <span><img src="/assets/images/new-textures/default_obsidian_shard.png" alt="时间">{{ formatTime(order.createTime) }}</span>
                <button class="btn-pay mc-box" :class="{ paid: order.paymentStatus !== 0 }">
                  {{ getSubscriptionOrderStatusText(order.paymentStatus) }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <footer class="page-footer">
            © 2026 材质工坊
        </footer>
      </main>

      <!-- 支付弹窗 -->
      <el-dialog
        v-model="paymentDialogVisible"
        title="扫码支付"
        width="360px"
        :before-close="handleDialogClose"
        :close-on-click-modal="false"
        :lock-scroll="false"
        center
        append-to-body
        destroy-on-close
      >
        <div class="payment-dialog">
          <!-- 二维码支付 -->
          <div v-if="paymentInfo.payType === 'qrcode'" class="qrcode-payment">
            <div class="qrcode-wrapper">
              <img :src="qrcodeDataUrl" alt="支付二维码" class="qrcode-image" />
            </div>
            <div class="payment-tips">
              <p class="order-no">订单号：{{ paymentInfo.outTradeNo }}</p>
              <p class="pay-hint">
                请使用{{ paymentType === 'alipay' ? '支付宝' : '微信' }}扫描二维码完成支付
              </p>
              <p class="amount">￥{{ paymentInfo.amount }}</p>
            </div>

            <!-- 支付状态检测 -->
            <div class="payment-status">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: checkProgress + '%' }"></div>
              </div>
              <p class="status-text">正在检测支付状态...</p>
            </div>

            <!-- 手动刷新按钮 -->
            <button class="btn-refresh mc-box" @click="manualCheckStatus">
              我已完成支付
            </button>
          </div>

          <!-- 跳转支付提示 -->
          <div v-else-if="paymentInfo.payType === 'payurl'" class="url-payment">
            <p>支付页面已在新窗口打开</p>
            <p>完成支付后点击下方按钮</p>
            <button class="btn-refresh mc-box" @click="manualCheckStatus">
              我已完成支付
            </button>
          </div>
        </div>
      </el-dialog>

      <div class="side-border right"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPackages, getOrders, createPayment, pay, checkOrderStatus } from '@/api/payment'
import { logout } from '@/api/auth'
import { getMySubscription, getAvailablePackages, createSubscriptionOrder, getOrderStatus as getSubscriptionOrderStatus, getMySubscriptionOrders } from '@/api/subscription'
import QRCode from 'qrcode' // 需要先安装: npm install qrcode

const router = useRouter()
const loading = ref(false)
const activeTab = ref('diamond')
const rechargeTab = ref('package')
const selectedPackage = ref(null)
const paymentType = ref('alipay')
const packages = ref([])
const orders = ref([])
const username = ref('')

// 订阅相关
const subscriptionPackages = ref([])
const selectedSubscription = ref(null)
const subscriptionOrders = ref([])
const subscriptionInfo = ref({
  isSubscribed: false,
  discountRate: 1,
  monthlyPoints: 0,
  packageName: ''
})

const customForm = reactive({
  amount: 10
})

// 支付弹窗相关
const paymentDialogVisible = ref(false)
const paymentInfo = reactive({
  payType: '', // qrcode, payurl, urlscheme
  payData: '', // 二维码内容或URL
  outTradeNo: '', // 订单号
  tradeNo: '', // 易支付订单号
  amount: 0,
  isSubscription: false // 是否为订阅支付
})
const qrcodeDataUrl = ref('') // 二维码图片Data URL
const checkTimer = ref(null)
const checkProgress = ref(0)

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  loadPackages()
  loadOrders()
  loadSubscriptionInfo()
  loadSubscriptionPackages()
  loadSubscriptionOrders()
})

onBeforeUnmount(() => {
  // 清理定时器
  if (checkTimer.value) {
    clearInterval(checkTimer.value)
  }
})

const loadPackages = async () => {
  try {
    const res = await getPackages()
    packages.value = res.data || []
    if (packages.value.length > 0) {
      selectedPackage.value = packages.value[0].packageId
    }
  } catch (error) {
    ElMessage.error('加载充值套餐失败')
  }
}

const loadSubscriptionInfo = async () => {
  try {
    const res = await getMySubscription()
    if (res.code === 200 && res.data) {
      subscriptionInfo.value = {
        isSubscribed: res.data.subscriptionType === 'diamond',
        discountRate: res.data.discountRate || 1,
        monthlyPoints: res.data.monthlyPoints || 0,
        packageName: res.data.packageName || ''
      }
    }
  } catch (error) {
    console.error('加载订阅信息失败', error)
  }
}

const loadSubscriptionPackages = async () => {
  try {
    const res = await getAvailablePackages()
    subscriptionPackages.value = res.data || []
    // 默认选择第一个套餐
    if (subscriptionPackages.value.length > 0) {
      selectedSubscription.value = subscriptionPackages.value[0].packageId
    }
  } catch (error) {
    console.error('加载订阅套餐失败', error)
  }
}

const loadSubscriptionOrders = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) return
    const res = await getMySubscriptionOrders()
    if (res.code === 200) {
      subscriptionOrders.value = res.data || []
    }
  } catch (error) {
    console.error('加载订阅订单失败', error)
  }
}

const getDiscountedPrice = (originalPrice) => {
  if (!subscriptionInfo.value.isSubscribed) return null
  return (originalPrice * subscriptionInfo.value.discountRate).toFixed(2)
}

const loadOrders = async () => {
  try {
    const res = await getOrders()
    orders.value = res.rows || []
  } catch (error) {
    console.error('加载充值记录失败', error)
  }
}

const selectPackage = (pkg) => {
  selectedPackage.value = pkg.packageId
}

const selectSubscription = (packageId) => {
  selectedSubscription.value = packageId
}

const handleBuySelectedSubscription = async () => {
  if (!selectedSubscription.value) {
    ElMessage.warning('请选择订阅套餐')
    return
  }
  await handleBuySubscription(selectedSubscription.value)
}

const handleRecharge = async () => {
  if (!paymentType.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  try {
    loading.value = true

    // 构建支付数据
    let paymentData = {
      paymentType: paymentType.value
    }

    let amount = 0

    if (rechargeTab.value === 'package') {
      if (!selectedPackage.value) {
        ElMessage.warning('请选择充值套餐')
        return
      }
      const pkg = packages.value.find(p => p.packageId === selectedPackage.value)
      if (!pkg) {
        ElMessage.warning('套餐信息不存在')
        return
      }
      paymentData.productName = pkg.packageName
      // 应用钻石会员折扣
      const originalAmount = pkg.amount
      const finalAmount = subscriptionInfo.value.isSubscribed
        ? (originalAmount * subscriptionInfo.value.discountRate)
        : originalAmount
      paymentData.amount = finalAmount.toFixed(2)
      paymentData.points = pkg.points + (pkg.bonusPoints || 0)
      amount = finalAmount
    } else {
      if (customForm.amount < 1) {
        ElMessage.warning('最低充值金额为1元')
        return
      }
      if (customForm.amount > 10000) {
        ElMessage.warning('最高单笔充值10000元')
        return
      }
      // 应用钻石会员折扣
      const finalAmount = subscriptionInfo.value.isSubscribed
        ? (customForm.amount * subscriptionInfo.value.discountRate)
        : customForm.amount
      paymentData.productName = `充值${customForm.amount}元`
      paymentData.amount = finalAmount.toFixed(2)
      paymentData.points = Math.floor(customForm.amount * 100)
      amount = finalAmount
    }

    console.log('创建支付订单:', paymentData)

    // 调用支付接口
    const response = await createPayment(paymentData)
    console.log('支付接口响应:', response)

    if (response.code === 200) {
      // 解析返回的JSON数据
      let paymentResult
      try {
        paymentResult = typeof response.data === 'string'
          ? JSON.parse(response.data)
          : response.data
      } catch (e) {
        console.error('解析支付数据失败:', e)
        ElMessage.error('支付数据格式错误')
        return
      }

      console.log('解析后的支付数据:', paymentResult)

      // 更新支付信息
      paymentInfo.payType = paymentResult.payType
      paymentInfo.payData = paymentResult.payData
      paymentInfo.outTradeNo = paymentResult.outTradeNo
      paymentInfo.tradeNo = paymentResult.tradeNo
      paymentInfo.amount = amount
      paymentInfo.isSubscription = false  // ✅ 标记为积分充值支付

      // 根据支付类型处理
      if (paymentResult.payType === 'qrcode') {
        // 生成二维码
        await generateQRCode(paymentResult.payData)
        // 显示支付弹窗
        paymentDialogVisible.value = true
        // 等待弹窗渲染后滚动到顶部
        setTimeout(() => {
          window.scrollTo({ top: 0, behavior: 'smooth' })
        }, 100)
        // 开始轮询订单状态
        startCheckPaymentStatus()
      } else if (paymentResult.payType === 'payurl') {
        // 跳转支付
        window.open(paymentResult.payData, '_blank')
        ElMessage.success('已打开支付页面')
        // 也轮询状态
        paymentDialogVisible.value = true
        setTimeout(() => {
          window.scrollTo({ top: 0, behavior: 'smooth' })
        }, 100)
        startCheckPaymentStatus()
      } else if (paymentResult.payType === 'urlscheme') {
        // 小程序支付（暂时用弹窗提示）
        ElMessageBox.alert(
          `请使用微信打开以下链接完成支付：${paymentResult.payData}`,
          '微信小程序支付',
          {
            confirmButtonText: '我已支付',
            callback: () => {
              startCheckPaymentStatus()
            }
          }
        )
      }
    } else {
      ElMessage.error(response.msg || '创建支付订单失败')
    }
  } catch (error) {
    console.error('创建支付订单失败:', error)
    ElMessage.error(error.message || '创建订单失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleBuySubscription = async (packageId) => {
  if (!packageId) {
    ElMessage.warning('请选择订阅套餐')
    return
  }

  if (!paymentType.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  try {
    // 获取选中的套餐信息
    const selectedPkg = subscriptionPackages.value.find(pkg => pkg.packageId === packageId)
    if (!selectedPkg) {
      ElMessage.error('套餐信息不存在')
      return
    }

    await ElMessageBox.confirm(
      `确认购买 ${selectedPkg.packageName} ¥${selectedPkg.amount}？`,
      '订阅确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    loading.value = true

    // 创建订阅订单并获取支付数据
    const res = await createSubscriptionOrder(packageId, paymentType.value)

    if (res.code === 200) {
      // 解析支付数据（格式与充值订单一致）
      const paymentResult = res.data

      console.log('订阅支付数据:', paymentResult)

      // 设置支付信息（包括金额）
      paymentInfo.payType = paymentResult.payType
      paymentInfo.outTradeNo = paymentResult.outTradeNo
      paymentInfo.qrcodeUrl = paymentResult.payData || paymentResult.tradeNo
      paymentInfo.amount = parseFloat(selectedPkg.amount)  // ✅ 设置金额
      paymentInfo.isSubscription = true  // ✅ 标记为订阅支付

      // 根据支付类型处理
      if (paymentResult.payType === 'qrcode') {
        // 生成二维码
        await generateQRCode(paymentResult.payData || paymentResult.tradeNo)
        // 显示支付弹窗
        paymentDialogVisible.value = true
        // 开始轮询订单状态
        startCheckSubscriptionStatus()
      } else if (paymentResult.payType === 'payurl') {
        // 跳转支付
        window.open(paymentResult.payData, '_blank')
        ElMessage.success('已打开支付页面')
        // 显示支付弹窗
        paymentDialogVisible.value = true
        // 开始轮询订单状态
        startCheckSubscriptionStatus()
      } else {
        ElMessage.error('不支持的支付类型: ' + paymentResult.payType)
      }
    } else {
      ElMessage.error(res.msg || '创建订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('购买订阅失败:', error)
      ElMessage.error(error.message || '购买失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const getDurationText = (months) => {
  const map = { 1: '1个月', 3: '3个月', 6: '6个月', 12: '1年' }
  return map[months] || `${months}个月`
}

const formatPoints = (points) => {
  if (points >= 10000) {
    return (points / 10000).toFixed(1).replace('.0', '') + '万'
  }
  return points.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 生成二维码
const generateQRCode = async (url) => {
  try {
    qrcodeDataUrl.value = await QRCode.toDataURL(url, {
      width: 200,
      margin: 1,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成二维码失败')
  }
}

// 开始检测支付状态
const startCheckPaymentStatus = () => {
  checkProgress.value = 0
  let count = 0
  const maxCount = 180 // 最多检测3分钟（每秒一次）

  checkTimer.value = setInterval(async () => {
    count++
    checkProgress.value = Math.min((count / maxCount) * 100, 99)

    try {
      const response = await checkOrderStatus(paymentInfo.outTradeNo)

      if (response.code === 200) {
        const order = response.data

        // 检查订单状态
        if (order.status === '1') {
          // 支付成功
          checkProgress.value = 100
          clearInterval(checkTimer.value)

          ElMessage.success('支付成功！积分已到账')
          paymentDialogVisible.value = false

          // 刷新订单列表
          loadOrders()

          // 刷新页面积分（如果有显示的话）
          setTimeout(() => {
            window.location.reload()
          }, 1500)
        }
      }
    } catch (error) {
      console.error('检测订单状态失败:', error)
    }

    // 超时停止检测
    if (count >= maxCount) {
      clearInterval(checkTimer.value)
      checkProgress.value = 100
      ElMessage.warning('支付检测超时，请刷新页面查看订单状态')
    }
  }, 1000)
}

// 开始检测订阅支付状态
const startCheckSubscriptionStatus = () => {
  checkProgress.value = 0
  let count = 0
  const maxCount = 180 // 最多检测3分钟（每秒一次）

  checkTimer.value = setInterval(async () => {
    count++
    checkProgress.value = Math.min((count / maxCount) * 100, 99)

    try {
      const response = await getSubscriptionOrderStatus(paymentInfo.outTradeNo)

      if (response.code === 200) {
        const order = response.data

        // 检查订单状态
        if (order.paymentStatus === 1) {
          // 支付成功
          checkProgress.value = 100
          clearInterval(checkTimer.value)

          ElMessage.success('支付成功！订阅已激活')
          paymentDialogVisible.value = false

          // 刷新订阅信息
          await loadSubscriptionInfo()

          // 刷新页面
          setTimeout(() => {
            window.location.reload()
          }, 1500)
        }
      }
    } catch (error) {
      console.error('检测订单状态失败:', error)
    }

    // 超时停止检测
    if (count >= maxCount) {
      clearInterval(checkTimer.value)
      checkProgress.value = 100
      ElMessage.warning('支付检测超时，请刷新页面查看订单状态')
    }
  }, 1000)
}

// 关闭弹窗
const handleDialogClose = () => {
  if (checkTimer.value) {
    clearInterval(checkTimer.value)
    checkTimer.value = null
  }
  paymentDialogVisible.value = false
}

// 手动刷新订单状态
const manualCheckStatus = async () => {
  try {
    // 根据支付类型调用不同的订单查询接口
    if (paymentInfo.isSubscription) {
      // 订阅订单
      const response = await getSubscriptionOrderStatus(paymentInfo.outTradeNo)
      if (response.code === 200) {
        const order = response.data
        if (order.paymentStatus === 1) {
          ElMessage.success('支付成功！订阅已激活')
          handleDialogClose()
          await loadSubscriptionInfo()
          setTimeout(() => window.location.reload(), 1000)
        } else {
          ElMessage.info('订单仍未支付，请继续扫码')
        }
      }
    } else {
      // 积分充值订单
      const response = await checkOrderStatus(paymentInfo.outTradeNo)
      if (response.code === 200) {
        const order = response.data
        if (order.status === '1') {
          ElMessage.success('支付成功！')
          handleDialogClose()
          loadOrders()
          setTimeout(() => window.location.reload(), 1000)
        } else {
          ElMessage.info('订单仍未支付，请继续扫码')
        }
      }
    }
  } catch (error) {
    console.error('查询订单失败:', error)
    ElMessage.error('查询订单失败')
  }
}

const handlePay = async (order) => {
  try {
    const res = await pay(order.orderId)
    window.location.href = res.data
  } catch (error) {
    ElMessage.error('发起支付失败')
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

const getOrderStatusText = (status) => {
  const statusMap = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }
  return statusMap[status] || '未知'
}

const getPaymentTypeText = (type) => {
  const typeMap = { 'alipay': '支付宝', 'wxpay': '微信支付' }
  return typeMap[type] || type
}

const getSubscriptionOrderStatusText = (status) => {
  const statusMap = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '已退款' }
  return statusMap[status] || '未知'
}

const goToProfile = () => {
  router.push('/profile')
}

const goToLogin = () => {
  router.push('/login')
}

const handleLogout = async () => {
  try {
    await logout()
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('退出失败')
  }
}
</script>

<style scoped>
/* --- 整体布局 (与主页一致) --- */
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

/* --- 通用UI组件 --- */
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
  background-image: none;
}
a.mc-box:hover, button.mc-box:hover {
  background-color: rgba(60, 60, 60, 0.9);
  box-shadow: inset 2px 2px 0px #666, inset -2px -2px 0px #222;
}
button.mc-box { font-size: 14px; }

/* --- 页头 Header --- */
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

/* --- 充值页特定样式 --- */
.recharge-panel {
  background: rgba(30, 30, 30, 0.6);
  border: 3px solid #000;
  box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.panel-header {
  font-size: 20px;
  color: #fff;
  text-shadow: 2px 2px 0 #000;
  margin-bottom: 15px;
}

.info-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 14px;
}
.info-bar img { width: 32px; height: 32px; image-rendering: pixelated; }

.main-tab-switcher {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  justify-content: center;
}

.main-tab-switcher .tab-btn {
  padding: 12px 30px;
  font-size: 16px;
  background-image: url('/assets/images/new-textures/default_copper_block.png');
  background-size: 64px 64px;
  color: #ddd;
  transition: all 0.3s;
  text-shadow: 
    -1px -1px 0 #000,
    1px -1px 0 #000,
    -1px 1px 0 #000,
    1px 1px 0 #000,
    2px 2px 4px rgba(0,0,0,0.8);
}

.main-tab-switcher .tab-btn.active {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  color: #FFD700;
  border-color: #FFD700;
  transform: scale(1.05);
}

.tab-switcher {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.tab-btn {
  background-image: url('/assets/images/new-textures/default_copper_block.png');
  background-size: 64px 64px;
  color: #ddd;
}
.tab-btn.active {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  color: #FFD700;
  border-color: #FFD700;
}

.section-title {
  font-size: 16px;
  color: #ddd;
  text-shadow: 1px 1px 0 #000;
  margin: 20px 0 10px 0;
}

.package-grid, .payment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 15px;
  width: 100%;
  max-width: 800px;
}

.package-card, .payment-card {
  position: relative;
  border: 4px solid transparent;
  padding: 15px;
  font-family: inherit;
  cursor: pointer;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--mc-text-color);
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark);
  background-size: 64px 64px;
}
.stone-bg { background-color: rgba(0, 0, 0, 0.6); }
.package-card.selected, .payment-card.selected {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  border-color: #FFD700;
  box-shadow: 0 0 10px #FFD700;
}

.package-card img { width: 48px; height: 48px; image-rendering: pixelated; }
.package-card h4 { margin: 0; font-size: 16px; }
.package-card .price { margin: 0; font-size: 20px; color: #FFD700; text-shadow: 2px 2px 0 #8B0000; }
.package-card .points { font-size: 12px; color: #DDD; }
.package-card .total-points { font-size: 10px; color: #FFF; }
.package-card .bonus { color: #55FF55; font-weight: bold; }

.discount-price-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.original-price {
  text-decoration: line-through;
  color: #999 !important;
  font-size: 14px !important;
  margin: 0 !important;
}

.discounted-price {
  color: #FFD700 !important;
  font-size: 20px !important;
  margin: 0 !important;
  text-shadow: 2px 2px 0 #8B0000;
}

.discount-tag {
  font-size: 10px;
  color: #FFD700;
  background: rgba(255, 215, 0, 0.2);
  padding: 2px 6px;
  border-radius: 4px;
  margin: 2px 0 0 0;
}

.payment-card img { width: 40px; height: 40px; image-rendering: pixelated; }

.selected-mark {
  position: absolute;
  top: 5px; right: 5px;
}
.check-icon {
  width: 24px; height: 24px;
  filter: drop-shadow(0 0 2px #fff);
  animation: float 2s infinite ease-in-out;
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } }

.custom-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 20px;
  align-items: center;
}
.custom-form-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}
.mc-input {
  background: rgba(0,0,0,0.5);
  border: 2px solid #aaa;
  color: #fff;
  padding: 8px;
  font-family: inherit;
  width: 100px;
  text-align: right;
}
.calculated-points { color: #FFD700; text-shadow: 2px 2px 0 #000; }
.form-hint { font-size: 12px; color: #ccc; }

.btn-recharge {
  margin-top: 30px;
  background-image: none;
  background-color: #2a632b;
  font-size: 20px;
  padding: 12px 40px;
  border: 4px solid #000;
  box-shadow: none;
}
.btn-recharge:hover { background-color: #3c8f3e; }

/* --- 钻石订阅样式 --- */
.diamond-subscription-section {
  width: 100%;
}

.subscription-benefits {
  background-image: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  padding: 20px;
  margin-bottom: 25px;
  border: 2px solid rgba(102, 126, 234, 0.5);
  width: 100%;
  max-width: 100%;
}

.subscription-benefits h3 {
  margin: 0 0 15px 0;
  color: #FFD700;
  font-size: 18px;
  text-shadow: 2px 2px 0 #000;
  text-align: center;
}

.benefits-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.benefits-list li {
  color: #fff;
  font-size: 14px;
  text-shadow: 1px 1px 0 #000;
  padding: 8px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

/* 订阅套餐卡片使用统一的package-card样式 */
.package-card .duration {
  color: #AAAAAA;
  font-size: 12px;
  margin: 5px 0;
}

.package-card .benefits {
  color: #55FF55;
  font-size: 11px;
  font-weight: bold;
  text-align: center;
  margin: 5px 0;
}

/* --- 历史记录 --- */
.history-section h2 {
  background-image: url('/assets/images/new-textures/default_copper_block.png');
  background-size: 64px 64px;
  display: inline-block;
  padding: 10px 20px;
  margin: 30px 0 15px;
  border: 2px solid #000;
  font-size: 18px;
}

.history-list { display: flex; flex-direction: column; gap: 10px; }
.empty-history { padding: 20px; color: #aaa; }

.history-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 15px;
  font-size: 14px;
  align-items: flex-start;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: 64px 64px;
}
.history-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
  width: 100%;
}
.history-row span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.history-row img { width: 20px; height: 20px; image-rendering: pixelated; }

.btn-pay {
  padding: 4px 10px;
  font-size: 12px;
  background-image: url('/assets/images/new-textures/default_copper_block.png');
  background-size: 64px 64px;
}
.btn-pay.paid {
  background-image: url('/assets/images/new-textures/default_cobble.png');
  color: #aaa;
  cursor: default;
}

.page-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #ccc;
  text-shadow: 1px 1px 0 #000;
}

/* --- 支付弹窗样式 --- */
.payment-dialog {
  text-align: center;
  padding: 10px 0;
}

.qrcode-payment {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode-wrapper {
  margin: 10px 0;
  padding: 10px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.qrcode-image {
  display: block;
  width: 200px;
  height: 200px;
}

.payment-tips {
  margin: 12px 0;
}

.payment-tips .order-no {
  font-size: 11px;
  color: #666;
  margin-bottom: 8px;
  word-break: break-all;
}

.payment-tips .pay-hint {
  font-size: 13px;
  color: #333;
  margin: 8px 0;
}

.payment-tips .amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  margin: 10px 0;
}

.payment-status {
  width: 100%;
  margin: 12px 0;
  padding: 0 10px;
}

.progress-bar {
  width: 100%;
  height: 3px;
  background: #e4e7ed;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  transition: width 0.3s ease;
}

.status-text {
  margin-top: 8px;
  font-size: 11px;
  color: #909399;
}

.btn-refresh {
  margin-top: 12px;
  padding: 8px 24px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.btn-refresh:hover {
  background: #66b1ff;
  transform: translateY(-1px);
}

.url-payment {
  padding: 20px 15px;
}

.url-payment p {
  margin: 12px 0;
  font-size: 13px;
  color: #333;
}

/* 弹窗样式覆盖 */
:deep(.el-dialog) {
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}

:deep(.el-dialog__header) {
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  padding: 12px 20px;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 15px 20px;
  max-height: calc(90vh - 120px);
  overflow-y: auto;
}

:deep(.el-dialog__close) {
  font-size: 18px;
}

@media (max-width: 768px) {
  .mc-header .header-content { flex-direction: column; }
  .history-row { flex-direction: column; align-items: flex-start; gap: 5px; }

  .main-tab-switcher {
    flex-direction: column;
    gap: 10px;
  }

  .benefits-list {
    grid-template-columns: 1fr;
  }

  .diamond-package-grid {
    grid-template-columns: 1fr;
  }

  /* 移动端弹窗优化 */
  :deep(.el-dialog) {
    width: 95vw !important;
    margin: 5vh auto !important;
  }

  .qrcode-image {
    width: 180px !important;
    height: 180px !important;
  }

  .payment-tips .amount {
    font-size: 20px !important;
  }

  :deep(.el-dialog__body) {
    padding: 10px 15px !important;
  }
}

@media (max-width: 480px) {
  :deep(.el-dialog) {
    width: 98vw !important;
    margin: 2vh auto !important;
  }

  .qrcode-image {
    width: 160px !important;
    height: 160px !important;
  }

  .payment-tips .order-no {
    font-size: 10px !important;
  }

  .payment-tips .pay-hint {
    font-size: 12px !important;
  }
}
</style>

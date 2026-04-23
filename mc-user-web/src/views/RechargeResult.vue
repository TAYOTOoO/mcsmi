<template>
  <div class="result-page">
    <div class="result-container">
      <div class="result-card">
        <!-- 等待中 -->
        <div v-if="loading" class="loading-content">
          <el-icon class="loading-icon" :size="80"><Loading /></el-icon>
          <p class="loading-text">正在查询支付结果...</p>
        </div>

        <!-- 支付成功 -->
        <div v-else-if="orderStatus === 1" class="success-content">
          <el-icon class="success-icon" :size="100" color="#67C23A"><SuccessFilled /></el-icon>
          <h2 class="result-title">支付成功！</h2>
          <div class="result-info">
            <p>订单号：{{ orderInfo.outTradeNo }}</p>
            <p>充值金额：¥{{ orderInfo.amount }}</p>
            <p class="points-info">
              获得积分：<strong class="points-number">{{ orderInfo.points }}</strong>
            </p>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="goToGenerate">
              去生成材质包
            </el-button>
            <el-button size="large" @click="goToHome">返回首页</el-button>
          </div>
        </div>

        <!-- 待支付 -->
        <div v-else-if="orderStatus === 0" class="pending-content">
          <el-icon class="pending-icon" :size="100" color="#E6A23C"><Clock /></el-icon>
          <h2 class="result-title">等待支付</h2>
          <div class="result-info">
            <p>订单号：{{ orderInfo.outTradeNo }}</p>
            <p>充值金额：¥{{ orderInfo.amount }}</p>
            <p>如已完成支付，请等待系统确认...</p>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="refreshStatus">
              刷新状态
            </el-button>
            <el-button size="large" @click="goToRecharge">返回充值</el-button>
          </div>
        </div>

        <!-- 支付失败 -->
        <div v-else class="fail-content">
          <el-icon class="fail-icon" :size="100" color="#F56C6C"><CircleCloseFilled /></el-icon>
          <h2 class="result-title">支付失败</h2>
          <div class="result-info">
            <p>订单号：{{ orderInfo.outTradeNo }}</p>
            <p>请重新发起支付</p>
          </div>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="goToRecharge">
              重新充值
            </el-button>
            <el-button size="large" @click="goToHome">返回首页</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, SuccessFilled, CircleCloseFilled, Clock } from '@element-plus/icons-vue'
import { checkOrderStatus } from '@/api/payment'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const orderStatus = ref(null)
const orderInfo = ref({})

onMounted(() => {
  // 易支付回调返回的参数名是 out_trade_no
  const orderNo = route.query.out_trade_no || route.query.orderNo
  if (!orderNo) {
    ElMessage.error('订单号不存在')
    router.push('/recharge')
    return
  }

  loadOrderStatus(orderNo)
})

const loadOrderStatus = async (orderNo) => {
  try {
    loading.value = true
    const res = await checkOrderStatus(orderNo)
    if (res.code === 200) {
      orderInfo.value = res.data
      orderStatus.value = res.data.status === '1' ? 1 : 0
    } else {
      ElMessage.error(res.msg || '查询订单失败')
    }
  } catch (error) {
    ElMessage.error('查询订单失败')
  } finally {
    loading.value = false
  }
}

const refreshStatus = () => {
  const orderNo = route.query.out_trade_no || route.query.orderNo
  loadOrderStatus(orderNo)
}

const goToGenerate = () => {
  router.push('/generate')
}

const goToHome = () => {
  router.push('/')
}

const goToRecharge = () => {
  router.push('/recharge')
}
</script>

<style scoped lang="scss">
.result-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.result-container {
  width: 100%;
  max-width: 600px;
}

.result-card {
  background: white;
  border-radius: 16px;
  padding: 60px 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.loading-content {
  .loading-icon {
    animation: rotate 1.5s linear infinite;
    color: #667eea;
  }

  .loading-text {
    margin-top: 20px;
    font-size: 18px;
    color: #666;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.success-icon,
.pending-icon,
.fail-icon {
  margin-bottom: 20px;
}

.result-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
}

.result-info {
  margin-bottom: 40px;

  p {
    font-size: 16px;
    color: #666;
    margin: 10px 0;
  }

  .points-info {
    font-size: 20px;
    margin-top: 20px;

    .points-number {
      color: #667eea;
      font-size: 32px;
    }
  }
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;

  .el-button {
    min-width: 150px;
  }
}

@media (max-width: 768px) {
  .result-card {
    padding: 40px 20px;
  }

  .result-title {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>

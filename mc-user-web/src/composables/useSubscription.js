import { ref, computed, onMounted, onActivated } from 'vue'
import { getMySubscription } from '@/api/subscription'

const CACHE_KEY = 'subscriptionCache'

function readCache() {
  try {
    // 无token则不使用缓存（防止退出后残留）
    if (!localStorage.getItem('token')) return null
    const raw = localStorage.getItem(CACHE_KEY)
    if (!raw) return null
    const cached = JSON.parse(raw)
    // 缓存1小时有效
    if (Date.now() - cached.time > 60 * 60 * 1000) return null
    return cached.data
  } catch { return null }
}

function writeCache(data) {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({ data, time: Date.now() }))
  } catch {}
}

export function clearSubscriptionCache() {
  localStorage.removeItem(CACHE_KEY)
}

export function useSubscription() {
  // 优先使用缓存，让按钮立即显示，无需等待API
  const cached = readCache()
  const subscriptionInfo = ref(cached || {
    isSubscribed: false,
    subscriptionType: '',
    expireTime: '',
    isPermanent: false
  })

  const isDiamond = computed(() => subscriptionInfo.value.isSubscribed)

  const loadSubscriptionInfo = async () => {
    // 只有在用户已登录时才查询订阅信息
    const token = localStorage.getItem('token')
    if (!token) {
      // 未登录时清空订阅信息
      subscriptionInfo.value = {
        isSubscribed: false,
        subscriptionType: '',
        expireTime: '',
        isPermanent: false
      }
      clearSubscriptionCache()
      return
    }

    try {
      const res = await getMySubscription()
      if (res.code === 200 && res.data) {
        const info = {
          isSubscribed: res.data.subscriptionType === 'diamond',
          subscriptionType: res.data.subscriptionType || '',
          expireTime: res.data.expireTime || '',
          isPermanent: res.data.isPermanent || false
        }
        subscriptionInfo.value = info
        writeCache(info)
      }
    } catch (error) {
      console.error('加载订阅信息失败', error)
    }
  }

  // Auto load on mount and when component is activated (for keep-alive)
  onMounted(() => {
    loadSubscriptionInfo()
  })

  onActivated(() => {
    loadSubscriptionInfo()
  })

  return {
    subscriptionInfo,
    isDiamond,
    loadSubscriptionInfo
  }
}

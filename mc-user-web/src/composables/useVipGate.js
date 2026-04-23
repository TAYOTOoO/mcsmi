import { ref, onMounted } from 'vue'
import { useSubscription } from './useSubscription'
import request from '@/utils/request'

// VIP功能配置缓存（全局单例）
let vipFeaturesCache = null
let vipFeaturesCacheTime = 0
const CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存

export function useVipGate() {
  const { isDiamond, subscriptionInfo, loadSubscriptionInfo } = useSubscription()
  const vipFeatures = ref({})
  const vipFeaturesLoaded = ref(false)

  /**
   * 加载VIP功能配置（从后端mc_system_config表）
   */
  const loadVipFeatures = async () => {
    // 使用缓存
    if (vipFeaturesCache && (Date.now() - vipFeaturesCacheTime < CACHE_TTL)) {
      vipFeatures.value = vipFeaturesCache
      vipFeaturesLoaded.value = true
      return
    }

    try {
      // 批量获取所有vip.feature.* 配置
      const keys = [
        'vip.feature.smart_edge_repair',
        'vip.feature.auto_bg_detect',
        'vip.feature.color_tolerance',
        'vip.feature.alpha_binarize',
        'vip.feature.history_library',
        'vip.feature.presets',
        'vip.feature.factory_entry',
        'vip.feature.fringe_repair'
      ]

      const features = {}
      await Promise.all(keys.map(async (key) => {
        try {
          const res = await request({
            url: '/mc/system/config/key/' + key,
            method: 'get'
          })
          if (res.code === 200) {
            // 若依AjaxResult.success(String)会把值放到msg而不是data
            // 因为Java方法重载优先匹配success(String msg)而非success(Object data)
            const val = res.data != null ? res.data : res.msg
            features[key] = val === '1' || val === 'true' || val === true
          }
        } catch (e) {
          // 默认启用
          features[key] = true
        }
      }))

      vipFeatures.value = features
      vipFeaturesCache = features
      vipFeaturesCacheTime = Date.now()
      vipFeaturesLoaded.value = true
    } catch (error) {
      console.error('加载VIP功能配置失败', error)
      vipFeaturesLoaded.value = true
    }
  }

  /**
   * 检查用户是否为VIP（钻石订阅用户）
   */
  const isVip = () => {
    return isDiamond.value
  }

  /**
   * 检查某个VIP功能是否可用（用户是VIP + 功能已启用）
   * @param {string} featureKey - 功能键名，如 'smart_edge_repair'
   */
  const isFeatureAvailable = (featureKey) => {
    if (!isDiamond.value) return false
    const fullKey = featureKey.startsWith('vip.feature.') ? featureKey : `vip.feature.${featureKey}`
    // 如果配置未加载完成，默认对VIP用户启用
    if (!vipFeaturesLoaded.value) return true
    return vipFeatures.value[fullKey] !== false
  }

  /**
   * 要求VIP权限，非VIP时弹出提示
   * @param {string} featureName - 功能名称（用于提示）
   * @returns {boolean} 是否有权限
   */
  const requireVip = (featureName) => {
    if (isDiamond.value) return true
    // 返回 false，调用方自行处理提示
    return false
  }

  onMounted(() => {
    loadVipFeatures()
  })

  return {
    isDiamond,
    subscriptionInfo,
    vipFeatures,
    vipFeaturesLoaded,
    isVip,
    isFeatureAvailable,
    requireVip,
    loadVipFeatures,
    loadSubscriptionInfo
  }
}

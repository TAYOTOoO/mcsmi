import { ref } from 'vue'
import { getUserPageConfigByPage } from '@/api/pageConfig'

/**
 * 用户端页面配置加载
 * @param {string} pageName - 页面名称（home/generate/tasks等）
 * @returns {Object} - 包含配置数据和加载状态的响应式对象
 */
export function usePageConfig(pageName) {
  const configs = ref([])
  const configMap = ref({})
  const loading = ref(false)
  const error = ref(null)

  // 立即从缓存加载配置（同步），避免首次渲染时闪烁
  const cacheKey = `pageConfig_${pageName}`
  try {
    const cachedData = localStorage.getItem(cacheKey)
    if (cachedData) {
      const cached = JSON.parse(cachedData)
      // 检查缓存是否过期（24小时）
      const now = Date.now()
      if (cached.timestamp && now - cached.timestamp < 24 * 60 * 60 * 1000) {
        configMap.value = cached.data
      }
    }
  } catch (err) {
    console.error('读取配置缓存失败:', err)
  }

  /**
   * 加载页面配置
   */
  const loadConfig = async () => {
    loading.value = true
    error.value = null

    try {
      const response = await getUserPageConfigByPage(pageName)
      configs.value = response.data || []

      // 构建配置映射表，方便通过key快速获取配置值
      const map = {}
      configs.value.forEach(config => {
        map[config.configKey] = config.configValue
      })
      configMap.value = map

      // 保存到缓存
      try {
        localStorage.setItem(cacheKey, JSON.stringify({
          data: map,
          timestamp: Date.now()
        }))
      } catch (err) {
        console.error('保存配置缓存失败:', err)
      }

      return configMap.value
    } catch (err) {
      console.error(`加载页面配置失败 (${pageName}):`, err)
      error.value = err
      return {}
    } finally {
      loading.value = false
    }
  }

  /**
   * 根据配置键获取配置值
   * @param {string} key - 配置键
   * @param {*} defaultValue - 默认值
   * @returns {*} - 配置值或默认值
   */
  const getConfig = (key, defaultValue = '') => {
    return configMap.value[key] || defaultValue
  }

  /**
   * 根据分组获取配置列表
   * @param {string} groupName - 分组名称
   * @returns {Array} - 配置列表
   */
  const getConfigsByGroup = (groupName) => {
    return configs.value.filter(config => config.groupName === groupName)
  }

  return {
    configs,
    configMap,
    loading,
    error,
    loadConfig,
    getConfig,
    getConfigsByGroup
  }
}

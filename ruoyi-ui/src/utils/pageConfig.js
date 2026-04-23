import { getUserPageConfigByPage } from '@/api/mc/pageConfig'

/**
 * 页面配置加载工具
 * Vue2版本 - 使用Mixin方式
 */
export function createPageConfig(pageName) {
  return {
    data() {
      return {
        pageConfig: {},
        configLoading: false
      }
    },
    created() {
      this.loadPageConfig()
    },
    methods: {
      async loadPageConfig() {
        try {
          this.configLoading = true
          const res = await getUserPageConfigByPage(pageName)
          if (res.code === 200 && res.data) {
            const configMap = {}
            res.data.forEach(item => {
              configMap[item.configKey] = item.configValue
            })
            this.pageConfig = configMap
          }
        } catch (error) {
          console.error(`加载页面配置失败 (${pageName}):`, error)
          // 不影响页面正常显示，使用默认值
        } finally {
          this.configLoading = false
        }
      },
      getConfig(key, defaultValue = '') {
        return this.pageConfig[key] || defaultValue
      },
      getConfigsByGroup(group) {
        const result = []
        Object.keys(this.pageConfig).forEach(key => {
          if (key.startsWith(`${group}.`)) {
            result.push({
              key: key,
              value: this.pageConfig[key]
            })
          }
        })
        return result
      }
    }
  }
}

/**
 * 使用示例：
 *
 * import { createPageConfig } from '@/utils/pageConfig'
 *
 * export default {
 *   name: 'Home',
 *   mixins: [createPageConfig('home')],
 *   mounted() {
 *     const title = this.getConfig('home.hero.title', '默认标题')
 *     console.log(title)
 *   }
 * }
 */

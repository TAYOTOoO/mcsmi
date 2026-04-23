/**
 * 图片加载 Mixin
 * 用于处理需要 token 验证的图片资源
 *
 * 使用方法：
 * 1. 在组件中引入: import imageLoader from '@/mixins/imageLoader'
 * 2. 在 mixins 中注册: mixins: [imageLoader]
 * 3. 使用 loadAuthImage 方法加载图片
 */

import { getToken } from '@/utils/auth'

export default {
  data() {
    return {
      // 图片 blob URL 缓存
      imageBlobCache: {}
    }
  },

  methods: {
    /**
     * 加载需要鉴权的图片
     * @param {String} path - 图片路径（可以是相对路径或绝对路径）
     * @param {Number} taskId - 任务ID（用于鉴权）
     * @returns {Promise<String>} - blob URL
     */
    async loadAuthImage(path, taskId) {
      if (!path) {
        return ''
      }

      // 如果已经是 blob URL，直接返回
      if (path.startsWith('blob:')) {
        return path
      }

      // 检查缓存
      const cacheKey = `${taskId}_${path}`
      if (this.imageBlobCache[cacheKey]) {
        return this.imageBlobCache[cacheKey]
      }

      try {
        // 构建 API 路径
        const apiUrl = this.getImageApiUrl(path, taskId)
        if (!apiUrl) {
          console.warn('无法构建图片API路径:', path)
          return ''
        }

        // 使用 fetch 请求图片（携带 token）
        const token = getToken()
        const response = await fetch(apiUrl, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (!response.ok) {
          console.error('图片加载失败:', response.status, apiUrl)
          return ''
        }

        // 转换为 blob
        const blob = await response.blob()
        // 创建 blob URL
        const blobUrl = URL.createObjectURL(blob)

        // 缓存 blob URL
        this.imageBlobCache[cacheKey] = blobUrl

        return blobUrl
      } catch (error) {
        console.error('加载图片失败:', error, path)
        return ''
      }
    },

    /**
     * 构建图片API路径
     * @param {String} path - 原始路径
     * @param {Number} taskId - 任务ID
     * @returns {String} - API 路径
     */
    getImageApiUrl(path, taskId) {
      if (!path) return ''

      // 提取文件名
      const fileName = this.extractFileName(path)
      if (!fileName) return ''

      // 判断资源类型
      let resourceType = 'temp' // 默认temp
      if (path.includes('/textures/') || path.includes('\\textures\\')) {
        resourceType = 'texture'
      } else if (path.includes('/output/') || path.includes('\\output\\')) {
        resourceType = 'download'
      }

      // 构建 API 路径
      return `${process.env.VUE_APP_BASE_API}/mc/resource/${resourceType}?path=${fileName}&taskId=${taskId}`
    },

    /**
     * 提取文件名
     * @param {String} path - 路径
     * @returns {String} - 文件名
     */
    extractFileName(path) {
      if (!path) return ''

      // 移除可能的 URL 前缀
      let cleanPath = path
      if (path.startsWith('http://') || path.startsWith('https://')) {
        try {
          const url = new URL(path)
          cleanPath = url.pathname
        } catch (e) {
          console.error('解析URL失败:', e)
        }
      }

      // 提取最后的文件名
      const parts = cleanPath.split(/[/\\]/)
      return parts[parts.length - 1]
    }
  },

  /**
   * 组件销毁时清理 blob URL
   */
  beforeDestroy() {
    // 释放所有 blob URL
    Object.values(this.imageBlobCache).forEach(blobUrl => {
      if (blobUrl && blobUrl.startsWith('blob:')) {
        URL.revokeObjectURL(blobUrl)
      }
    })
    this.imageBlobCache = {}
  }
}

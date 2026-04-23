import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 通过vite代理转发到 http://localhost:8080
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 若依框架的响应格式
    if (res.code !== 200 && res.code !== undefined) {
      // 401/403: 认证失败，登录过期
      if (res.code === 401 || res.code === 403) {
        // 公开API不跳转登录页
        const isPublicApi = response.config.url && response.config.url.includes('/public/')
        if (!isPublicApi) {
          ElMessage.error('登录已失效，请重新登录')

          // 清除本地存储的认证信息
          localStorage.removeItem('token')
          localStorage.removeItem('username')

          // 跳转到登录页面
          router.push('/login')
        }

        return Promise.reject(new Error('登录已失效'))
      }

      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误：', error)

    // 处理HTTP状态码401/403
    if (error.response) {
      const status = error.response.status

      if (status === 401 || status === 403) {
        // 公开API不跳转登录页
        const isPublicApi = error.config && error.config.url && error.config.url.includes('/public/')
        if (!isPublicApi) {
          ElMessage.error('登录已失效，请重新登录')

          // 清除本地存储的认证信息
          localStorage.removeItem('token')
          localStorage.removeItem('username')

          // 跳转到登录页面
          router.push('/login')
        }

        return Promise.reject(new Error('登录已失效'))
      }
    }

    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service

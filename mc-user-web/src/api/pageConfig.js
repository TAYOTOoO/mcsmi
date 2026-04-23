import request from '@/utils/request'

/**
 * 根据页面名称获取配置列表（公开API，无需登录）
 */
export function getUserPageConfigByPage(pageName) {
  return request({
    url: '/mc/userpage/public/page/' + pageName,
    method: 'get'
  })
}

/**
 * 根据配置键获取配置（公开API，无需登录）
 */
export function getUserPageConfigByKey(configKey) {
  return request({
    url: '/mc/userpage/public/key/' + configKey,
    method: 'get'
  })
}

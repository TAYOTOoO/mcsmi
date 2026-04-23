import request from '@/utils/request'

/**
 * 页面配置API（公开API，无需登录）
 * 用于动态配置页面文案、按钮文本等
 */

/**
 * 根据页面名称获取配置列表
 */
export function getUserPageConfigByPage(pageName) {
  return request({
    url: '/mc/userpage/public/page/' + pageName,
    headers: { isToken: false, skipAutoRedirect: true },
    method: 'get'
  })
}

/**
 * 根据配置键获取配置
 */
export function getUserPageConfigByKey(configKey) {
  return request({
    url: '/mc/userpage/public/key/' + configKey,
    headers: { isToken: false, skipAutoRedirect: true },
    method: 'get'
  })
}

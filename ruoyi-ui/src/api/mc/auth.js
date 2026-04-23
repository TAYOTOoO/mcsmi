import request from '@/utils/request'

/**
 * 用户端认证API
 * 用于用户端的登录、注册等功能
 */

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/login',
    headers: { isToken: false },
    method: 'post',
    data: data
  })
}

/**
 * 用户注册
 */
export function register(data) {
  return request({
    url: '/register',
    headers: { isToken: false },
    method: 'post',
    data: data
  })
}

/**
 * 发送邮箱验证码
 */
export function sendEmailCode(data) {
  return request({
    url: '/sendEmailCode',
    headers: { isToken: false },
    method: 'post',
    data: data
  })
}

/**
 * 获取用户信息
 */
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

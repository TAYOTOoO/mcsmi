import request from '@/utils/request'

/**
 * 用户登录
 */
export function login(data) {
  return request({
    url: '/login',
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

/**
 * 获取邮箱验证开关配置
 */
export function getEmailVerifyConfig() {
  return request({
    url: '/mc/system/config/key/system.register.emailVerify',
    method: 'get'
  })
}


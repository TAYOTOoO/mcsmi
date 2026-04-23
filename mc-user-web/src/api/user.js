import request from '@/utils/request'

// 获取用户信息
export function getUserProfile() {
  return request({
    url: '/mc/user/profile',
    method: 'get'
  })
}

// 获取积分信息
export function getPointsInfo() {
  return request({
    url: '/mc/user/points',
    method: 'get'
  })
}

// 获取积分变动记录
export function getPointsRecords(params) {
  return request({
    url: '/mc/user/points/records',
    method: 'get',
    params
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/mc/user/password',
    method: 'put',
    data
  })
}

// 修改邮箱
export function updateEmail(data) {
  return request({
    url: '/mc/user/email',
    method: 'put',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

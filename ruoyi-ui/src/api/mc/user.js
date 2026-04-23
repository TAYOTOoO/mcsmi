import request from '@/utils/request'

// 查询用户列表（包含积分）
export function listUser(query) {
  return request({
    url: '/mc/user/list',
    method: 'get',
    params: query
  })
}

// 查询用户详细
export function getUser(userId) {
  return request({
    url: '/mc/user/' + userId,
    method: 'get'
  })
}

// 修改用户积分
export function updateUserPoints(data) {
  return request({
    url: '/mc/user/points',
    method: 'put',
    data: data
  })
}

// 初始化用户积分
export function initUserPoints() {
  return request({
    url: '/mc/user/initPoints',
    method: 'post'
  })
}

// 修改用户状态
export function updateUserStatus(data) {
  return request({
    url: '/mc/user/status',
    method: 'put',
    data: data
  })
}

// 重置用户密码
export function resetUserPwd(data) {
  return request({
    url: '/mc/user/resetPwd',
    method: 'put',
    data: data
  })
}

// 批量修改用户状态
export function batchUpdateStatus(data) {
  return request({
    url: '/mc/user/batchStatus',
    method: 'put',
    data: data
  })
}

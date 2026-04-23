import request from '@/utils/request'

// 查询用户端页面配置列表
export function listUserPageConfig(query) {
  return request({
    url: '/mc/userpage/list',
    method: 'get',
    params: query
  })
}

// 查询用户端页面配置详细
export function getUserPageConfig(configId) {
  return request({
    url: '/mc/userpage/' + configId,
    method: 'get'
  })
}

// 根据配置键获取配置
export function getUserPageConfigByKey(configKey) {
  return request({
    url: '/mc/userpage/public/key/' + configKey,
    method: 'get'
  })
}

// 根据页面名称获取配置列表
export function getUserPageConfigByPage(pageName) {
  return request({
    url: '/mc/userpage/public/page/' + pageName,
    method: 'get'
  })
}

// 新增用户端页面配置
export function addUserPageConfig(data) {
  return request({
    url: '/mc/userpage',
    method: 'post',
    data: data
  })
}

// 修改用户端页面配置
export function updateUserPageConfig(data) {
  return request({
    url: '/mc/userpage',
    method: 'put',
    data: data
  })
}

// 删除用户端页面配置
export function delUserPageConfig(configIds) {
  return request({
    url: '/mc/userpage/' + configIds,
    method: 'delete'
  })
}

// 导出用户端页面配置
export function exportUserPageConfig(query) {
  return request({
    url: '/mc/userpage/export',
    method: 'post',
    params: query
  })
}

import request from '@/utils/request'

// 查询系统配置列表
export function listConfig(query) {
  return request({
    url: '/mc/system/config/list',
    method: 'get',
    params: query
  })
}

// 查询系统配置详细
export function getConfig(configId) {
  return request({
    url: '/mc/system/config/' + configId,
    method: 'get'
  })
}

// 根据配置键获取配置值
export function getConfigKey(configKey) {
  return request({
    url: '/mc/system/config/key/' + configKey,
    method: 'get'
  })
}

// 新增系统配置
export function addConfig(data) {
  return request({
    url: '/mc/system/config',
    method: 'post',
    data: data
  })
}

// 修改系统配置
export function updateConfig(data) {
  return request({
    url: '/mc/system/config',
    method: 'put',
    data: data
  })
}

// 删除系统配置
export function delConfig(configId) {
  return request({
    url: '/mc/system/config/' + configId,
    method: 'delete'
  })
}

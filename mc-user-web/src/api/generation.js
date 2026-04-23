import request from '@/utils/request'

// 查询生成任务列表
export function listGeneration(query) {
  return request({
    url: '/mc/generate/list',
    method: 'get',
    params: query
  })
}

// 查询生成任务详细
export function getGeneration(taskId) {
  return request({
    url: '/mc/generate/' + taskId,
    method: 'get'
  })
}

// 新增生成任务
export function addGeneration(data) {
  return request({
    url: '/mc/generate',
    method: 'post',
    data: data
  })
}

// 重新生成
export function regenerateTask(taskId) {
  return request({
    url: '/mc/generate/regenerate/' + taskId,
    method: 'post'
  })
}

// 获取用户积分
export function getUserPoints() {
  return request({
    url: '/mc/points/my',
    method: 'get'
  })
}

// 查询任务的材质列表
export function getTaskTextures(taskId) {
  return request({
    url: '/mc/texture/list/' + taskId,
    method: 'get'
  })
}

// 查询所有用户的生成任务列表（管理员专用）
export function adminListGeneration(query) {
  return request({
    url: '/mc/generate/admin/list',
    method: 'get',
    params: query
  })
}

// 关闭任务并返还积分（管理员专用）
export function cancelTask(taskId) {
  return request({
    url: '/mc/generate/cancel/' + taskId,
    method: 'post'
  })
}

// 获取所有启用的模板列表
export function getActiveTemplates(query) {
  return request({
    url: '/mc/prompt/activeList',
    method: 'get',
    params: query
  })
}

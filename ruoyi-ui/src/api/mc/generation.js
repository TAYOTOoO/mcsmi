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

// 修改生成任务
export function updateGeneration(data) {
  return request({
    url: '/mc/generate',
    method: 'put',
    data: data
  })
}

// 删除生成任务
export function delGeneration(taskId) {
  return request({
    url: '/mc/generate/' + taskId,
    method: 'delete'
  })
}

// 重新生成
export function regenerate(taskId) {
  return request({
    url: '/mc/generate/regenerate/' + taskId,
    method: 'post'
  })
}

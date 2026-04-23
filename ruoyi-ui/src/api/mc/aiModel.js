import request from '@/utils/request'

// 查询AI模型列表
export function listAiModel(query) {
  return request({
    url: '/mc/ai-model/list',
    method: 'get',
    params: query
  })
}

// 查询AI模型详细
export function getAiModel(modelId) {
  return request({
    url: '/mc/ai-model/' + modelId,
    method: 'get'
  })
}

// 新增AI模型
export function addAiModel(data) {
  return request({
    url: '/mc/ai-model',
    method: 'post',
    data: data
  })
}

// 修改AI模型
export function updateAiModel(data) {
  return request({
    url: '/mc/ai-model',
    method: 'put',
    data: data
  })
}

// 删除AI模型
export function delAiModel(modelId) {
  return request({
    url: '/mc/ai-model/' + modelId,
    method: 'delete'
  })
}

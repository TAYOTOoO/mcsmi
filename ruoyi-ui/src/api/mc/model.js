import request from '@/utils/request'

// 查询AI模型配置列表
export function listModel(query) {
  return request({
    url: '/mc/model/list',
    method: 'get',
    params: query
  })
}

// 查询AI模型配置详细
export function getModel(modelId) {
  return request({
    url: '/mc/model/' + modelId,
    method: 'get'
  })
}

// 新增AI模型配置
export function addModel(data) {
  return request({
    url: '/mc/model',
    method: 'post',
    data: data
  })
}

// 修改AI模型配置
export function updateModel(data) {
  return request({
    url: '/mc/model',
    method: 'put',
    data: data
  })
}

// 删除AI模型配置
export function delModel(modelId) {
  return request({
    url: '/mc/model/' + modelId,
    method: 'delete'
  })
}

// 测试模型连通性
export function testModel(modelId) {
  return request({
    url: '/mc/model/test/' + modelId,
    method: 'get'
  })
}

// 获取可用模型列表
export function fetchModels(params) {
  return request({
    url: '/mc/model/fetchModels',
    method: 'get',
    params: params
  })
}

// 设置启用的模型
export function setActiveModel(modelId, data) {
  return request({
    url: '/mc/model/setActive/' + modelId,
    method: 'put',
    data: data
  })
}

// 获取当前启用的模型
export function getActiveModels() {
  return request({
    url: '/mc/model/getActiveModels',
    method: 'get'
  })
}

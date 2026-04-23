import request from '@/utils/request'

// 查询模板模型配置列表
export function listTemplateModelConfig(query) {
  return request({
    url: '/mc/template-model-config/list',
    method: 'get',
    params: query
  })
}

// 查询模板的模型配置
export function getTemplateModelConfig(templateId) {
  return request({
    url: '/mc/template-model-config/template/' + templateId,
    method: 'get'
  })
}

// 查询模板模型配置详细
export function getTemplateModelConfigDetail(configId) {
  return request({
    url: '/mc/template-model-config/' + configId,
    method: 'get'
  })
}

// 新增模板模型配置
export function addTemplateModelConfig(data) {
  return request({
    url: '/mc/template-model-config',
    method: 'post',
    data: data
  })
}

// 修改模板模型配置
export function updateTemplateModelConfig(data) {
  return request({
    url: '/mc/template-model-config',
    method: 'put',
    data: data
  })
}

// 删除模板模型配置
export function delTemplateModelConfig(configId) {
  return request({
    url: '/mc/template-model-config/' + configId,
    method: 'delete'
  })
}

// 批量更新优先级
export function updatePriority(data) {
  return request({
    url: '/mc/template-model-config/priority',
    method: 'put',
    data: data
  })
}

import request from '@/utils/request'

// 查询预提示词模板列表
export function listPromptTemplate(query) {
  return request({
    url: '/mc/prompt/list',
    method: 'get',
    params: query
  })
}

// 查询预提示词模板详细
export function getPromptTemplate(templateId) {
  return request({
    url: '/mc/prompt/' + templateId,
    method: 'get'
  })
}

// 获取当前启用的预提示词模板
export function getActiveTemplate() {
  return request({
    url: '/mc/prompt/active',
    method: 'get'
  })
}

// 获取所有启用的预提示词模板列表
export function getActiveTemplates() {
  return request({
    url: '/mc/prompt/activeList',
    method: 'get'
  })
}

// 新增预提示词模板
export function addPromptTemplate(data) {
  return request({
    url: '/mc/prompt',
    method: 'post',
    data: data
  })
}

// 修改预提示词模板
export function updatePromptTemplate(data) {
  return request({
    url: '/mc/prompt',
    method: 'put',
    data: data
  })
}

// 删除预提示词模板
export function delPromptTemplate(templateIds) {
  return request({
    url: '/mc/prompt/' + templateIds,
    method: 'delete'
  })
}

// 启用预提示词模板
export function activatePromptTemplate(templateId) {
  return request({
    url: '/mc/prompt/activate/' + templateId,
    method: 'put'
  })
}

import request from '@/utils/request'

// 查询订阅套餐列表
export function listSubscriptionPackage(query) {
  return request({
    url: '/mc/subscription/package/list',
    method: 'get',
    params: query
  })
}

// 查询订阅套餐详细
export function getSubscriptionPackage(packageId) {
  return request({
    url: '/mc/subscription/package/' + packageId,
    method: 'get'
  })
}

// 新增订阅套餐
export function addSubscriptionPackage(data) {
  return request({
    url: '/mc/subscription/package',
    method: 'post',
    data: data
  })
}

// 修改订阅套餐
export function updateSubscriptionPackage(data) {
  return request({
    url: '/mc/subscription/package',
    method: 'put',
    data: data
  })
}

// 删除订阅套餐
export function delSubscriptionPackage(packageIds) {
  return request({
    url: '/mc/subscription/package/' + packageIds,
    method: 'delete'
  })
}

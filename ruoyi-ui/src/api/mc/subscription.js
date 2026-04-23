import request from '@/utils/request'

// 订阅套餐管理 APIs
export function listPackage(query) {
  return request({
    url: '/mc/subscription/package/list',
    method: 'get',
    params: query
  })
}

export function getPackage(packageId) {
  return request({
    url: '/mc/subscription/package/' + packageId,
    method: 'get'
  })
}

export function addPackage(data) {
  return request({
    url: '/mc/subscription/package',
    method: 'post',
    data: data
  })
}

export function updatePackage(data) {
  return request({
    url: '/mc/subscription/package',
    method: 'put',
    data: data
  })
}

export function delPackage(packageIds) {
  return request({
    url: '/mc/subscription/package/' + packageIds,
    method: 'delete'
  })
}

// 订阅订单管理 APIs
export function listOrder(query) {
  return request({
    url: '/mc/subscription/order/list',
    method: 'get',
    params: query
  })
}

export function getOrder(orderId) {
  return request({
    url: '/mc/subscription/order/' + orderId,
    method: 'get'
  })
}

// 用户订阅管理 APIs
export function updateUserSubscription(data) {
  return request({
    url: '/mc/user/subscription/update',
    method: 'post',
    data: data
  })
}

export function grantUserPoints(data) {
  return request({
    url: '/mc/user/points/grant',
    method: 'post',
    data: data
  })
}

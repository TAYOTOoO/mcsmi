import request from '@/utils/request'

// 查询订阅订单列表
export function listSubscriptionOrder(query) {
  return request({
    url: '/mc/subscriptionOrder/list',
    method: 'get',
    params: query
  })
}

// 查询订阅订单详细
export function getSubscriptionOrder(orderId) {
  return request({
    url: '/mc/subscriptionOrder/' + orderId,
    method: 'get'
  })
}

// 新增订阅订单
export function addSubscriptionOrder(data) {
  return request({
    url: '/mc/subscriptionOrder',
    method: 'post',
    data: data
  })
}

// 修改订阅订单
export function updateSubscriptionOrder(data) {
  return request({
    url: '/mc/subscriptionOrder',
    method: 'put',
    data: data
  })
}

// 删除订阅订单
export function delSubscriptionOrder(orderId) {
  return request({
    url: '/mc/subscriptionOrder/' + orderId,
    method: 'delete'
  })
}

// 导出订阅订单
export function exportSubscriptionOrder(query) {
  return request({
    url: '/mc/subscriptionOrder/export',
    method: 'get',
    params: query
  })
}

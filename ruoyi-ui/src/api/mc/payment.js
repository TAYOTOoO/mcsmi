import request from '@/utils/request'

// 查询支付配置列表
export function listPaymentConfig(query) {
  return request({
    url: '/mc/paymentConfig/list',
    method: 'get',
    params: query
  })
}

// 查询支付配置详细
export function getPaymentConfig(configId) {
  return request({
    url: '/mc/paymentConfig/' + configId,
    method: 'get'
  })
}

// 查询默认支付配置
export function getDefaultPaymentConfig() {
  return request({
    url: '/mc/paymentConfig/default',
    method: 'get'
  })
}

// 新增支付配置
export function addPaymentConfig(data) {
  return request({
    url: '/mc/paymentConfig',
    method: 'post',
    data: data
  })
}

// 修改支付配置
export function updatePaymentConfig(data) {
  return request({
    url: '/mc/paymentConfig',
    method: 'put',
    data: data
  })
}

// 删除支付配置
export function delPaymentConfig(configId) {
  return request({
    url: '/mc/paymentConfig/' + configId,
    method: 'delete'
  })
}

// 导出支付配置
export function exportPaymentConfig(query) {
  return request({
    url: '/mc/paymentConfig/export',
    method: 'post',
    params: query
  })
}

// 查询支付订单列表
export function listPaymentOrder(query) {
  return request({
    url: '/mc/paymentOrder/list',
    method: 'get',
    params: query
  })
}

// 查询支付订单详细
export function getPaymentOrder(orderId) {
  return request({
    url: '/mc/paymentOrder/' + orderId,
    method: 'get'
  })
}

// 新增支付订单
export function addPaymentOrder(data) {
  return request({
    url: '/mc/paymentOrder',
    method: 'post',
    data: data
  })
}

// 修改支付订单
export function updatePaymentOrder(data) {
  return request({
    url: '/mc/paymentOrder',
    method: 'put',
    data: data
  })
}

// 删除支付订单
export function delPaymentOrder(orderId) {
  return request({
    url: '/mc/paymentOrder/' + orderId,
    method: 'delete'
  })
}

// 导出支付订单
export function exportPaymentOrder(query) {
  return request({
    url: '/mc/paymentOrder/export',
    method: 'post',
    params: query
  })
}

// 测试支付
export function testPayment(data) {
  return request({
    url: '/mc/paymentOrder/test',
    method: 'post',
    data: data
  })
}

// 查询订单状态
export function checkOrderStatus(outTradeNo) {
  return request({
    url: '/mc/paymentOrder/status/' + outTradeNo,
    method: 'get'
  })
}

// 查询易支付订单状态并补单
export function reconcileOrder(outTradeNo) {
  return request({
    url: '/mc/paymentOrder/reconcile/' + outTradeNo,
    method: 'post'
  })
}

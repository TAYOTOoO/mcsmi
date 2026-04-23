import request from '@/utils/request'

/**
 * 获取可用订阅套餐列表
 */
export function getAvailablePackages() {
  return request({
    url: '/mc/subscription/packages',
    method: 'get'
  })
}

/**
 * 创建订阅订单
 */
export function createSubscriptionOrder(packageId, paymentType) {
  return request({
    url: '/mc/subscription/order/create',
    method: 'post',
    data: { packageId, paymentType }
  })
}

/**
 * 查询订单状态
 */
export function getOrderStatus(orderNo) {
  return request({
    url: `/mc/subscription/order/status/${orderNo}`,
    method: 'get'
  })
}

/**
 * 获取我的订阅信息
 */
export function getMySubscription() {
  return request({
    url: '/mc/subscription/my/info',
    method: 'get'
  })
}

/**
 * 获取我的订阅订单列表
 */
export function getMySubscriptionOrders() {
  return request({
    url: '/mc/subscription/my/orders',
    method: 'get'
  })
}

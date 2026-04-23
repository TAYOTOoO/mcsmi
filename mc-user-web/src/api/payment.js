import request from '@/utils/request'

/**
 * 获取充值套餐列表
 */
export function getPackages() {
  return request({
    url: '/mc/payment/packages',
    method: 'get'
  })
}

/**
 * 获取充值订单列表
 */
export function getOrders(params) {
  return request({
    url: '/mc/payment/orders',
    method: 'get',
    params: params
  })
}

/**
 * 创建充值订单（套餐）
 */
export function createOrder(data) {
  return request({
    url: '/mc/payment/create',
    method: 'post',
    data: data
  })
}

/**
 * 创建充值订单（自定义金额）
 */
export function createCustomOrder(data) {
  return request({
    url: '/mc/payment/create/custom',
    method: 'post',
    data: data
  })
}

/**
 * 发起支付
 */
export function pay(orderId) {
  return request({
    url: `/mc/payment/pay/${orderId}`,
    method: 'get'
  })
}

/**
 * 根据订单号查询订单
 */
export function getOrderByNo(orderNo) {
  return request({
    url: `/mc/payment/order/${orderNo}`,
    method: 'get'
  })
}

/**
 * 创建支付订单（新版接口）
 */
export function createPayment(data) {
  return request({
    url: '/mc/paymentOrder/create',
    method: 'post',
    data: data
  })
}

/**
 * 查询订单状态（新版接口）
 */
export function checkOrderStatus(outTradeNo) {
  return request({
    url: '/mc/paymentOrder/status/' + outTradeNo,
    method: 'get'
  })
}

/**
 * 获取默认支付配置
 */
export function getDefaultPaymentConfig() {
  return request({
    url: '/mc/paymentConfig/default',
    method: 'get'
  })
}

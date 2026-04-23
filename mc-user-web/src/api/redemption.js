import request from '@/utils/request'

// 兑换兑换码
export function redeemCode(key) {
  return request({
    url: '/mc/redemption/redeem',
    method: 'post',
    data: { key }
  })
}

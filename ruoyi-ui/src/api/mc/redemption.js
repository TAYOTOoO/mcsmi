import request from '@/utils/request'

// 查询兑换码列表
export function listRedemption(query) {
  return request({
    url: '/mc/redemption/list',
    method: 'get',
    params: query
  })
}

// 查询兑换码详细
export function getRedemption(id) {
  return request({
    url: '/mc/redemption/' + id,
    method: 'get'
  })
}

// 新增兑换码
export function addRedemption(data) {
  return request({
    url: '/mc/redemption',
    method: 'post',
    data: data
  })
}

// 批量生成兑换码
export function generateRedemptions(data) {
  return request({
    url: '/mc/redemption/generate',
    method: 'post',
    data: data
  })
}

// 修改兑换码
export function updateRedemption(data) {
  return request({
    url: '/mc/redemption',
    method: 'put',
    data: data
  })
}

// 删除兑换码
export function delRedemption(ids) {
  return request({
    url: '/mc/redemption/' + ids,
    method: 'delete'
  })
}

// 导出兑换码TXT
export function exportTxt(ids) {
  return request({
    url: '/mc/redemption/exportTxt',
    method: 'post',
    data: ids,
    responseType: 'blob'
  })
}

// 查询所有兑换码ID（用于全选）
export function getAllRedemptionIds(query) {
  return request({
    url: '/mc/redemption/list',
    method: 'get',
    params: {
      ...query,
      pageNum: 1,
      pageSize: 999999
    }
  })
}

import request from '@/utils/request'

// 查询材质生成日志列表
export function listGenLog(query) {
  return request({
    url: '/mc/genlog/list',
    method: 'get',
    params: query
  })
}

// 查询材质生成日志详细
export function getGenLog(logId) {
  return request({
    url: '/mc/genlog/' + logId,
    method: 'get'
  })
}

// 删除材质生成日志
export function delGenLog(logId) {
  return request({
    url: '/mc/genlog/' + logId,
    method: 'delete'
  })
}

// 查询生成日志统计
export function getGenLogStats(query) {
  return request({
    url: '/mc/genlog/stats',
    method: 'get',
    params: query
  })
}

// 导出生成日志
export function exportGenLog(query) {
  return request({
    url: '/mc/genlog/export',
    method: 'get',
    params: query
  })
}

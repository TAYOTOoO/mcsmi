import request from '@/utils/request'

/**
 * 获取所有正常状态的版本列表（用户端）
 */
export function getVersionList() {
  return request({
    url: '/mc/version/list',
    method: 'get'
  })
}

/**
 * 获取版本详情
 */
export function getVersion(versionId) {
  return request({
    url: `/mc/version/${versionId}`,
    method: 'get'
  })
}

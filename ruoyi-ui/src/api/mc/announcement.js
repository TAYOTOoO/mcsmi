import request from '@/utils/request'

// 查询公告列表
export function listAnnouncement(query) {
  return request({
    url: '/mc/announcement/list',
    method: 'get',
    params: query
  })
}

// 查询公告详细
export function getAnnouncement(announcementId) {
  return request({
    url: '/mc/announcement/' + announcementId,
    method: 'get'
  })
}

// 新增公告
export function addAnnouncement(data) {
  return request({
    url: '/mc/announcement',
    method: 'post',
    data: data
  })
}

// 修改公告
export function updateAnnouncement(data) {
  return request({
    url: '/mc/announcement',
    method: 'put',
    data: data
  })
}

// 删除公告
export function delAnnouncement(announcementId) {
  return request({
    url: '/mc/announcement/' + announcementId,
    method: 'delete'
  })
}

// 导出公告
export function exportAnnouncement(query) {
  return request({
    url: '/mc/announcement/export',
    method: 'post',
    params: query
  })
}

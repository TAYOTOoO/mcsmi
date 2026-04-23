import request from '@/utils/request'

/**
 * 批量上传裁剪图片到临时存储
 * @param {FormData} formData - 包含files和taskId的FormData对象
 * @returns {Promise} batchId
 */
export function uploadCroppedImages(formData) {
  return request({
    url: '/mc/cropped-temp/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取批次信息（包含图片列表）
 * @param {string} batchId - 批次ID
 * @returns {Promise} 批次信息
 */
export function getBatchInfo(batchId) {
  return request({
    url: `/mc/cropped-temp/batch/${batchId}`,
    method: 'get'
  })
}

/**
 * 获取批次的图片列表
 * @param {string} batchId - 批次ID
 * @returns {Promise} 图片URL数组
 */
export function getCroppedImages(batchId) {
  return request({
    url: `/mc/cropped-temp/images/${batchId}`,
    method: 'get'
  })
}

/**
 * 删除批次（用户完成编辑后清理）
 * @param {string} batchId - 批次ID
 * @returns {Promise}
 */
export function deleteBatch(batchId) {
  return request({
    url: `/mc/cropped-temp/${batchId}`,
    method: 'delete'
  })
}

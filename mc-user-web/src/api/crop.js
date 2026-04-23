import request from '@/utils/request'

/**
 * 裁剪图片并打包
 * @param {Object} data - 裁剪参数
 * @param {Number} data.taskId - 任务ID
 * @param {String} data.versionName - 版本号(如1.20.1)
 * @param {Array<Number>} data.horizontalLines - 横线Y坐标数组
 * @param {Array<Number>} data.verticalLines - 竖线X坐标数组
 * @returns {Promise}
 */
export function cropAndPack(data) {
  return request({
    url: '/mc/crop/split-and-pack',
    method: 'post',
    data: data
  })
}

/**
 * 获取所有正常状态的版本列表
 * @returns {Promise}
 */
export function getVersions() {
  return request({
    url: '/mc/version/list',
    method: 'get'
  })
}

/**
 * 下载ZIP文件
 * @param {String} zipPath - ZIP文件路径
 * @returns {Promise}
 */
export function downloadZipFile(zipPath) {
  return request({
    url: zipPath,
    method: 'get',
    responseType: 'blob'
  })
}

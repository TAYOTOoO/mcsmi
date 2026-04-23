/**
 * Format 32→34, 34→42 转换
 * 删除 shaders
 */
import { log, deleteFolder } from '../fileUtils'

/**
 * 删除 shaders 文件夹
 */
export async function deleteShadersFolder(zip, logCb) {
  deleteFolder(zip, 'assets/minecraft/shaders', logCb)
}

export default {
  deleteShadersFolder
}

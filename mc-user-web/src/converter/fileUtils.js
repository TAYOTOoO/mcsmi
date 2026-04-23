/**
 * JSZip 文件操作工具
 * 替代 Python 的 os/shutil/zipfile 文件操作
 */
import {
  loadImage,
  imageDataToArrayBuffer,
  cropImageData,
  determineScaleFactor,
  scaledBox
} from './imageUtils'

/**
 * 日志函数 — 收集日志消息供 UI 显示
 * @param {string} message
 * @param {Function} [logCallback]
 */
export function log(message, logCallback) {
  console.log(`[Converter] ${message}`)
  if (logCallback) logCallback(message)
}

/**
 * 判断文件是否存在于 zip 中
 * @param {JSZip} zip
 * @param {string} path
 * @returns {boolean}
 */
export function fileExists(zip, path) {
  return zip.file(path) !== null
}

/**
 * 判断文件夹是否存在于 zip 中
 * @param {JSZip} zip
 * @param {string} path - 必须以 '/' 结尾
 * @returns {boolean}
 */
export function folderExists(zip, path) {
  const prefix = path.endsWith('/') ? path : path + '/'
  return Object.keys(zip.files).some(f => f.startsWith(prefix))
}

/**
 * 读取文件为 ArrayBuffer
 * @param {JSZip} zip
 * @param {string} path
 * @returns {Promise<ArrayBuffer|null>}
 */
export async function readFile(zip, path) {
  const file = zip.file(path)
  if (!file) return null
  return await file.async('arraybuffer')
}

/**
 * 读取文件为文本
 * @param {JSZip} zip
 * @param {string} path
 * @returns {Promise<string|null>}
 */
export async function readFileText(zip, path) {
  const file = zip.file(path)
  if (!file) return null
  return await file.async('string')
}

/**
 * 写入文件
 * @param {JSZip} zip
 * @param {string} path
 * @param {ArrayBuffer|Blob|string|Uint8Array} data
 */
export function writeFile(zip, path, data) {
  zip.file(path, data)
}

/**
 * 从 zip 读取图片为 ImageData
 * @param {JSZip} zip
 * @param {string} path
 * @returns {Promise<{imageData: ImageData, width: number, height: number}|null>}
 */
export async function readImageFromZip(zip, path) {
  const data = await readFile(zip, path)
  if (!data) return null
  return await loadImage(data)
}

/**
 * 将 ImageData 写入 zip 为 PNG
 * @param {JSZip} zip
 * @param {string} path
 * @param {ImageData} imageData
 */
export async function writeImageToZip(zip, path, imageData) {
  const buf = await imageDataToArrayBuffer(imageData)
  zip.file(path, buf)
}

/**
 * 重命名文件 (JSZip 无原生 rename，需读取→写入新路径→删除旧路径)
 * @param {JSZip} zip
 * @param {string} oldPath
 * @param {string} newPath
 * @returns {Promise<boolean>}
 */
export async function renameFile(zip, oldPath, newPath) {
  const file = zip.file(oldPath)
  if (!file) return false
  const data = await file.async('arraybuffer')
  zip.file(newPath, data)
  zip.remove(oldPath)
  return true
}

/**
 * 复制文件
 * @param {JSZip} zip
 * @param {string} srcPath
 * @param {string} dstPath
 * @returns {Promise<boolean>}
 */
export async function copyFile(zip, srcPath, dstPath) {
  const file = zip.file(srcPath)
  if (!file) return false
  const data = await file.async('arraybuffer')
  zip.file(dstPath, data)
  return true
}

/**
 * 删除文件
 * @param {JSZip} zip
 * @param {string} path
 * @returns {boolean}
 */
export function deleteFile(zip, path) {
  if (zip.file(path)) {
    zip.remove(path)
    return true
  }
  return false
}

/**
 * 删除文件夹及其所有内容
 * @param {JSZip} zip
 * @param {string} folderPath
 * @param {Function} [logCb]
 * @returns {number} 删除的文件数
 */
export function deleteFolder(zip, folderPath, logCb) {
  const prefix = folderPath.endsWith('/') ? folderPath : folderPath + '/'
  const toRemove = Object.keys(zip.files).filter(f => f.startsWith(prefix))
  toRemove.forEach(f => zip.remove(f))
  if (toRemove.length > 0) {
    log(`已删除文件夹: ${folderPath} (${toRemove.length} 个文件)`, logCb)
  }
  return toRemove.length
}

/**
 * 重命名文件夹 (将所有前缀匹配的文件移动到新前缀下)
 * @param {JSZip} zip
 * @param {string} oldFolder
 * @param {string} newFolder
 * @param {Function} [logCb]
 * @returns {Promise<number>} 重命名的文件数
 */
export async function renameFolder(zip, oldFolder, newFolder, logCb) {
  const oldPrefix = oldFolder.endsWith('/') ? oldFolder : oldFolder + '/'
  const newPrefix = newFolder.endsWith('/') ? newFolder : newFolder + '/'
  const files = Object.keys(zip.files).filter(f => f.startsWith(oldPrefix) && !zip.files[f].dir)
  for (const f of files) {
    const newPath = newPrefix + f.substring(oldPrefix.length)
    const data = await zip.file(f).async('arraybuffer')
    zip.file(newPath, data)
    zip.remove(f)
  }
  // 移除空目录条目
  Object.keys(zip.files)
    .filter(f => f.startsWith(oldPrefix) && zip.files[f].dir)
    .forEach(f => zip.remove(f))
  if (files.length > 0) {
    log(`已重命名文件夹: ${oldFolder} → ${newFolder} (${files.length} 个文件)`, logCb)
  }
  return files.length
}

/**
 * 列出文件夹内的文件
 * @param {JSZip} zip
 * @param {string} folderPath
 * @returns {string[]} 文件名列表 (不含路径前缀)
 */
export function listFiles(zip, folderPath) {
  const prefix = folderPath.endsWith('/') ? folderPath : folderPath + '/'
  return Object.keys(zip.files)
    .filter(f => f.startsWith(prefix) && !zip.files[f].dir)
    .map(f => f.substring(prefix.length))
    .filter(f => !f.includes('/')) // 只返回直接子文件
}

/**
 * 列出文件夹内所有文件（递归）
 * @param {JSZip} zip
 * @param {string} folderPath
 * @returns {string[]} 相对路径列表
 */
export function listFilesRecursive(zip, folderPath) {
  const prefix = folderPath.endsWith('/') ? folderPath : folderPath + '/'
  return Object.keys(zip.files)
    .filter(f => f.startsWith(prefix) && !zip.files[f].dir)
    .map(f => f.substring(prefix.length))
}

/**
 * 批量重命名文件夹下的文件
 * @param {JSZip} zip
 * @param {string} folderPath
 * @param {Object} renameMap - { oldName: newName }
 * @param {Function} [logCb]
 * @returns {Promise<number>} 重命名的文件数
 */
export async function batchRenameFiles(zip, folderPath, renameMap, logCb) {
  const prefix = folderPath.endsWith('/') ? folderPath : folderPath + '/'
  let count = 0
  for (const [oldName, newName] of Object.entries(renameMap)) {
    const oldPath = prefix + oldName
    const newPath = prefix + newName
    if (oldName === newName) continue
    if (zip.file(oldPath)) {
      await renameFile(zip, oldPath, newPath)
      log(`重命名: ${oldName} → ${newName}`, logCb)
      count++
    }
  }
  return count
}

/**
 * 处理容器 GUI 精灵图裁剪 (process1_* 的通用实现)
 * @param {JSZip} zip
 * @param {string} texturePath - 基础纹理路径前缀
 * @param {Object} config - CONTAINER_CROP_REGIONS 中的配置项
 * @param {Function} [logCb]
 */
export async function processContainerCropRegions(zip, texturePath, config, logCb) {
  const sourcePath = texturePath + config.source
  const imgResult = await readImageFromZip(zip, sourcePath)
  if (!imgResult) {
    log(`未找到 ${sourcePath}，跳过`, logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const [baseW, baseH] = config.baseSize
  const scale = determineScaleFactor(width, height, baseW, baseH)
  if (scale === null) {
    log(`不支持的图片尺寸 ${sourcePath}: ${width}x${height}`, logCb)
    return
  }

  for (const region of config.regions) {
    const [x1, y1, x2, y2] = scaledBox(...region.crop, scale)
    const cropped = cropImageData(imageData, x1, y1, x2, y2)
    const savePath = texturePath + config.targetDir + '/' + region.saveName
    await writeImageToZip(zip, savePath, cropped)
    log(`已保存 ${region.saveName} (${cropped.width}x${cropped.height})`, logCb)
  }
}

// 资源路径常量
export const TEXTURES_PATH = 'assets/minecraft/textures/'
export const GUI_PATH = 'assets/minecraft/textures/gui/'
export const CONTAINER_PATH = 'assets/minecraft/textures/gui/container/'
export const ITEMS_PATH_OLD = 'assets/minecraft/textures/items/'
export const ITEMS_PATH_NEW = 'assets/minecraft/textures/item/'
export const BLOCKS_PATH_OLD = 'assets/minecraft/textures/blocks/'
export const BLOCKS_PATH_NEW = 'assets/minecraft/textures/block/'
export const ENTITY_PATH = 'assets/minecraft/textures/entity/'
export const MOB_EFFECT_PATH = 'assets/minecraft/textures/mob_effect/'
export const PARTICLE_PATH = 'assets/minecraft/textures/particle/'
export const MODELS_PATH = 'assets/minecraft/models/'
export const ARMOR_PATH = 'assets/minecraft/textures/models/armor/'

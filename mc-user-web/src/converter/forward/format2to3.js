/**
 * Format 2→3 转换
 * 生成 shulker_box UI, 删除 horse 文件夹, 修复马 UI
 */
import {
  log, fileExists, readImageFromZip, writeImageToZip,
  deleteFolder, CONTAINER_PATH, ENTITY_PATH
} from '../fileUtils'
import {
  cropImageData, pasteImageData, createImageData,
  determineScaleFactor, scaledBox, alphaComposite, cloneImageData
} from '../imageUtils'

/**
 * 生成潜影盒 UI (从 generic_54.png)
 */
export async function generateShulkerBoxUi(zip, logCb) {
  const generic54Path = CONTAINER_PATH + 'generic_54.png'
  const imgResult = await readImageFromZip(zip, generic54Path)
  if (!imgResult) {
    log('未找到 generic_54.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 generic_54.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const s = scale
  const newW = 176 * s
  const newH = 166 * s
  const shulker = createImageData(newW, newH)

  const top = cropImageData(imageData, 0, 0, 176 * s, 17 * s)
  pasteImageData(shulker, top, 0, 0)

  const rows = cropImageData(imageData, 0, 17 * s, 176 * s, 71 * s)
  pasteImageData(shulker, rows, 0, 17 * s)

  const bottom = cropImageData(imageData, 0, 125 * s, 176 * s, 222 * s)
  pasteImageData(shulker, bottom, 0, 71 * s)

  await writeImageToZip(zip, CONTAINER_PATH + 'shulker_box.png', shulker)
  log('已生成 shulker_box.png', logCb)
}

/**
 * 删除 horse 文件夹
 */
export async function deleteHorseFolder(zip, logCb) {
  deleteFolder(zip, ENTITY_PATH + 'horse', logCb)
}

/**
 * 修复马 UI
 */
export async function fixHorseUi(zip, logCb) {
  const horsePath = CONTAINER_PATH + 'horse.png'
  const imgResult = await readImageFromZip(zip, horsePath)
  if (!imgResult) {
    log('未找到 horse.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 horse.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  // 需要外部 horse overlay 文件，跳过
  log('horse UI overlay 需要外部文件，跳过', logCb)
}

export default {
  generateShulkerBoxUi,
  deleteHorseFolder,
  fixHorseUi
}

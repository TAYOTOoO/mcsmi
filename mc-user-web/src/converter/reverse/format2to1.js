/**
 * 反向转换: Format 2→1
 * 删除 blockstates, 反向时钟/指南针, 反向 UI
 */
import {
  log, fileExists, readImageFromZip, writeImageToZip,
  deleteFolder, listFiles, ITEMS_PATH_OLD, CONTAINER_PATH, GUI_PATH
} from '../fileUtils'
import {
  mergeImagesVertical, cropImageData, pasteImageData,
  determineScaleFactor, scaledBox, moveRegion, colorFillRegion,
  copyAndPasteRegion, getPixel, fillRect
} from '../imageUtils'
import { REVERSE_BREWING_STAND_PARAMS } from '../constants'

/**
 * 删除 blockstates 和 models
 */
export async function deleteBlockstatesModels(zip, logCb) {
  deleteFolder(zip, 'assets/minecraft/blockstates', logCb)
  deleteFolder(zip, 'assets/minecraft/models', logCb)
}

/**
 * 反向拆分时钟/指南针 (合并帧回单个文件)
 */
export async function reverseFixClockCompass(zip, logCb) {
  // 时钟
  const clockFrames = []
  for (let i = 0; i < 64; i++) {
    const framePath = ITEMS_PATH_OLD + `clock_${String(i).padStart(2, '0')}.png`
    const imgResult = await readImageFromZip(zip, framePath)
    if (imgResult) {
      clockFrames.push(imgResult.imageData)
      zip.remove(framePath)
    }
  }
  if (clockFrames.length > 0) {
    const merged = mergeImagesVertical(clockFrames)
    await writeImageToZip(zip, ITEMS_PATH_OLD + 'clock.png', merged)
    // 创建 mcmeta
    const mcmeta = JSON.stringify({ animation: {} }, null, 4)
    zip.file(ITEMS_PATH_OLD + 'clock.png.mcmeta', mcmeta)
    log(`已合并 ${clockFrames.length} 帧时钟图片`, logCb)
  }

  // 指南针
  const compassFrames = []
  for (let i = 0; i < 32; i++) {
    const framePath = ITEMS_PATH_OLD + `compass_${String(i).padStart(2, '0')}.png`
    const imgResult = await readImageFromZip(zip, framePath)
    if (imgResult) {
      compassFrames.push(imgResult.imageData)
      zip.remove(framePath)
    }
  }
  if (compassFrames.length > 0) {
    const merged = mergeImagesVertical(compassFrames)
    await writeImageToZip(zip, ITEMS_PATH_OLD + 'compass.png', merged)
    const mcmeta = JSON.stringify({ animation: {} }, null, 4)
    zip.file(ITEMS_PATH_OLD + 'compass.png.mcmeta', mcmeta)
    log(`已合并 ${compassFrames.length} 帧指南针图片`, logCb)
  }
}

/**
 * 反向修复生存模式 UI
 */
export async function reverseFixUiSurvival(zip, logCb) {
  // 反向操作基本上是最小处理，因为原始数据可能已丢失
  log('反向 UI 修复 (survival) - 基本处理', logCb)
}

/**
 * 反向修复创造模式 UI
 */
export async function reverseFixUiCreative(zip, logCb) {
  log('反向 UI 修复 (creative) - 基本处理', logCb)
}

/**
 * 反向修复酿造台 UI
 */
export async function reverseFixBrewingStandUi(zip, logCb) {
  const brewingPath = CONTAINER_PATH + 'brewing_stand.png'
  const imgResult = await readImageFromZip(zip, brewingPath)
  if (!imgResult) {
    log('未找到 brewing_stand.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 brewing_stand.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const s = scale
  const p = REVERSE_BREWING_STAND_PARAMS

  // 获取填充颜色
  const fillColor = getPixel(imageData, p.colorSample[0] * s, p.colorSample[1] * s)

  // 填充区域1
  fillRect(imageData, p.fillBox1[0] * s, p.fillBox1[1] * s, p.fillBox1[2] * s, p.fillBox1[3] * s, fillColor)

  // 填充区域2
  fillRect(imageData, p.fillBox2[0] * s, p.fillBox2[1] * s, p.fillBox2[2] * s, p.fillBox2[3] * s, fillColor)

  // 移动区域
  const moveBox = scaledBox(...p.moveBox, s)
  const region = cropImageData(imageData, ...moveBox)
  pasteImageData(imageData, region, p.moveDst[0] * s, p.moveDst[1] * s)

  // 填充区域3
  fillRect(imageData, p.fillBox3[0] * s, p.fillBox3[1] * s, p.fillBox3[2] * s, p.fillBox3[3] * s, fillColor)

  await writeImageToZip(zip, brewingPath, imageData)
  log('已反向修复 brewing_stand.png', logCb)
}

export default {
  deleteBlockstatesModels,
  reverseFixClockCompass,
  reverseFixUiSurvival,
  reverseFixUiCreative,
  reverseFixBrewingStandUi
}

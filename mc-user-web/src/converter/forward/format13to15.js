/**
 * Format 13→15 转换
 * 修复 smithing2/villager2 UI, slider
 */
import {
  log, fileExists, readImageFromZip, writeImageToZip,
  copyFile, CONTAINER_PATH, GUI_PATH
} from '../fileUtils'
import {
  cropImageData, pasteImageData, createImageData,
  determineScaleFactor, scaledBox, cloneImageData
} from '../imageUtils'

/**
 * 修复锻造台2和村民交易2 UI
 */
export async function fixSmithing2Villager2Ui(zip, logCb) {
  // smithing2 从 smithing.png 创建
  const smithingPath = CONTAINER_PATH + 'smithing.png'
  if (fileExists(zip, smithingPath)) {
    // 复制为基础版本，实际修改需要外部 overlay
    log('smithing2/villager2 UI 修复需要外部 overlay，跳过', logCb)
  }
}

/**
 * 从 widgets.png 生成 slider.png
 */
export async function fixSlider(zip, logCb) {
  const widgetsPath = GUI_PATH + 'widgets.png'
  const imgResult = await readImageFromZip(zip, widgetsPath)
  if (!imgResult) {
    log('未找到 widgets.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 widgets.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const s = scale

  // 从 widgets.png 裁剪 slider 区域 (0,46,200,66)
  const sliderRegion = cropImageData(imageData, 0, 46 * s, 200 * s, 66 * s)
  await writeImageToZip(zip, GUI_PATH + 'slider.png', sliderRegion)
  log('已生成 slider.png', logCb)
}

export default {
  fixSmithing2Villager2Ui,
  fixSlider
}

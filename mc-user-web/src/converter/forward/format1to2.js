/**
 * Format 1→2 转换
 * 删除 blockstates/models, 生成箭矢/船/药水, 修复UI等
 */
import {
  log, fileExists, folderExists, readImageFromZip, writeImageToZip,
  deleteFolder, TEXTURES_PATH, ITEMS_PATH_OLD, GUI_PATH, CONTAINER_PATH,
  MOB_EFFECT_PATH, ENTITY_PATH, copyFile
} from '../fileUtils'
import {
  adjustHueBrightness, makeTopThirdTransparent, cropImageData,
  pasteImageData, createImageData, determineScaleFactor, scaledBox,
  moveRegion, colorFillRegion, copyAndPasteRegion, alphaComposite,
  cloneImageData, splitAnimationFrames, imageDataToArrayBuffer
} from '../imageUtils'
import { BOAT_VARIANTS, MOB_EFFECT_IMAGES } from '../constants'

/**
 * 删除 blockstates 和 models 文件夹
 */
export async function deleteBlockstatesModels(zip, logCb) {
  deleteFolder(zip, 'assets/minecraft/blockstates', logCb)
  deleteFolder(zip, 'assets/minecraft/models', logCb)
}

/**
 * 生成箭矢图片 (tipped_arrow_base + tipped_arrow_head)
 * 注意: 需要外部 tipped_arrow_head 文件，浏览器端不可用时跳过
 */
export async function generateTippedArrowImages(zip, logCb) {
  const arrowPath = ITEMS_PATH_OLD + 'arrow.png'
  if (!fileExists(zip, arrowPath)) {
    log('未找到 arrow.png，跳过生成箭矢图像', logCb)
    return
  }
  // 需要外部 overlay 文件，浏览器端跳过
  log('箭矢生成需要外部 overlay 文件，浏览器端跳过', logCb)
}

/**
 * 修复生存模式 UI (inventory.png)
 * 提取 mob effect 图标, 移动/填充区域, 叠加 overlay
 */
export async function fixUiSurvival(zip, logCb) {
  const inventoryPath = CONTAINER_PATH + 'inventory.png'
  const imgResult = await readImageFromZip(zip, inventoryPath)
  if (!imgResult) {
    log('未找到 inventory.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 inventory.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const imageWidth = 18 * scale
  const imageHeight = 18 * scale

  // 提取 mob effect 区域
  const effectBox = scaledBox(0, 198, 144, 254, scale)
  const effectRegion = cropImageData(imageData, ...effectBox)

  // 保存 mob effect 图标
  for (let rowIdx = 0; rowIdx < MOB_EFFECT_IMAGES.length; rowIdx++) {
    for (let colIdx = 0; colIdx < MOB_EFFECT_IMAGES[rowIdx].length; colIdx++) {
      const name = MOB_EFFECT_IMAGES[rowIdx][colIdx]
      const xOff = colIdx * imageWidth
      const yOff = rowIdx * imageHeight
      const effectImg = cropImageData(effectRegion, xOff, yOff, xOff + imageWidth, yOff + imageHeight)
      await writeImageToZip(zip, MOB_EFFECT_PATH + name, effectImg)
      log(`已保存 ${name}`, logCb)
    }
  }

  // 移动区域: (86,24,162,62) 偏移 (10,-8)
  const s = scale
  moveRegion(imageData, 86 * s, 24 * s, 162 * s, 62 * s, 10 * s, -8 * s)

  // 颜色填充
  colorFillRegion(imageData, 75 * s, 6 * s, 96 * s, 80 * s, 90 * s, 10 * s)
  colorFillRegion(imageData, 96 * s, 54 * s, 161 * s, 62 * s, 90 * s, 10 * s)

  // 复制粘贴区域
  copyAndPasteRegion(imageData, 152 * s, 26 * s, 172 * s, 46 * s, 75 * s, 60 * s)

  // overlay 叠加 (需要外部文件，跳过)
  log('inventory overlay 需要外部文件，跳过叠加', logCb)

  await writeImageToZip(zip, inventoryPath, imageData)
  log('已修复 inventory.png', logCb)
}

/**
 * 修复创造模式 UI (tab_inventory.png)
 */
export async function fixUiCreative(zip, logCb) {
  const tabInvPath = CONTAINER_PATH + 'creative_inventory/tab_inventory.png'
  const imgResult = await readImageFromZip(zip, tabInvPath)
  if (!imgResult) {
    log('未找到 tab_inventory.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 tab_inventory.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const s = scale

  // 复制区域 (111,32,123,50) → (83,20)
  copyAndPasteRegion(imageData, 111 * s, 32 * s, 123 * s, 50 * s, 83 * s, 20 * s)

  // 颜色填充 (111,32,123,50) 用 (90,10)
  colorFillRegion(imageData, 111 * s, 32 * s, 123 * s, 50 * s, 90 * s, 10 * s)

  await writeImageToZip(zip, tabInvPath, imageData)
  log('已修复 tab_inventory.png', logCb)
}

/**
 * 修复副手 UI (widgets.png)
 */
export async function fixUiSubHand(zip, logCb) {
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

  // 从 (24,23,46,45) 复制到 (53,22)
  copyAndPasteRegion(imageData, 24 * s, 23 * s, 46 * s, 45 * s, 53 * s, 22 * s)

  await writeImageToZip(zip, widgetsPath, imageData)
  log('已修复 widgets.png 副手UI', logCb)
}

/**
 * 生成船变体
 */
export async function generateBoat(zip, logCb) {
  const boatPath = ITEMS_PATH_OLD + 'boat.png'
  const imgResult = await readImageFromZip(zip, boatPath)
  if (!imgResult) {
    log('未找到 boat.png', logCb)
    return
  }

  const { imageData } = imgResult

  // 生成各木材变体
  for (const [variant, params] of Object.entries(BOAT_VARIANTS)) {
    const adjusted = adjustHueBrightness(imageData, params.hue || 0, params.brightness || 0)
    await writeImageToZip(zip, ITEMS_PATH_OLD + `${variant}_boat.png`, adjusted)
    log(`已生成 ${variant}_boat.png`, logCb)
  }

  // spruce: 直接重命名
  await copyFile(zip, boatPath, ITEMS_PATH_OLD + 'spruce_boat.png')
  log('已复制 spruce_boat.png', logCb)
}

/**
 * 生成滞留药水
 */
export async function generatePotionLingering(zip, logCb) {
  const potionPath = ITEMS_PATH_OLD + 'potion_bottle_drinkable.png'
  const imgResult = await readImageFromZip(zip, potionPath)
  if (!imgResult) {
    log('未找到药水图片', logCb)
    return
  }

  const lingering = makeTopThirdTransparent(imgResult.imageData)
  await writeImageToZip(zip, ITEMS_PATH_OLD + 'potion_bottle_lingering.png', lingering)
  log('已生成 potion_bottle_lingering.png', logCb)
}

/**
 * 生成潜影盒 UI
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
  // 创建新图像 (176x166 基础尺寸)
  const newW = 176 * s
  const newH = 166 * s
  const shulker = createImageData(newW, newH)

  // 复制顶部 (0,0,176,17)
  const top = cropImageData(imageData, 0, 0, 176 * s, 17 * s)
  pasteImageData(shulker, top, 0, 0)

  // 复制3行 (从 generic_54 的前3行物品槽)
  const rows = cropImageData(imageData, 0, 17 * s, 176 * s, 71 * s)
  pasteImageData(shulker, rows, 0, 17 * s)

  // 复制底部 (hotbar区域)
  const bottom = cropImageData(imageData, 0, 125 * s, 176 * s, 222 * s)
  pasteImageData(shulker, bottom, 0, 71 * s)

  await writeImageToZip(zip, CONTAINER_PATH + 'shulker_box.png', shulker)
  log('已生成 shulker_box.png', logCb)
}

/**
 * 修复酿造台 UI
 */
export async function fixBrewingStandUi(zip, logCb) {
  // 需要外部 overlay, 浏览器端做基本处理
  log('酿造台 UI overlay 需要外部文件，跳过', logCb)
}

/**
 * 拆分时钟/指南针动画帧
 */
export async function fixClockCompass(zip, logCb) {
  // 时钟: 64 帧
  const clockPath = ITEMS_PATH_OLD + 'clock.png'
  const clockResult = await readImageFromZip(zip, clockPath)
  if (clockResult) {
    const frames = splitAnimationFrames(clockResult.imageData, 64)
    for (let i = 0; i < frames.length; i++) {
      await writeImageToZip(zip, ITEMS_PATH_OLD + `clock_${String(i).padStart(2, '0')}.png`, frames[i])
    }
    zip.remove(clockPath)
    // 删除 mcmeta
    const mcmetaPath = clockPath + '.mcmeta'
    if (fileExists(zip, mcmetaPath)) zip.remove(mcmetaPath)
    log(`已拆分 clock.png 为 ${frames.length} 帧`, logCb)
  }

  // 指南针: 32 帧
  const compassPath = ITEMS_PATH_OLD + 'compass.png'
  const compassResult = await readImageFromZip(zip, compassPath)
  if (compassResult) {
    const frames = splitAnimationFrames(compassResult.imageData, 32)
    for (let i = 0; i < frames.length; i++) {
      await writeImageToZip(zip, ITEMS_PATH_OLD + `compass_${String(i).padStart(2, '0')}.png`, frames[i])
    }
    zip.remove(compassPath)
    const mcmetaPath = compassPath + '.mcmeta'
    if (fileExists(zip, mcmetaPath)) zip.remove(mcmetaPath)
    log(`已拆分 compass.png 为 ${frames.length} 帧`, logCb)
  }
}

/**
 * 叠加 icons
 */
export async function overlayIcons(zip, logCb) {
  // 需要外部 overlay 文件
  log('icons overlay 需要外部文件，跳过', logCb)
}

export default {
  deleteBlockstatesModels,
  generateTippedArrowImages,
  fixUiSurvival,
  fixUiCreative,
  fixUiSubHand,
  generateBoat,
  generatePotionLingering,
  generateShulkerBoxUi,
  fixBrewingStandUi,
  fixClockCompass,
  overlayIcons
}

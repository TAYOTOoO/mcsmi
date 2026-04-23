/**
 * Format 4→5 转换
 * 处理 chest 文件夹, 生成 netherite 系列, 删除 enchanted_item_glint
 */
import {
  log, fileExists, folderExists, readImageFromZip, writeImageToZip,
  copyFile, deleteFile, ENTITY_PATH, ITEMS_PATH_NEW, BLOCKS_PATH_NEW,
  ARMOR_PATH, CONTAINER_PATH
} from '../fileUtils'
import {
  adjustHueBrightness, netheriteColorTransform, darkenImage,
  cropImageData, pasteImageData, createImageData,
  flipHorizontal, flipVertical, flipBoth,
  swapAndMirror, determineScaleFactor, scaledBox, cloneImageData
} from '../imageUtils'
import {
  NETHERITE_HSV, NETHERITE_TOOL_PAIRS, NETHERITE_ARMOR_LAYER_PAIRS,
  SINGLE_CHEST_FILES, SINGLE_CHEST_SWAP_BOXES, SINGLE_CHEST_FLIP_BOXES,
  DOUBLE_CHEST_PREFIXES
} from '../constants'

/**
 * 处理箱子文件夹 - 单箱子转换
 */
export async function processChestFolder(zip, logCb) {
  const chestPath = ENTITY_PATH + 'chest/'

  // 处理单箱子
  for (const chestFile of SINGLE_CHEST_FILES) {
    const filePath = chestPath + chestFile
    const imgResult = await readImageFromZip(zip, filePath)
    if (!imgResult) continue

    const { imageData, width, height } = imgResult

    // 确定缩放因子 (基于64x64)
    const scale = determineScaleFactor(width, height, 64, 64)
    if (scale === null) {
      log(`不支持的 ${chestFile} 尺寸: ${width}x${height}`, logCb)
      continue
    }

    // swap_and_mirror 操作
    for (const [box1Base, box2Base] of SINGLE_CHEST_SWAP_BOXES) {
      const box1 = scaledBox(...box1Base, scale)
      const box2 = scaledBox(...box2Base, scale)
      swapAndMirror(imageData, box1, box2)
    }

    // flip 操作
    for (const boxBase of SINGLE_CHEST_FLIP_BOXES) {
      const box = scaledBox(...boxBase, scale)
      const region = cropImageData(imageData, ...box)
      const flipped = flipBoth(flipHorizontal(region)) // FLIP_LEFT_RIGHT + FLIP_TOP_BOTTOM
      pasteImageData(imageData, flipBoth(cropImageData(imageData, ...box)), box[0], box[1])
    }

    await writeImageToZip(zip, filePath, imageData)
    log(`已处理箱子 ${chestFile}`, logCb)
  }

  // 处理双箱子 - 分割为左右
  for (const prefix of DOUBLE_CHEST_PREFIXES) {
    const doublePath = chestPath + `${prefix}_double.png`
    const imgResult = await readImageFromZip(zip, doublePath)
    if (!imgResult) continue

    const { imageData, width, height } = imgResult
    // 双箱子基础尺寸 128x64
    const scale = determineScaleFactor(width, height, 128, 64)
    if (scale === null) {
      log(`不支持的双箱子尺寸: ${width}x${height}`, logCb)
      continue
    }

    const s = scale
    // 创建左右图像 (64x64 * scale)
    const leftImg = createImageData(64 * s, 64 * s)
    const rightImg = createImageData(64 * s, 64 * s)

    const sb = (x1, y1, x2, y2) => scaledBox(x1, y1, x2, y2, s)

    // 生成左箱子图像
    pasteImageData(leftImg, flipVertical(cropImageData(imageData, ...sb(29, 0, 44, 14))), 29 * s, 0)
    pasteImageData(leftImg, flipVertical(cropImageData(imageData, ...sb(59, 0, 74, 14))), 14 * s, 0)
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(29, 14, 44, 19))), 43 * s, 14 * s)
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(44, 14, 58, 19))), 29 * s, 14 * s)
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(58, 14, 73, 19))), 14 * s, 14 * s)
    pasteImageData(leftImg, flipVertical(cropImageData(imageData, ...sb(29, 19, 44, 33))), 29 * s, 19 * s)
    pasteImageData(leftImg, flipVertical(cropImageData(imageData, ...sb(59, 19, 74, 33))), 14 * s, 19 * s)
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(29, 33, 44, 43))), 43 * s, 33 * s)
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(44, 33, 58, 43))), 29 * s, 33 * s)
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(58, 33, 73, 43))), 14 * s, 33 * s)
    // 锁扣
    pasteImageData(leftImg, flipBoth(cropImageData(imageData, ...sb(2, 1, 5, 5))), 1 * s, 1 * s)
    pasteImageData(leftImg, cropImageData(imageData, ...sb(2, 0, 3, 1)), 2 * s, 0)
    pasteImageData(leftImg, cropImageData(imageData, ...sb(4, 0, 5, 1)), 1 * s, 0)
    pasteImageData(leftImg, flipVertical(cropImageData(imageData, ...sb(5, 1, 6, 5))), 1 * s, 1 * s)
    pasteImageData(leftImg, cropImageData(imageData, ...sb(1, 0, 2, 1)), 2 * s, 0)
    pasteImageData(leftImg, cropImageData(imageData, ...sb(3, 0, 4, 1)), 1 * s, 0)

    // 生成右箱子图像
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(44, 0, 59, 14))), 14 * s, 0)
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(14, 0, 29, 14))), 29 * s, 0)
    pasteImageData(rightImg, flipBoth(cropImageData(imageData, ...sb(0, 14, 14, 19))), 0, 14 * s)
    pasteImageData(rightImg, flipBoth(cropImageData(imageData, ...sb(73, 14, 88, 19))), 14 * s, 14 * s)
    pasteImageData(rightImg, flipBoth(cropImageData(imageData, ...sb(14, 14, 29, 19))), 43 * s, 14 * s)
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(14, 19, 29, 33))), 29 * s, 19 * s)
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(44, 19, 59, 33))), 14 * s, 19 * s)
    pasteImageData(rightImg, flipBoth(cropImageData(imageData, ...sb(14, 33, 29, 43))), 43 * s, 33 * s)
    pasteImageData(rightImg, flipBoth(cropImageData(imageData, ...sb(0, 33, 14, 43))), 0, 33 * s)
    pasteImageData(rightImg, flipBoth(cropImageData(imageData, ...sb(73, 33, 88, 43))), 14 * s, 33 * s)
    // 锁扣
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(0, 1, 1, 5))), 0, 1 * s)
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(1, 1, 2, 5))), 3 * s, 1 * s)
    pasteImageData(rightImg, flipVertical(cropImageData(imageData, ...sb(5, 1, 6, 5))), 1 * s, 1 * s)
    pasteImageData(rightImg, cropImageData(imageData, ...sb(1, 0, 2, 1)), 2 * s, 0)
    pasteImageData(rightImg, cropImageData(imageData, ...sb(3, 0, 4, 1)), 1 * s, 0)

    await writeImageToZip(zip, chestPath + `${prefix}_left.png`, leftImg)
    await writeImageToZip(zip, chestPath + `${prefix}_right.png`, rightImg)
    log(`已处理双箱子 ${prefix}`, logCb)
  }
}

/**
 * 生成下界合金块
 */
export async function generateNetheriteBlock(zip, logCb) {
  const diamondBlockPath = BLOCKS_PATH_NEW + 'diamond_block.png'
  const imgResult = await readImageFromZip(zip, diamondBlockPath)
  if (!imgResult) {
    log('未找到 diamond_block.png', logCb)
    return
  }

  const netherite = netheriteColorTransform(
    imgResult.imageData,
    NETHERITE_HSV.hue,
    NETHERITE_HSV.satDivisor,
    NETHERITE_HSV.valDivisor
  )
  await writeImageToZip(zip, BLOCKS_PATH_NEW + 'netherite_block.png', netherite)
  log('已生成 netherite_block.png', logCb)
}

/**
 * 生成下界合金锭
 */
export async function generateNetheriteIngot(zip, logCb) {
  const diamondPath = ITEMS_PATH_NEW + 'diamond.png'
  const imgResult = await readImageFromZip(zip, diamondPath)
  if (!imgResult) {
    log('未找到 diamond.png', logCb)
    return
  }

  const netherite = netheriteColorTransform(
    imgResult.imageData,
    NETHERITE_HSV.hue,
    NETHERITE_HSV.satDivisor,
    NETHERITE_HSV.valDivisor
  )
  await writeImageToZip(zip, ITEMS_PATH_NEW + 'netherite_ingot.png', netherite)
  log('已生成 netherite_ingot.png', logCb)
}

/**
 * 删除附魔物品光效
 */
export async function deleteEnchantedItemGlint(zip, logCb) {
  const glintPath = 'assets/minecraft/textures/misc/enchanted_item_glint.png'
  if (fileExists(zip, glintPath)) {
    deleteFile(zip, glintPath)
    log('已删除 enchanted_item_glint.png', logCb)
  }
}

/**
 * 生成下界合金工具
 */
export async function generateNetheriteTools(zip, logCb) {
  for (const [src, dst] of Object.entries(NETHERITE_TOOL_PAIRS)) {
    const srcPath = ITEMS_PATH_NEW + src
    const dstPath = ITEMS_PATH_NEW + dst
    const imgResult = await readImageFromZip(zip, srcPath)
    if (!imgResult) continue

    const darkened = darkenImage(imgResult.imageData, 0.6)
    await writeImageToZip(zip, dstPath, darkened)
    log(`已生成 ${dst}`, logCb)
  }

  // 光灵箭
  const spectralArrowPath = ITEMS_PATH_NEW + 'spectral_arrow.png'
  const arrowResult = await readImageFromZip(zip, spectralArrowPath)
  if (arrowResult) {
    const adjusted = adjustHueBrightness(arrowResult.imageData, 0.167 * 360, 0)
    await writeImageToZip(zip, spectralArrowPath, adjusted)
    log('已处理 spectral_arrow.png', logCb)
  }
}

/**
 * 生成下界合金盔甲模型
 */
export async function generateNetheriteArmorModels(zip, logCb) {
  for (const [src, dst] of Object.entries(NETHERITE_ARMOR_LAYER_PAIRS)) {
    const srcPath = ARMOR_PATH + src
    const dstPath = ARMOR_PATH + dst
    const imgResult = await readImageFromZip(zip, srcPath)
    if (!imgResult) continue

    const darkened = darkenImage(imgResult.imageData, 0.6)
    await writeImageToZip(zip, dstPath, darkened)
    log(`已生成 ${dst}`, logCb)
  }
}

/**
 * 生成锻造台 UI
 */
export async function generateSmithingUi(zip, logCb) {
  const anvilPath = CONTAINER_PATH + 'anvil.png'
  if (fileExists(zip, anvilPath)) {
    await copyFile(zip, anvilPath, CONTAINER_PATH + 'smithing.png')
    log('已生成 smithing.png (从 anvil.png)', logCb)
  }
  // overlay 需要外部文件，跳过
}

export default {
  processChestFolder,
  generateNetheriteBlock,
  generateNetheriteIngot,
  deleteEnchantedItemGlint,
  generateNetheriteTools,
  generateNetheriteArmorModels,
  generateSmithingUi
}

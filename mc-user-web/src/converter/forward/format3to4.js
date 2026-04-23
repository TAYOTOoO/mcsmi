/**
 * Format 3→4 转换
 * 重命名方块/物品, 修复sign, 生成furnace/crossbow等
 */
import {
  log, fileExists, folderExists, readImageFromZip, writeImageToZip,
  copyFile, renameFolder, batchRenameFiles, deleteFolder,
  TEXTURES_PATH, ITEMS_PATH_OLD, ITEMS_PATH_NEW, BLOCKS_PATH_OLD, BLOCKS_PATH_NEW,
  ENTITY_PATH, CONTAINER_PATH, PARTICLE_PATH
} from '../fileUtils'
import {
  adjustHueBrightness, cropImageData, pasteImageData, createImageData,
  determineScaleFactor, scaledBox, cloneImageData
} from '../imageUtils'
import {
  ITEM_RENAME_PAIRS, BLOCK_PROCESS_RENAME, SIGN_VARIANTS,
  PARTICLE_SPLIT_MAP
} from '../constants'

/**
 * 重命名方块和物品文件夹及文件
 */
export async function renameBlocksItems(zip, logCb) {
  // 重命名 items → item
  if (folderExists(zip, ITEMS_PATH_OLD)) {
    await renameFolder(zip, ITEMS_PATH_OLD, ITEMS_PATH_NEW, logCb)
  }

  // 重命名 blocks → block
  if (folderExists(zip, BLOCKS_PATH_OLD)) {
    await renameFolder(zip, BLOCKS_PATH_OLD, BLOCKS_PATH_NEW, logCb)
  }

  // 重命名物品文件
  if (folderExists(zip, ITEMS_PATH_NEW)) {
    await batchRenameFiles(zip, ITEMS_PATH_NEW, ITEM_RENAME_PAIRS, logCb)
  }

  // 重命名方块文件
  if (folderExists(zip, BLOCKS_PATH_NEW)) {
    await batchRenameFiles(zip, BLOCKS_PATH_NEW, BLOCK_PROCESS_RENAME, logCb)
  }
}

/**
 * 修复标牌 (sign.png → 各木材变体)
 */
export async function fixSign(zip, logCb) {
  const signPath = ENTITY_PATH + 'sign.png'
  const imgResult = await readImageFromZip(zip, signPath)
  if (!imgResult) {
    log('未找到 sign.png', logCb)
    return
  }

  const { imageData } = imgResult

  for (const [variant, params] of Object.entries(SIGN_VARIANTS)) {
    const adjusted = adjustHueBrightness(imageData, params.hue, params.brightness, params.saturation || 0)
    await writeImageToZip(zip, ENTITY_PATH + `signs/${variant}.png`, adjusted)
    log(`已生成标牌 ${variant}.png`, logCb)
  }

  // 原始 sign.png 作为 spruce
  await copyFile(zip, signPath, ENTITY_PATH + 'signs/spruce.png')
  log('已复制标牌 spruce.png', logCb)
}

/**
 * 修复标牌实体 (sign_entities)
 */
export async function fixSignEntities(zip, logCb) {
  // sign_entities 使用与 sign 相同的变体但在不同路径
  const signPath = ENTITY_PATH + 'sign.png'
  const imgResult = await readImageFromZip(zip, signPath)
  if (!imgResult) {
    log('未找到 sign.png (entities)', logCb)
    return
  }

  const { imageData } = imgResult

  for (const [variant, params] of Object.entries(SIGN_VARIANTS)) {
    const adjusted = adjustHueBrightness(imageData, params.hue, params.brightness, params.saturation || 0)
    await writeImageToZip(zip, ENTITY_PATH + `signs/hanging/${variant}.png`, adjusted)
    log(`已生成悬挂标牌 ${variant}.png`, logCb)
  }

  await copyFile(zip, signPath, ENTITY_PATH + 'signs/hanging/spruce.png')
  log('已复制悬挂标牌 spruce.png', logCb)
}

/**
 * 生成熔炉变体 (blast_furnace, smoker)
 */
export async function generateFurnace(zip, logCb) {
  const furnaceFrontPath = BLOCKS_PATH_NEW + 'furnace_front.png'
  const furnaceFrontOnPath = BLOCKS_PATH_NEW + 'furnace_front_on.png'

  // 复制到高炉
  if (fileExists(zip, furnaceFrontPath)) {
    await copyFile(zip, furnaceFrontPath, BLOCKS_PATH_NEW + 'blast_furnace_front.png')
    log('已复制 blast_furnace_front.png', logCb)
  }
  if (fileExists(zip, furnaceFrontOnPath)) {
    await copyFile(zip, furnaceFrontOnPath, BLOCKS_PATH_NEW + 'blast_furnace_front_on.png')
    log('已复制 blast_furnace_front_on.png', logCb)
  }

  // 复制到烟熏炉
  if (fileExists(zip, furnaceFrontPath)) {
    await copyFile(zip, furnaceFrontPath, BLOCKS_PATH_NEW + 'smoker_front.png')
    log('已复制 smoker_front.png', logCb)
  }
  if (fileExists(zip, furnaceFrontOnPath)) {
    await copyFile(zip, furnaceFrontOnPath, BLOCKS_PATH_NEW + 'smoker_front_on.png')
    log('已复制 smoker_front_on.png', logCb)
  }

  // GUI 容器也复制
  const furnaceGuiPath = CONTAINER_PATH + 'furnace.png'
  if (fileExists(zip, furnaceGuiPath)) {
    await copyFile(zip, furnaceGuiPath, CONTAINER_PATH + 'blast_furnace.png')
    await copyFile(zip, furnaceGuiPath, CONTAINER_PATH + 'smoker.png')
    log('已复制熔炉 GUI 到 blast_furnace.png 和 smoker.png', logCb)
  }
}

/**
 * 修复机械 UI (grindstone, cartography, stonecutter, loom, villager)
 */
export async function fixMachineryUi(zip, logCb) {
  // 这些需要外部 overlay 文件或者是简单的创建
  log('机械 UI 修复需要外部 overlay 文件，跳过', logCb)
}

/**
 * 拆分粒子图片
 */
export async function fixParticles(zip, logCb) {
  const particlesPath = PARTICLE_PATH + 'particles.png'
  const imgResult = await readImageFromZip(zip, particlesPath)
  if (!imgResult) {
    log('未找到 particles.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  if (width !== height || width % 16 !== 0) {
    log(`particles.png 尺寸不正确: ${width}x${height}`, logCb)
    return
  }

  const splitSize = width / 16

  for (const [filename, [row, col]] of Object.entries(PARTICLE_SPLIT_MAP)) {
    const x = col * splitSize
    const y = row * splitSize
    const cropped = cropImageData(imageData, x, y, x + splitSize, y + splitSize)

    // 部分粒子保存到 entity 目录
    const isEntity = filename === 'fishing_hook.png'
    const savePath = isEntity
      ? ENTITY_PATH + filename
      : PARTICLE_PATH + filename

    await writeImageToZip(zip, savePath, cropped)
  }

  zip.remove(particlesPath)
  log('已拆分 particles.png', logCb)
}

/**
 * 生成鱼桶
 */
export async function generateFishBucket(zip, logCb) {
  // 需要外部 overlay 文件
  log('鱼桶生成需要外部 overlay 文件，跳过', logCb)
}

/**
 * 生成弩
 */
export async function generateCrossbow(zip, logCb) {
  // 需要外部 overlay 文件
  log('弩生成需要外部 overlay 文件，跳过', logCb)
}

export default {
  renameBlocksItems,
  fixSign,
  fixSignEntities,
  generateFurnace,
  fixMachineryUi,
  fixParticles,
  generateFishBucket,
  generateCrossbow
}

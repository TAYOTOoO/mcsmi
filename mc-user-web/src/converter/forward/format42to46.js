/**
 * Format 42→46 转换
 * 马 UI, 盔甲模型, pale_planks
 */
import {
  log, fileExists, folderExists, readImageFromZip, writeImageToZip,
  copyFile, renameFile, listFiles, ENTITY_PATH, ARMOR_PATH, BLOCKS_PATH_NEW,
  CONTAINER_PATH
} from '../fileUtils'
import { adjustHueBrightness } from '../imageUtils'
import {
  ARMOR_MODEL_LAYER1_MAP, ARMOR_MODEL_LAYER2_MAP,
  HORSE_SLOT_FILES
} from '../constants'

/**
 * 修复马 UI 插槽 (复制到 slot 目录)
 */
export async function fix2HorseUi(zip, logCb) {
  const horseSpritesDir = 'assets/minecraft/textures/gui/sprites/container/horse/'
  const slotDir = 'assets/minecraft/textures/gui/sprites/container/slot/'

  for (const [srcName, dstName] of Object.entries(HORSE_SLOT_FILES)) {
    const srcPath = horseSpritesDir + srcName
    if (fileExists(zip, srcPath)) {
      await copyFile(zip, srcPath, slotDir + dstName)
      log(`已复制 ${srcName} → slot/${dstName}`, logCb)
    }
  }
}

/**
 * 修复盔甲模型 (移动到 humanoid/humanoid_leggings 目录)
 */
export async function fixArmorModels(zip, logCb) {
  const humanoidDir = ARMOR_PATH.replace('armor/', 'equipment/humanoid/')
  const leggingsDir = ARMOR_PATH.replace('armor/', 'equipment/humanoid_leggings/')

  // Layer 1 → humanoid
  for (const [oldName, newName] of Object.entries(ARMOR_MODEL_LAYER1_MAP)) {
    const oldPath = ARMOR_PATH + oldName
    if (fileExists(zip, oldPath)) {
      await copyFile(zip, oldPath, humanoidDir + newName)
      log(`盔甲 ${oldName} → humanoid/${newName}`, logCb)
    }
  }

  // Layer 2 → humanoid_leggings
  for (const [oldName, newName] of Object.entries(ARMOR_MODEL_LAYER2_MAP)) {
    const oldPath = ARMOR_PATH + oldName
    if (fileExists(zip, oldPath)) {
      await copyFile(zip, oldPath, leggingsDir + newName)
      log(`盔甲 ${oldName} → humanoid_leggings/${newName}`, logCb)
    }
  }
}

/**
 * 生成 pale_planks
 */
export async function generatePalePlanks(zip, logCb) {
  const oakPlanksPath = BLOCKS_PATH_NEW + 'oak_planks.png'
  const imgResult = await readImageFromZip(zip, oakPlanksPath)
  if (!imgResult) {
    log('未找到 oak_planks.png', logCb)
    return
  }

  // hue=0, brightness=+30, saturation=-100
  const adjusted = adjustHueBrightness(imgResult.imageData, 0, 30, -100)
  await writeImageToZip(zip, BLOCKS_PATH_NEW + 'pale_oak_planks.png', adjusted)
  log('已生成 pale_oak_planks.png', logCb)
}

export default {
  fix2HorseUi,
  fixArmorModels,
  generatePalePlanks
}

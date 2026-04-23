/**
 * 反向转换: Format 46→42
 * 反向盔甲模型
 */
import {
  log, fileExists, copyFile, ARMOR_PATH
} from '../fileUtils'
import {
  REVERSE_ARMOR_MODEL_LAYER1_MAP, REVERSE_ARMOR_MODEL_LAYER2_MAP
} from '../constants'

/**
 * 反向修复盔甲模型 (从 humanoid 目录移回 armor 目录)
 */
export async function reverseFixArmorModels(zip, logCb) {
  const humanoidDir = ARMOR_PATH.replace('armor/', 'equipment/humanoid/')
  const leggingsDir = ARMOR_PATH.replace('armor/', 'equipment/humanoid_leggings/')

  // humanoid → Layer 1
  for (const [srcName, dstName] of Object.entries(REVERSE_ARMOR_MODEL_LAYER1_MAP)) {
    const srcPath = humanoidDir + srcName
    if (fileExists(zip, srcPath)) {
      await copyFile(zip, srcPath, ARMOR_PATH + dstName)
      log(`盔甲 humanoid/${srcName} → ${dstName}`, logCb)
    }
  }

  // humanoid_leggings → Layer 2
  for (const [srcName, dstName] of Object.entries(REVERSE_ARMOR_MODEL_LAYER2_MAP)) {
    const srcPath = leggingsDir + srcName
    if (fileExists(zip, srcPath)) {
      await copyFile(zip, srcPath, ARMOR_PATH + dstName)
      log(`盔甲 humanoid_leggings/${srcName} → ${dstName}`, logCb)
    }
  }
}

export default {
  reverseFixArmorModels
}

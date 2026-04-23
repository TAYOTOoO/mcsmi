/**
 * Minecraft 材质包版本转换器 - 主编排器
 * 等价于 Python 的 process_zip 函数
 */
import JSZip from 'jszip'
import { PACK_FORMAT_MAP, PACK_FORMAT_ORDER } from './constants'
import { log, readFileText, writeFile } from './fileUtils'

// 正向转换函数导入
import {
  deleteBlockstatesModels, generateTippedArrowImages, fixUiSurvival,
  fixUiCreative, fixUiSubHand, generateBoat, generatePotionLingering,
  generateShulkerBoxUi as generateShulkerBoxUi1, fixBrewingStandUi, fixClockCompass,
  overlayIcons
} from './forward/format1to2'

import {
  generateShulkerBoxUi as generateShulkerBoxUi2,
  deleteHorseFolder as deleteHorseFolder2, fixHorseUi
} from './forward/format2to3'

import {
  renameBlocksItems, fixSign, fixSignEntities, generateFurnace,
  fixMachineryUi, fixParticles, generateFishBucket, generateCrossbow
} from './forward/format3to4'

import {
  processChestFolder, generateNetheriteBlock, generateNetheriteIngot,
  deleteEnchantedItemGlint, generateNetheriteTools,
  generateNetheriteArmorModels, generateSmithingUi
} from './forward/format4to5'

import { deleteFontFolder, generateSnowBucket } from './forward/format6to7'
import { renameMcpatcherToOptifine } from './forward/format8to9'
import { fixTabs, generateRedwoodCherryBambooPlanks } from './forward/format12to13'
import { fixSmithing2Villager2Ui, fixSlider } from './forward/format13to15'
import { deleteTabs, cutGui } from './forward/format15to18'
import { deleteShadersFolder } from './forward/format32to34'
import { fix2HorseUi, fixArmorModels, generatePalePlanks } from './forward/format42to46'

// 反向转换函数导入
import { reverseFixArmorModels } from './reverse/format46to42'
import { reverseProcessChestFolder } from './reverse/format5to4'
import {
  reverseRenameBlocksItems, reverseFixParticles,
  deleteHorseFolder as deleteHorseFolder4to3
} from './reverse/format4to3'
import { deleteHorseFolder as deleteHorseFolder3to2 } from './forward/format2to3'
import {
  deleteBlockstatesModels as deleteBlockstatesModelsReverse,
  reverseFixClockCompass, reverseFixUiSurvival,
  reverseFixUiCreative, reverseFixBrewingStandUi
} from './reverse/format2to1'

// 正向相邻转换映射
const ADJACENT_CONVERSIONS = {
  '1,2': [deleteBlockstatesModels, generateTippedArrowImages, fixUiSurvival, fixUiCreative, fixUiSubHand, generateBoat, generatePotionLingering, generateShulkerBoxUi1, fixBrewingStandUi, fixClockCompass, overlayIcons],
  '2,3': [generateShulkerBoxUi2, deleteHorseFolder2, fixHorseUi],
  '3,4': [renameBlocksItems, fixSign, fixSignEntities, generateFurnace, fixMachineryUi, fixParticles, generateFishBucket, generateCrossbow],
  '4,5': [processChestFolder, generateNetheriteBlock, generateNetheriteIngot, deleteEnchantedItemGlint, generateNetheriteTools, generateNetheriteArmorModels, generateSmithingUi],
  '5,6': [],
  '6,7': [deleteFontFolder, generateSnowBucket],
  '7,8': [],
  '8,9': [renameMcpatcherToOptifine],
  '9,12': [],
  '12,13': [fixTabs, generateRedwoodCherryBambooPlanks],
  '13,15': [fixSmithing2Villager2Ui, fixSlider],
  '15,18': [deleteTabs, cutGui],
  '18,22': [],
  '22,32': [],
  '32,34': [deleteShadersFolder],
  '34,42': [deleteShadersFolder],
  '42,46': [fix2HorseUi, fixArmorModels, generatePalePlanks]
}

// 反向相邻转换映射
const ADJACENT_CONVERSIONS_REVERSE = {
  '46,42': [reverseFixArmorModels],
  '42,34': [],
  '34,32': [],
  '32,22': [],
  '22,18': [],
  '18,15': [],
  '15,13': [],
  '13,12': [],
  '12,9': [],
  '9,8': [],
  '8,7': [],
  '7,6': [],
  '6,5': [],
  '5,4': [reverseProcessChestFolder],
  '4,3': [reverseRenameBlocksItems, reverseFixParticles, deleteHorseFolder4to3],
  '3,2': [deleteHorseFolder3to2],
  '2,1': [deleteBlockstatesModelsReverse, reverseFixClockCompass, reverseFixUiSurvival, reverseFixUiCreative, reverseFixBrewingStandUi]
}

/**
 * 修复 pack.mcmeta 中的 description (清理特殊字符)
 */
function fixDescription(description) {
  if (typeof description === 'string') {
    return description.replace(/[\x00-\x1F\x7F\x80-\x9F]/g, '')
  }
  return description
}

/**
 * 从 pack.mcmeta 读取 pack_format
 * @param {JSZip} zip
 * @returns {Promise<number|null>}
 */
export async function getPackFormat(zip) {
  const metaText = await readFileText(zip, 'pack.mcmeta')
  if (!metaText) return null

  try {
    let text = metaText
    // 处理 BOM
    if (text.charCodeAt(0) === 0xFEFF) {
      text = text.substring(1)
    }
    // 清理控制字符
    text = text.replace(/[\x00-\x1F\x7F\x80-\x9F]/g, '')

    const data = JSON.parse(text)
    if (data.pack && typeof data.pack.pack_format === 'number') {
      return data.pack.pack_format
    }
  } catch (e) {
    console.error('解析 pack.mcmeta 失败:', e)
  }
  return null
}

/**
 * 获取 pack_format 对应的版本字符串
 * @param {number} format
 * @returns {string}
 */
export function getVersionString(format) {
  return PACK_FORMAT_MAP[format] || `未知 (format ${format})`
}

/**
 * 获取转换路径描述
 * @param {number} fromFormat
 * @param {number} toFormat
 * @returns {string[]} 路径步骤描述数组
 */
export function getConversionPath(fromFormat, toFormat) {
  const startIdx = PACK_FORMAT_ORDER.indexOf(fromFormat)
  const endIdx = PACK_FORMAT_ORDER.indexOf(toFormat)
  if (startIdx === -1 || endIdx === -1) return []

  const steps = []
  if (startIdx <= endIdx) {
    for (let i = startIdx; i < endIdx; i++) {
      const from = PACK_FORMAT_ORDER[i]
      const to = PACK_FORMAT_ORDER[i + 1]
      steps.push(`${PACK_FORMAT_MAP[from]} → ${PACK_FORMAT_MAP[to]}`)
    }
  } else {
    for (let i = startIdx; i > endIdx; i--) {
      const from = PACK_FORMAT_ORDER[i]
      const to = PACK_FORMAT_ORDER[i - 1]
      steps.push(`${PACK_FORMAT_MAP[from]} → ${PACK_FORMAT_MAP[to]}`)
    }
  }
  return steps
}

/**
 * 主转换函数
 * @param {JSZip} zip - 已加载的 ZIP 对象
 * @param {number} packFormat1 - 源版本 pack_format
 * @param {number} packFormat2 - 目标版本 pack_format
 * @param {Function} [logCallback] - 日志回调
 * @param {Function} [progressCallback] - 进度回调 (0-1)
 * @returns {Promise<JSZip>} 修改后的 ZIP 对象
 */
export async function processZip(zip, packFormat1, packFormat2, logCallback, progressCallback) {
  const startIndex = PACK_FORMAT_ORDER.indexOf(packFormat1)
  const endIndex = PACK_FORMAT_ORDER.indexOf(packFormat2)

  if (startIndex === -1 || endIndex === -1) {
    throw new Error(`无效的 pack_format: ${packFormat1} 或 ${packFormat2}`)
  }

  const direction = startIndex <= endIndex ? 'forward' : 'reverse'
  const conversionMap = direction === 'forward' ? ADJACENT_CONVERSIONS : ADJACENT_CONVERSIONS_REVERSE

  const totalSteps = Math.abs(endIndex - startIndex)
  let currentStep = 0

  if (direction === 'forward') {
    for (let i = startIndex; i < endIndex; i++) {
      const currentFormat = PACK_FORMAT_ORDER[i]
      const nextFormat = PACK_FORMAT_ORDER[i + 1]
      const key = `${currentFormat},${nextFormat}`
      const actions = conversionMap[key]

      if (!actions || actions.length === 0) {
        log(`${PACK_FORMAT_MAP[currentFormat]} → ${PACK_FORMAT_MAP[nextFormat]}: 无需操作`, logCallback)
      } else {
        log(`正在转换: ${PACK_FORMAT_MAP[currentFormat]} → ${PACK_FORMAT_MAP[nextFormat]}`, logCallback)
        for (const action of actions) {
          try {
            await action(zip, logCallback)
          } catch (e) {
            log(`转换操作失败: ${e.message}`, logCallback)
            console.error(e)
          }
        }
      }

      currentStep++
      if (progressCallback) {
        progressCallback(currentStep / totalSteps)
      }
    }
  } else {
    for (let i = startIndex; i > endIndex; i--) {
      const currentFormat = PACK_FORMAT_ORDER[i]
      const nextFormat = PACK_FORMAT_ORDER[i - 1]
      const key = `${currentFormat},${nextFormat}`
      const actions = conversionMap[key]

      if (!actions || actions.length === 0) {
        log(`${PACK_FORMAT_MAP[currentFormat]} → ${PACK_FORMAT_MAP[nextFormat]}: 无需操作`, logCallback)
      } else {
        log(`正在转换: ${PACK_FORMAT_MAP[currentFormat]} → ${PACK_FORMAT_MAP[nextFormat]}`, logCallback)
        for (const action of actions) {
          try {
            await action(zip, logCallback)
          } catch (e) {
            log(`转换操作失败: ${e.message}`, logCallback)
            console.error(e)
          }
        }
      }

      currentStep++
      if (progressCallback) {
        progressCallback(currentStep / totalSteps)
      }
    }
  }

  // 更新 pack.mcmeta
  const metaText = await readFileText(zip, 'pack.mcmeta')
  if (metaText) {
    try {
      let text = metaText
      if (text.charCodeAt(0) === 0xFEFF) text = text.substring(1)
      text = text.replace(/[\x00-\x1F\x7F\x80-\x9F]/g, '')

      const data = JSON.parse(text)
      if (data.pack) {
        data.pack.pack_format = packFormat2
        if (data.pack.description) {
          data.pack.description = fixDescription(data.pack.description)
        }
        writeFile(zip, 'pack.mcmeta', JSON.stringify(data, null, 4))
        log(`已更新 pack_format: ${packFormat1} → ${packFormat2}`, logCallback)
      }
    } catch (e) {
      log(`更新 pack.mcmeta 失败: ${e.message}`, logCallback)
    }
  }

  return zip
}

/**
 * 加载 ZIP 文件
 * @param {File|Blob|ArrayBuffer} file
 * @returns {Promise<JSZip>}
 */
export async function loadZip(file) {
  return await JSZip.loadAsync(file)
}

/**
 * 导出 ZIP 文件为 Blob
 * @param {JSZip} zip
 * @returns {Promise<Blob>}
 */
export async function exportZip(zip) {
  return await zip.generateAsync({ type: 'blob', compression: 'DEFLATE' })
}

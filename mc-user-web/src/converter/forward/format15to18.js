/**
 * Format 15→18 转换
 * 删除 tabs, 切割 GUI 精灵图 (最复杂)
 */
import {
  log, fileExists, deleteFile, readImageFromZip, writeImageToZip,
  processContainerCropRegions, TEXTURES_PATH, CONTAINER_PATH, GUI_PATH
} from '../fileUtils'
import {
  cropImageData, determineScaleFactor, scaledBox, concatenateHorizontal
} from '../imageUtils'
import { CONTAINER_CROP_REGIONS } from '../constants'

/**
 * 删除 tabs.png
 */
export async function deleteTabs(zip, logCb) {
  const tabsPath = CONTAINER_PATH + 'creative_inventory/tabs.png'
  if (fileExists(zip, tabsPath)) {
    deleteFile(zip, tabsPath)
    log('已删除 tabs.png', logCb)
  }
}

/**
 * 切割 GUI 精灵图 — 调用所有 process_* 和 process1_* 函数
 */
export async function cutGui(zip, logCb) {
  // 处理所有容器裁剪区域
  for (const [name, config] of Object.entries(CONTAINER_CROP_REGIONS)) {
    log(`正在处理容器: ${name}`, logCb)
    await processContainerCropRegions(zip, TEXTURES_PATH, config, logCb)
  }

  // 处理 slider.png 精灵图
  await processSlider(zip, logCb)

  // 处理 icons.png 精灵图
  await processIcons(zip, logCb)

  // 处理 widgets.png 精灵图
  await processWidgets(zip, logCb)

  log('GUI 精灵图切割完成', logCb)
}

/**
 * 处理 slider.png (生成 slider 精灵图)
 */
async function processSlider(zip, logCb) {
  const sliderPath = GUI_PATH + 'slider.png'
  const imgResult = await readImageFromZip(zip, sliderPath)
  if (!imgResult) {
    log('未找到 slider.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 200, 20)
  if (scale === null) {
    log(`不支持的 slider.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const s = scale
  const spritesPath = TEXTURES_PATH + 'gui/sprites/widget/'

  // 1. slider.png (0,0,200,20)
  const sliderBg = cropImageData(imageData, 0, 0, 200 * s, 20 * s)
  await writeImageToZip(zip, spritesPath + 'slider.png', sliderBg)

  // 2. slider_handle.png: 拼接左侧(0,40,4,60) + 右侧(196,40,200,60)
  const handleLeft = cropImageData(imageData, 0, 40 * s, 4 * s, 60 * s)
  const handleRight = cropImageData(imageData, 196 * s, 40 * s, 200 * s, 60 * s)
  const handle = concatenateHorizontal(handleLeft, handleRight)
  await writeImageToZip(zip, spritesPath + 'slider_handle.png', handle)

  // 3. slider_handle_highlighted.png: 拼接左侧(0,60,4,80) + 右侧(196,60,200,80)
  const highlightLeft = cropImageData(imageData, 0, 60 * s, 4 * s, 80 * s)
  const highlightRight = cropImageData(imageData, 196 * s, 60 * s, 200 * s, 80 * s)
  const highlight = concatenateHorizontal(highlightLeft, highlightRight)
  await writeImageToZip(zip, spritesPath + 'slider_handle_highlighted.png', highlight)

  log('已处理 slider.png 精灵图', logCb)
}

/**
 * 处理 icons.png (提取心、饥饿、盔甲等图标)
 */
async function processIcons(zip, logCb) {
  const iconsPath = GUI_PATH + 'icons.png'
  const imgResult = await readImageFromZip(zip, iconsPath)
  if (!imgResult) {
    log('未找到 icons.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) {
    log(`不支持的 icons.png 尺寸: ${width}x${height}`, logCb)
    return
  }

  const s = scale
  const spritesPath = TEXTURES_PATH + 'gui/sprites/'

  // Step 1: Crosshair (准星)
  const crosshair = cropImageData(imageData, 0, 0, 15 * s, 15 * s)
  await writeImageToZip(zip, spritesPath + 'hud/crosshair.png', crosshair)

  // Step 2: Heart系列 (16,0,196,9) - 20个9x9图标
  const heartY = 0
  const heartNames = [
    'container.png', 'container_blinking.png', null, null,
    'full.png', 'half.png', 'full_blinking.png', 'half_blinking.png',
    'poisoned_full.png', 'poisoned_half.png', 'poisoned_full_blinking.png', 'poisoned_half_blinking.png',
    'withered_full.png', 'withered_half.png', 'withered_full_blinking.png', 'withered_half_blinking.png',
    'absorbing_full.png', 'absorbing_half.png', 'frozen_full.png', 'frozen_half.png'
  ]
  for (let i = 0; i < heartNames.length; i++) {
    if (heartNames[i]) {
      const x = 16 + i * 9
      const heart = cropImageData(imageData, x * s, heartY * s, (x + 9) * s, (heartY + 9) * s)
      await writeImageToZip(zip, spritesPath + 'hud/heart/' + heartNames[i], heart)
    }
  }

  // Step 3: Armor系列 (16,9,52,18) - 4个9x9图标
  const armorNames = ['empty.png', 'half.png', 'full.png']
  for (let i = 0; i < armorNames.length; i++) {
    const x = 16 + i * 9
    const armor = cropImageData(imageData, x * s, 9 * s, (x + 9) * s, 18 * s)
    await writeImageToZip(zip, spritesPath + 'hud/armor/' + armorNames[i], armor)
  }

  // Step 4: Air系列 (16,18,52,27) - 4个9x9图标
  const airNames = ['full.png', 'bursting_full.png', null, null]
  for (let i = 0; i < airNames.length; i++) {
    if (airNames[i]) {
      const x = 16 + i * 9
      const air = cropImageData(imageData, x * s, 18 * s, (x + 9) * s, 27 * s)
      await writeImageToZip(zip, spritesPath + 'hud/air/' + airNames[i], air)
    }
  }

  // Step 5: Food系列 (16,27,142,36) - 14个9x9图标
  const foodNames = [
    'empty.png', null, null, null,
    'full.png', 'half.png', null, null,
    'full_hunger.png', 'half_hunger.png', null, null, null, 'empty_hunger.png'
  ]
  for (let i = 0; i < foodNames.length; i++) {
    if (foodNames[i]) {
      const x = 16 + i * 9
      const food = cropImageData(imageData, x * s, 27 * s, (x + 9) * s, 36 * s)
      await writeImageToZip(zip, spritesPath + 'hud/food/' + foodNames[i], food)
    }
  }

  log('已处理 icons.png 精灵图', logCb)
}

/**
 * 处理 widgets.png
 */
async function processWidgets(zip, logCb) {
  const widgetsPath = GUI_PATH + 'widgets.png'
  const imgResult = await readImageFromZip(zip, widgetsPath)
  if (!imgResult) {
    log('未找到 widgets.png', logCb)
    return
  }

  const { imageData, width, height } = imgResult
  const scale = determineScaleFactor(width, height, 256, 256)
  if (scale === null) return

  const s = scale
  const spritesPath = TEXTURES_PATH + 'gui/sprites/'

  // hotbar
  const hotbar = cropImageData(imageData, 0, 0, 182 * s, 22 * s)
  await writeImageToZip(zip, spritesPath + 'hud/hotbar.png', hotbar)

  // hotbar selection
  const hotbarSel = cropImageData(imageData, 0, 22 * s, 24 * s, 46 * s)
  await writeImageToZip(zip, spritesPath + 'hud/hotbar_selection.png', hotbarSel)

  // hotbar offhand left
  const offhandLeft = cropImageData(imageData, 24 * s, 22 * s, 53 * s, 46 * s)
  await writeImageToZip(zip, spritesPath + 'hud/hotbar_offhand_left.png', offhandLeft)

  // hotbar offhand right
  const offhandRight = cropImageData(imageData, 53 * s, 22 * s, 82 * s, 46 * s)
  await writeImageToZip(zip, spritesPath + 'hud/hotbar_offhand_right.png', offhandRight)

  // button_disabled
  const btnDisabled = cropImageData(imageData, 0, 46 * s, 200 * s, 66 * s)
  await writeImageToZip(zip, spritesPath + 'widget/button_disabled.png', btnDisabled)

  // button
  const btn = cropImageData(imageData, 0, 66 * s, 200 * s, 86 * s)
  await writeImageToZip(zip, spritesPath + 'widget/button.png', btn)

  // button_highlighted
  const btnHighlight = cropImageData(imageData, 0, 86 * s, 200 * s, 106 * s)
  await writeImageToZip(zip, spritesPath + 'widget/button_highlighted.png', btnHighlight)

  // XP bar background
  const xpBg = cropImageData(imageData, 0, 64 * s, 182 * s, 69 * s)
  await writeImageToZip(zip, spritesPath + 'hud/experience_bar_background.png', xpBg)

  // XP bar progress
  const xpProgress = cropImageData(imageData, 0, 69 * s, 182 * s, 74 * s)
  await writeImageToZip(zip, spritesPath + 'hud/experience_bar_progress.png', xpProgress)

  log('已处理 widgets.png 精灵图', logCb)
}

export default {
  deleteTabs,
  cutGui
}

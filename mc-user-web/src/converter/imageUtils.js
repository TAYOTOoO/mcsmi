/**
 * Canvas 图像处理工具函数
 * 替代 Python PIL/Pillow 的功能
 */

/**
 * 将 PNG blob/arrayBuffer 加载为 ImageData
 * @param {Blob|ArrayBuffer} data - 图片数据
 * @returns {Promise<{imageData: ImageData, width: number, height: number}>}
 */
export async function loadImage(data) {
  const blob = data instanceof Blob ? data : new Blob([data], { type: 'image/png' })
  const bitmap = await createImageBitmap(blob)
  const canvas = new OffscreenCanvas(bitmap.width, bitmap.height)
  const ctx = canvas.getContext('2d')
  ctx.drawImage(bitmap, 0, 0)
  const imageData = ctx.getImageData(0, 0, bitmap.width, bitmap.height)
  bitmap.close()
  return { imageData, width: bitmap.width, height: bitmap.height }
}

/**
 * 将 ImageData 编码为 PNG Blob
 * @param {ImageData} imageData
 * @returns {Promise<Blob>}
 */
export async function imageDataToBlob(imageData) {
  const canvas = new OffscreenCanvas(imageData.width, imageData.height)
  const ctx = canvas.getContext('2d')
  ctx.putImageData(imageData, 0, 0)
  return await canvas.convertToBlob({ type: 'image/png' })
}

/**
 * 将 ImageData 编码为 ArrayBuffer (PNG)
 * @param {ImageData} imageData
 * @returns {Promise<ArrayBuffer>}
 */
export async function imageDataToArrayBuffer(imageData) {
  const blob = await imageDataToBlob(imageData)
  return await blob.arrayBuffer()
}

/**
 * 创建空白 RGBA ImageData
 * @param {number} width
 * @param {number} height
 * @param {Array} [fillColor=[0,0,0,0]] - RGBA fill color
 * @returns {ImageData}
 */
export function createImageData(width, height, fillColor = [0, 0, 0, 0]) {
  const imageData = new ImageData(width, height)
  if (fillColor[0] !== 0 || fillColor[1] !== 0 || fillColor[2] !== 0 || fillColor[3] !== 0) {
    for (let i = 0; i < imageData.data.length; i += 4) {
      imageData.data[i] = fillColor[0]
      imageData.data[i + 1] = fillColor[1]
      imageData.data[i + 2] = fillColor[2]
      imageData.data[i + 3] = fillColor[3]
    }
  }
  return imageData
}

/**
 * 裁剪 ImageData 区域
 * @param {ImageData} imageData
 * @param {number} x1
 * @param {number} y1
 * @param {number} x2
 * @param {number} y2
 * @returns {ImageData}
 */
export function cropImageData(imageData, x1, y1, x2, y2) {
  const w = x2 - x1
  const h = y2 - y1
  const result = new ImageData(w, h)
  const srcW = imageData.width
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const srcIdx = ((y1 + y) * srcW + (x1 + x)) * 4
      const dstIdx = (y * w + x) * 4
      result.data[dstIdx] = imageData.data[srcIdx]
      result.data[dstIdx + 1] = imageData.data[srcIdx + 1]
      result.data[dstIdx + 2] = imageData.data[srcIdx + 2]
      result.data[dstIdx + 3] = imageData.data[srcIdx + 3]
    }
  }
  return result
}

/**
 * 将一块 ImageData 粘贴到另一块上
 * @param {ImageData} target - 目标图像
 * @param {ImageData} source - 要粘贴的图像
 * @param {number} dx - 目标 x
 * @param {number} dy - 目标 y
 */
export function pasteImageData(target, source, dx, dy) {
  const tw = target.width
  const sw = source.width
  const sh = source.height
  for (let y = 0; y < sh; y++) {
    for (let x = 0; x < sw; x++) {
      const tx = dx + x
      const ty = dy + y
      if (tx < 0 || tx >= target.width || ty < 0 || ty >= target.height) continue
      const srcIdx = (y * sw + x) * 4
      const dstIdx = (ty * tw + tx) * 4
      target.data[dstIdx] = source.data[srcIdx]
      target.data[dstIdx + 1] = source.data[srcIdx + 1]
      target.data[dstIdx + 2] = source.data[srcIdx + 2]
      target.data[dstIdx + 3] = source.data[srcIdx + 3]
    }
  }
}

/**
 * Alpha 混合粘贴 (source-over)
 * @param {ImageData} target
 * @param {ImageData} source
 * @param {number} dx
 * @param {number} dy
 */
export function alphaComposite(target, source, dx, dy) {
  const tw = target.width
  const sw = source.width
  const sh = source.height
  for (let y = 0; y < sh; y++) {
    for (let x = 0; x < sw; x++) {
      const tx = dx + x
      const ty = dy + y
      if (tx < 0 || tx >= target.width || ty < 0 || ty >= target.height) continue
      const srcIdx = (y * sw + x) * 4
      const dstIdx = (ty * tw + tx) * 4
      const sA = source.data[srcIdx + 3] / 255
      const dA = target.data[dstIdx + 3] / 255
      const outA = sA + dA * (1 - sA)
      if (outA === 0) {
        target.data[dstIdx] = 0
        target.data[dstIdx + 1] = 0
        target.data[dstIdx + 2] = 0
        target.data[dstIdx + 3] = 0
      } else {
        target.data[dstIdx] = Math.round((source.data[srcIdx] * sA + target.data[dstIdx] * dA * (1 - sA)) / outA)
        target.data[dstIdx + 1] = Math.round((source.data[srcIdx + 1] * sA + target.data[dstIdx + 1] * dA * (1 - sA)) / outA)
        target.data[dstIdx + 2] = Math.round((source.data[srcIdx + 2] * sA + target.data[dstIdx + 2] * dA * (1 - sA)) / outA)
        target.data[dstIdx + 3] = Math.round(outA * 255)
      }
    }
  }
}

/**
 * 水平翻转 ImageData (FLIP_LEFT_RIGHT)
 * @param {ImageData} imageData
 * @returns {ImageData}
 */
export function flipHorizontal(imageData) {
  const w = imageData.width
  const h = imageData.height
  const result = new ImageData(w, h)
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const srcIdx = (y * w + x) * 4
      const dstIdx = (y * w + (w - 1 - x)) * 4
      result.data[dstIdx] = imageData.data[srcIdx]
      result.data[dstIdx + 1] = imageData.data[srcIdx + 1]
      result.data[dstIdx + 2] = imageData.data[srcIdx + 2]
      result.data[dstIdx + 3] = imageData.data[srcIdx + 3]
    }
  }
  return result
}

/**
 * 垂直翻转 ImageData (FLIP_TOP_BOTTOM)
 * @param {ImageData} imageData
 * @returns {ImageData}
 */
export function flipVertical(imageData) {
  const w = imageData.width
  const h = imageData.height
  const result = new ImageData(w, h)
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const srcIdx = (y * w + x) * 4
      const dstIdx = ((h - 1 - y) * w + x) * 4
      result.data[dstIdx] = imageData.data[srcIdx]
      result.data[dstIdx + 1] = imageData.data[srcIdx + 1]
      result.data[dstIdx + 2] = imageData.data[srcIdx + 2]
      result.data[dstIdx + 3] = imageData.data[srcIdx + 3]
    }
  }
  return result
}

/**
 * 同时水平+垂直翻转 (等同于 180° 旋转)
 * Python: .transpose(Image.FLIP_LEFT_RIGHT).transpose(Image.FLIP_TOP_BOTTOM)
 * @param {ImageData} imageData
 * @returns {ImageData}
 */
export function flipBoth(imageData) {
  const w = imageData.width
  const h = imageData.height
  const result = new ImageData(w, h)
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const srcIdx = (y * w + x) * 4
      const dstIdx = ((h - 1 - y) * w + (w - 1 - x)) * 4
      result.data[dstIdx] = imageData.data[srcIdx]
      result.data[dstIdx + 1] = imageData.data[srcIdx + 1]
      result.data[dstIdx + 2] = imageData.data[srcIdx + 2]
      result.data[dstIdx + 3] = imageData.data[srcIdx + 3]
    }
  }
  return result
}

/**
 * RGB → HSV 转换
 * @param {number} r (0-255)
 * @param {number} g (0-255)
 * @param {number} b (0-255)
 * @returns {Array} [h(0-1), s(0-1), v(0-1)]
 */
export function rgbToHsv(r, g, b) {
  r /= 255
  g /= 255
  b /= 255
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const d = max - min
  let h = 0
  const s = max === 0 ? 0 : d / max
  const v = max
  if (d !== 0) {
    if (max === r) {
      h = ((g - b) / d + (g < b ? 6 : 0)) / 6
    } else if (max === g) {
      h = ((b - r) / d + 2) / 6
    } else {
      h = ((r - g) / d + 4) / 6
    }
  }
  return [h, s, v]
}

/**
 * HSV → RGB 转换
 * @param {number} h (0-1)
 * @param {number} s (0-1)
 * @param {number} v (0-1)
 * @returns {Array} [r(0-255), g(0-255), b(0-255)]
 */
export function hsvToRgb(h, s, v) {
  let r, g, b
  const i = Math.floor(h * 6)
  const f = h * 6 - i
  const p = v * (1 - s)
  const q = v * (1 - f * s)
  const t = v * (1 - (1 - f) * s)
  switch (i % 6) {
    case 0: r = v; g = t; b = p; break
    case 1: r = q; g = v; b = p; break
    case 2: r = p; g = v; b = t; break
    case 3: r = p; g = q; b = v; break
    case 4: r = t; g = p; b = v; break
    case 5: r = v; g = p; b = q; break
  }
  return [
    Math.round(Math.min(255, Math.max(0, r * 255))),
    Math.round(Math.min(255, Math.max(0, g * 255))),
    Math.round(Math.min(255, Math.max(0, b * 255)))
  ]
}

/**
 * 调整图像色相和亮度 (逐像素 HSV 操作)
 * 等价于 Python 的 adjust_hue_brightness()
 * @param {ImageData} imageData
 * @param {number} hueShift - 色相偏移 (度, -180 到 180)
 * @param {number} brightnessShift - 亮度偏移 (-100 到 100)
 * @param {number} [saturationShift=0] - 饱和度偏移
 * @returns {ImageData} 新的 ImageData
 */
export function adjustHueBrightness(imageData, hueShift, brightnessShift, saturationShift = 0) {
  const result = new ImageData(
    new Uint8ClampedArray(imageData.data),
    imageData.width,
    imageData.height
  )
  const hShift = hueShift / 360
  const bShift = brightnessShift / 100
  const sShift = saturationShift / 100
  for (let i = 0; i < result.data.length; i += 4) {
    if (result.data[i + 3] === 0) continue // 跳过透明像素
    const [h, s, v] = rgbToHsv(result.data[i], result.data[i + 1], result.data[i + 2])
    let newH = (h + hShift) % 1
    if (newH < 0) newH += 1
    const newS = Math.min(1, Math.max(0, s + sShift))
    const newV = Math.min(1, Math.max(0, v + bShift))
    const [r, g, b] = hsvToRgb(newH, newS, newV)
    result.data[i] = r
    result.data[i + 1] = g
    result.data[i + 2] = b
  }
  return result
}

/**
 * 处理图像变暗 (process_image 的等价)
 * 将像素 HSV 中的 V 值乘以因子
 * @param {ImageData} imageData
 * @param {number} factor - 亮度因子 (0-1)
 * @returns {ImageData}
 */
export function darkenImage(imageData, factor = 0.6) {
  const result = new ImageData(
    new Uint8ClampedArray(imageData.data),
    imageData.width,
    imageData.height
  )
  for (let i = 0; i < result.data.length; i += 4) {
    if (result.data[i + 3] === 0) continue
    const [h, s, v] = rgbToHsv(result.data[i], result.data[i + 1], result.data[i + 2])
    const [r, g, b] = hsvToRgb(h, s, v * factor)
    result.data[i] = r
    result.data[i + 1] = g
    result.data[i + 2] = b
  }
  return result
}

/**
 * 下界合金色调转换
 * hue 设为固定值, saturation 和 value 除以因子
 * @param {ImageData} imageData
 * @param {number} fixedHue - 固定色相值 (0-1)
 * @param {number} satDivisor - 饱和度除数
 * @param {number} valDivisor - 亮度除数
 * @returns {ImageData}
 */
export function netheriteColorTransform(imageData, fixedHue, satDivisor, valDivisor) {
  const result = new ImageData(
    new Uint8ClampedArray(imageData.data),
    imageData.width,
    imageData.height
  )
  for (let i = 0; i < result.data.length; i += 4) {
    if (result.data[i + 3] === 0) continue
    const [, s, v] = rgbToHsv(result.data[i], result.data[i + 1], result.data[i + 2])
    const [r, g, b] = hsvToRgb(fixedHue, s / satDivisor, v / valDivisor)
    result.data[i] = r
    result.data[i + 1] = g
    result.data[i + 2] = b
  }
  return result
}

/**
 * 获取像素颜色
 * @param {ImageData} imageData
 * @param {number} x
 * @param {number} y
 * @returns {Array} [r, g, b, a]
 */
export function getPixel(imageData, x, y) {
  const idx = (y * imageData.width + x) * 4
  return [
    imageData.data[idx],
    imageData.data[idx + 1],
    imageData.data[idx + 2],
    imageData.data[idx + 3]
  ]
}

/**
 * 设置像素颜色
 * @param {ImageData} imageData
 * @param {number} x
 * @param {number} y
 * @param {Array} color - [r, g, b, a]
 */
export function setPixel(imageData, x, y, color) {
  const idx = (y * imageData.width + x) * 4
  imageData.data[idx] = color[0]
  imageData.data[idx + 1] = color[1]
  imageData.data[idx + 2] = color[2]
  imageData.data[idx + 3] = color[3]
}

/**
 * 用指定颜色填充矩形区域
 * @param {ImageData} imageData
 * @param {number} x1
 * @param {number} y1
 * @param {number} x2
 * @param {number} y2
 * @param {Array} color - [r, g, b, a]
 */
export function fillRect(imageData, x1, y1, x2, y2, color) {
  for (let y = y1; y < y2; y++) {
    for (let x = x1; x < x2; x++) {
      setPixel(imageData, x, y, color)
    }
  }
}

/**
 * 用采样点颜色填充矩形区域
 * 等价于 Python 的 color_fill_region
 * @param {ImageData} imageData
 * @param {number} x1
 * @param {number} y1
 * @param {number} x2
 * @param {number} y2
 * @param {number} colorX - 采样点 x
 * @param {number} colorY - 采样点 y
 */
export function colorFillRegion(imageData, x1, y1, x2, y2, colorX, colorY) {
  const color = getPixel(imageData, colorX, colorY)
  fillRect(imageData, x1, y1, x2, y2, color)
}

/**
 * 移动区域 (先裁剪，再粘贴到偏移位置)
 * 等价于 Python 的 move_region
 * @param {ImageData} imageData
 * @param {number} x1
 * @param {number} y1
 * @param {number} x2
 * @param {number} y2
 * @param {number} shiftX
 * @param {number} shiftY
 */
export function moveRegion(imageData, x1, y1, x2, y2, shiftX, shiftY) {
  const region = cropImageData(imageData, x1, y1, x2, y2)
  pasteImageData(imageData, region, x1 + shiftX, y1 + shiftY)
}

/**
 * 复制粘贴区域
 * 等价于 Python 的 copy_and_paste_region
 * @param {ImageData} imageData
 * @param {number} srcX1
 * @param {number} srcY1
 * @param {number} srcX2
 * @param {number} srcY2
 * @param {number} dstX
 * @param {number} dstY
 */
export function copyAndPasteRegion(imageData, srcX1, srcY1, srcX2, srcY2, dstX, dstY) {
  const region = cropImageData(imageData, srcX1, srcY1, srcX2, srcY2)
  pasteImageData(imageData, region, dstX, dstY)
}

/**
 * 交换两个矩形区域
 * @param {ImageData} imageData
 * @param {Array} box1 - [x1, y1, x2, y2]
 * @param {Array} box2 - [x1, y1, x2, y2]
 */
export function swapRectangles(imageData, box1, box2) {
  const region1 = cropImageData(imageData, ...box1)
  const region2 = cropImageData(imageData, ...box2)
  pasteImageData(imageData, region2, box1[0], box1[1])
  pasteImageData(imageData, region1, box2[0], box2[1])
}

/**
 * 交换并镜像两个区域
 * 等价于 Python 的 swap_and_mirror
 * @param {ImageData} imageData
 * @param {Array} box1 - [x1, y1, x2, y2]
 * @param {Array} box2 - [x1, y1, x2, y2]
 */
export function swapAndMirror(imageData, box1, box2) {
  // 先交换
  const region1 = cropImageData(imageData, ...box1)
  const region2 = cropImageData(imageData, ...box2)
  pasteImageData(imageData, region2, box1[0], box1[1])
  pasteImageData(imageData, region1, box2[0], box2[1])

  // 再对交换后的区域做双向翻转
  const newRegion1 = flipBoth(cropImageData(imageData, ...box1))
  const newRegion2 = flipBoth(cropImageData(imageData, ...box2))
  pasteImageData(imageData, newRegion1, box1[0], box1[1])
  pasteImageData(imageData, newRegion2, box2[0], box2[1])
}

/**
 * 确定缩放因子
 * @param {number} width
 * @param {number} height
 * @param {number} baseW - 基础宽度 (默认 256)
 * @param {number} baseH - 基础高度 (默认 256)
 * @returns {number|null} 缩放因子，或 null 表示不支持
 */
export function determineScaleFactor(width, height, baseW = 256, baseH = 256) {
  const scales = [1, 2, 4, 8, 16]
  for (const s of scales) {
    if (width === baseW * s && height === baseH * s) {
      return s
    }
  }
  return null
}

/**
 * 缩放坐标框
 * @param {number} x1
 * @param {number} y1
 * @param {number} x2
 * @param {number} y2
 * @param {number} scale
 * @returns {Array} [x1*s, y1*s, x2*s, y2*s]
 */
export function scaledBox(x1, y1, x2, y2, scale) {
  return [x1 * scale, y1 * scale, x2 * scale, y2 * scale]
}

/**
 * 将图像的顶部三分之一像素设为透明
 * 用于生成滞留药水 (generate_potion_lingering)
 * @param {ImageData} imageData
 * @returns {ImageData}
 */
export function makeTopThirdTransparent(imageData) {
  const result = new ImageData(
    new Uint8ClampedArray(imageData.data),
    imageData.width,
    imageData.height
  )
  const thirdH = Math.floor(result.height / 3)
  for (let y = 0; y < thirdH; y++) {
    for (let x = 0; x < result.width; x++) {
      const idx = (y * result.width + x) * 4
      result.data[idx + 3] = 0
    }
  }
  return result
}

/**
 * 拆分竖向动画帧图 (split_image 的等价)
 * @param {ImageData} imageData
 * @param {number} retainNum - 保留帧数
 * @returns {ImageData[]} 拆分后的帧数组
 */
export function splitAnimationFrames(imageData, retainNum) {
  const w = imageData.width
  const totalH = imageData.height
  const numSplits = Math.floor(totalH / w)
  const splitH = Math.floor(totalH / numSplits)

  let indices
  if (numSplits > retainNum) {
    const step = numSplits / retainNum
    indices = Array.from({ length: retainNum }, (_, i) => Math.min(Math.floor(i * step), numSplits - 1))
  } else {
    indices = Array.from({ length: numSplits }, (_, i) => i)
  }

  return indices.map(i => cropImageData(imageData, 0, i * splitH, w, (i + 1) * splitH))
}

/**
 * 合并多张图像为竖向拼接
 * @param {ImageData[]} frames
 * @returns {ImageData}
 */
export function mergeImagesVertical(frames) {
  if (frames.length === 0) return createImageData(1, 1)
  const w = frames[0].width
  const totalH = frames.reduce((sum, f) => sum + f.height, 0)
  const result = createImageData(w, totalH)
  let currentY = 0
  for (const frame of frames) {
    pasteImageData(result, frame, 0, currentY)
    currentY += frame.height
  }
  return result
}

/**
 * 克隆 ImageData
 * @param {ImageData} imageData
 * @returns {ImageData}
 */
export function cloneImageData(imageData) {
  return new ImageData(
    new Uint8ClampedArray(imageData.data),
    imageData.width,
    imageData.height
  )
}

/**
 * 水平拼接多个 ImageData
 * @param {ImageData[]} images - 要拼接的图像数组
 * @returns {ImageData}
 */
export function concatenateHorizontal(...images) {
  if (images.length === 0) return createImageData(1, 1)
  const totalWidth = images.reduce((sum, img) => sum + img.width, 0)
  const maxHeight = Math.max(...images.map(img => img.height))
  const result = createImageData(totalWidth, maxHeight)
  let currentX = 0
  for (const img of images) {
    pasteImageData(result, img, currentX, 0)
    currentX += img.width
  }
  return result
}

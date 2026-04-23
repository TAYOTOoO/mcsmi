/**
 * useImageProcessing.js
 * 图片处理算法库 - 从 Result.vue 提取 + 新增VIP专属算法
 */

// =================== 基础像素操作 ===================

/**
 * 获取像素颜色 [R, G, B, A]
 */
export function getPixelColor(imageData, x, y) {
  const index = (y * imageData.width + x) * 4
  return [
    imageData.data[index],
    imageData.data[index + 1],
    imageData.data[index + 2],
    imageData.data[index + 3]
  ]
}

/**
 * 设置像素颜色
 */
export function setPixelColor(imageData, x, y, color) {
  const index = (y * imageData.width + x) * 4
  imageData.data[index] = color[0]
  imageData.data[index + 1] = color[1]
  imageData.data[index + 2] = color[2]
  imageData.data[index + 3] = color[3]
}

/**
 * 比较两个颜色是否相似（基于容差）
 */
export function isColorSimilar(color1, color2, tolerance) {
  return Math.abs(color1[0] - color2[0]) <= tolerance &&
         Math.abs(color1[1] - color2[1]) <= tolerance &&
         Math.abs(color1[2] - color2[2]) <= tolerance
}

// =================== 现有算法（从 Result.vue 提取） ===================

/**
 * 洪水填充算法（非递归）
 */
export function floodFill(imageData, x, y, targetColor, fillColor, tolerance) {
  const stack = [[x, y]]
  const visited = new Set()
  const width = imageData.width
  const height = imageData.height

  while (stack.length > 0) {
    const [px, py] = stack.pop()
    const key = `${px},${py}`

    if (visited.has(key) || px < 0 || py < 0 || px >= width || py >= height) continue
    visited.add(key)

    const currentColor = getPixelColor(imageData, px, py)
    if (!isColorSimilar(currentColor, targetColor, tolerance)) continue

    setPixelColor(imageData, px, py, fillColor)

    // 4方向扩展
    stack.push([px + 1, py], [px - 1, py], [px, py + 1], [px, py - 1])
  }
}

/**
 * 添加内描边（纯黑色，沿非透明内容边缘）
 * 原始 Result.vue 版本 - 保留作为选项
 */
export function addInnerStroke(imageData, strokeWidth) {
  const width = imageData.width
  const height = imageData.height
  const data = new Uint8ClampedArray(imageData.data)

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 4
      const alpha = data[idx + 3]

      if (alpha > 0) {
        let isEdge = false
        for (let dy = -strokeWidth; dy <= strokeWidth; dy++) {
          for (let dx = -strokeWidth; dx <= strokeWidth; dx++) {
            if (dx === 0 && dy === 0) continue
            const nx = x + dx
            const ny = y + dy
            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
              const nIdx = (ny * width + nx) * 4
              if (data[nIdx + 3] === 0) {
                isEdge = true
                break
              }
            }
          }
          if (isEdge) break
        }

        if (isEdge) {
          imageData.data[idx] = 0
          imageData.data[idx + 1] = 0
          imageData.data[idx + 2] = 0
          imageData.data[idx + 3] = 255
        }
      }
    }
  }

  return imageData
}

/**
 * 实体化半透明边缘（模拟PS的多次复制图层+合并）
 */
export function solidifyEdges(imageData, iterations = 5) {
  const width = imageData.width
  const height = imageData.height
  const result = new Uint8ClampedArray(imageData.data)

  for (let iter = 0; iter < iterations; iter++) {
    for (let i = 0; i < result.length; i += 4) {
      const alpha = result[i + 3]
      if (result[i] === 0 && result[i + 1] === 0 && result[i + 2] === 0) {
        if (alpha > 0 && alpha < 255) {
          const newAlpha = Math.min(255, alpha + (255 - alpha) * 0.5)
          result[i + 3] = Math.floor(newAlpha)
        }
      }
    }
  }

  for (let i = 0; i < result.length; i++) {
    imageData.data[i] = result[i]
  }

  return imageData
}

// =================== F1: 智能边缘修复算法 ===================

/**
 * 智能边缘修复 - 用内层像素颜色覆盖边缘半透明像素
 * 解决抠白底后边缘残留半透明白色像素的问题
 *
 * 算法流程：
 * 1. 从原始数据快照读取（避免级联修改）
 * 2. 遍历所有非透明像素，检测8邻域中是否有透明像素（判定为边缘）
 * 3. 对边缘像素：向内搜索最近的完全不透明像素（alpha=255）
 * 4. 用找到的内层像素RGB颜色覆盖边缘像素，alpha设为255
 *
 * @param {ImageData} imageData - 图像数据
 * @param {number} searchDepth - 向内搜索深度（默认5像素）
 * @returns {ImageData} 处理后的图像数据
 */
export function addSmartEdgeRepair(imageData, searchDepth = 5) {
  const width = imageData.width
  const height = imageData.height
  // 从原始数据快照读取，避免级联修改
  const snapshot = new Uint8ClampedArray(imageData.data)

  // 第一步：标记所有边缘像素
  const edgePixels = []

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 4
      const alpha = snapshot[idx + 3]

      // 跳过完全透明和完全不透明的像素
      // 边缘像素特征：有一定透明度（alpha < 255）且alpha > 0
      // 或者alpha=255但邻近透明像素
      if (alpha === 0) continue

      // 检查是否为边缘（8邻域中有透明像素）
      let isEdge = false
      for (let dy = -1; dy <= 1; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
          if (dx === 0 && dy === 0) continue
          const nx = x + dx
          const ny = y + dy
          if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
            isEdge = true
            break
          }
          const nIdx = (ny * width + nx) * 4
          if (snapshot[nIdx + 3] === 0) {
            isEdge = true
            break
          }
        }
        if (isEdge) break
      }

      if (isEdge && alpha < 255) {
        edgePixels.push({ x, y, idx })
      }
    }
  }

  // 第二步：对每个边缘像素，向内搜索最近的完全不透明像素
  for (const { x, y, idx } of edgePixels) {
    let bestColor = null
    let bestDist = Infinity

    // 在搜索深度内寻找不透明像素
    for (let dy = -searchDepth; dy <= searchDepth; dy++) {
      for (let dx = -searchDepth; dx <= searchDepth; dx++) {
        if (dx === 0 && dy === 0) continue
        const nx = x + dx
        const ny = y + dy
        if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue

        const nIdx = (ny * width + nx) * 4
        // 必须是完全不透明的像素
        if (snapshot[nIdx + 3] === 255) {
          // 确保这个像素不是边缘像素（它的邻域不能全是半透明的）
          const dist = dx * dx + dy * dy
          if (dist < bestDist) {
            bestDist = dist
            bestColor = [snapshot[nIdx], snapshot[nIdx + 1], snapshot[nIdx + 2]]
          }
        }
      }
    }

    // 如果找到了内层像素，使用其颜色覆盖
    if (bestColor) {
      imageData.data[idx] = bestColor[0]
      imageData.data[idx + 1] = bestColor[1]
      imageData.data[idx + 2] = bestColor[2]
      imageData.data[idx + 3] = 255
    }
  }

  return imageData
}

// =================== F2: 自动识别底色算法 ===================

/**
 * 自动检测图片背景颜色
 * 采样图片4条边缘5%区域的像素，量化后统计主色调
 *
 * @param {ImageData} imageData - 图像数据
 * @returns {{ r: number, g: number, b: number, confidence: number }} 检测到的背景颜色和置信度
 */
export function autoDetectBackgroundColor(imageData) {
  const width = imageData.width
  const height = imageData.height
  const margin = Math.max(1, Math.floor(Math.min(width, height) * 0.05))

  // 收集边缘像素
  const samples = []

  // 上边缘
  for (let y = 0; y < margin; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 4
      if (imageData.data[idx + 3] > 128) { // 只采集半透明以上的像素
        samples.push([imageData.data[idx], imageData.data[idx + 1], imageData.data[idx + 2]])
      }
    }
  }

  // 下边缘
  for (let y = height - margin; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 4
      if (imageData.data[idx + 3] > 128) {
        samples.push([imageData.data[idx], imageData.data[idx + 1], imageData.data[idx + 2]])
      }
    }
  }

  // 左边缘
  for (let y = margin; y < height - margin; y++) {
    for (let x = 0; x < margin; x++) {
      const idx = (y * width + x) * 4
      if (imageData.data[idx + 3] > 128) {
        samples.push([imageData.data[idx], imageData.data[idx + 1], imageData.data[idx + 2]])
      }
    }
  }

  // 右边缘
  for (let y = margin; y < height - margin; y++) {
    for (let x = width - margin; x < width; x++) {
      const idx = (y * width + x) * 4
      if (imageData.data[idx + 3] > 128) {
        samples.push([imageData.data[idx], imageData.data[idx + 1], imageData.data[idx + 2]])
      }
    }
  }

  if (samples.length === 0) {
    return { r: 255, g: 255, b: 255, confidence: 0 }
  }

  // 量化颜色（将RGB各通道量化到16级），统计频率
  const colorCounts = {}
  for (const [r, g, b] of samples) {
    // 量化到16级
    const qr = Math.round(r / 16) * 16
    const qg = Math.round(g / 16) * 16
    const qb = Math.round(b / 16) * 16
    const key = `${qr},${qg},${qb}`
    colorCounts[key] = (colorCounts[key] || 0) + 1
  }

  // 找出频率最高的颜色
  let maxCount = 0
  let dominantKey = null
  for (const [key, count] of Object.entries(colorCounts)) {
    if (count > maxCount) {
      maxCount = count
      dominantKey = key
    }
  }

  // 取所有属于主色调的原始像素求平均值（更精确的颜色）
  const [qr, qg, qb] = dominantKey.split(',').map(Number)
  let sumR = 0, sumG = 0, sumB = 0, count = 0
  for (const [r, g, b] of samples) {
    if (Math.abs(r - qr) <= 16 && Math.abs(g - qg) <= 16 && Math.abs(b - qb) <= 16) {
      sumR += r
      sumG += g
      sumB += b
      count++
    }
  }

  const confidence = maxCount / samples.length // 置信度 = 主色调占比

  return {
    r: Math.round(sumR / count),
    g: Math.round(sumG / count),
    b: Math.round(sumB / count),
    confidence
  }
}

/**
 * 自动移除背景色
 * 从图片4条边缘开始flood fill，以检测到的底色为目标色
 *
 * @param {ImageData} imageData - 图像数据
 * @param {{ r: number, g: number, b: number }} bgColor - 检测到的背景颜色
 * @param {number} tolerance - 颜色容差
 * @returns {ImageData} 处理后的图像数据
 */
export function autoRemoveBackground(imageData, bgColor, tolerance = 30) {
  const width = imageData.width
  const height = imageData.height
  const targetColor = [bgColor.r, bgColor.g, bgColor.b, 255]
  const fillColor = [0, 0, 0, 0]

  // 用Set记录已处理像素，从4条边缘的所有匹配像素开始
  const visited = new Set()
  const stack = []

  // 收集4条边缘的起始点
  for (let x = 0; x < width; x++) {
    // 上边缘
    const topColor = getPixelColor(imageData, x, 0)
    if (topColor[3] > 0 && isColorSimilar(topColor, targetColor, tolerance)) {
      stack.push([x, 0])
    }
    // 下边缘
    const bottomColor = getPixelColor(imageData, x, height - 1)
    if (bottomColor[3] > 0 && isColorSimilar(bottomColor, targetColor, tolerance)) {
      stack.push([x, height - 1])
    }
  }
  for (let y = 0; y < height; y++) {
    // 左边缘
    const leftColor = getPixelColor(imageData, 0, y)
    if (leftColor[3] > 0 && isColorSimilar(leftColor, targetColor, tolerance)) {
      stack.push([0, y])
    }
    // 右边缘
    const rightColor = getPixelColor(imageData, width - 1, y)
    if (rightColor[3] > 0 && isColorSimilar(rightColor, targetColor, tolerance)) {
      stack.push([width - 1, y])
    }
  }

  // 从边缘开始flood fill
  while (stack.length > 0) {
    const [px, py] = stack.pop()
    const key = py * width + px

    if (visited.has(key) || px < 0 || py < 0 || px >= width || py >= height) continue
    visited.add(key)

    const currentColor = getPixelColor(imageData, px, py)
    if (currentColor[3] === 0) continue // 已经透明
    if (!isColorSimilar(currentColor, targetColor, tolerance)) continue

    setPixelColor(imageData, px, py, fillColor)

    stack.push([px + 1, py], [px - 1, py], [px, py + 1], [px, py - 1])
  }

  return imageData
}

// =================== F3: 毛边修复算法（VIP）===================

/**
 * 毛边修复 - 移除抠底后残留的白色/浅色边缘像素
 *
 * 算法：
 * 1. 找出所有"边缘像素"（至少有一个相邻透明像素，alpha=0）
 * 2. 对边缘像素检测"白色污染"：min(R,G,B) > (255 - threshold)
 * 3. 符合条件的像素设为完全透明
 *
 * @param {ImageData} imageData - 图像数据（直接修改）
 * @param {number} threshold - 阈值 1-100
 *   1  → 仅移除几乎纯白的边缘(min>254)
 *   50 → 移除所有通道>205的浅色边缘
 *   100→ 移除所有通道>155的中亮边缘（较激进）
 * @returns {ImageData}
 */
export function applyFringeRepair(imageData, threshold) {
  const width = imageData.width
  const height = imageData.height
  const data = imageData.data
  // 读取原始数据的快照，避免级联修改
  const snapshot = new Uint8ClampedArray(data)
  const cutoff = 255 - threshold

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 4
      const alpha = snapshot[idx + 3]

      // 跳过完全透明的像素
      if (alpha === 0) continue

      // 检测是否为边缘像素（8邻域中有透明像素）
      let isEdge = false
      for (let dy = -1; dy <= 1 && !isEdge; dy++) {
        for (let dx = -1; dx <= 1 && !isEdge; dx++) {
          if (dx === 0 && dy === 0) continue
          const nx = x + dx, ny = y + dy
          if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
            // 图片边界也算边缘
            isEdge = true
          } else {
            const nIdx = (ny * width + nx) * 4
            if (snapshot[nIdx + 3] === 0) isEdge = true
          }
        }
      }

      if (!isEdge) continue

      // 检测白色污染：三个通道都超过阈值
      const r = snapshot[idx], g = snapshot[idx + 1], b = snapshot[idx + 2]
      if (r > cutoff && g > cutoff && b > cutoff) {
        data[idx + 3] = 0  // 设为透明
      }
    }
  }

  return imageData
}

// =================== F4: Alpha二值化算法 ===================

/**
 * Alpha二值化 - 将所有半透明像素转为完全透明或完全不透明
 *
 * @param {ImageData} imageData - 图像数据
 * @param {number} threshold - 阈值（默认128，范围1-254）
 *   alpha < threshold → 0（完全透明）
 *   alpha >= threshold → 255（完全不透明）
 * @returns {ImageData} 处理后的图像数据
 */
export function applyAlphaBinarization(imageData, threshold = 128) {
  const data = imageData.data
  for (let i = 3; i < data.length; i += 4) {
    data[i] = data[i] < threshold ? 0 : 255
  }
  return imageData
}

// =================== 导出组合式函数 ===================

export function useImageProcessing() {
  return {
    // 基础操作
    getPixelColor,
    setPixelColor,
    isColorSimilar,
    // 现有算法
    floodFill,
    addInnerStroke,
    solidifyEdges,
    // F1: 智能边缘修复
    addSmartEdgeRepair,
    // F2: 自动识别底色
    autoDetectBackgroundColor,
    autoRemoveBackground,
    // F4: Alpha二值化
    applyAlphaBinarization
  }
}

/**
 * 反向转换: Format 5→4
 * 反向箱子处理
 */
import {
  log, fileExists, readImageFromZip, writeImageToZip,
  ENTITY_PATH
} from '../fileUtils'
import {
  swapAndMirror, flipBoth, cropImageData, pasteImageData,
  createImageData, determineScaleFactor, scaledBox
} from '../imageUtils'
import {
  SINGLE_CHEST_FILES, SINGLE_CHEST_SWAP_BOXES, SINGLE_CHEST_FLIP_BOXES
} from '../constants'

/**
 * 反向处理箱子
 */
export async function reverseProcessChestFolder(zip, logCb) {
  const chestPath = ENTITY_PATH + 'chest/'

  for (const chestFile of SINGLE_CHEST_FILES) {
    const filePath = chestPath + chestFile
    const imgResult = await readImageFromZip(zip, filePath)
    if (!imgResult) continue

    const { imageData, width, height } = imgResult
    const scale = determineScaleFactor(width, height, 64, 64)
    if (scale === null) {
      log(`不支持的 ${chestFile} 尺寸: ${width}x${height}`, logCb)
      continue
    }

    // 反向操作: 先 swap_and_mirror, 再 flip
    for (const [box1Base, box2Base] of SINGLE_CHEST_SWAP_BOXES) {
      const box1 = scaledBox(...box1Base, scale)
      const box2 = scaledBox(...box2Base, scale)
      swapAndMirror(imageData, box1, box2)
    }

    for (const boxBase of SINGLE_CHEST_FLIP_BOXES) {
      const box = scaledBox(...boxBase, scale)
      const region = flipBoth(cropImageData(imageData, ...box))
      pasteImageData(imageData, region, box[0], box[1])
    }

    await writeImageToZip(zip, filePath, imageData)
    log(`已反向处理箱子 ${chestFile}`, logCb)
  }
}

export default {
  reverseProcessChestFolder
}

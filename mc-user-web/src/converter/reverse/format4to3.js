/**
 * 反向转换: Format 4→3
 * 反向方块/物品重命名, 反向粒子, 删除马文件夹
 */
import {
  log, fileExists, folderExists, readImageFromZip, writeImageToZip,
  renameFolder, batchRenameFiles, deleteFolder,
  TEXTURES_PATH, ITEMS_PATH_NEW, ITEMS_PATH_OLD, BLOCKS_PATH_NEW, BLOCKS_PATH_OLD,
  ENTITY_PATH, PARTICLE_PATH
} from '../fileUtils'
import {
  cropImageData, pasteImageData, createImageData
} from '../imageUtils'
import {
  ITEM_RENAME_PAIRS, BLOCK_PROCESS_RENAME, PARTICLE_MERGE_MAP
} from '../constants'

/**
 * 反向重命名方块和物品
 */
export async function reverseRenameBlocksItems(zip, logCb) {
  // 反转物品重命名映射
  const reverseItemRename = Object.fromEntries(
    Object.entries(ITEM_RENAME_PAIRS).map(([k, v]) => [v, k])
  )

  // 反转方块重命名映射
  const reverseBlockRename = Object.fromEntries(
    Object.entries(BLOCK_PROCESS_RENAME).map(([k, v]) => [v, k])
  )

  // 重命名 item → items
  if (folderExists(zip, ITEMS_PATH_NEW)) {
    // 先重命名文件
    await batchRenameFiles(zip, ITEMS_PATH_NEW, reverseItemRename, logCb)
    // 再重命名文件夹
    await renameFolder(zip, ITEMS_PATH_NEW, ITEMS_PATH_OLD, logCb)
  }

  // 重命名 block → blocks
  if (folderExists(zip, BLOCKS_PATH_NEW)) {
    await batchRenameFiles(zip, BLOCKS_PATH_NEW, reverseBlockRename, logCb)
    await renameFolder(zip, BLOCKS_PATH_NEW, BLOCKS_PATH_OLD, logCb)
  }
}

/**
 * 反向粒子拆分 (合并粒子小图片回 particles.png)
 */
export async function reverseFixParticles(zip, logCb) {
  // 确定分割大小
  let splitSize = null
  for (const filename of Object.keys(PARTICLE_MERGE_MAP)) {
    const particlePath = PARTICLE_PATH + filename
    const entityPath = ENTITY_PATH + filename
    let imgResult = await readImageFromZip(zip, particlePath)
    if (!imgResult) imgResult = await readImageFromZip(zip, entityPath)
    if (imgResult) {
      splitSize = imgResult.width
      break
    }
  }

  if (splitSize === null) {
    log('未找到粒子图片', logCb)
    return
  }

  const rows = 16
  const cols = 16
  const merged = createImageData(cols * splitSize, rows * splitSize)

  for (const [filename, [row, col]] of Object.entries(PARTICLE_MERGE_MAP)) {
    const particlePath = PARTICLE_PATH + filename
    const entityPath = ENTITY_PATH + filename
    let imgResult = await readImageFromZip(zip, particlePath)
    if (!imgResult) imgResult = await readImageFromZip(zip, entityPath)
    if (imgResult) {
      pasteImageData(merged, imgResult.imageData, col * splitSize, row * splitSize)
      // 删除小图片
      if (fileExists(zip, particlePath)) zip.remove(particlePath)
      if (fileExists(zip, entityPath)) zip.remove(entityPath)
    }
  }

  await writeImageToZip(zip, PARTICLE_PATH + 'particles.png', merged)
  log('已合并粒子图片为 particles.png', logCb)
}

/**
 * 删除马文件夹
 */
export async function deleteHorseFolder(zip, logCb) {
  deleteFolder(zip, ENTITY_PATH + 'horse', logCb)
}

export default {
  reverseRenameBlocksItems,
  reverseFixParticles,
  deleteHorseFolder
}

/**
 * Format 6→7 转换
 * 删除 font, 生成 snow_bucket
 */
import { log, fileExists, deleteFolder, copyFile, ITEMS_PATH_NEW } from '../fileUtils'

/**
 * 删除 font 文件夹
 */
export async function deleteFontFolder(zip, logCb) {
  deleteFolder(zip, 'assets/minecraft/font', logCb)
}

/**
 * 生成雪桶
 */
export async function generateSnowBucket(zip, logCb) {
  const milkBucketPath = ITEMS_PATH_NEW + 'milk_bucket.png'
  if (fileExists(zip, milkBucketPath)) {
    await copyFile(zip, milkBucketPath, ITEMS_PATH_NEW + 'powder_snow_bucket.png')
    log('已生成 powder_snow_bucket.png (从 milk_bucket)', logCb)
    // overlay 需要外部文件，跳过
  } else {
    log('未找到 milk_bucket.png', logCb)
  }
}

export default {
  deleteFontFolder,
  generateSnowBucket
}

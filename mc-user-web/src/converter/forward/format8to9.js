/**
 * Format 8→9 转换
 * mcpatcher → optifine 重命名
 */
import { log, folderExists, renameFolder } from '../fileUtils'

/**
 * 重命名 mcpatcher 为 optifine
 */
export async function renameMcpatcherToOptifine(zip, logCb) {
  const mcpatcherPath = 'assets/minecraft/mcpatcher'
  const optifinePath = 'assets/minecraft/optifine'
  if (folderExists(zip, mcpatcherPath)) {
    await renameFolder(zip, mcpatcherPath, optifinePath, logCb)
  } else {
    log('未找到 mcpatcher 文件夹', logCb)
  }
}

export default {
  renameMcpatcherToOptifine
}

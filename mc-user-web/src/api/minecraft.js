import axios from 'axios'

// 版本清单 API
const VERSION_MANIFEST = {
  official: 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json',
  bmclapi: '/api/bmclapi/mc/game/version_manifest_v2.json'
}

// 获取版本清单
export async function getVersionManifest(source = 'bmclapi') {
  try {
    const url = VERSION_MANIFEST[source]
    const response = await fetch(url, {
      method: 'GET',
      mode: 'cors'
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    // 如果国内源失败，尝试官方源
    if (source === 'bmclapi') {
      console.warn('BMCLAPI 源失败，尝试官方源...', error)
      return getVersionManifest('official')
    }
    throw error
  }
}

// 获取指定版本的详细信息
export async function getVersionDetail(versionId, source = 'bmclapi') {
  try {
    const manifest = await getVersionManifest(source)
    const version = manifest.versions.find(v => v.id === versionId)

    if (!version) {
      throw new Error(`未找到版本: ${versionId}`)
    }

    // 获取版本详细信息
    let versionUrl = version.url

    // 如果使用 BMCLAPI，替换域名
    if (source === 'bmclapi') {
      versionUrl = versionUrl.replace(
        'piston-meta.mojang.com',
        '/api/bmclapi'
      ).replace(
        'launchermeta.mojang.com',
        'bmclapi2.bangbang93.com'
      )
    }

    const response = await fetch(versionUrl, {
      method: 'GET',
      mode: 'cors'
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    return await response.json()
  } catch (error) {
    if (source === 'bmclapi') {
      console.warn('BMCLAPI 源失败，尝试官方源...', error)
      return getVersionDetail(versionId, 'official')
    }
    throw error
  }
}

// 获取 client.jar 下载 URL
export async function getClientJarUrl(versionId, source = 'bmclapi') {
  try {
    if (source === 'bmclapi') {
      // BMCLAPI 快速下载接口
      return `/api/bmclapi/version/${versionId}/client`
    } else {
      // 官方源需要先获取版本详情
      const versionDetail = await getVersionDetail(versionId, 'official')

      if (!versionDetail.downloads || !versionDetail.downloads.client) {
        throw new Error('该版本没有 client.jar 下载链接')
      }

      return versionDetail.downloads.client.url
    }
  } catch (error) {
    if (source === 'bmclapi') {
      console.warn('BMCLAPI 源失败，尝试官方源...', error)
      return getClientJarUrl(versionId, 'official')
    }
    throw error
  }
}

// 下载 client.jar（直接下载，不通过后端代理）
export async function downloadClientJar(versionId, source = 'bmclapi', onProgress) {
  try {
    const downloadUrl = await getClientJarUrl(versionId, source)

    console.log('开始下载:', downloadUrl)

    // 使用 fetch 下载
    const response = await fetch(downloadUrl, {
      method: 'GET',
      mode: 'cors'
    })

    if (!response.ok) {
      throw new Error(`下载失败: HTTP ${response.status}`)
    }

    // 检查 Content-Type
    const contentType = response.headers.get('Content-Type')
    console.log('Content-Type:', contentType)

    // 获取文件大小
    const contentLength = response.headers.get('Content-Length')
    const total = contentLength ? parseInt(contentLength, 10) : 0

    // 读取响应体
    const reader = response.body.getReader()
    const chunks = []
    let receivedLength = 0

    while (true) {
      const { done, value } = await reader.read()

      if (done) break

      chunks.push(value)
      receivedLength += value.length

      // 更新进度
      if (onProgress && total > 0) {
        const percentCompleted = Math.round((receivedLength / total) * 100)
        onProgress(percentCompleted)
      }
    }

    // 合并所有 chunks
    const allChunks = new Uint8Array(receivedLength)
    let position = 0
    for (const chunk of chunks) {
      allChunks.set(chunk, position)
      position += chunk.length
    }

    console.log('下载完成，大小:', receivedLength, 'bytes')

    // 创建 Blob
    const blob = new Blob([allChunks], { type: 'application/java-archive' })

    // 验证 Blob
    if (blob.size === 0) {
      throw new Error('下载的文件为空')
    }

    if (blob.size < 1024 * 1024) {
      // 小于 1MB，可能是错误页面
      console.warn('文件太小，可能不是有效的 client.jar')
    }

    return blob
  } catch (error) {
    console.error('下载出错:', error)

    // 如果是 BMCLAPI 失败，尝试官方源
    if (source === 'bmclapi') {
      console.warn('BMCLAPI 下载失败，尝试官方源...', error)
      return downloadClientJar(versionId, 'official', onProgress)
    }

    throw error
  }
}

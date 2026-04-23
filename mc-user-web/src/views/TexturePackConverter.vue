<template>
  <div class="page-wrapper">
    <div class="top-border"></div>
    <div class="content-body">
      <div class="side-border left"></div>
      <main>
        <!-- 页头 -->
        <header class="page-header">
          <div class="mc-box logo">材质工坊</div>
          <nav class="main-nav">
            <ul>
              <li><router-link to="/" class="mc-box">首页</router-link></li>
              <li><router-link to="/generate" class="mc-box">开始生成</router-link></li>
              <li><router-link to="/my-tasks" class="mc-box">我的任务</router-link></li>
              <li><router-link to="/texture-pack-editor" class="mc-box">材质编辑</router-link></li>
              <li><router-link to="/texture-converter" class="mc-box active">版本转换</router-link></li>
              <li><router-link to="/recharge" class="mc-box">积分充值</router-link></li>
            </ul>
          </nav>
          <div class="user-actions">
            <template v-if="username">
              <span class="mc-box username-display" @click="$router.push('/profile')" title="个人中心">
                {{ username }}
              </span>
              <button class="mc-box logout-btn" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <button class="mc-box login-btn" @click="$router.push('/login')">登录</button>
            </template>
          </div>
        </header>

        <!-- 主要内容 -->
        <section class="converter-section">
          <div class="section-header mc-box">
            <h2>材质包版本转换器</h2>
          </div>

          <div class="converter-container mc-box">
            <!-- Step 1: 上传 -->
            <div class="step-block">
              <div class="step-title">
                <img src="/assets/images/new-textures/default_sapling.png" alt="" class="label-icon" />
                <span>上传材质包</span>
              </div>
              <div
                class="upload-zone mc-box"
                :class="{ 'drag-over': isDragOver }"
                @dragover.prevent="isDragOver = true"
                @dragleave="isDragOver = false"
                @drop.prevent="handleDrop"
                @click="triggerFileInput"
              >
                <input
                  ref="fileInput"
                  type="file"
                  accept=".zip"
                  multiple
                  style="display: none"
                  @change="handleFileSelect"
                />
                <div v-if="files.length === 0" class="upload-placeholder">
                  <img src="/assets/images/new-textures/default_cobble.png" alt="" class="upload-icon" />
                  <p>拖拽 ZIP 文件到此处，或点击选择文件</p>
                </div>
                <div v-else class="upload-files">
                  <div v-for="(file, idx) in files" :key="idx" class="file-item">
                    <span class="file-name">{{ file.name }}</span>
                    <span class="file-size">{{ formatSize(file.size) }}</span>
                    <button class="file-remove" @click.stop="removeFile(idx)">x</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Step 2: 配置 -->
            <div class="step-block" v-if="files.length > 0">
              <div class="step-title">
                <img src="/assets/images/new-textures/flowers_tulip.png" alt="" class="label-icon" />
                <span>选择目标版本</span>
              </div>

              <div class="version-info" v-if="detectedVersion">
                <span class="info-label">检测到源版本:</span>
                <span class="info-value">{{ detectedVersionStr }}</span>
              </div>

              <div class="version-select-group">
                <label class="select-label">目标版本:</label>
                <select v-model="targetVersion" class="mc-select">
                  <option v-for="ver in targetVersions" :key="ver" :value="ver">{{ ver }}</option>
                </select>
              </div>

              <div class="conversion-path" v-if="conversionSteps.length > 0">
                <span class="path-label">转换路径:</span>
                <div class="path-steps">
                  <span v-for="(step, i) in conversionSteps" :key="i" class="path-step">{{ step }}</span>
                </div>
              </div>

              <button
                class="mc-box btn-convert"
                :disabled="converting || !targetVersion"
                @click="startConversion"
              >
                <img src="/assets/images/new-textures/default_mese_crystal.png" alt="" class="btn-icon" />
                {{ converting ? '转换中...' : '开始转换' }}
              </button>
            </div>

            <!-- Step 3: 进度 -->
            <div class="step-block" v-if="converting || logs.length > 0">
              <div class="step-title">
                <img src="/assets/images/new-textures/default_tool_diamondpick.png" alt="" class="label-icon" />
                <span>转换进度</span>
              </div>

              <div class="progress-bar-container">
                <div class="progress-bar-bg mc-box">
                  <div class="progress-bar-fill" :style="{ width: (progress * 100) + '%' }"></div>
                </div>
                <span class="progress-text">{{ Math.round(progress * 100) }}%</span>
              </div>

              <div class="log-container mc-box" ref="logContainer">
                <div v-for="(logMsg, i) in logs" :key="i" class="log-line">{{ logMsg }}</div>
              </div>
            </div>

            <!-- Step 4: 下载 -->
            <div class="step-block" v-if="resultBlobs.length > 0">
              <div class="step-title">
                <img src="/assets/images/new-textures/default_mese_crystal.png" alt="" class="label-icon" />
                <span>下载结果</span>
              </div>

              <div class="download-list">
                <a
                  v-for="(result, i) in resultBlobs"
                  :key="i"
                  :href="result.url"
                  :download="result.name"
                  class="mc-box download-btn"
                >
                  <img src="/assets/images/new-textures/default_cobble.png" alt="" class="btn-icon" />
                  {{ result.name }}
                </a>
              </div>
            </div>
          </div>
        </section>
      </main>
      <div class="side-border right"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { PACK_FORMAT_MAP, VERSION_TO_PACK_FORMAT_MAP, PACK_FORMAT_ORDER } from '@/converter/constants'
import { loadZip, exportZip, processZip, getPackFormat, getVersionString, getConversionPath } from '@/converter/index'

const router = useRouter()

const username = ref(localStorage.getItem('username') || '')
const files = ref([])
const fileInput = ref(null)
const isDragOver = ref(false)
const detectedVersion = ref(null)
const targetVersion = ref('')
const converting = ref(false)
const progress = ref(0)
const logs = ref([])
const logContainer = ref(null)
const resultBlobs = ref([])

const targetVersions = Object.values(PACK_FORMAT_MAP)

const detectedVersionStr = computed(() => {
  if (!detectedVersion.value) return ''
  return getVersionString(detectedVersion.value)
})

const conversionSteps = computed(() => {
  if (!detectedVersion.value || !targetVersion.value) return []
  const targetFormat = VERSION_TO_PACK_FORMAT_MAP[targetVersion.value]
  if (!targetFormat) return []
  return getConversionPath(detectedVersion.value, targetFormat)
})

function triggerFileInput() {
  fileInput.value?.click()
}

function handleFileSelect(e) {
  const selected = Array.from(e.target.files).filter(f => f.name.toLowerCase().endsWith('.zip'))
  if (selected.length > 0) {
    files.value = selected
    detectVersion()
  }
}

function handleDrop(e) {
  isDragOver.value = false
  const dropped = Array.from(e.dataTransfer.files).filter(f => f.name.toLowerCase().endsWith('.zip'))
  if (dropped.length > 0) {
    files.value = dropped
    detectVersion()
  }
}

function removeFile(idx) {
  files.value.splice(idx, 1)
  if (files.value.length === 0) {
    detectedVersion.value = null
  }
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

async function detectVersion() {
  if (files.value.length === 0) return
  try {
    const zip = await loadZip(files.value[0])
    const format = await getPackFormat(zip)
    if (format !== null) {
      detectedVersion.value = format
      addLog(`检测到 pack_format: ${format} (${getVersionString(format)})`)
    } else {
      addLog('无法检测 pack_format')
      detectedVersion.value = null
    }
  } catch (e) {
    addLog(`读取 ZIP 失败: ${e.message}`)
    detectedVersion.value = null
  }
}

function addLog(msg) {
  logs.value.push(`[${new Date().toLocaleTimeString()}] ${msg}`)
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

async function startConversion() {
  if (files.value.length === 0 || !targetVersion.value) return

  const targetFormat = VERSION_TO_PACK_FORMAT_MAP[targetVersion.value]
  if (!targetFormat) {
    addLog('无效的目标版本')
    return
  }

  converting.value = true
  progress.value = 0
  resultBlobs.value = []

  // 清理之前的 URL
  revokeUrls()

  for (const file of files.value) {
    try {
      addLog(`开始处理: ${file.name}`)
      const zip = await loadZip(file)
      const sourceFormat = await getPackFormat(zip)

      if (sourceFormat === null) {
        addLog(`无法检测 ${file.name} 的版本，跳过`)
        continue
      }

      if (sourceFormat === targetFormat) {
        addLog(`${file.name} 已经是目标版本，跳过`)
        continue
      }

      addLog(`源版本: ${getVersionString(sourceFormat)}, 目标版本: ${targetVersion.value}`)

      await processZip(
        zip,
        sourceFormat,
        targetFormat,
        (msg) => addLog(msg),
        (p) => { progress.value = p }
      )

      const blob = await exportZip(zip)
      const newName = `[${targetVersion.value}]${file.name}`
      const url = URL.createObjectURL(blob)
      resultBlobs.value.push({ name: newName, url, blob })

      addLog(`已完成: ${newName}`)
    } catch (e) {
      addLog(`处理 ${file.name} 失败: ${e.message}`)
      console.error(e)
    }
  }

  progress.value = 1
  converting.value = false
  addLog('所有文件处理完成')
}

function revokeUrls() {
  for (const r of resultBlobs.value) {
    if (r.url) URL.revokeObjectURL(r.url)
  }
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  username.value = ''
  router.push('/login')
}

onBeforeUnmount(() => {
  revokeUrls()
})
</script>

<style scoped>
/* --- 整体布局 (与其他页面一致) --- */
.page-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px 20px 20px;
  transform: scale(1.5);
  transform-origin: top center;
  margin-bottom: 200px;
}

.top-border {
  background-image: url('/assets/images/new-textures/default_obsidian_brick.png');
  background-repeat: repeat-x;
  background-size: 64px 64px;
  height: 48px;
  width: 100%;
  max-width: 996px;
  border: 4px solid var(--mc-border-black);
  border-bottom: 0;
  flex-shrink: 0;
  image-rendering: pixelated;
}

.content-body {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 1004px;
}

.side-border {
  background-image: url('/assets/images/new-textures/default_obsidian_block.png');
  background-repeat: repeat-y;
  background-size: 64px 64px;
  width: 48px;
  flex-shrink: 0;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  border-bottom: 0;
  image-rendering: pixelated;
}
.side-border.left { border-right: 4px solid var(--mc-border-black); }
.side-border.right { border-left: 4px solid var(--mc-border-black); }

main {
  max-width: 900px;
  width: 100%;
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('/assets/images/new-textures/default_pine_wood.png');
  background-size: auto, 64px 64px;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  padding: 0 20px 20px 20px;
  image-rendering: pixelated;
}

/* --- 通用UI组件 --- */
.mc-box {
  background-color: rgba(40, 40, 40, 0.85);
  backdrop-filter: blur(4px);
  border: 2px solid var(--mc-border-black);
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark);
  padding: 8px 12px;
  text-align: center;
  display: inline-block;
  color: var(--mc-text-color);
  text-decoration: none;
  font-family: inherit;
  cursor: pointer;
}
a.mc-box:hover, button.mc-box:hover {
  background-color: rgba(60, 60, 60, 0.9);
  box-shadow: inset 2px 2px 0px #666, inset -2px -2px 0px #222;
}
button.mc-box { font-size: 14px; }

/* --- 页头 Header --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  flex-wrap: wrap;
  gap: 10px;
}
.logo { font-size: 24px; }
.main-nav ul { display: flex; gap: 10px; list-style: none; padding: 0; margin: 0; }
.main-nav a.mc-box { padding: 6px 10px; font-size: 14px; }
.main-nav a.mc-box.active {
  background-color: #585858;
  box-shadow: inset 2px 2px 0px #3a3a3a, inset -2px -2px 0px #a0a0a0;
  color: #ffffa0;
}

.user-actions { display: flex; align-items: center; gap: 10px; }
.username-display {
  font-size: 12px;
  padding: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.username-display:hover {
  background: #4a4a4a;
  transform: translateY(-2px);
}
.login-btn, .logout-btn { padding: 6px 10px; font-size: 14px; }

/* --- 转换器页面样式 --- */
.converter-section { margin-bottom: 24px; }

.section-header {
  width: 100%;
  margin-bottom: 16px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  text-align: left;
  padding: 12px 20px;
  image-rendering: pixelated;
}
.section-header h2 {
  font-size: 24px;
  margin: 0;
  text-shadow: 2px 2px 0 #3a3a3a, -2px -2px 0 #3a3a3a, 2px -2px 0 #3a3a3a, -2px 2px 0 #3a3a3a;
}

.converter-container {
  width: 100%;
  padding: 24px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/assets/images/new-textures/default_wood.png');
  background-size: auto, 64px 64px;
  text-align: left;
  cursor: default;
  image-rendering: pixelated;
}
.converter-container:hover {
  background-color: rgba(40, 40, 40, 0.85);
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark);
}

.step-block {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(255,255,255,0.08);
}
.step-block:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.step-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  color: #fff;
  margin-bottom: 10px;
  text-shadow: 2px 2px 0 #000;
}

.label-icon {
  width: 24px;
  height: 24px;
  image-rendering: pixelated;
}

/* 上传区域 */
.upload-zone {
  padding: 24px;
  border: 3px dashed rgba(255,255,255,0.15);
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
  background-color: rgba(0, 0, 0, 0.4);
  min-height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-zone:hover,
.upload-zone.drag-over {
  border-color: #4CAF50;
  background-color: rgba(0, 0, 0, 0.5);
  box-shadow: inset 2px 2px 0px var(--mc-border-light), inset -2px -2px 0px var(--mc-border-dark);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.upload-icon {
  width: 48px;
  height: 48px;
  image-rendering: pixelated;
  opacity: 0.7;
}
.upload-placeholder p {
  font-size: 14px;
  color: #ccc;
  text-shadow: 1px 1px 0 #000;
}

.upload-files { width: 100%; text-align: left; }

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  margin-bottom: 4px;
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(255,255,255,0.05);
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.file-size {
  color: #aaa;
  font-size: 12px;
  text-shadow: 1px 1px 0 #000;
}

.file-remove {
  background: rgba(255,0,0,0.3);
  border: 1px solid rgba(255,0,0,0.5);
  color: #fff;
  cursor: pointer;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-family: inherit;
}
.file-remove:hover { background: rgba(255,0,0,0.5); }

/* 版本选择 */
.version-info {
  margin-bottom: 10px;
  padding: 8px 12px;
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(76,175,80,0.3);
}

.info-label {
  color: #aaa;
  margin-right: 6px;
  font-size: 13px;
  text-shadow: 1px 1px 0 #000;
}

.info-value {
  color: #4CAF50;
  font-weight: bold;
  font-size: 13px;
}

.version-select-group {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.select-label {
  font-size: 14px;
  white-space: nowrap;
  text-shadow: 1px 1px 0 #000;
}

.mc-select {
  flex: 1;
  max-width: 280px;
  width: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  border: 2px solid #a0a0a0;
  border-top-color: #000;
  border-left-color: #000;
  color: #fff;
  padding: 8px 10px;
  font-family: inherit;
  font-size: 14px;
  outline: none;
  cursor: pointer;
}
.mc-select:focus {
  background-color: rgba(0, 0, 0, 0.7);
  border-bottom-color: #fff;
  border-right-color: #fff;
}
.mc-select option { background-color: #333; }

.conversion-path {
  margin-bottom: 12px;
  padding: 8px 12px;
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(255,255,255,0.05);
}

.path-label {
  color: #aaa;
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  text-shadow: 1px 1px 0 #000;
}

.path-steps {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.path-step {
  padding: 2px 6px;
  background: rgba(76, 175, 80, 0.15);
  border: 1px solid rgba(76, 175, 80, 0.3);
  font-size: 12px;
  color: #8f8;
  text-shadow: 1px 1px 0 #000;
}

.btn-convert {
  width: 100%;
  font-size: 16px;
  padding: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: linear-gradient(to right, #29a9a2, #47d6cd);
  font-family: inherit;
}
.btn-convert:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  width: 24px;
  height: 24px;
  image-rendering: pixelated;
}

/* 进度条 */
.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.progress-bar-bg {
  flex: 1;
  height: 14px;
  padding: 0;
  background: rgba(0,0,0,0.5);
  border: 2px solid var(--mc-border-black);
  box-shadow: inset 2px 2px 0px var(--mc-border-dark);
  overflow: hidden;
  position: relative;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(to right, #29a9a2, #47d6cd);
  transition: width 0.3s;
  box-shadow: inset 0 2px 0 rgba(255,255,255,0.2);
}

.progress-text {
  font-size: 13px;
  font-weight: bold;
  min-width: 36px;
  text-align: right;
  text-shadow: 1px 1px 0 #000;
}

/* 日志 */
.log-container {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  background: rgba(0,0,0,0.6);
  border: 2px solid var(--mc-border-black);
  box-shadow: inset 2px 2px 0px var(--mc-border-dark);
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #0f0;
  text-shadow: 0 0 3px rgba(0,255,0,0.3);
  text-align: left;
  cursor: default;
}
.log-container:hover {
  background-color: rgba(0,0,0,0.6);
  box-shadow: inset 2px 2px 0px var(--mc-border-dark);
}

.log-line {
  white-space: pre-wrap;
  word-break: break-all;
}

/* 下载 */
.download-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.download-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  text-decoration: none;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  background-image: linear-gradient(rgba(0, 80, 150, 0.3), rgba(0, 40, 80, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  image-rendering: pixelated;
}
.download-btn:hover {
  background-color: rgba(60, 60, 60, 0.9);
  box-shadow: inset 2px 2px 0px #666, inset -2px -2px 0px #222;
}

/* 滚动条 */
.log-container::-webkit-scrollbar { width: 6px; }
.log-container::-webkit-scrollbar-track { background: #1a1a1a; }
.log-container::-webkit-scrollbar-thumb { background: #444; }
.log-container::-webkit-scrollbar-thumb:hover { background: #555; }
</style>

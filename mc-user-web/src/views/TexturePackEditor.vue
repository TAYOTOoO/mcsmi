<template>
  <div class="page-wrapper">
    <div class="top-border"></div>
    <div class="content-body">
      <div class="side-border left"></div>
      <main
        @dragover.prevent="handleGlobalDragOver"
        @dragleave.prevent="handleGlobalDragLeave"
        @drop.prevent="handleGlobalDrop"
      >
        <!-- 拖拽上传提示层 -->
        <div v-if="showDropOverlay" class="drop-overlay" @click="cancelDrop">
          <div class="drop-hint-box" @click.stop>
            <div class="drop-icon">📦</div>
            <div class="drop-text">松开鼠标导入材质包</div>
            <div class="drop-subtext">支持 .zip 格式的材质包文件</div>
            <div class="drop-cancel-hint">按 ESC 或点击外部区域取消</div>
          </div>
        </div>

        <!-- 页头 -->
        <header class="page-header">
          <div class="mc-box logo">材质工坊</div>
          <nav class="main-nav">
            <ul>
              <li><router-link to="/" class="mc-box">首页</router-link></li>
              <li><router-link to="/generate" class="mc-box">开始生成</router-link></li>
              <li><router-link to="/my-tasks" class="mc-box">我的任务</router-link></li>
              <li><router-link to="/texture-pack-editor" class="mc-box active">材质编辑</router-link></li>
              <li><router-link to="/recharge" class="mc-box">积分充值</router-link></li>
            </ul>
          </nav>
        </header>

        <!-- 编辑器工具栏 -->
        <div class="editor-toolbar mc-box">
          <div class="toolbar-group">
            <select v-model="selectedVersion" class="mc-select" @change="handleVersionChange">
              <option value="" disabled selected>选择MC版本</option>
              <option v-for="version in versions" :key="version.versionId" :value="version.versionName">
                {{ version.versionName }}
              </option>
            </select>

            <template v-if="selectedVersion">
              <div class="radio-group">
                <button
                  :class="['mc-box', { active: packSource === 'download' }]"
                  @click="packSource = 'download'"
                >下载原版</button>
                <button
                  :class="['mc-box', { active: packSource === 'upload' }]"
                  @click="packSource = 'upload'"
                >上传文件</button>
              </div>

              <template v-if="packSource === 'upload'">
                <el-upload
                  ref="uploadRef"
                  :auto-upload="false"
                  :on-change="handleVanillaPackUpload"
                  :show-file-list="false"
                  accept=".zip"
                  style="display: inline-block;"
                >
                  <button class="mc-box action-btn">选择文件</button>
                </el-upload>
              </template>

              <template v-if="packSource === 'download'">
                <div class="radio-group">
                  <button
                    :class="['mc-box', { active: downloadSource === 'cn' }]"
                    @click="downloadSource = 'cn'"
                  >国内源</button>
                  <button
                    :class="['mc-box', { active: downloadSource === 'global' }]"
                    @click="downloadSource = 'global'"
                  >国外源</button>
                </div>
                <button class="mc-box action-btn" @click="handleDownloadVanillaPack" :disabled="downloading">
                  {{ downloading ? '下载中...' : '下载资源' }}
                </button>
              </template>
            </template>
          </div>

          <div class="toolbar-group right">
            <button class="mc-box action-btn primary" @click="handleExport" :disabled="!canExport">
              <img src="/assets/images/new-textures/default_stick.png" class="icon"/> 导出
            </button>
            <button class="mc-box action-btn" @click="handleBack">
              <img src="/assets/images/new-textures/default_stick.png" class="icon"/> 返回
            </button>
          </div>
        </div>

        <!-- 版权声明 -->
        <div class="copyright-notice mc-box" v-if="vanillaPackLoaded">
          <div class="notice-icon">⚠️</div>
          <div class="notice-content">
            <strong>版权声明：</strong>
            原版材质仅用于预览和参考，所有权归Mojang Studios所有。
            <template v-if="subscriptionInfo.isSubscribed">
              您已是尊敬的钻石订阅用户，拥有生成图片的所有权，可用于商业用途。
            </template>
            <template v-else>
              平台拥有生成图片的所有权，非订阅用户仅限个人使用，禁止商用。
            </template>
          </div>
        </div>

        <!-- 主要内容区 -->
        <div class="editor-main" v-if="vanillaPackLoaded">
          <!-- 左侧：材质树 -->
          <div class="panel-left mc-box-panel">
            <div class="panel-header">
              <h4>材质文件</h4>
              <button class="mc-box mini" @click="expandAll = !expandAll">{{ expandAll ? '-' : '+' }}</button>
            </div>

            <input
              v-model="searchText"
              placeholder="搜索材质..."
              class="mc-input search-input"
            />

            <div class="quick-access">
              <div class="quick-items">
                <span
                  v-for="item in getQuickAccessItems"
                  :key="item.path"
                  class="quick-tag"
                  @click="showCategoryInPreview(item)"
                >{{ item.label }}</span>

                <!-- PVP常用物品按钮 -->
                <span
                  class="quick-tag pvp-tag"
                  @click="showPvpItemsInPreview"
                >PVP常用物品</span>
              </div>
            </div>

            <div class="tree-container custom-scrollbar">
              <el-tree
                ref="treeRef"
                :data="textureTree"
                :props="treeProps"
                :filter-node-method="filterNode"
                :default-expanded-keys="expandAll ? allNodeKeys : defaultExpandedKeys"
                node-key="path"
                highlight-current
                @node-click="handleNodeClick"
                class="pixel-tree"
              >
                <template #default="{ node, data }">
                  <div
                    class="tree-node-content"
                    :class="{ 'is-replaced': data.replaced }"
                    @drop.prevent="handleFileDrop($event, data)"
                    @dragover.prevent
                    @dragenter.prevent="handleDragEnter($event)"
                    @dragleave.prevent="handleDragLeave($event)"
                  >
                    <img
                      :src="data.isFile ? '/assets/minecraft/textures/item/paper.png' : '/assets/minecraft/textures/item/chest_minecart.png'"
                      class="node-icon"
                      @error="e => e.target.src = data.isFile ? 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=' : ''"
                    />
                    <span class="node-text">{{ getChineseName(node.label, data.path) }}</span>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>

          <!-- 中间：预览区 -->
          <div class="panel-center mc-box-panel">
            <div class="panel-header">
              <h4>{{ previewTitle }}</h4>
            </div>

            <!-- 快捷素材网格显示 -->
            <div
              v-if="quickPreviewItems.length > 0"
              class="quick-preview-grid custom-scrollbar"
              @drop.prevent="handleGridDrop"
              @dragover.prevent
              @dragenter.prevent="handleGridDragEnter"
              @dragleave.prevent="handleGridDragLeave"
            >
              <div
                v-for="item in quickPreviewItems"
                :key="item.path"
                class="preview-grid-item"
                :class="{ 'is-replaced': item.replaced }"
                :data-path="item.path"
                @click="selectedNode = item"
              >
                <div class="item-thumb">
                  <img v-if="replacedPreviews[item.path] || item.preview"
                       :src="replacedPreviews[item.path] || item.preview" />
                </div>
                <div class="item-name">{{ getChineseName(item.label, item.path) }}</div>
              </div>
            </div>

            <!-- 文件预览 -->
            <div v-else-if="selectedNode && selectedNode.isFile" class="preview-content">
              <div
                class="preview-viewport"
                @drop.prevent="handleFileDrop($event, selectedNode)"
                @dragover.prevent
                @dragenter.prevent="handlePreviewDragEnter($event)"
                @dragleave.prevent="handlePreviewDragLeave($event)"
              >
                <img :src="replacedPreviews[selectedNode.path] || selectedNode.preview" class="preview-img" />
              </div>

              <div class="file-info">
                <div class="info-row">
                  <span class="label">名称:</span>
                  <span class="value">{{ getChineseName(selectedNode.label, selectedNode.path) }}</span>
                </div>
                <div class="info-row">
                  <span class="label">状态:</span>
                  <span :class="['status-tag', selectedNode.replaced ? 'replaced' : 'original']">
                    {{ selectedNode.replaced ? '已替换' : '原版' }}
                  </span>
                </div>
              </div>

              <div class="preview-actions">
                <button class="mc-box full-width" @click="replaceFromCropped" :disabled="croppedImages.length === 0">
                  使用选中材质替换
                </button>
              </div>
            </div>

            <!-- 文件夹网格 -->
            <div v-else-if="selectedNode && !selectedNode.isFile" class="folder-grid-container custom-scrollbar">
              <div
                class="folder-grid"
                @drop.prevent="handleGridDrop"
                @dragover.prevent
                @dragenter.prevent="handleGridDragEnter"
                @dragleave.prevent="handleGridDragLeave"
              >
                <div
                  v-for="fileNode in getFolderFiles(selectedNode)"
                  :key="fileNode.path"
                  class="grid-item"
                  :class="{ 'is-replaced': fileNode.replaced }"
                  :data-path="fileNode.path"
                  @click="selectFileFromFolder(fileNode)"
                >
                  <div class="item-thumb">
                    <img v-if="replacedPreviews[fileNode.path] || fileNode.preview"
                         :src="replacedPreviews[fileNode.path] || fileNode.preview" />
                  </div>
                  <div class="item-name">{{ getChineseName(fileNode.label, fileNode.path) }}</div>
                </div>
              </div>
            </div>

            <div v-else class="empty-preview">
              <img src="/assets/images/new-textures/default_grass.png" class="empty-icon" />
              <p>请点击左侧快捷按钮或选择文件</p>
            </div>
          </div>

          <!-- 右侧：素材库 -->
          <div class="panel-right mc-box-panel">
            <div class="panel-header">
              <h4>素材库 ({{ croppedImages.length }})</h4>
            </div>

            <!-- 素材列表 -->
            <div class="palette-grid custom-scrollbar">
              <div
                v-for="(image, index) in croppedImages"
                :key="index"
                class="palette-item"
                :class="{ 'selected': selectedCroppedIndex === index, 'used': image.used }"
                draggable="true"
                @dragstart="handleCroppedDragStart($event, image, index)"
                @dragend="handleCroppedDragEnd"
                @click="handlePaletteItemClick(index)"
              >
                <img :src="image.url" />
                <div class="item-idx">{{ index + 1 }}</div>
                <button class="delete-btn" @click.stop="handleDeleteCropped(index)">x</button>
              </div>
            </div>

            <!-- 功能按钮区域 -->
            <div class="palette-actions">
              <button
                class="mc-box action-toggle-btn"
                :class="{ active: bladeRotateEnabled }"
                @click="bladeRotateEnabled = !bladeRotateEnabled"
              >
                {{ bladeRotateEnabled ? '🔄 剑刃转向已开启' : '🔄 剑刃转向已关闭' }}
              </button>
              <button
                class="mc-box action-toggle-btn"
                :class="{ active: rotateClockwiseEnabled }"
                @click="rotateClockwiseEnabled = !rotateClockwiseEnabled"
              >
                {{ rotateClockwiseEnabled ? '↻ 顺时针旋转已开启' : '↻ 顺时针旋转已关闭' }}
              </button>
              <button
                class="mc-box action-toggle-btn"
                :class="{ active: flipVerticalEnabled }"
                @click="flipVerticalEnabled = !flipVerticalEnabled"
              >
                {{ flipVerticalEnabled ? '↕ 垂直翻转已开启' : '↕ 垂直翻转已关闭' }}
              </button>
              <button
                class="mc-box action-toggle-btn"
                :class="{ active: flipHorizontalEnabled }"
                @click="flipHorizontalEnabled = !flipHorizontalEnabled"
              >
                {{ flipHorizontalEnabled ? '↔ 水平翻转已开启' : '↔ 水平翻转已关闭' }}
              </button>
            </div>
          </div>
        </div>

        <div class="empty-state-panel mc-box-panel" v-else>
          <img src="/assets/images/new-textures/farming_bread.png" class="state-icon" />
          <p>请先在上方工具栏选择版本并加载资源</p>
        </div>

        <!-- 底部信息 -->
        <div class="pack-info mc-box-panel" v-if="vanillaPackLoaded">
          <h3>材质包设置</h3>
          <div class="info-form">
            <div class="form-row">
              <label>名称</label>
              <input v-model="packInfo.name" class="mc-input" />
            </div>
            <div class="form-row">
              <label>描述</label>
              <input v-model="packInfo.description" class="mc-input" />
            </div>
            <div class="form-row">
              <label>封面</label>
              <div class="icon-selector">
                <div class="icon-source-tabs">
                  <button
                    class="mc-box tab-btn"
                    :class="{ active: packInfo.iconSource === 'select' }"
                    @click="packInfo.iconSource = 'select'"
                  >
                    选择素材
                  </button>
                  <button
                    class="mc-box tab-btn"
                    :class="{ active: packInfo.iconSource === 'upload' }"
                    @click="packInfo.iconSource = 'upload'"
                  >
                    上传封面
                  </button>
                </div>

                <!-- 选择素材模式 -->
                <div v-if="packInfo.iconSource === 'select'" class="icon-select-mode">
                  <select v-model="packInfo.selectedIconIndex" class="mc-select">
                    <option v-for="(img, idx) in croppedImages" :key="idx" :value="idx">素材 {{ idx + 1 }}</option>
                  </select>
                </div>

                <!-- 上传封面模式 -->
                <div v-if="packInfo.iconSource === 'upload'" class="icon-upload-mode">
                  <input
                    type="file"
                    ref="iconFileInput"
                    accept="image/png,image/jpeg,image/jpg"
                    @change="handleIconUpload"
                    style="display: none;"
                  />
                  <button class="mc-box upload-btn" @click="$refs.iconFileInput.click()">
                    选择图片
                  </button>
                  <span class="upload-hint">建议尺寸: 128×128 或更大</span>
                </div>

                <!-- 预览 -->
                <div class="preview-icon-box" v-if="packInfo.iconPreview">
                  <img :src="packInfo.iconPreview" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <footer class="page-footer">
            © 2026 材质工坊
        </footer>
      </main>
      <div class="side-border right"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import JSZip from 'jszip'
import { getBatchInfo } from '@/api/croppedTemp'
import { getVersionList } from '@/api/version'
import { downloadClientJar } from '@/api/minecraft'
import { getMySubscription } from '@/api/subscription'
import { listGeneration, getGeneration } from '@/api/generation'
import { useVipGate } from '@/composables/useVipGate'

const route = useRoute()
const router = useRouter()

// 状态变量
const loading = ref(false)
const croppedImages = ref([])
const versions = ref([])
const selectedVersion = ref('')
const packSource = ref('download')
const downloadSource = ref('cn')
const downloading = ref(false)
const vanillaPackLoaded = ref(false)
const searchText = ref('')
const textureTree = ref([])
const vanillaPack = ref(null)
const replacedTextures = ref({})
const replacedPreviews = ref({})
const expandAll = ref(false)

// 订阅信息
const subscriptionInfo = reactive({
  isSubscribed: false,
  subscriptionType: 'normal'
})
const selectedNode = ref(null)
const selectedCroppedIndex = ref(null)
const packSource_origin = ref('')
const draggingItem = ref(null)
const showTreeView = ref(false) // 默认显示PVP网格，false=网格视图，true=树形视图
const quickPreviewItems = ref([]) // 快捷预览区域显示的素材列表
const showDropOverlay = ref(false) // 拖拽上传提示层
const dragCounter = ref(0) // 拖拽计数器，用于处理嵌套元素的dragenter/dragleave
const bladeRotateEnabled = ref(false) // 剑刃转向功能开关
const rotateClockwiseEnabled = ref(false) // 顺时针旋转90°功能开关
const flipVerticalEnabled = ref(false) // 垂直翻转功能开关
const flipHorizontalEnabled = ref(false) // 水平翻转功能开关

// VIP: 功能权限
const { isFeatureAvailable } = useVipGate()

const previewTitle = computed(() => {
  if (quickPreviewItems.value.length > 0) {
    return `快捷素材 (${quickPreviewItems.value.length})`
  }
  if (selectedNode.value) {
    return selectedNode.value.isFile ? '预览' : '文件夹'
  }
  return '预览区域'
})

const treeProps = { children: 'children', label: 'label' }
const treeRef = ref(null)
const defaultExpandedKeys = ref([])
const allNodeKeys = ref([])
const iconFileInput = ref(null)

const packInfo = reactive({
  name: '我的材质包',
  description: '',
  iconSource: 'select',
  selectedIconIndex: 0,
  iconPreview: null,
  customIcon: null
})

// 常用PVP物品（50个）
// 注意：file字段使用新版本命名（1.13+），oldFile字段用于旧版本（1.12-）
// minVersion: 物品加入的最低版本（格式：major.minor）
const pvpQuickItems = [
  // 第一行：镐
  { name: '木镐', file: 'wooden_pickaxe.png', oldFile: 'wood_pickaxe.png' },
  { name: '石镐', file: 'stone_pickaxe.png' },
  { name: '铁镐', file: 'iron_pickaxe.png' },
  { name: '金镐', file: 'golden_pickaxe.png', oldFile: 'gold_pickaxe.png' },
  { name: '钻石镐', file: 'diamond_pickaxe.png' },

  // 第二行：下界镐+斧
  { name: '下界镐', file: 'netherite_pickaxe.png', minVersion: '1.16' },
  { name: '木斧', file: 'wooden_axe.png', oldFile: 'wood_axe.png' },
  { name: '石斧', file: 'stone_axe.png' },
  { name: '铁斧', file: 'iron_axe.png' },
  { name: '钻石斧', file: 'diamond_axe.png' },

  // 第三行：下界斧+锹
  { name: '下界斧', file: 'netherite_axe.png', minVersion: '1.16' },
  { name: '木锹', file: 'wooden_shovel.png', oldFile: 'wood_shovel.png' },
  { name: '石锹', file: 'stone_shovel.png' },
  { name: '铁锹', file: 'iron_shovel.png' },
  { name: '钻石锹', file: 'diamond_shovel.png' },

  // 第四行：下界锹+剪刀+剑
  { name: '下界锹', file: 'netherite_shovel.png', minVersion: '1.16' },
  { name: '剪刀', file: 'shears.png' },
  { name: '木剑', file: 'wooden_sword.png', oldFile: 'wood_sword.png' },
  { name: '石剑', file: 'stone_sword.png' },
  { name: '铁剑', file: 'iron_sword.png' },

  // 第五行：剑+弓箭+钓鱼竿
  { name: '钻石剑', file: 'diamond_sword.png' },
  { name: '下界剑', file: 'netherite_sword.png', minVersion: '1.16' },
  { name: '弓', file: 'bow.png', oldFile: 'bow_standby.png' },
  { name: '箭', file: 'arrow.png' },
  { name: '钓鱼竿', file: 'fishing_rod.png', oldFile: 'fishing_rod_uncast.png' },

  // 第六行：投掷物
  { name: '雪球', file: 'snowball.png' },
  { name: '鸡蛋', file: 'egg.png' },
  { name: '火药', file: 'gunpowder.png', oldFile: 'sulphur.png' },
  { name: '烈焰弹', file: 'blaze_powder.png' },
  { name: '烈焰棒', file: 'blaze_rod.png' },

  // 第七行：食物和药水
  { name: '苹果', file: 'apple.png' },
  { name: '金苹果', file: 'golden_apple.png', oldFile: 'apple_golden.png' },
  { name: '蘑菇煲', file: 'mushroom_stew.png' },
  { name: '牛奶桶', file: 'milk_bucket.png' },
  { name: '药水', file: 'potion.png', oldFile: 'potion_bottle_drinkable.png' },

  // 第八行：特殊道具
  { name: '末影珍珠', file: 'ender_pearl.png' },
  { name: '不死图腾', file: 'totem_of_undying.png', minVersion: '1.11' },
  { name: '水桶', file: 'water_bucket.png' },
  { name: '岩浆桶', file: 'lava_bucket.png' },
  { name: '空桶', file: 'bucket.png' },

  // 第九行：PVP道具
  { name: '末地之眼', file: 'ender_eye.png' },
  { name: '床', file: 'white_bed.png', oldFile: 'bed.png', isBlock: false, oldIsBlock: false },
  { name: '盾牌', file: 'shield.png', minVersion: '1.9' },
  { name: '重生锚', file: 'respawn_anchor.png', isBlock: true, minVersion: '1.16' },
  { name: '粘液球', file: 'slime_ball.png' },

  // 第十行：材料
  { name: '铁锭', file: 'iron_ingot.png' },
  { name: '金锭', file: 'gold_ingot.png' },
  { name: '钻石', file: 'diamond.png' },
  { name: '绿宝石', file: 'emerald.png' },
  { name: '下界之星', file: 'nether_star.png' }
]

// 旧版快捷访问（保留用于其他功能）
const quickAccessItems = [
  { label: '方块', path: 'assets/minecraft/textures/block' },
  { label: '物品', path: 'assets/minecraft/textures/item' },
  { label: '实体', path: 'assets/minecraft/textures/entity' },
  { label: '界面', path: 'assets/minecraft/textures/gui' },
  { label: '粒子', path: 'assets/minecraft/textures/particle' }
]

// 获取适配版本的快捷访问路径
const getQuickAccessItems = computed(() => {
  const blockFolder = getTextureFolder(true)
  const itemFolder = getTextureFolder(false)

  return [
    { label: '方块', path: `assets/minecraft/textures/${blockFolder}` },
    { label: '物品', path: `assets/minecraft/textures/${itemFolder}` },
    { label: '实体', path: 'assets/minecraft/textures/entity' },
    { label: '界面', path: 'assets/minecraft/textures/gui' },
    { label: '粒子', path: 'assets/minecraft/textures/particle' }
  ]
})

const chineseNameMap = {
  'block': '方块', 'item': '物品', 'entity': '实体', 'gui': '界面', 'environment': '环境',
  'particle': '粒子', 'painting': '画', 'mob_effect': '效果', 'models': '模型',
  'stone': '石头', 'dirt': '泥土', 'grass_block': '草方块', 'oak_planks': '橡木板',
  'diamond_sword': '钻石剑', 'diamond_pickaxe': '钻石镐', 'diamond': '钻石', 'gold_ingot': '金锭'
}

const canExport = computed(() => vanillaPackLoaded.value && Object.keys(replacedTextures.value).length > 0)

// 判断版本是否使用新路径（1.13+）
const isNewVersion = computed(() => {
  if (!selectedVersion.value) return true

  try {
    const parts = selectedVersion.value.split('.')
    const major = parseInt(parts[0])
    const minor = parseInt(parts[1])

    // 1.13及以上版本使用 item/block (单数)
    // 1.12及以下版本使用 items/blocks (复数)
    if (major > 1) return true
    if (major === 1 && minor >= 13) return true
    return false
  } catch (e) {
    // 解析失败时默认使用新版本路径
    console.warn('版本号解析失败:', selectedVersion.value)
    return true
  }
})

// 获取物品/方块文件夹名称
const getTextureFolder = (isBlock = false) => {
  if (isNewVersion.value) {
    return isBlock ? 'block' : 'item'
  } else {
    return isBlock ? 'blocks' : 'items'
  }
}

watch(searchText, (val) => treeRef.value?.filter(val))

const loadVersions = async () => {
  try {
    const res = await getVersionList()
    versions.value = res.data || []
  } catch (e) { ElMessage.error('加载版本失败') }
}

const loadCroppedImages = async () => {
  loading.value = true
  try {
    const source = route.query.source
    const batchId = route.query.batchId
    if (source === 'local') {
      const data = JSON.parse(sessionStorage.getItem('croppedImages'))
      croppedImages.value = data.images.map((b64, idx) => ({ url: b64, index: idx, used: false }))
    } else if (batchId) {
      const res = await getBatchInfo(batchId)
      // 使用 fetch 获取图片并转换为 blob URL（携带 token）
      const token = localStorage.getItem('token')
      const imagePromises = res.data.imageUrls.map(async (url, idx) => {
        try {
          const response = await fetch(`/api${url}`, {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          })
          if (response.ok) {
            const blob = await response.blob()
            return { url: URL.createObjectURL(blob), index: idx, used: false }
          } else {
            console.error(`加载图片失败: ${url}`)
            return null
          }
        } catch (error) {
          console.error(`加载图片失败: ${url}`, error)
          return null
        }
      })
      const images = await Promise.all(imagePromises)
      croppedImages.value = images.filter(img => img !== null)
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const loadSubscriptionInfo = async () => {
  try {
    const res = await getMySubscription()
    if (res.code === 200 && res.data) {
      subscriptionInfo.isSubscribed = res.data.subscriptionType === 'diamond'
      subscriptionInfo.subscriptionType = res.data.subscriptionType || 'normal'
    }
  } catch (error) {
    console.error('加载订阅信息失败', error)
  }
}

const handleVersionChange = () => { vanillaPackLoaded.value = false; textureTree.value = [] }

const handleVanillaPackUpload = async (file) => {
  try {
    loading.value = true
    const zip = await JSZip.loadAsync(await file.raw.arrayBuffer())
    vanillaPack.value = zip
    packSource_origin.value = 'uploaded'
    await buildTextureTree(zip)
    vanillaPackLoaded.value = true
  } catch (e) { ElMessage.error('加载失败: ' + e.message) } finally { loading.value = false }
}

const handleDownloadVanillaPack = async () => {
  if (!selectedVersion.value) return ElMessage.warning('请选择版本')
  try {
    downloading.value = true
    const blob = await downloadClientJar(selectedVersion.value, downloadSource.value === 'global' ? 'official' : 'bmclapi')
    const zip = await JSZip.loadAsync(blob)
    const assets = Object.keys(zip.files).filter(p => p.startsWith('assets/'))
    if (!assets.length) throw new Error('无资源文件')
    
    vanillaPack.value = zip
    packSource_origin.value = 'downloaded'
    await buildTextureTree(zip)
    vanillaPackLoaded.value = true
    
    // UI材质自动替换widgets.png
    console.log('=== UI材质替换调试 ===')
    console.log('materialType:', route.query.materialType)
    const materialType = route.query.materialType
    if (materialType == 2) {
      console.log('检测到UI材质，开始替换')
      try {
        const croppedData = sessionStorage.getItem('croppedImages')
        console.log('sessionStorage数据:', croppedData ? '存在' : '不存在')
        if (croppedData) {
          const data = JSON.parse(croppedData)
          console.log('解析后的数据:', data)
          const images = data.images
          console.log('图片数组:', images)
          console.log('图片数量:', images?.length)
          if (images && images.length > 0) {
            const base64Image = images[0]
            console.log('base64图片:', base64Image ? '存在' : '不存在')
            if (base64Image) {
              // 将base64转换为blob
              const response = await fetch(base64Image)
              const blob = await response.blob()
              console.log('blob大小:', blob.size)
              
              // 查找并替换widgets.png
              const widgetsPath = 'assets/minecraft/textures/gui/widgets.png'
              console.log('检查widgets.png是否存在:', zip.files[widgetsPath] ? '存在' : '不存在')
              if (zip.files[widgetsPath]) {
                zip.file(widgetsPath, blob)
                replacedTextures.value[widgetsPath] = blob
                console.log('替换成功！')
                
                // 重新构建材质树以显示替换后的效果
                await buildTextureTree(zip)
                
                ElMessage.success('已自动替换widgets.png')
              } else {
                console.log('当前版本不包含widgets.png')
                ElMessage.warning('当前版本不包含widgets.png')
              }
            }
          }
        }
      } catch (e) {
        console.error('替换widgets.png失败:', e)
        ElMessage.error('替换失败: ' + e.message)
      }
    } else {
      console.log('不是UI材质，跳过替换')
    }
    
    ElMessage.success('加载成功')
  } catch (e) { ElMessage.error('下载失败: ' + e.message) } finally { downloading.value = false }
}

const buildTextureTree = async (zip) => {
  const tree = []; const nodeKeys = []
  zip.forEach((path, file) => {
    if (file.dir || !path.startsWith('assets/') || !path.endsWith('.png')) return
    const parts = path.split('/'); let level = tree
    parts.forEach((part, i) => {
      const isFile = i === parts.length - 1
      const curPath = parts.slice(0, i + 1).join('/')
      if (isFile) {
        const node = { label: part, path, isFile: true, file, preview: null, replaced: false }
        file.async('blob').then(b => node.preview = URL.createObjectURL(b))
        level.push(node); nodeKeys.push(path)
      } else {
        let folder = level.find(item => item.label === part && !item.isFile)
        if (!folder) { folder = { label: part, path: curPath, isFile: false, children: [] }; level.push(folder); nodeKeys.push(curPath) }
        level = folder.children
      }
    })
  })
  textureTree.value = tree; allNodeKeys.value = nodeKeys
  if (tree.length) defaultExpandedKeys.value = [tree[0].path]
}

const getChineseName = (name, path) => {
  const pure = name.replace('.png', '')

  // 优先使用displayName（用于PVP物品的自定义中文名）
  const node = quickPreviewItems.value.find(item => item.path === path)
  if (node?.displayName) {
    return node.displayName
  }

  return chineseNameMap[pure] || pure.replace(/_/g, ' ')
}

const filterNode = (val, data) => !val || data.label.includes(val)

const navigateToPath = (path) => {
  if (treeRef.value) {
    treeRef.value.setCurrentKey(path)
    const parts = path.split('/')
    const keys = []
    for(let i=1; i<=parts.length; i++) keys.push(parts.slice(0, i).join('/'))
    defaultExpandedKeys.value = keys
  }
}

// 在中间预览区显示分类素材（方块、物品等）
const showCategoryInPreview = (category) => {
  if (!vanillaPackLoaded.value) {
    ElMessage.warning('请先加载原版资源包')
    return
  }

  // 查找对应路径的文件夹节点
  const findNode = (tree, path) => {
    for (const node of tree) {
      if (node.path === path) return node
      if (node.children) {
        const found = findNode(node.children, path)
        if (found) return found
      }
    }
    return null
  }

  const folderNode = findNode(textureTree.value, category.path)

  if (folderNode) {
    // 获取该文件夹下所有文件
    const files = getFolderFiles(folderNode)
    quickPreviewItems.value = files
    selectedNode.value = null
    ElMessage.success(`已显示 ${category.label} (${files.length}个素材)`)
  } else {
    ElMessage.warning(`未找到分类：${category.label}`)
  }
}

// 在中间预览区显示所有PVP常用物品
const showPvpItemsInPreview = () => {
  if (!vanillaPackLoaded.value) {
    ElMessage.warning('请先加载原版资源包')
    return
  }

  // 查找所有PVP物品对应的节点
  const findNode = (tree, path) => {
    for (const node of tree) {
      if (node.path === path) return node
      if (node.children) {
        const found = findNode(node.children, path)
        if (found) return found
      }
    }
    return null
  }

  // 检查当前版本是否支持某个物品
  const isItemSupported = (item) => {
    if (!item.minVersion) return true
    if (!selectedVersion.value) return true

    try {
      const [currentMajor, currentMinor] = selectedVersion.value.split('.').map(v => parseInt(v))
      const [minMajor, minMinor] = item.minVersion.split('.').map(v => parseInt(v))

      if (currentMajor > minMajor) return true
      if (currentMajor === minMajor && currentMinor >= minMinor) return true
      return false
    } catch (e) {
      console.warn('版本号解析失败:', selectedVersion.value, item.minVersion)
      return true
    }
  }

  const pvpNodes = []

  for (const item of pvpQuickItems) {
    // 检查版本兼容性
    if (!isItemSupported(item)) {
      continue
    }

    // 根据版本判断是否为方块
    let isBlock = item.isBlock
    if (!isNewVersion.value && item.oldIsBlock !== undefined) {
      isBlock = item.oldIsBlock
    }

    // 根据版本动态获取正确的文件夹名称
    const folder = getTextureFolder(isBlock)

    // 根据版本选择正确的文件名
    // 1.13+使用新命名（wooden_*, golden_*），1.12-使用旧命名（wood_*, gold_*）
    let fileName = item.file
    if (!isNewVersion.value && item.oldFile) {
      fileName = item.oldFile
    }

    const fullPath = `assets/minecraft/textures/${folder}/${fileName}`
    const targetNode = findNode(textureTree.value, fullPath)

    if (targetNode) {
      // 添加中文名称
      pvpNodes.push({
        ...targetNode,
        displayName: item.name
      })
    }
  }

  if (pvpNodes.length > 0) {
    quickPreviewItems.value = pvpNodes
    selectedNode.value = null
    ElMessage.success(`已显示PVP常用物品 (${pvpNodes.length}个)`)
  } else {
    ElMessage.warning('未找到PVP常用物品')
  }
}

// 在中间预览区显示单个PVP物品（已废弃）
const showPvpItemInPreview = (item) => {
  if (!vanillaPackLoaded.value) {
    ElMessage.warning('请先加载原版资源包')
    return
  }

  // 根据版本动态获取正确的文件夹名称
  const folder = getTextureFolder(item.isBlock)
  const fullPath = `assets/minecraft/textures/${folder}/${item.file}`

  // 在树中查找对应节点
  const findNode = (tree, path) => {
    for (const node of tree) {
      if (node.path === path) return node
      if (node.children) {
        const found = findNode(node.children, path)
        if (found) return found
      }
    }
    return null
  }

  const targetNode = findNode(textureTree.value, fullPath)

  if (targetNode) {
    // 直接在中间预览区显示该物品
    quickPreviewItems.value = [targetNode]
    selectedNode.value = targetNode
    ElMessage.success(`已显示：${item.name}`)
  } else {
    ElMessage.warning(`未找到材质文件：${item.name}`)
  }
}

// 点击PVP快捷物品（旧版，已废弃）
const handlePvpItemClick = (item) => {
  showPvpItemInPreview(item)
}

const handleNodeClick = (data) => {
  selectedNode.value = data
  quickPreviewItems.value = [] // 清空快捷预览
}

const getFolderFiles = (folder) => {
  if (!folder?.children) return []
  const files = []
  const traverse = (node) => {
    if (node.isFile) files.push({...node})
    else node.children?.forEach(traverse)
  }
  folder.children.forEach(traverse)
  return files.sort((a,b) => a.label.localeCompare(b.label))
}

const selectFileFromFolder = (node) => selectedNode.value = node
const selectCroppedImage = (idx) => selectedCroppedIndex.value = idx

const replaceFromCropped = () => {
  if (selectedCroppedIndex.value === null) return ElMessage.warning('请先选择右侧素材')
  handleFileDrop({}, selectedNode.value, croppedImages.value[selectedCroppedIndex.value], selectedCroppedIndex.value)
}

const handleFileDrop = async (e, node, img = null, idx = null) => {
  if (!node.isFile) return

  // 清除所有拖拽高亮
  if (currentDragOverElement) {
    currentDragOverElement.classList.remove('drag-over')
    currentDragOverElement = null
  }

  try {
    let blob, url
    if (!img && draggingItem.value) { img = draggingItem.value.image; idx = draggingItem.value.index }

    if (img) {
      blob = await fetch(img.url).then(r => r.blob()); url = img.url
      croppedImages.value[idx].used = true
    } else if (e.dataTransfer?.files?.length) {
      blob = e.dataTransfer.files[0]; url = URL.createObjectURL(blob)
    } else return

    replacedTextures.value[node.path] = blob
    replacedPreviews.value[node.path] = url
    node.replaced = true
    if (selectedNode.value?.path === node.path) selectedNode.value = { ...node }
    ElMessage.success('已替换')
  } catch (err) { ElMessage.error('替换失败') }
}

// 优化拖拽高亮：使用全局状态管理，确保同一时间只有一个元素高亮
let currentDragOverElement = null

// 事件委托：网格容器的拖拽处理（优化大量元素性能）
const handleGridDragEnter = (e) => {
  e.stopPropagation()

  // 找到最近的网格项
  const gridItem = e.target.closest('.grid-item, .preview-grid-item')
  if (!gridItem) return

  // 如果已经有其他元素高亮，先清除
  if (currentDragOverElement && currentDragOverElement !== gridItem) {
    currentDragOverElement.classList.remove('drag-over')
  }

  // 高亮当前元素
  if (!gridItem.classList.contains('drag-over')) {
    gridItem.classList.add('drag-over')
  }

  currentDragOverElement = gridItem
}

const handleGridDragLeave = (e) => {
  e.stopPropagation()

  const gridItem = e.target.closest('.grid-item, .preview-grid-item')
  if (!gridItem) return

  // 使用 relatedTarget 判断是否真正离开
  const relatedTarget = e.relatedTarget
  if (relatedTarget && gridItem.contains(relatedTarget)) {
    return
  }

  // 移除高亮
  gridItem.classList.remove('drag-over')
  if (currentDragOverElement === gridItem) {
    currentDragOverElement = null
  }
}

const handleGridDrop = async (e) => {
  e.stopPropagation()

  // 清除高亮
  if (currentDragOverElement) {
    currentDragOverElement.classList.remove('drag-over')
    currentDragOverElement = null
  }

  // 找到目标网格项
  const gridItem = e.target.closest('.grid-item, .preview-grid-item')
  if (!gridItem) return

  // 从 data-path 获取路径
  const path = gridItem.dataset.path
  if (!path) return

  // 查找对应的节点数据
  const node = findNodeByPath(path)
  if (!node || !node.isFile) return

  // 调用原有的文件替换逻辑
  await handleFileDrop(e, node)
}

// 辅助函数：根据路径查找节点
const findNodeByPath = (path) => {
  // 先在快捷预览列表中查找
  for (const item of quickPreviewItems.value) {
    if (item.path === path) return item
  }

  // 在树形结构中查找
  const findInTree = (nodes) => {
    for (const node of nodes) {
      if (node.path === path) return node
      if (node.children) {
        const found = findInTree(node.children)
        if (found) return found
      }
    }
    return null
  }

  return findInTree(textureTree.value)
}

const handleDragEnter = (e) => {
  e.stopPropagation()
  const target = e.currentTarget

  // 如果已经有其他元素高亮，先清除
  if (currentDragOverElement && currentDragOverElement !== target) {
    currentDragOverElement.classList.remove('drag-over')
  }

  // 高亮当前元素
  if (!target.classList.contains('drag-over')) {
    target.classList.add('drag-over')
  }

  currentDragOverElement = target
}

const handleDragLeave = (e) => {
  e.stopPropagation()
  const target = e.currentTarget

  // 使用 relatedTarget 判断是否真正离开了元素
  // 如果进入的是子元素，不移除高亮
  const relatedTarget = e.relatedTarget
  if (relatedTarget && target.contains(relatedTarget)) {
    return
  }

  // 移除高亮
  target.classList.remove('drag-over')
  if (currentDragOverElement === target) {
    currentDragOverElement = null
  }
}

const handlePreviewDragEnter = (e) => {
  e.stopPropagation()
  const target = e.currentTarget

  // 清除其他元素的高亮
  if (currentDragOverElement && currentDragOverElement !== target) {
    currentDragOverElement.classList.remove('drag-over')
  }

  target.classList.add('drag-over')
  currentDragOverElement = target
}

const handlePreviewDragLeave = (e) => {
  e.stopPropagation()
  const target = e.currentTarget

  // 使用 relatedTarget 判断
  const relatedTarget = e.relatedTarget
  if (relatedTarget && target.contains(relatedTarget)) {
    return
  }

  target.classList.remove('drag-over')
  if (currentDragOverElement === target) {
    currentDragOverElement = null
  }
}

const handleCroppedDragStart = (e, img, idx) => {
  draggingItem.value = {image: img, index: idx}
  e.dataTransfer.effectAllowed = 'copy'
  e.dataTransfer.setData('text/plain', 'texture')

  // 设置拖拽样式
  e.currentTarget.style.opacity = '0.5'
  e.currentTarget.style.transform = 'scale(0.9)'
}

const handleCroppedDragEnd = () => {
  // 恢复素材库拖拽样式
  document.querySelectorAll('.palette-item').forEach(el => {
    el.style.opacity = ''
    el.style.transform = ''
  })
  draggingItem.value = null

  // 清除所有拖拽高亮
  if (currentDragOverElement) {
    currentDragOverElement.classList.remove('drag-over')
    currentDragOverElement = null
  }
}
const handleDeleteCropped = (idx) => croppedImages.value.splice(idx, 1)

// 素材库点击处理
const handlePaletteItemClick = (index) => {
  if (bladeRotateEnabled.value) {
    // 如果剑刃转向功能开启，执行转向
    handleBladeRotate(index)
  } else if (rotateClockwiseEnabled.value) {
    // 如果顺时针旋转功能开启，执行旋转
    handleRotateClockwise(index)
  } else if (flipVerticalEnabled.value) {
    // 如果垂直翻转功能开启，执行翻转
    handleFlipVertical(index)
  } else if (flipHorizontalEnabled.value) {
    // 如果水平翻转功能开启，执行翻转
    handleFlipHorizontal(index)
  } else {
    // 否则执行原有的选中功能
    selectCroppedImage(index)
  }
}

// 剑刃转向：水平翻转 + 逆时针旋转90度
const handleBladeRotate = async (index) => {
  if (!croppedImages.value[index]) {
    return
  }

  try {
    const image = croppedImages.value[index]
    const img = new Image()
    img.crossOrigin = 'anonymous'

    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = image.url
    })

    // 创建canvas进行图片处理
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    // 逆时针旋转90度后，宽高互换
    canvas.width = img.height
    canvas.height = img.width

    // 先平移到中心点
    ctx.translate(canvas.width / 2, canvas.height / 2)

    // 水平翻转
    ctx.scale(-1, 1)

    // 逆时针旋转90度（-90度）
    ctx.rotate(-Math.PI / 2)

    // 绘制图片（需要调整位置使图片居中）
    ctx.drawImage(img, -img.width / 2, -img.height / 2)

    // 转换为blob并创建新的URL
    canvas.toBlob((blob) => {
      const newUrl = URL.createObjectURL(blob)
      // 释放旧的URL
      if (image.url.startsWith('blob:')) {
        URL.revokeObjectURL(image.url)
      }
      // 更新图片
      croppedImages.value[index] = {
        ...image,
        url: newUrl
      }
      ElMessage.success('剑刃转向完成')
    }, 'image/png')

  } catch (error) {
    console.error('剑刃转向失败:', error)
    ElMessage.error('剑刃转向失败')
  }
}

// 顺时针旋转90度
const handleRotateClockwise = async (index) => {
  if (!croppedImages.value[index]) {
    return
  }

  try {
    const image = croppedImages.value[index]
    const img = new Image()
    img.crossOrigin = 'anonymous'

    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = image.url
    })

    // 创建canvas进行图片处理
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    // 顺时针旋转90度后，宽高互换
    canvas.width = img.height
    canvas.height = img.width

    // 平移到中心点
    ctx.translate(canvas.width / 2, canvas.height / 2)

    // 顺时针旋转90度
    ctx.rotate(Math.PI / 2)

    // 绘制图片（需要调整位置使图片居中）
    ctx.drawImage(img, -img.width / 2, -img.height / 2)

    // 转换为blob并创建新的URL
    canvas.toBlob((blob) => {
      const newUrl = URL.createObjectURL(blob)
      // 释放旧的URL
      if (image.url.startsWith('blob:')) {
        URL.revokeObjectURL(image.url)
      }
      // 更新图片
      croppedImages.value[index] = {
        ...image,
        url: newUrl
      }
      ElMessage.success('顺时针旋转90°完成')
    }, 'image/png')

  } catch (error) {
    console.error('顺时针旋转失败:', error)
    ElMessage.error('顺时针旋转失败')
  }
}

// 垂直翻转
const handleFlipVertical = async (index) => {
  if (!croppedImages.value[index]) {
    return
  }

  try {
    const image = croppedImages.value[index]
    const img = new Image()
    img.crossOrigin = 'anonymous'

    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = image.url
    })

    // 创建canvas进行图片处理
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    canvas.width = img.width
    canvas.height = img.height

    // 平移到中心点
    ctx.translate(canvas.width / 2, canvas.height / 2)

    // 垂直翻转（沿X轴翻转）
    ctx.scale(1, -1)

    // 绘制图片
    ctx.drawImage(img, -img.width / 2, -img.height / 2)

    // 转换为blob并创建新的URL
    canvas.toBlob((blob) => {
      const newUrl = URL.createObjectURL(blob)
      // 释放旧的URL
      if (image.url.startsWith('blob:')) {
        URL.revokeObjectURL(image.url)
      }
      // 更新图片
      croppedImages.value[index] = {
        ...image,
        url: newUrl
      }
      ElMessage.success('垂直翻转完成')
    }, 'image/png')

  } catch (error) {
    console.error('垂直翻转失败:', error)
    ElMessage.error('垂直翻转失败')
  }
}

// 水平翻转
const handleFlipHorizontal = async (index) => {
  if (!croppedImages.value[index]) {
    return
  }

  try {
    const image = croppedImages.value[index]
    const img = new Image()
    img.crossOrigin = 'anonymous'

    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
      img.src = image.url
    })

    // 创建canvas进行图片处理
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    canvas.width = img.width
    canvas.height = img.height

    // 平移到中心点
    ctx.translate(canvas.width / 2, canvas.height / 2)

    // 水平翻转（沿Y轴翻转）
    ctx.scale(-1, 1)

    // 绘制图片
    ctx.drawImage(img, -img.width / 2, -img.height / 2)

    // 转换为blob并创建新的URL
    canvas.toBlob((blob) => {
      const newUrl = URL.createObjectURL(blob)
      // 释放旧的URL
      if (image.url.startsWith('blob:')) {
        URL.revokeObjectURL(image.url)
      }
      // 更新图片
      croppedImages.value[index] = {
        ...image,
        url: newUrl
      }
      ElMessage.success('水平翻转完成')
    }, 'image/png')

  } catch (error) {
    console.error('水平翻转失败:', error)
    ElMessage.error('水平翻转失败')
  }
}

watch(() => packInfo.selectedIconIndex, (idx) => {
  if (packInfo.iconSource === 'select' && croppedImages.value[idx]) {
    packInfo.iconPreview = croppedImages.value[idx].url
  }
})

// 处理上传封面
const handleIconUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  // 验证文件大小（限制5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    // 读取并预览图片
    const reader = new FileReader()
    reader.onload = (event) => {
      const img = new Image()
      img.onload = () => {
        // 创建canvas进行尺寸处理（如果需要）
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')

        // 保持原始尺寸，但建议最小128x128
        let width = img.width
        let height = img.height

        // 如果图片太小，提示用户
        if (width < 64 || height < 64) {
          ElMessage.warning('封面图片太小，建议至少128×128像素')
        }

        canvas.width = width
        canvas.height = height
        ctx.drawImage(img, 0, 0, width, height)

        // 转换为blob
        canvas.toBlob((blob) => {
          const url = URL.createObjectURL(blob)
          packInfo.customIcon = blob
          packInfo.iconPreview = url
          ElMessage.success('封面上传成功')
        }, 'image/png')
      }
      img.src = event.target.result
    }
    reader.readAsDataURL(file)
  } catch (error) {
    console.error('上传封面失败:', error)
    ElMessage.error('上传封面失败')
  }
}

const handleExport = async () => {
  // 检查是否有替换的材质
  if (Object.keys(replacedTextures.value).length === 0) {
    ElMessage.warning('请至少替换一个材质后再导出')
    return
  }

  try {
    loading.value = true
    const zip = new JSZip()

    // ⚠️ 重要：只导出用户替换过的材质，不导出原版材质（避免版权问题）
    for (const [path, blob] of Object.entries(replacedTextures.value)) {
      zip.file(path, blob)
    }

    // 添加pack.mcmeta
    const packMeta = {
      pack: {
        pack_format: 15,
        description: packInfo.description || '由材质工坊制作'
      }
    }
    zip.file('pack.mcmeta', JSON.stringify(packMeta, null, 2))

    // 添加pack.png（如果有）
    if (packInfo.iconSource === 'upload' && packInfo.customIcon) {
      // 使用上传的封面
      zip.file('pack.png', packInfo.customIcon)
    } else if (packInfo.iconSource === 'select' && packInfo.iconPreview) {
      // 使用选择的素材作为封面
      const blob = await fetch(packInfo.iconPreview).then(r => r.blob())
      zip.file('pack.png', blob)
    }

    // 生成并下载
    const content = await zip.generateAsync({type: 'blob'})
    const url = URL.createObjectURL(content)
    const a = document.createElement('a')
    a.href = url
    a.download = `${packInfo.name}.zip`
    a.click()

    ElMessage.success(`导出成功！共 ${Object.keys(replacedTextures.value).length} 个材质`)
  } catch (e) {
    console.error('导出失败:', e)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const handleBack = () => router.push('/my-tasks')

// 取消拖拽
const cancelDrop = () => {
  showDropOverlay.value = false
  dragCounter.value = 0
}

// 全局拖拽上传处理
const handleGlobalDragOver = (e) => {
  e.preventDefault()

  // 只有拖拽 ZIP 文件时才显示全局提示层
  // PNG 文件应该拖拽到具体的材质图片上，不显示全局提示
  if (e.dataTransfer.types.includes('Files')) {
    const items = e.dataTransfer.items
    if (items && items.length > 0) {
      // 检查是否是 ZIP 文件
      for (let i = 0; i < items.length; i++) {
        if (items[i].type === 'application/zip' || items[i].type === 'application/x-zip-compressed') {
          showDropOverlay.value = true
          break
        }
      }
    }
  }
}

const handleGlobalDragLeave = (e) => {
  e.preventDefault()
  e.stopPropagation()

  // 检查是否真的离开了main区域
  const mainElement = e.currentTarget
  const rect = mainElement.getBoundingClientRect()
  const x = e.clientX
  const y = e.clientY

  // 只在真正离开main边界时隐藏提示层
  if (x <= rect.left || x >= rect.right || y <= rect.top || y >= rect.bottom) {
    showDropOverlay.value = false
    dragCounter.value = 0
  }
}

const handleGlobalDrop = async (e) => {
  // 重置状态
  dragCounter.value = 0
  showDropOverlay.value = false

  // 清除拖拽高亮
  if (currentDragOverElement) {
    currentDragOverElement.classList.remove('drag-over')
    currentDragOverElement = null
  }

  // 检查是否有文件
  if (!e.dataTransfer.files || e.dataTransfer.files.length === 0) {
    return
  }

  const files = Array.from(e.dataTransfer.files)
  const zipFile = files.find(f => f.name.toLowerCase().endsWith('.zip'))

  // 只处理 ZIP 文件 - 导入材质包
  // PNG 文件应该拖拽到具体的材质图片上，由 handleFileDrop 处理
  if (zipFile) {
    e.preventDefault()
    e.stopPropagation()

    // 如果没有选择版本，提示用户
    if (!selectedVersion.value) {
      try {
        await ElMessageBox.confirm(
          '检测到您拖入了材质包文件，但尚未选择Minecraft版本。是否继续加载？',
          '提示',
          {
            confirmButtonText: '继续加载',
            cancelButtonText: '取消',
            type: 'info'
          }
        )
      } catch {
        return
      }
    }

    // 加载材质包
    try {
      loading.value = true
      ElMessage.info('正在加载材质包...')

      const zip = await JSZip.loadAsync(await zipFile.arrayBuffer())
      vanillaPack.value = zip
      packSource_origin.value = 'uploaded'
      packSource.value = 'upload'

      await buildTextureTree(zip)
      vanillaPackLoaded.value = true

      ElMessage.success(`材质包加载成功：${zipFile.name}`)
    } catch (error) {
      console.error('加载失败:', error)
      ElMessage.error('材质包加载失败：' + error.message)
    } finally {
      loading.value = false
    }
  }
  // PNG 文件不在这里处理，让它走原有的拖拽到具体节点的流程
}

// 监听页面级别的拖拽结束事件（用于处理拖拽到页面外的情况）
onMounted(async () => {
  await loadVersions()
  await loadCroppedImages()
  await loadSubscriptionInfo()

  // 添加全局拖拽结束监听
  const handleDragEnd = () => {
    showDropOverlay.value = false
    dragCounter.value = 0
  }

  document.addEventListener('dragleave', (e) => {
    // 当拖拽离开整个文档时
    if (e.clientX === 0 && e.clientY === 0) {
      handleDragEnd()
    }
  })

  document.addEventListener('dragend', handleDragEnd)
  document.addEventListener('drop', handleDragEnd)

  // 添加 ESC 键监听
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && showDropOverlay.value) {
      cancelDrop()
    }
  })
})

// 清理 blob URL，防止内存泄漏
onUnmounted(() => {
  croppedImages.value.forEach(image => {
    if (image.url && image.url.startsWith('blob:')) {
      URL.revokeObjectURL(image.url)
    }
  })
})

</script>

<style scoped>
/* --- 全局布局 --- */
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
  border: 4px solid #000;
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
  border: 4px solid #000;
  border-top: 0;
  border-bottom: 0;
  image-rendering: pixelated;
}
.side-border.left { border-right: 4px solid #000; }
.side-border.right { border-left: 4px solid #000; }

main {
  max-width: 900px;
  width: 100%;
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('/assets/images/new-textures/default_pine_wood.png');
  background-size: auto, 64px 64px;
  border: 4px solid #000;
  border-top: 0;
  padding: 20px;
  image-rendering: pixelated;
  color: #fff;
  text-shadow: 2px 2px 0 #000;
}

/* --- UI 组件 --- */
.mc-box {
  background-color: rgba(40, 40, 40, 0.85);
  backdrop-filter: blur(4px);
  background-image: none; /* 移除原有纹理 */
  border: 2px solid #000;
  box-shadow: inset 2px 2px 0px rgba(255, 255, 255, 0.2), inset -2px -2px 0px rgba(0, 0, 0, 0.4);
  padding: 8px 12px;
  text-align: center;
  display: inline-block;
  color: #fff;
  text-decoration: none;
  cursor: pointer;
  image-rendering: pixelated;
}
.mc-box:hover { background-color: rgba(60, 60, 60, 0.9); }
.mc-box:disabled { filter: grayscale(1); cursor: not-allowed; opacity: 0.5; }
.mc-box.small { padding: 4px 8px; font-size: 10px; }
.mc-box.mini { padding: 2px 6px; font-size: 10px; }
.mc-box.active { border-color: #FFD700; color: #FFD700; }

.mc-box-panel {
  background: rgba(30, 30, 30, 0.8);
  backdrop-filter: blur(4px);
  border: 3px solid #000;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.1);
  padding: 12px;
  color: #eee;
  text-shadow: none;
}

/* --- 头部 --- */
.page-header {
  display: flex; justify-content: space-between; align-items: center; padding-bottom: 20px;
}
.logo { font-size: 20px; }
.main-nav ul { display: flex; gap: 10px; list-style: none; padding: 0; margin: 0; }
.main-nav a.mc-box { padding: 6px 10px; font-size: 14px; }
.main-nav a.mc-box.active {
  background-color: #585858;
  box-shadow: inset 2px 2px 0px #3a3a3a, inset -2px -2px 0px #a0a0a0;
  color: #ffffa0;
}

/* --- 工具栏 --- */
.editor-toolbar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px; margin-bottom: 16px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
}
.toolbar-group { display: flex; gap: 10px; align-items: center; }
.radio-group { display: flex; gap: 5px; }
.mc-select {
  background: rgba(0, 0, 0, 0.6); border: 2px solid #888; color: white; padding: 6px; font-family: inherit;
}
.icon { width: 16px; height: 16px; vertical-align: middle; margin-right: 4px; }

/* --- 版权声明 --- */
.copyright-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #ffeb3b 0%, #ffc107 100%);
  border: 3px solid #ff9800;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.notice-icon {
  font-size: 24px;
  line-height: 1;
  flex-shrink: 0;
}

.notice-content {
  font-size: 13px;
  line-height: 1.6;
  color: #000;
  text-shadow: none;
}

.notice-content strong {
  color: #d84315;
  font-size: 14px;
}

/* --- 主编辑器区域 --- */
.editor-main {
  display: grid; grid-template-columns: 240px 1fr 200px; gap: 12px;
  height: 550px; /* 固定高度确保对齐 */
}

.panel-header {
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 2px solid #999; padding-bottom: 8px; margin-bottom: 8px;
  flex-shrink: 0;
}
.panel-header h4 { margin: 0; font-size: 14px; }

/* 左侧树 */
.panel-left {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.search-input {
  flex: none; /* 防止被 flex-grow 拉伸 */
  width: 100%;
  margin-bottom: 8px;
  background: rgba(0,0,0,0.5);
  border: 1px solid #666;
  padding: 4px 8px;
  color: #fff;
  height: 32px; /* 稍微增加高度以容纳文字 */
  font-size: 12px;
  line-height: 20px;
}

.quick-access {
  flex-shrink: 0;
}

.quick-tag {
  display: inline-block; font-size: 10px; padding: 2px 4px;
  background: #444; margin: 2px; cursor: pointer; border: 1px solid #666; color: #ddd;
  transition: all 0.2s ease;
}

.quick-tag:hover {
  background: #FFD700;
  border-color: #FFD700;
  color: #333;
  transform: scale(1.05);
}

.quick-tag.pvp-tag {
  background: #1a3a5a;
  border-color: #4a9eff;
  color: #8dbfff;
  font-weight: 500;
}

.quick-tag.pvp-tag:hover {
  background: #FFD700;
  border-color: #FFD700;
  color: #333;
}

.tree-container {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  background: rgba(20, 20, 20, 0.6);
  border: 2px solid #000;
  padding: 4px;
  overscroll-behavior: contain;
  color: #ddd;
}

/* 树形控件样式覆盖 */
:deep(.el-tree) {
  background: transparent;
  color: #ddd;
}
:deep(.el-tree-node__content) {
  height: 24px;
}
:deep(.el-tree-node:focus > .el-tree-node__content) {
  background-color: transparent;
}
:deep(.el-tree-node__content:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: rgba(255, 215, 0, 0.2);
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px;
  cursor: pointer;
  font-size: 12px;
  position: relative;
  transition: all 0.15s ease-in-out;
  width: 100%;
}
.tree-node-content:hover { background: transparent; }
.tree-node-content.is-replaced { color: #4ac156; font-weight: bold; }
.tree-node-content.drag-over {
  background: #FFF8DC !important;
  border: 2px solid #FFD700;
  box-shadow: 0 0 8px rgba(255, 215, 0, 0.6);
  color: #333;
}
.node-icon { width: 16px; height: 16px; pointer-events: none; }
.node-text { pointer-events: none; }

/* 中间预览 */
.panel-center {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.preview-viewport {
  background-image: linear-gradient(45deg, #333 25%, transparent 25%),
    linear-gradient(-45deg, #333 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #333 75%),
    linear-gradient(-45deg, transparent 75%, #333 75%);
  background-color: #222;
  background-size: 20px 20px;
  border: 4px solid #000;
  height: 300px; display: flex; align-items: center; justify-content: center;
  margin-bottom: 12px;
  transition: all 0.15s ease-in-out;
  position: relative;
  flex-shrink: 0;
}
.preview-viewport.drag-over {
  border-color: #FFD700 !important;
  border-width: 5px;
  background-color: rgba(255, 248, 220, 0.3);
  box-shadow: 0 0 16px rgba(255, 215, 0, 0.6);
}
.preview-img { max-width: 90%; max-height: 90%; image-rendering: pixelated; pointer-events: none; }
.file-info { font-size: 12px; margin-bottom: 12px; color: #ddd; flex-shrink: 0; }
.info-row { display: flex; justify-content: space-between; margin-bottom: 4px; border-bottom: 1px dotted #555; }
.status-tag.replaced { color: #4ac156; }
.status-tag.original { color: #aaa; }

.folder-grid-container {
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.folder-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(60px, 1fr)); gap: 4px; }

/* 快捷预览网格 */
.quick-preview-grid {
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  grid-auto-rows: 90px;
  gap: 8px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid #000;
}

.preview-grid-item {
  border: 2px solid #444;
  padding: 6px;
  text-align: center;
  cursor: pointer;
  background: rgba(40, 40, 40, 0.8);
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 90px;
  color: #ddd;
}

.preview-grid-item:hover {
  border-color: #FFD700;
  background: #555;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.6);
}

.preview-grid-item.is-replaced {
  border-color: #4ac156;
  background: #1a3a1a;
}

.preview-grid-item.drag-over {
  border-color: #FFD700 !important;
  border-width: 3px;
  background: #FFF8DC !important;
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.8);
  z-index: 10;
  color: #333;
}

.grid-item {
  border: 1px solid #444;
  padding: 4px;
  text-align: center;
  cursor: pointer;
  background: rgba(40, 40, 40, 0.8);
  transition: all 0.15s ease-in-out;
  position: relative;
  color: #ddd;
}
.grid-item:hover { border-color: #4a9eff; transform: translateY(-1px); background: #555; }
.grid-item.is-replaced { border-color: #4ac156; background: #1a3a1a; }
.grid-item.drag-over {
  border-color: #FFD700 !important;
  border-width: 3px;
  background: #FFF8DC !important;
  box-shadow: 0 0 12px rgba(255, 215, 0, 0.8);
  z-index: 10;
  color: #333;
}
.item-thumb { 
  pointer-events: none;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.item-thumb img { 
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  image-rendering: pixelated; 
  object-fit: contain;
}
.item-name { font-size: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; pointer-events: none; }

/* 右侧素材 */
.panel-right {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.palette-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}

/* 功能按钮区域 */
.palette-actions {
  padding: 8px;
  border-top: 2px solid #444;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.action-toggle-btn {
  width: 100%;
  padding: 8px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  background: #3a3a3a;
  color: #aaa;
  border: 2px solid #555;
}

.action-toggle-btn:hover {
  background: #4a4a4a;
  border-color: #666;
  color: #ccc;
}

.action-toggle-btn.active {
  background: #4a6a2a;
  color: #ffffa0;
  border-color: #5a7a3a;
  box-shadow: 0 0 8px rgba(90, 122, 58, 0.5);
}

.action-toggle-btn.active:hover {
  background: #5a7a3a;
  border-color: #6a8a4a;
}

/* 历史素材Tab (已移除) */
.palette-item {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid #444;
  padding: 4px;
  position: relative;
  cursor: move;
  display: flex;
  justify-content: center;
  transition: all 0.15s ease-in-out;
}
.palette-item:hover { transform: scale(1.05); border-color: #aaa; background: rgba(255, 255, 255, 0.2); }
.palette-item:active { transform: scale(0.95); }
.palette-item.selected { border-color: #FFD700; box-shadow: 0 0 8px rgba(255, 215, 0, 0.6); }
.palette-item.used { opacity: 0.5; }
.palette-item img { width: 48px; height: 48px; image-rendering: pixelated; pointer-events: none; }
.delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  background: #c14a4a;
  color: white;
  border: none;
  font-size: 10px;
  cursor: pointer;
  padding: 2px 4px;
  transition: all 0.15s;
}
.delete-btn:hover { background: #ff5555; transform: scale(1.1); }
.item-idx {
  position: absolute;
  bottom: 0;
  left: 0;
  font-size: 10px;
  background: #0008;
  color: white;
  padding: 1px 3px;
  pointer-events: none;
}

/* 底部信息 */
.pack-info { margin-top: 12px; }
.form-row { display: flex; align-items: center; margin-bottom: 8px; font-size: 12px; }
.form-row label { width: 60px; }
.mc-input { flex: 1; padding: 4px; }
.icon-selector {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.icon-source-tabs {
  display: flex;
  gap: 10px;
}

.icon-source-tabs .tab-btn {
  flex: 1;
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-source-tabs .tab-btn.active {
  background-color: #585858;
  box-shadow: inset 2px 2px 0px #3a3a3a, inset -2px -2px 0px #a0a0a0;
  color: #ffffa0;
}

.icon-select-mode,
.icon-upload-mode {
  display: flex;
  align-items: center;
  gap: 10px;
}

.icon-upload-mode .upload-btn {
  padding: 6px 12px;
  font-size: 12px;
}

.icon-upload-mode .upload-hint {
  font-size: 11px;
  color: #999;
  text-shadow: 1px 1px 0 #000;
}

.preview-icon-box {
  width: 64px;
  height: 64px;
  border: 2px solid #999;
  background: #222;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-icon-box img {
  max-width: 100%;
  max-height: 100%;
  image-rendering: pixelated;
}

.empty-state-panel { text-align: center; padding: 40px; }
.state-icon { width: 64px; height: 64px; margin-bottom: 10px; }

.custom-scrollbar::-webkit-scrollbar { width: 8px; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #999; border: 1px solid #fff; }
.custom-scrollbar::-webkit-scrollbar-track { background: #ccc; }

/* 拖拽优化 */
.tree-node-content,
.grid-item,
.preview-viewport,
.palette-item {
  user-select: none;
}

.mc-input,
input,
textarea,
select {
  user-select: auto;
}

/* 拖拽时的光标 */
.palette-item {
  cursor: grab;
}

.palette-item:active {
  cursor: grabbing !important;
}

/* 拖拽目标高亮（移除动画以提升性能） */
.page-footer { text-align: center; font-size: 10px; color: #ccc; margin-top: 20px; }

/* --- 拖拽上传提示层 --- */
.drop-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease-in-out;
  cursor: pointer;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.drop-hint-box {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 5px dashed #FFD700;
  border-radius: 16px;
  padding: 60px 80px;
  text-align: center;
  box-shadow:
    0 0 40px rgba(255, 215, 0, 0.6),
    inset 0 0 20px rgba(255, 255, 255, 0.2);
  animation: bounce 1s ease-in-out infinite;
  transform: scale(1);
  cursor: default;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-10px) scale(1.02);
  }
}

.drop-icon {
  font-size: 80px;
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  animation: rotate 2s ease-in-out infinite;
}

@keyframes rotate {
  0%, 100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}

.drop-text {
  font-size: 28px;
  font-weight: bold;
  color: #fff;
  text-shadow:
    2px 2px 4px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(255, 215, 0, 0.8);
  margin-bottom: 12px;
}

.drop-subtext {
  font-size: 16px;
  color: #FFD700;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  font-weight: 500;
}

.drop-cancel-hint {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

</style>
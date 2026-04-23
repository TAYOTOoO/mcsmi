<template>
  <div v-if="visible" class="announcement-overlay" @click.self="close">
    <div class="announcement-dialog mc-panel">
      <!-- 顶部标题栏 -->
      <div class="dialog-header">
        <div class="header-title">
          <i class="el-icon-bell"></i>
          <span>系统公告</span>
        </div>
        <button class="close-btn" @click="close">×</button>
      </div>

      <div class="dialog-body">
        <!-- 左侧列表 -->
        <div class="announcement-list">
          <div 
            v-for="(item, index) in announcements" 
            :key="item.announcementId"
            :class="['list-item', { active: currentIndex === index }]"
            @click="currentIndex = index"
          >
            <div class="item-icon">
              <span :class="['type-dot', getTypeClass(item.type)]"></span>
            </div>
            <div class="item-info">
              <div class="item-title" :title="item.title">{{ item.title }}</div>
              <div class="item-date">{{ formatDateShort(item.createTime) }}</div>
            </div>
            <div class="item-arrow">›</div>
          </div>
        </div>

        <!-- 右侧内容 -->
        <div class="announcement-content">
          <template v-if="currentAnnouncement">
            <div class="content-header">
              <h2 class="title">{{ currentAnnouncement.title }}</h2>
              <div class="meta">
                <span :class="['type-tag', getTypeClass(currentAnnouncement.type)]">
                  {{ getTypeText(currentAnnouncement.type) }}
                </span>
                <span class="time">发布时间：{{ currentAnnouncement.createTime }}</span>
              </div>
            </div>
            
            <div class="content-scrollable">
              <!-- 图片展示 -->
              <div v-if="currentAnnouncement.imageUrl" class="content-image-wrapper">
                <img :src="getImageUrl(currentAnnouncement.imageUrl)" alt="公告图片" class="content-image" />
              </div>
              
              <!-- 文本内容 -->
              <div class="text-body">{{ currentAnnouncement.content }}</div>
            </div>
          </template>
          <div v-else class="empty-state">
            暂无公告
          </div>
        </div>
      </div>
      
      <!-- 底部操作栏 -->
      <div class="dialog-footer">
        <div class="footer-left">
          <el-checkbox v-model="doNotShowToday" class="checkbox-dark">今日不再自动弹出</el-checkbox>
        </div>
        <button class="mc-btn" @click="close">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'

const visible = ref(false)
const announcements = ref([])
const currentIndex = ref(0)
const doNotShowToday = ref(false)

const currentAnnouncement = computed(() => {
  return announcements.value[currentIndex.value] || null
})

// 监听不再显示选项的变化
watch(doNotShowToday, (val) => {
  if (val) {
    localStorage.setItem('announcement_hide_date', new Date().toDateString())
  } else {
    localStorage.removeItem('announcement_hide_date')
  }
})

onMounted(async () => {
  try {
    const res = await request({
      url: '/mc/announcement/active',
      method: 'get'
    })

    if (res.data && res.data.length > 0) {
      announcements.value = res.data

      // 检查是否需要自动弹出
      const hideDate = localStorage.getItem('announcement_hide_date')
      const today = new Date().toDateString()

      // 总是显示公告（用于测试）
      console.log('公告数据:', announcements.value)
      console.log('hideDate:', hideDate, 'today:', today)

      // 如果没有勾选"今日不再显示"，则显示
      if (hideDate !== today) {
        visible.value = true
      } else {
        console.log('今日已选择不再显示')
      }
    } else {
      console.log('没有有效公告数据')
    }
  } catch (error) {
    console.error('Failed to load announcements', error)
  }
})

// 暴露给父组件手动打开的方法
const open = () => {
  visible.value = true
}

const close = () => {
  visible.value = false
}

const formatDateShort = (str) => {
  if (!str) return ''
  // 返回 MM-dd
  const date = new Date(str)
  return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
}

const getTypeText = (type) => {
  const map = { '1': '通知', '2': '公告', '3': '更新' }
  return map[type] || '消息'
}

const getTypeClass = (type) => {
  const map = { '1': 'info', '2': 'primary', '3': 'success' }
  return map[type] || 'info'
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 通过API代理访问上传的图片资源
  return '/api' + path
}

defineExpose({ open })
</script>

<style scoped>
.announcement-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.75);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.mc-panel {
  width: 900px;
  height: 600px;
  max-width: 95vw;
  max-height: 90vh;
  background-color: #1a1a1a; /* 深色背景 */
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23262626' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  border: 2px solid #4a4a4a;
  box-shadow: 0 20px 50px rgba(0,0,0,0.8), 0 0 0 4px rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
  color: #eee;
  overflow: hidden;
  border-radius: 4px;
}

.dialog-header {
  height: 50px;
  background: linear-gradient(180deg, #3c3c3c 0%, #2b2b2b 100%);
  border-bottom: 2px solid #111;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 2px 2px 0 #000;
}

.close-btn {
  background: none;
  border: none;
  color: #aaa;
  font-size: 28px;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}
.close-btn:hover {
  color: #fff;
}

.dialog-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧列表 */
.announcement-list {
  width: 250px;
  background: rgba(0, 0, 0, 0.2);
  border-right: 2px solid #111;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 15px 15px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
  position: relative;
}

.list-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.list-item.active {
  background: rgba(58, 134, 255, 0.15);
  border-left: 4px solid #3a86ff;
}

.item-icon {
  width: 20px;
  display: flex;
  justify-content: center;
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ccc;
}
.type-dot.info { background: #909399; box-shadow: 0 0 5px #909399; }
.type-dot.primary { background: #3a86ff; box-shadow: 0 0 5px #3a86ff; }
.type-dot.success { background: #67c23a; box-shadow: 0 0 5px #67c23a; }

.item-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.item-title {
  font-size: 14px;
  color: #ddd;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}
.list-item.active .item-title {
  color: #fff;
  font-weight: bold;
}

.item-date {
  font-size: 12px;
  color: #777;
  margin-top: 4px;
}

.item-arrow {
  color: #555;
  font-size: 18px;
  margin-left: 5px;
}

/* 右侧内容 */
.announcement-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(0, 0, 0, 0.1);
}

.content-header {
  padding: 20px 30px;
  border-bottom: 1px dashed #444;
  background: rgba(0, 0, 0, 0.1);
}

.title {
  margin: 0 0 15px 0;
  font-size: 24px;
  color: #ffc107;
  text-shadow: 1px 1px 0 #000;
  line-height: 1.4;
}

.meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #888;
}

.type-tag {
  padding: 2px 8px;
  border-radius: 4px;
  background: #333;
  color: #fff;
  font-size: 12px;
  border: 1px solid #555;
}
.type-tag.info { border-color: #909399; color: #909399; }
.type-tag.primary { border-color: #3a86ff; color: #3a86ff; }
.type-tag.success { border-color: #67c23a; color: #67c23a; }

.content-scrollable {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
}

/* 滚动条美化 */
.content-scrollable::-webkit-scrollbar,
.announcement-list::-webkit-scrollbar {
  width: 8px;
}
.content-scrollable::-webkit-scrollbar-track,
.announcement-list::-webkit-scrollbar-track {
  background: #1a1a1a;
}
.content-scrollable::-webkit-scrollbar-thumb,
.announcement-list::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 4px;
}
.content-scrollable::-webkit-scrollbar-thumb:hover,
.announcement-list::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.content-image-wrapper {
  margin-bottom: 25px;
  border-radius: 8px;
  overflow: hidden;
  border: 4px solid #000;
  box-shadow: 0 5px 15px rgba(0,0,0,0.5);
  text-align: center;
}

.content-image {
  max-width: 100%;
  display: block;
  margin: 0 auto;
  object-fit: contain;
}

.text-body {
  font-size: 16px;
  line-height: 1.8;
  color: #ccc;
  white-space: pre-wrap;
}

.empty-state {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #555;
  font-size: 18px;
}

/* 底部 */
.dialog-footer {
  height: 60px;
  background: #252525;
  border-top: 2px solid #111;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.mc-btn {
  background: #3a3a3a;
  color: #fff;
  border: 2px solid #111;
  border-bottom-width: 4px;
  padding: 8px 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.1s;
  font-weight: bold;
}
.mc-btn:hover {
  background: #4a4a4a;
  transform: translateY(-1px);
}
.mc-btn:active {
  background: #2a2a2a;
  transform: translateY(2px);
  border-bottom-width: 2px;
}

/* 复选框样式适配 */
:deep(.el-checkbox__label) {
  color: #aaa !important;
}
:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #fff !important;
}
</style>
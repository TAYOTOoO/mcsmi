<template>
  <div id="app">
    <router-view />
    <AnnouncementDialog />
    <FloatingButtons />
  </div>
</template>

<script setup>
import AnnouncementDialog from '@/components/AnnouncementDialog.vue'
import FloatingButtons from '@/components/FloatingButtons.vue'
</script>

<style>
/* 全局样式定义 */
:root {
  --mc-text-shadow: 2px 2px 0 #1a1a1a;
  --mc-text-color: #e0e0e0;
  --mc-border-light: #505050;
  --mc-border-dark: #2a2a2a;
  --mc-border-black: #000000;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;
}

body {
  background-image: url('@/assets/images/new-textures/default_cobble.png'); /* 圆石背景 */
  background-repeat: repeat;
  background-size: 64px 64px; /* 放大背景贴图 */
  background-attachment: fixed; /* 固定背景，防止滚动时闪烁 */
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  color: #e0e0e0; /* 稍微柔和的白色 */
  text-shadow: 2px 2px 0 #1a1a1a; /* 更深的阴影 */
  image-rendering: pixelated; /* 关键：让像素图片保持锐利 */
  min-height: 100vh;
  height: 100%;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overscroll-behavior-y: none;
}

#app {
  width: 100%;
  min-height: 100vh;
  height: 100%;
}

/* 修复 Element Plus 组件的文字重影问题 */
.el-message-box,
.el-message,
.el-dialog,
.el-notification,
.el-overlay {
  text-shadow: none !important;
  color: #333 !important; /* 确保在亮色背景下文字颜色为深色 */
}

/* 强制清除所有子元素的阴影 */
.el-message-box *,
.el-message *,
.el-dialog *,
.el-notification *,
.el-overlay * {
  text-shadow: none !important;
}

.el-message-box__title,
.el-message-box__content {
  color: #333 !important;
}
</style>

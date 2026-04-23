<template>
  <!-- 左侧浮动按钮组 -->
  <div class="floating-buttons-left">
    <button
      class="mc-box floating-btn floating-converter-btn"
      @click="goToConverter"
      title="材质包版本转换"
    >
      <img src="/assets/images/new-textures/default_mese_crystal.png" class="floating-icon-img" alt="icon" />
      <span class="floating-text">版本转换</span>
    </button>
  </div>

  <!-- 右侧浮动按钮组 -->
  <div class="floating-buttons-container">
    <!-- 订阅图片分割工厂入口（仅钻石用户可见） -->
    <button
      v-if="isDiamond && isFeatureAvailable('factory_entry')"
      class="mc-box floating-btn floating-vip-btn"
      @click="goToVipFactory"
      title="订阅图片分割工厂"
    >
      <img src="/assets/images/new-textures/default_tool_diamondpick.png" class="floating-icon-img" alt="icon" />
      <span class="floating-text">订阅工厂</span>
    </button>

    <!-- QQ群聊按钮（所有页面展示） -->
    <button
      v-if="qqGroupUrl"
      class="mc-box floating-btn floating-qq-btn"
      @click="joinQQGroup"
      title="加入QQ群"
    >
      <img src="/assets/images/new-textures/default_mese_crystal.png" class="floating-icon-img" alt="icon" />
      <span class="floating-text">加入群聊</span>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useVipGate } from '@/composables/useVipGate'
import { usePageConfig } from '@/composables/usePageConfig'

const router = useRouter()
const { isDiamond, isFeatureAvailable, loadSubscriptionInfo } = useVipGate()
const systemConfig = usePageConfig('system')

const qqGroupUrl = ref('')
const currentToken = ref(localStorage.getItem('token'))

onMounted(async () => {
  await systemConfig.loadConfig()
  qqGroupUrl.value = systemConfig.getConfig('system.social.qq_group_url', '')
})

// 使用轮询检测 token 变化（因为 localStorage 不会触发同页面的事件）
let tokenCheckInterval = null
onMounted(() => {
  tokenCheckInterval = setInterval(() => {
    const newToken = localStorage.getItem('token')
    if (newToken !== currentToken.value) {
      currentToken.value = newToken
      if (newToken) {
        console.log('检测到登录状态变化，刷新订阅信息')
        loadSubscriptionInfo()
      }
    }
  }, 500) // 每500ms检查一次
})

// 清理定时器
onUnmounted(() => {
  if (tokenCheckInterval) {
    clearInterval(tokenCheckInterval)
  }
})

const joinQQGroup = () => {
  if (qqGroupUrl.value) {
    window.open(qqGroupUrl.value, '_blank')
  }
}

const goToVipFactory = () => {
  router.push('/vip-factory')
}

const goToConverter = () => {
  router.push('/texture-converter')
}
</script><style scoped>
.floating-buttons-left {
  position: fixed;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}

.floating-buttons-container {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

.floating-btn {
  writing-mode: vertical-rl;
  text-orientation: upright;
  padding: 16px 12px;
  border: 2px solid var(--mc-border-black, #000);
  border-right: none;
  border-radius: 8px 0 0 8px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  letter-spacing: 4px;
  image-rendering: pixelated;
  text-shadow: 2px 2px 0 #000;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.floating-qq-btn {
  background-image: linear-gradient(rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.1)), url('/assets/images/new-textures/default_wood.png');
  background-size: auto, 64px 64px;
  box-shadow:
    inset 2px 2px 0px rgba(255,255,255,0.2),
    inset -2px -2px 0px rgba(0,0,0,0.4),
    -4px 4px 10px rgba(0,0,0,0.5);
}

.floating-vip-btn {
  background-image: linear-gradient(rgba(80, 0, 120, 0.3), rgba(40, 0, 80, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  box-shadow:
    inset 2px 2px 0px rgba(200,150,255,0.3),
    inset -2px -2px 0px rgba(0,0,0,0.4),
    -4px 4px 10px rgba(100,0,200,0.3);
  border-color: #6a0dad;
}

.floating-btn:hover {
  padding-right: 20px;
  transform: translateX(-5px);
}

.floating-buttons-left .floating-btn {
  border-right: 2px solid var(--mc-border-black, #000);
  border-left: none;
  border-radius: 0 8px 8px 0;
}

.floating-buttons-left .floating-btn:hover {
  padding-left: 20px;
  padding-right: 12px;
  transform: translateX(5px);
}

.floating-converter-btn {
  background-image: linear-gradient(rgba(60, 40, 20, 0.3), rgba(30, 20, 10, 0.3)), url('/assets/images/new-textures/default_wood.png');
  background-size: auto, 64px 64px;
  box-shadow:
    inset 2px 2px 0px rgba(255,255,255,0.2),
    inset -2px -2px 0px rgba(0,0,0,0.4),
    4px 4px 10px rgba(0,0,0,0.5);
}

.floating-converter-btn:hover {
  background-image: linear-gradient(rgba(60, 40, 20, 0.1), rgba(30, 20, 10, 0.1)), url('/assets/images/new-textures/default_wood.png');
  box-shadow:
    inset 2px 2px 0px rgba(255,255,255,0.4),
    inset -2px -2px 0px rgba(0,0,0,0.4),
    6px 6px 15px rgba(0,0,0,0.6);
}

.floating-qq-btn:hover {
  background-image: linear-gradient(rgba(0, 0, 0, 0), rgba(0, 0, 0, 0)), url('/assets/images/new-textures/default_wood.png');
  box-shadow:
    inset 2px 2px 0px rgba(255,255,255,0.4),
    inset -2px -2px 0px rgba(0,0,0,0.4),
    -6px 6px 15px rgba(0,0,0,0.6);
}

.floating-vip-btn:hover {
  background-image: linear-gradient(rgba(100, 0, 150, 0.2), rgba(60, 0, 100, 0.2)), url('/assets/images/new-textures/default_obsidian_brick.png');
  box-shadow:
    inset 2px 2px 0px rgba(200,150,255,0.5),
    inset -2px -2px 0px rgba(0,0,0,0.4),
    -6px 6px 15px rgba(100,0,200,0.5);
}

.floating-icon-img {
  width: 32px;
  height: 32px;
  image-rendering: pixelated;
  filter: drop-shadow(2px 2px 0 rgba(0,0,0,0.5));
  animation: float 3s ease-in-out infinite;
  writing-mode: horizontal-tb;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.floating-text {
  display: block;
  font-size: 18px;
  line-height: 1.2;
}
</style>

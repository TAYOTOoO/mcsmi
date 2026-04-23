<template>
  <div class="page-wrapper">
    <div class="top-border"></div>
    <div class="content-body">
      <div class="side-border left"></div>
      <main>
        <!-- 页头 -->
        <header class="page-header">
          <div class="mc-box logo">{{ getConfig('system.site.name', '材质工坊') }}</div>
          <nav class="main-nav">
            <ul>
              <li><router-link to="/" class="mc-box active">首页</router-link></li>
              <li><router-link to="/generate" class="mc-box">开始生成</router-link></li>
              <li><router-link to="/my-tasks" class="mc-box">我的任务</router-link></li>
              <li><router-link to="/texture-pack-editor" class="mc-box">材质编辑</router-link></li>
              <li><router-link to="/recharge" class="mc-box">积分充值</router-link></li>
            </ul>
          </nav>

          <!-- 用户操作区 -->
          <div class="user-actions">
            <template v-if="username">
              <span class="mc-box username-display" @click="goToProfile" title="个人中心">
                {{ username }}
                <span v-if="isDiamond" class="diamond-icon">💎</span>
              </span>
              <button class="mc-box logout-btn" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <button class="mc-box login-btn" @click="goToLogin">登录</button>
            </template>
          </div>
        </header>

        <!-- Hero 主视觉区域 -->
        <section class="hero">
          <h1 v-html="getConfig('home.hero.title')"></h1>
          <p class="subtitle">{{ getConfig('home.hero.subtitle', '灵感驱动的Minecraft材质包生成平台') }}</p>
          <div class="hero-buttons">
            <button class="btn btn-primary" @click="goToGenerate">{{ getConfig('home.hero.button.primary', '开始创作') }}</button>
            <button class="btn mc-box" style="padding: 12px 24px; font-size: 20px;" @click="scrollToFeatures">{{ getConfig('home.hero.button.secondary', '了解更多') }}</button>
          </div>
        </section>

        <!-- Stats区域 -->
        <section class="stats">
          <div class="mc-box stat-item">
            {{ getConfig('home.stats.1.number', '10,000+') }}<br>{{ getConfig('home.stats.1.label', '已生成') }}
          </div>
          <div class="mc-box stat-item">
            {{ getConfig('home.stats.2.number', '5,000+') }}<br>{{ getConfig('home.stats.2.label', '用户') }}
          </div>
          <div class="mc-box stat-item">
            {{ getConfig('home.stats.3.number', '50+') }}<br>{{ getConfig('home.stats.3.label', '材质类型') }}
          </div>
        </section>

        <!-- Features区域 -->
        <section class="features" ref="featuresRef">
          <h2 class="mc-box section-title">{{ getConfig('home.features.title', '为什么选择材质工坊') }}</h2>
          <div class="features-grid">
            <!-- 卡片 1 -->
            <div class="mc-box feature-card">
              <img :src="getConfig('home.features.card1.icon', '/assets/images/new-textures/default_tool_diamondpick.png')" alt="特征图标">
              <h3>{{ getConfig('home.features.card1.title', 'AI智能生成') }}</h3>
              <div class="description">
                <span>{{ getConfig('home.features.card1.desc1', '输入关键词描述') }}</span>
                <span>{{ getConfig('home.features.card1.desc2', 'AI自动生成高质量材质') }}</span>
              </div>
            </div>
            <!-- 卡片 2 -->
            <div class="mc-box feature-card">
              <img :src="getConfig('home.features.card2.icon', '/assets/images/new-textures/default_tool_diamondsword.png')" alt="特征图标">
              <h3>{{ getConfig('home.features.card2.title', '精准切割') }}</h3>
              <div class="description">
                <span>{{ getConfig('home.features.card2.desc1', '自动识别材质类型') }}</span>
                <span>{{ getConfig('home.features.card2.desc2', '精确切割到像素级') }}</span>
              </div>
            </div>
            <!-- 卡片 3 -->
            <div class="mc-box feature-card">
              <img :src="getConfig('home.features.card3.icon', '/assets/images/new-textures/default_mese_crystal_fragment.png')" alt="特征图标">
              <h3>{{ getConfig('home.features.card3.title', '极速处理') }}</h3>
              <div class="description">
                <span>{{ getConfig('home.features.card3.desc1', '云端GPU加速') }}</span>
                <span>{{ getConfig('home.features.card3.desc2', '秒级生成材质包') }}</span>
              </div>
            </div>
            <!-- 卡片 4 -->
            <div class="mc-box feature-card">
              <img :src="getConfig('home.features.card4.icon', '/assets/images/new-textures/bucket.png')" alt="特征图标">
              <h3>{{ getConfig('home.features.card4.title', '多版本支持') }}</h3>
              <div class="description">
                <span>{{ getConfig('home.features.card4.desc1', '支持1.8-1.20全版本') }}</span>
                <span>{{ getConfig('home.features.card4.desc2', '自动适配版本格式') }}</span>
              </div>
            </div>
            <!-- 卡片 5 -->
            <div class="mc-box feature-card">
              <img :src="getConfig('home.features.card5.icon', '/assets/images/new-textures/ebiomes_bucket_peas.png')" alt="特征图标">
              <h3>{{ getConfig('home.features.card5.title', '一键导出') }}</h3>
              <div class="description">
                <span>{{ getConfig('home.features.card5.desc1', '自动打包ZIP格式') }}</span>
                <span>{{ getConfig('home.features.card5.desc2', '即下即用无需配置') }}</span>
              </div>
            </div>
            <!-- 卡片 6 -->
            <div class="mc-box feature-card">
              <img :src="getConfig('home.features.card6.icon', '/assets/images/new-textures/farming_bread.png')" alt="特征图标">
              <h3>{{ getConfig('home.features.card6.title', '风格多样') }}</h3>
              <div class="description">
                <span>{{ getConfig('home.features.card6.desc1', '写实/卡通/像素/科幻') }}</span>
                <span>{{ getConfig('home.features.card6.desc2', '数十种预设风格') }}</span>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout } from '@/api/auth'
import { usePageConfig } from '@/composables/usePageConfig'
import { useSubscription } from '@/composables/useSubscription'

const router = useRouter()
const featuresRef = ref(null)
const username = ref('')

// 订阅信息
const { isDiamond } = useSubscription()

// 加载页面配置（支持home和system配置）
const homeConfig = usePageConfig('home')
const systemConfig = usePageConfig('system')

// 统一的getConfig函数
const getConfig = (key, defaultValue = '') => {
  if (key.startsWith('system.')) {
    return systemConfig.getConfig(key, defaultValue)
  }
  return homeConfig.getConfig(key, defaultValue)
}

onMounted(async () => {
  username.value = localStorage.getItem('username') || ''
  // 并行加载首页和系统配置
  await Promise.all([
    homeConfig.loadConfig(),
    systemConfig.loadConfig()
  ])
})

const goToGenerate = () => {
  router.push('/generate')
}

const goToLogin = () => {
  router.push('/login')
}

const goToProfile = () => {
  router.push('/profile')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    try {
      await logout()
    } catch (error) {
      // 忽略后端错误
    }

    localStorage.removeItem('token')
    localStorage.removeItem('username')
    username.value = ''

    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    console.log('用户取消退出')
  }
}

const scrollToFeatures = () => {
  featuresRef.value?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
/* === 性能优化：防止滚动闪屏 === */
* {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 优化滚动性能 */
html {
  overflow-y: auto;
  overflow-x: hidden;
}

/* --- 整体布局 --- */
.page-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px 20px 20px;
  transform: scale(1.5);
  transform-origin: top center;
  margin-bottom: 200px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  will-change: transform;
  contain: paint;
}

.top-border {
  background-image: url('@/assets/images/new-textures/default_obsidian_brick.png');
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
  background-image: url('@/assets/images/new-textures/default_obsidian_block.png');
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
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('@/assets/images/new-textures/default_pine_wood.png');
  background-size: auto, 64px 64px;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  image-rendering: pixelated;
  overflow: hidden;
}

/* --- Minecraft UI 方块通用样式 --- */
.mc-box {
  background-color: rgba(40, 40, 40, 0.85);
  backdrop-filter: blur(4px);
  border: 2px solid var(--mc-border-black);
  box-shadow:
      inset 2px 2px 0px var(--mc-border-light),
      inset -2px -2px 0px var(--mc-border-dark);
  padding: 8px 12px;
  text-align: center;
  display: inline-block;
  color: var(--mc-text-color);
  text-decoration: none;
}
a.mc-box:hover, button.mc-box:hover {
  background-color: rgba(60, 60, 60, 0.9);
  box-shadow:
      inset 2px 2px 0px #666,
      inset -2px -2px 0px #222;
  cursor: pointer;
}

/* --- 页头 Header --- */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
.logo {
  font-size: 24px;
}
.main-nav ul {
  display: flex;
  gap: 10px;
  list-style: none;
}
.main-nav a.mc-box {
  padding: 6px 10px;
  font-size: 14px;
}
.main-nav a.mc-box.active {
  background-color: #3a3a3a;
  box-shadow: inset 2px 2px 0px #222, inset -2px -2px 0px #555;
  color: #ffffa0;
  border-color: #000;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

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

.diamond-icon {
  margin-left: 4px;
  animation: sparkle 2s infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}
.login-btn, .logout-btn {
  font-family: inherit;
  font-size: 14px;
  padding: 6px 10px;
  text-shadow: var(--mc-text-shadow);
}

/* --- Hero 主视觉区域 --- */
.hero {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('@/assets/images/new-textures/default_obsidian_glass.png');
  background-repeat: repeat;
  background-size: 64px auto;

  margin: 0 20px;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  border: 2px solid var(--mc-border-black);
  image-rendering: pixelated;
}

.hero h1 {
  font-size: 28px;
  text-align: center;
  line-height: 1.4;
}
.hero .subtitle {
  font-size: 14px;
  text-shadow: 1px 1px 0 #3a3a3a;
  text-align: center;
}
.hero-buttons {
  margin-top: 15px;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
}
.btn {
  font-family: inherit;
  color: var(--mc-text-color);
  text-shadow: var(--mc-text-shadow);
  text-decoration: none;
  cursor: pointer;
}
.btn-primary {
  position: relative;
  font-size: 20px;
  padding: 12px 20px 12px 50px;
  background: linear-gradient(to right, #29a9a2, #47d6cd);
  border: 2px solid #1c2d2c;
  box-shadow: inset 0 0 0 2px #4fe0d8;
}
.btn-primary::before {
  content: '';
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  background-image: url('@/assets/images/new-textures/default_mese_crystal.png');
  background-size: contain;
  image-rendering: pixelated;
}
.btn-primary::after {
  content: '✦';
  position: absolute;
  font-size: 20px;
  color: #fff;
  text-shadow: 0 0 8px #fff;
  animation: sparkle 2s infinite;
}
.btn-primary::after { right: -10px; top: -10px; }
@keyframes sparkle {
  0%, 100% { opacity: 0; transform: scale(0.5); }
  50% { opacity: 1; transform: scale(1.2); }
}


/* --- Stats 区域 --- */
.stats {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 30px 20px;
  flex-wrap: wrap;
}
.stat-item {
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('@/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  width: 180px;
  font-size: 16px;
  line-height: 1.6;
  image-rendering: pixelated;
}

/* --- Section Title --- */
.section-title {
  background-color: #373737;
  max-width: 400px;
  margin: 0 auto 30px auto;
  font-size: 20px;
}

/* --- Features 区域 --- */
.features {
  padding: 0 20px 30px 20px;
}
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}
.feature-card {
  background-image: linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), url('@/assets/images/new-textures/default_wood.png');
  background-size: auto, 64px 64px;
  padding: 20px 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  image-rendering: pixelated;
  border: 2px solid var(--mc-border-dark);
}
.feature-card img {
  will-change: transform; width: 64px; height: 64px; image-rendering: pixelated; }
.feature-card h3 { font-size: 18px; }
.feature-card .description { display: flex; flex-direction: column; gap: 8px; width: 100%; }
.feature-card .description span {
  background-color: rgba(0, 0, 0, 0.5);
  padding: 6px 4px;
  font-size: 12px;
  line-height: 1.5;
  text-shadow: 1px 1px 0 #3a3a3a;
}
</style>

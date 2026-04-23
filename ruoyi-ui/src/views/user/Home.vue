<template>
  <div class="page-wrapper minecraft-page">
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
              <span class="mc-box username-display" @click="goToProfile" title="个人中心">{{ username }}</span>
              <button class="mc-box logout-btn" @click="handleLogout">退出</button>
            </template>
            <template v-else>
              <button class="mc-box login-btn" @click="goToLogin">登录</button>
            </template>
          </div>
        </header>

        <!-- Hero 主视觉区域 -->
        <section class="hero">
          <h1 v-html="getConfig('home.hero.title', 'AI驱动的<br>Minecraft材质包生成平台')"></h1>
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
            <!-- 卡片 1-6 -->
            <div class="mc-box feature-card" v-for="i in 6" :key="i">
              <img :src="getConfig(`home.features.card${i}.icon`, `/assets/images/new-textures/default_tool_diamondpick.png`)" alt="特征图标">
              <h3>{{ getConfig(`home.features.card${i}.title`, 'AI智能生成') }}</h3>
              <div class="description">
                <span>{{ getConfig(`home.features.card${i}.desc1`, '输入关键词描述') }}</span>
                <span>{{ getConfig(`home.features.card${i}.desc2`, 'AI自动生成高质量材质') }}</span>
              </div>
            </div>
          </div>
        </section>
      </main>
      <div class="side-border right"></div>
    </div>
  </div>
</template>

<script>
import { logout } from '@/api/mc/auth'
import { createPageConfig } from '@/utils/pageConfig'

export default {
  name: 'UserHome',
  mixins: [createPageConfig('home'), createPageConfig('system')],
  data() {
    return {
      username: ''
    }
  },
  mounted() {
    this.username = localStorage.getItem('username') || ''
  },
  methods: {
    goToGenerate() {
      this.$router.push('/generate')
    },
    goToLogin() {
      this.$router.push('/login')
    },
    goToProfile() {
      this.$router.push('/user/profile')
    },
    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
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
        this.username = ''

        this.$message.success('已退出登录')
        this.$router.push('/login')
      } catch (error) {
        console.log('用户取消退出')
      }
    },
    scrollToFeatures() {
      if (this.$refs.featuresRef) {
        this.$refs.featuresRef.scrollIntoView({ behavior: 'smooth' })
      }
    }
  }
}
</script>

<style scoped>
/* --- 整体布局 --- */
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
  background-image: url('~@/assets/images/minecraft/new-textures/default_obsidian_brick.png');
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
  background-image: url('~@/assets/images/minecraft/new-textures/default_obsidian_block.png');
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
  background-image: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.4)), url('~@/assets/images/minecraft/new-textures/default_pine_wood.png');
  background-size: auto, 64px 64px;
  border: 4px solid var(--mc-border-black);
  border-top: 0;
  image-rendering: pixelated;
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
.login-btn, .logout-btn {
  font-family: inherit;
  font-size: 14px;
  padding: 6px 10px;
  text-shadow: var(--mc-text-shadow);
}

/* --- Hero 主视觉区域 --- */
.hero {
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('~@/assets/images/minecraft/new-textures/default_obsidian_glass.png');
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
  background-image: url('~@/assets/images/minecraft/new-textures/default_mese_crystal.png');
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
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('~@/assets/images/minecraft/new-textures/default_obsidian_brick.png');
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
  background-image: linear-gradient(rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.6)), url('~@/assets/images/minecraft/new-textures/default_wood.png');
  background-size: auto, 64px 64px;
  padding: 20px 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  image-rendering: pixelated;
  border: 2px solid var(--mc-border-dark);
}
.feature-card img { width: 64px; height: 64px; image-rendering: pixelated; }
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

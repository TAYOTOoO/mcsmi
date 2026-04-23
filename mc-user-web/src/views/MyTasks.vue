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
              <li><router-link to="/my-tasks" class="mc-box active">我的任务</router-link></li>
              <li><router-link to="/texture-pack-editor" class="mc-box">材质编辑</router-link></li>
              <li><router-link to="/recharge" class="mc-box">积分充值</router-link></li>
            </ul>
          </nav>
          
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

        <!-- 主要内容区域：任务列表 -->
        <section class="tasks-section">
          <header class="tasks-header mc-box">
            <h2>我的任务列表</h2>
            <button class="btn-refresh mc-box" @click="loadTasks(false)">
              <img src="/assets/images/new-textures/default_apple.png" alt="刷新">
              刷新
            </button>
          </header>

          <div class="task-list-container">
            <div v-if="loading" class="loading-state">
              <p>加载中...</p>
            </div>
            
            <div v-else-if="taskList.length === 0" class="empty-state">
              <img src="/assets/images/new-textures/default_grass.png" alt="空" class="empty-icon" />
              <p>还没有任务，快去生成吧！</p>
              <button class="mc-box btn-view" @click="goToGenerate">开始生成</button>
            </div>

            <ul v-else class="task-list">
              <li v-for="task in taskList" :key="task.taskId" class="task-item mc-box">
                <div class="task-info">
                  <p class="task-id">{{ task.taskNo }}</p>
                  <p class="task-prompt">{{ task.styleDescription }}</p>
                </div>
                <div class="task-details">
                  <div class="detail-row">
                    <!-- 状态显示 -->
                    <span v-if="task.taskStatus === 4" class="status-badge completed">
                      <img src="/assets/images/new-textures/default_mese_crystal.png" alt="已完成">已完成
                    </span>
                    <span v-else-if="task.taskStatus === 5" class="status-badge failed">
                      <img src="/assets/images/new-textures/flowers_mushroom_red.png" alt="失败">失败
                    </span>
                    <span v-else class="status-badge processing">
                      <img src="/assets/images/new-textures/default_apple.png" alt="生成中">生成中
                    </span>

                    <!-- 材质类型标识 -->
                    <span v-if="task.materialType === 2" class="material-type-badge ui-material">
                      <img src="/assets/images/new-textures/default_gold_block.png" alt="UI">UI
                    </span>
                    <span v-else-if="task.materialType === 1" class="material-type-badge item-material">
                      <img src="/assets/images/new-textures/default_diamond.png" alt="物品">物品
                    </span>

                    <span class="timestamp">
                      <img src="/assets/images/new-textures/default_apple.png" alt="时间">{{ formatTime(task.createTime) }}
                    </span>
                  </div>
                  
                  <div class="detail-row">
                    <span class="cost">
                      <img src="/assets/images/new-textures/default_mese_crystal.png" alt="积分">消耗: {{ task.costPoints }} 积分
                    </span>
                    
                    <button v-if="task.taskStatus === 4" class="btn-view mc-box" @click="viewResult(task)">
                      <img src="/assets/images/new-textures/default_stick.png" alt="查看">查看结果
                    </button>
                    <button v-else-if="task.taskStatus === 5" class="btn-view mc-box" @click="regenerate(task)">
                      <img src="/assets/images/new-textures/default_mese_crystal_fragment.png" alt="重试">重新生成
                    </button>
                    <button v-else class="btn-view mc-box" disabled style="opacity: 0.5; cursor: not-allowed;">
                      <img src="/assets/images/new-textures/default_stick.png" alt="查看">处理中
                    </button>
                  </div>
                  
                  <!-- 错误信息 -->
                  <div v-if="task.taskStatus === 5" class="error-row">
                    非典型错误请联系管理员，积分已返还
                  </div>
                </div>
              </li>
            </ul>
            
            <!-- 分页 -->
            <div v-if="total > queryParams.pageSize" class="pagination">
              <button class="mc-box btn-page" :disabled="queryParams.pageNum === 1" @click="prevPage">上一页</button>
              <span class="page-info mc-box">{{ queryParams.pageNum }} / {{ Math.ceil(total / queryParams.pageSize) }}</span>
              <button class="mc-box btn-page" :disabled="queryParams.pageNum >= Math.ceil(total / queryParams.pageSize)" @click="nextPage">下一页</button>
            </div>
          </div>
        </section>
      </main>
      <div class="side-border right"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listGeneration, regenerateTask } from '@/api/generation'
import { logout } from '@/api/auth'
import { useSubscription } from '@/composables/useSubscription'

const router = useRouter()
const loading = ref(false)
const taskList = ref([])
const total = ref(0)

// 订阅信息
const { isDiamond } = useSubscription()
const username = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const previousTaskStatus = ref(new Map())

onMounted(() => {
  username.value = localStorage.getItem('username') || ''
  loadTasks()
  setInterval(() => {
    loadTasks(true)
  }, 10000)
})

const loadTasks = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }

  try {
    const res = await listGeneration(queryParams)
    const newTaskList = res.rows || []

    if (silent && taskList.value.length > 0) {
      newTaskList.forEach(newTask => {
        const oldStatus = previousTaskStatus.value.get(newTask.taskId)
        if (oldStatus !== undefined && oldStatus !== newTask.taskStatus) {
          if (newTask.taskStatus === 4) {
            ElMessage.success({
              message: `任务 ${newTask.taskNo} 生成完成！`,
              duration: 5000,
              showClose: true
            })
          } else if (newTask.taskStatus === 5) {
            ElMessage.error({
              message: `任务 ${newTask.taskNo} 非典型错误请联系管理员，积分已返还`,
              duration: 8000,
              showClose: true
            })
          }
        }
        previousTaskStatus.value.set(newTask.taskId, newTask.taskStatus)
      })
    }

    if (previousTaskStatus.value.size === 0) {
      newTaskList.forEach(task => {
        previousTaskStatus.value.set(task.taskId, task.taskStatus)
      })
    }

    taskList.value = newTaskList
    total.value = res.total || 0
  } catch (error) {
    if (!silent) {
      ElMessage.error('加载任务列表失败')
    }
  } finally {
    loading.value = false
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ')
}

const viewResult = (row) => {
  router.push(`/result/${row.taskId}`)
}

const regenerate = async (row) => {
  try {
    await ElMessageBox.confirm(
      '重新生成将消耗100积分，是否继续？',
      '确认重新生成',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    row.taskStatus = 1
    await regenerateTask(row.taskId)
    ElMessage.success('重新生成任务已提交，请稍候...')
    await loadTasks()
  } catch (error) {
    if (error !== 'cancel') {
      // 如果错误信息包含"积分不足"，直接显示
      if (error.message && error.message.includes('积分不足')) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('重新生成失败')
      }
      loadTasks()
    }
  }
}

const prevPage = () => {
  if (queryParams.pageNum > 1) {
    queryParams.pageNum--
    loadTasks()
  }
}

const nextPage = () => {
  if (queryParams.pageNum < Math.ceil(total.value / queryParams.pageSize)) {
    queryParams.pageNum++
    loadTasks()
  }
}

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
      // 继续
    }

    localStorage.removeItem('token')
    localStorage.removeItem('username')
    username.value = ''

    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 取消
  }
}
</script>

<style scoped>
/* --- 整体布局 (与主页一致) --- */
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
  /* 内部深色背景材质 (松木+遮罩) */
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
button.mc-box {
  font-size: 14px;
}

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

.diamond-icon {
  margin-left: 4px;
  animation: sparkle 2s infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

.login-btn, .logout-btn { padding: 6px 10px; font-size: 14px; }

/* --- 任务列表特定样式 --- */
.tasks-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  margin-bottom: 20px;
  /* 标题栏背景材质 (黑曜石砖) */
  background-image: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
}
.tasks-header h2 { 
  font-size: 24px; 
  margin: 0; 
  text-shadow: 2px 2px 0 #3a3a3a, -2px -2px 0 #3a3a3a, 2px -2px 0 #3a3a3a, -2px 2px 0 #3a3a3a; 
}
.btn-refresh { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  font-size: 16px; 
  padding: 8px 16px; 
}
.btn-refresh img { 
  width: 32px; 
  height: 32px; 
  image-rendering: pixelated; 
}

.task-list-container {
  height: 65vh;
  overflow-y: auto;
  padding: 10px;
  background-color: rgba(0, 0, 0, 0.5); /* 更深的背景 */
  border: 2px solid var(--mc-border-dark);
  box-shadow: inset 2px 2px 0px #222, inset -2px -2px 0px #888;
}
.task-list-container::-webkit-scrollbar { width: 16px; }
.task-list-container::-webkit-scrollbar-track { 
  background-color: #222;
  border-left: 2px solid var(--mc-border-dark); 
}
.task-list-container::-webkit-scrollbar-thumb { 
  background-color: #555; 
  border: 2px solid var(--mc-border-dark); 
  box-shadow: inset 2px 2px 0px #777, inset -2px -2px 0px #333; 
}

.task-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 15px; }

.task-item {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 15px;
  /* 任务卡片背景 - 深色木纹或深色砖 */
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('/assets/images/new-textures/default_obsidian_brick.png');
  background-size: auto, 64px 64px;
  text-align: left;
}
.task-info { flex: 1; }
.task-id { 
  font-size: 14px; 
  color: #ddd; 
  margin: 0 0 8px 0; 
  text-shadow: 2px 2px 0 #000; 
}
.task-prompt { 
  font-size: 16px; 
  line-height: 1.6; 
  word-break: break-all; 
  margin: 0; 
  text-shadow: 2px 2px 0 #000; 
}

.task-details { width: 340px; display: flex; flex-direction: column; justify-content: space-between; gap: 10px; flex-shrink: 0; }
.detail-row { display: flex; justify-content: space-between; align-items: center; }
.timestamp, .cost { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  font-size: 14px; 
  color: #eee; 
  text-shadow: 2px 2px 0 #000; 
}
.timestamp img, .cost img { 
  width: 24px; 
  height: 24px; 
  image-rendering: pixelated; 
}

.status-badge { 
  display: inline-flex; 
  align-items: center; 
  gap: 8px; 
  padding: 6px 10px; 
  font-size: 14px; 
  border: 2px solid var(--mc-border-dark); 
  color: white; 
  text-shadow: 2px 2px 0 #000; 
}
.status-badge img { 
  width: 24px; 
  height: 24px; 
  image-rendering: pixelated; 
}
.status-badge.completed { background-color: #2a632b; box-shadow: inset 1px 1px 0 #3c8f3e; }
.status-badge.processing { background-color: #614b2c; box-shadow: inset 1px 1px 0 #a17d4a; }
.status-badge.failed { background-color: #7b2929; box-shadow: inset 1px 1px 0 #b13e3e; }

/* 材质类型标识 - 与status-badge统一样式 */
.material-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  font-size: 14px;
  border: 2px solid var(--mc-border-dark);
  color: white;
  text-shadow: 2px 2px 0 #000;
  white-space: nowrap;
}
.material-type-badge img {
  width: 24px;
  height: 24px;
  image-rendering: pixelated;
}
.material-type-badge.ui-material {
  background-color: #d4af37;
  box-shadow: inset 1px 1px 0 #f4cf57;
}
.material-type-badge.item-material {
  background-color: #4a90e2;
  box-shadow: inset 1px 1px 0 #6aa8f2;
}

.btn-view { 
  font-size: 14px; 
  padding: 8px 12px; 
  display: flex; 
  align-items: center; 
  gap: 8px; 
}
.btn-view img { 
  width: 24px; 
  height: 24px; 
  image-rendering: pixelated; 
}

.error-row { 
  color: #ff5555; 
  font-size: 14px; 
  margin-top: 5px; 
  text-shadow: 2px 2px 0 #000; 
}

.loading-state, .empty-state { 
  padding: 40px; 
  text-align: center; 
  color: white; 
  text-shadow: 2px 2px 0 #000; 
  font-size: 16px;
}
.empty-icon { width: 64px; height: 64px; margin-bottom: 20px; }

.pagination { display: flex; justify-content: center; gap: 15px; margin-top: 20px; }
.btn-page { padding: 8px 16px; font-size: 14px; }
.page-info { padding: 8px 16px; font-size: 14px; cursor: default; }
</style>
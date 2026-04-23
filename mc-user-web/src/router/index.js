import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页 - 材质工坊' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录 - 材质工坊' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册 - 材质工坊' }
  },
  {
    path: '/generate',
    name: 'Generate',
    component: () => import('../views/Generate.vue'),
    meta: { title: '生成材质包 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/my-tasks',
    name: 'MyTasks',
    component: () => import('../views/MyTasks.vue'),
    meta: { title: '我的任务 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/result/:taskId',
    name: 'Result',
    component: () => import('../views/Result.vue'),
    meta: { title: '查看结果 - 材质工坊' }
  },
  {
    path: '/vip-factory',
    name: 'VipFactory',
    component: () => import('../views/Result.vue'),
    meta: { title: '订阅图片分割工厂 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/texture-pack-editor',
    name: 'TexturePackEditor',
    component: () => import('../views/TexturePackEditor.vue'),
    meta: { title: '材质包编辑器 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/recharge',
    name: 'Recharge',
    component: () => import('../views/Recharge.vue'),
    meta: { title: '积分充值 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/recharge/result',
    name: 'RechargeResult',
    component: () => import('../views/RechargeResult.vue'),
    meta: { title: '支付结果 - 材质工坊', requiresAuth: true }
  },
  {
    path: '/texture-converter',
    name: 'TextureConverter',
    component: () => import('../views/TexturePackConverter.vue'),
    meta: { title: '材质包版本转换器 - 材质工坊' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '材质工坊'

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      ElMessage.warning('请先登录后再访问该页面')
      next({
        path: '/login',
        query: { redirect: to.fullPath } // 登录后跳转回原页面
      })
      return
    }
  }

  next()
})

export default router

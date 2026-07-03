import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/chat'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/chat',
      name: 'Chat',
      component: () => import('../views/ChatView.vue')
    },
    {
      path: '/ats',
      name: 'Ats',
      component: () => import('../views/AtsView.vue')
    },
    {
      path: '/jd',
      name: 'Jd',
      component: () => import('../views/JdView.vue')
    },
    {
      path: '/optimize',
      name: 'Optimize',
      component: () => import('../views/OptimizeView.vue')
    },
    {
      path: '/resume',
      name: 'Resume',
      component: () => import('../views/ResumeView.vue')
    }
  ]
})

/**
 * 全局前置守卫
 * 每次路由切换前检查：有没有 token
 *   - 有 token → 放行
 *   - 没 token → 跳转登录页（但登录页本身不需要拦截）
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

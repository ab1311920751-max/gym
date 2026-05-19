import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/home',
    children: [
      { path: 'home', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'course', name: 'Course', component: () => import('../views/Course.vue') },
      { path: 'my-booking', name: 'MyBooking', component: () => import('../views/MyBooking.vue') },
      { path: 'wallet', name: 'Wallet', component: () => import('../views/Wallet.vue') },
      {
      path: '/pay/success',
      name: 'PaySuccess',
      component: () => import('../views/PaySuccess.vue'),
      meta: { title: '支付结果' }
  },
      // 管理页面
      { path: 'admin-course', name: 'AdminCourse', component: () => import('../views/AdminCourse.vue') },
      { path: 'admin-user', name: 'AdminUser', component: () => import('../views/AdminUser.vue') }
    ]
  },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 🛡️ 路由守卫
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 1. 白名单：登录页直接放行
  if (to.path === '/login') {
    next()
    return
  }

  // 2. 未登录：踢回登录页
  if (!user) {
    next('/login')
    return
  }

  // 3. 🔒 权限拦截：普通用户试图访问管理员页面
  if (to.path.startsWith('/admin-') && user.role !== 'admin') {
    ElMessage.error('无权访问：该页面仅限管理员查看')
    next('/home') // 强制跳转回首页
    return
  }

  // 4. 放行
  next()
})

export default router
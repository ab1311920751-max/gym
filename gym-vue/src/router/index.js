import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'
import { ElMessage } from 'element-plus'

// 解析 JWT payload 中的 exp 字段，判断 token 是否已过期
function isTokenExpired(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        return !payload.exp || payload.exp * 1000 < Date.now()
    } catch {
        return true
    }
}

const routes = [
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            { path: 'home',        name: 'Home',        component: () => import('../views/Home.vue') },
            { path: 'course',      name: 'Course',      component: () => import('../views/Course.vue') },
            { path: 'course/:id',  name: 'CourseDetail',component: () => import('../views/CourseDetail.vue') },
            { path: 'my-booking',  name: 'MyBooking',   component: () => import('../views/MyBooking.vue') },
            { path: 'wallet',      name: 'Wallet',      component: () => import('../views/Wallet.vue') },
            { path: 'profile',     name: 'Profile',     component: () => import('../views/Profile.vue') },
            { path: 'ai-chat',     name: 'AiChat',      component: () => import('../views/AiChat.vue') },
            {
                path: '/pay/success',
                name: 'PaySuccess',
                component: () => import('../views/PaySuccess.vue'),
                meta: { title: '支付结果' }
            },
            // 管理员页面：命名必须以 admin- 开头，否则会绕过下方的权限拦截
            { path: 'admin-course', name: 'AdminCourse', component: () => import('../views/AdminCourse.vue') },
            { path: 'admin-user',   name: 'AdminUser',   component: () => import('../views/AdminUser.vue') }
        ]
    },
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

/**
 * 全局路由守卫，四道校验按顺序执行：
 *
 * 1. 白名单：/login 直接放行
 * 2. 登录态校验：token 或 user 任一缺失则清除残留并跳登录
 * 3. token 有效期校验：解析 JWT payload 的 exp，过期则跳登录
 * 4. 权限校验：/admin-* 路由仅 role=admin 可访问
 *
 * 后端 JwtInterceptor + Axios 401 拦截器是第二道防线，前端守卫只负责 UX 层拦截。
 */
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    // 1. 白名单
    if (to.path === '/login') {
        next()
        return
    }

    // 2. 登录态校验：token 和 user.id 缺一不可
    if (!token || !user || !user.id) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        next('/login')
        return
    }

    // 3. token 过期校验（前端本地判断，减少一次无效请求）
    if (isTokenExpired(token)) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.warning('登录已过期，请重新登录')
        next('/login')
        return
    }

    // 4. 管理员权限校验
    if (to.path.startsWith('/admin-') && user.role !== 'admin') {
        ElMessage.error('无权访问：该页面仅限管理员查看')
        next('/home')
        return
    }

    next()
})

export default router

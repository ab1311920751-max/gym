<template>
  <div class="app-layout">
    <el-container class="main-container">
      <!-- 左侧侧边栏 -->
      <el-aside width="220px" class="sidebar-container">
        <div class="sidebar-logo">
          <div class="logo-mark">
            <span class="mark-emoji">🏋️</span>
          </div>
          <div class="logo-text">
            <div class="logo-title">POWER FITNESS</div>
            <div class="logo-sub">课程预约系统</div>
          </div>
        </div>

        <el-menu
          router
          :default-active="activeMenu"
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#ff7a2f"
          class="el-menu-vertical"
          :unique-opened="true"
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>

          <el-menu-item index="/course">
            <el-icon><Calendar /></el-icon>
            <span>预约课程</span>
          </el-menu-item>

          <el-menu-item index="/my-booking">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>

          <el-menu-item index="/wallet">
            <el-icon><Wallet /></el-icon>
            <span>我的钱包</span>
          </el-menu-item>

          <el-sub-menu index="admin" v-if="user.role === ROLE.ADMIN">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin-user">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin-course">
              <el-icon><Tickets /></el-icon>
              <span>课程管理</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 右侧主体 -->
      <el-container>
        <el-header class="navbar">
          <div class="navbar-left">
            <el-tag
              v-if="user.role === ROLE.ADMIN"
              type="danger"
              effect="dark"
              size="small"
              class="role-badge"
            >
              <el-icon><Lock /></el-icon>
              <span>管理员模式</span>
            </el-tag>
            <span v-else class="welcome-msg">
              欢迎回来，<b>{{ user.username }}</b>
            </span>
          </div>

          <div class="navbar-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="avatar-wrapper">
                <div class="user-avatar">
                  {{ user.username ? user.username.charAt(0).toUpperCase() : 'U' }}
                </div>
                <span class="username">{{ user.username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="app-main">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  HomeFilled,
  Calendar,
  List,
  Wallet,
  Setting,
  ArrowDown,
  User,
  Lock,
  SwitchButton,
  Tickets
} from '@element-plus/icons-vue'
import { ROLE } from '../constants/role'

const router = useRouter()
const route = useRoute()
const user = ref({})

const activeMenu = computed(() => route.path)

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) {
    router.push('/login')
  } else {
    try {
      user.value = JSON.parse(userStr)
    } catch (e) {
      console.error('用户信息解析失败', e)
      logout()
    }
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    logout()
  } else if (command === 'profile') {
    router.push('/wallet')
  }
}

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.main-container {
  height: 100%;
}

/* 侧边栏 */
.sidebar-container {
  background-color: #1f2d3d;
  height: 100%;
  box-shadow: 2px 0 8px rgba(0, 21, 41, 0.25);
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

.sidebar-logo {
  height: 64px;
  background: #18222f;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  cursor: pointer;
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 9px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 107, 26, 0.4);
  flex-shrink: 0;
}

.mark-emoji {
  font-size: 20px;
  line-height: 1;
}

.logo-text {
  flex: 1;
  min-width: 0;
}

.logo-title {
  color: #fff;
  font-weight: 800;
  font-size: 15px;
  letter-spacing: 1px;
  line-height: 1.2;
}

.logo-sub {
  color: #8a98aa;
  font-size: 11px;
  margin-top: 2px;
  letter-spacing: 0.5px;
}

.el-menu-vertical {
  border: none;
  width: 100%;
  padding-top: 8px;
}

:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 6px;
}

:deep(.el-menu-item:hover) {
  background-color: #18222f !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #2a3a4f !important;
  color: #ff7a2f !important;
}

:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 6px;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #18222f !important;
}

/* 顶部导航 */
.navbar {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
  border-bottom: 1px solid #f0f0f0;
}

.role-badge {
  margin-right: 5px;
  font-weight: 600;
  letter-spacing: 1px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.welcome-msg {
  color: #606266;
  font-size: 14px;
}

.welcome-msg b {
  color: #ff7a2f;
  font-weight: 600;
}

.navbar-right {
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 8px;
  transition: background 0.2s;
}

.avatar-wrapper:hover {
  background: #fff5ee;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  border-radius: 8px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 10px;
  font-size: 15px;
  box-shadow: 0 2px 6px rgba(255, 107, 26, 0.35);
}

.username {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-right: 5px;
}

/* 主内容区 */
.app-main {
  background-color: #f5f7fa;
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
  position: relative;
}

/* 切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(12px);
}
</style>

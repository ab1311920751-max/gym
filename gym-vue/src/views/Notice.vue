<template>
  <div class="page-wrap">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon><Bell /></el-icon>
        <span>系统公告</span>
      </h2>
      <p class="page-subtitle">健身房最新动态与通知</p>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && notices.length === 0" description="暂无公告" />

      <el-card
        v-for="item in notices"
        :key="item.id"
        shadow="hover"
        class="notice-card"
      >
        <div class="notice-header">
          <span class="notice-title">{{ item.title }}</span>
          <span class="notice-time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div class="notice-content">{{ item.content }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { listNotices } from '../api/notice'

const loading = ref(false)
const notices = ref([])

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').slice(0, 16)
}

const load = async () => {
  loading.value = true
  try {
    const res = await listNotices()
    notices.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.page-wrap {
  max-width: 860px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
}

.page-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.notice-card {
  margin-bottom: 16px;
  border-radius: 10px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notice-time {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
  margin-left: 12px;
}

.notice-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>

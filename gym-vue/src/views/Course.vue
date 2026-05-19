<template>
  <div class="course-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><Calendar /></el-icon>
          <span>热门课程</span>
        </h2>
        <p class="page-subtitle">VIP 会员享受折扣 · 分布式锁防超卖保护</p>
      </div>
      <el-input
        v-model="keyword"
        placeholder="搜索课程名 / 教练"
        class="search-input"
        clearable
        :prefix-icon="Search"
      />
    </div>

    <div v-if="loading" class="grid">
      <el-card v-for="i in 8" :key="i" shadow="never" class="skeleton-card">
        <el-skeleton :rows="4" animated />
      </el-card>
    </div>

    <el-empty
      v-else-if="filteredList.length === 0"
      description="暂无可预约课程"
      class="empty-block"
    />

    <div v-else class="grid">
      <el-card
        v-for="row in filteredList"
        :key="row.id"
        shadow="hover"
        class="course-card"
        :class="{ 'is-disabled': row.stock <= 0 || isCourseExpired(row.startTime) }"
      >
        <div class="card-top">
          <div class="course-icon">
            <el-icon :size="22"><Basketball /></el-icon>
          </div>
          <div class="course-status">
            <el-tag v-if="row.stock <= 0" type="info" size="small">已售罄</el-tag>
            <el-tag
              v-else-if="isCourseExpired(row.startTime)"
              type="info"
              size="small"
              >已结束</el-tag
            >
            <el-tag v-else-if="row.stock < LOW_STOCK_THRESHOLD" type="danger" size="small">
              仅剩 {{ row.stock }} 位
            </el-tag>
            <el-tag v-else type="success" size="small">{{ row.stock }} 位可约</el-tag>
          </div>
        </div>

        <div class="course-name">{{ row.name }}</div>
        <div class="course-desc">{{ row.content || row.description || '暂无简介' }}</div>

        <div class="meta-line">
          <el-icon><User /></el-icon>
          <span>{{ row.coach || '待定' }}</span>
        </div>
        <div class="meta-line">
          <el-icon><Clock /></el-icon>
          <span>{{ formatTime(row.startTime) }}</span>
        </div>

        <div class="stock-bar">
          <div
            class="stock-fill"
            :style="{
              width: `${stockPercent(row)}%`,
              background: stockColor(row)
            }"
          ></div>
        </div>

        <div class="card-footer">
          <div class="price">
            <span class="price-symbol">￥</span>
            <span class="price-num">{{ row.price }}</span>
          </div>
          <el-button
            type="primary"
            :loading="row.loading"
            :disabled="row.stock <= 0 || isCourseExpired(row.startTime)"
            @click="handleBook(row)"
          >
            {{ getBtnText(row) }}
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Calendar,
  Clock,
  User,
  Search,
  Basketball
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { listCourses } from '../api/course'
import { createBooking } from '../api/booking'
import { LOW_STOCK_THRESHOLD } from '../constants/booking'

const router = useRouter()
const loading = ref(true)
const list = ref([])
const keyword = ref('')

const filteredList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const kw = keyword.value.trim().toLowerCase()
  return list.value.filter(
    (row) =>
      (row.name || '').toLowerCase().includes(kw) ||
      (row.coach || '').toLowerCase().includes(kw)
  )
})

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await listCourses()
    list.value = (res.data || []).map((item) => ({ ...item, loading: false }))
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleBook = async (row) => {
  if (!localStorage.getItem('user')) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  row.loading = true
  try {
    const res = await createBooking({ courseId: row.id })
    if (res.code === '200') {
      try {
        await ElMessageBox.confirm(
          `抢课成功！订单号：${res.data}，请尽快支付。`,
          '恭喜',
          { confirmButtonText: '去支付', cancelButtonText: '稍后', type: 'success' }
        )
        router.push('/my-booking')
      } catch (_) {
        // 用户点了「稍后」
      }
      loadCourses()
    }
  } catch (e) {
    console.error('抢课失败', e)
  } finally {
    row.loading = false
  }
}

const formatTime = (val) => (val ? dayjs(val).format('MM-DD HH:mm') : '时间待定')

const isCourseExpired = (timeStr) => {
  if (!timeStr) return false
  return dayjs(timeStr).isBefore(dayjs())
}

const getBtnText = (row) => {
  if (row.stock <= 0) return '已售罄'
  if (isCourseExpired(row.startTime)) return '已结束'
  return '立即抢购'
}

const stockPercent = (row) => {
  if (!row.capacity || row.capacity <= 0) return 0
  const pct = (row.stock / row.capacity) * 100
  return Math.max(0, Math.min(100, pct))
}

const stockColor = (row) => {
  if (row.stock <= 0) return '#dcdfe6'
  if (row.stock < LOW_STOCK_THRESHOLD) return '#f56c6c'
  return '#ff7a2f'
}

onMounted(() => loadCourses())
</script>

<style scoped>
.course-page {
  padding: 4px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-subtitle {
  color: #909399;
  font-size: 13px;
  margin: 6px 0 0;
}

.search-input {
  width: 280px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.course-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: all 0.25s;
}

.course-card:hover {
  transform: translateY(-3px);
  border-color: #ffcdb0;
  box-shadow: 0 10px 24px rgba(255, 122, 47, 0.12);
}

.course-card.is-disabled {
  opacity: 0.65;
}

.course-card :deep(.el-card__body) {
  padding: 20px !important;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.course-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fff0e6, #ffe0cc);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff7a2f;
}

.course-name {
  font-size: 17px;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-desc {
  font-size: 12px;
  color: #909399;
  height: 36px;
  line-height: 18px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 12px;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.stock-bar {
  height: 6px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin: 14px 0;
}

.stock-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  color: #ff7a2f;
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  font-weight: 600;
}

.price-num {
  font-size: 22px;
  font-weight: 700;
  margin-left: 2px;
}

.skeleton-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
}

.empty-block {
  margin: 60px auto;
}
</style>

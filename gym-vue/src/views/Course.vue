<template>
  <div class="course-page">
    <!-- 顶部：标题 + 关键词搜索 -->
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

    <!-- 分类 Tab -->
    <div class="category-bar">
      <el-tabs v-model="activeCategory" @tab-change="resetPage">
        <el-tab-pane
          v-for="cat in COURSE_CATEGORIES"
          :key="cat.value"
          :label="cat.label"
          :name="cat.value"
        />
      </el-tabs>
    </div>

    <!-- 状态筛选 + 排序 -->
    <div class="filter-bar">
      <div class="filter-left">
        <span class="filter-label">状态：</span>
        <el-radio-group v-model="statusFilter" size="small" @change="resetPage">
          <el-radio-button
            v-for="opt in STATUS_OPTIONS"
            :key="opt.value"
            :label="opt.value"
          >{{ opt.label }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-right">
        <span class="filter-label">排序：</span>
        <el-select v-model="sortBy" size="small" style="width: 120px" @change="resetPage">
          <el-option
            v-for="opt in SORT_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>
    </div>

    <!-- 加载骨架 -->
    <div v-if="loading" class="grid">
      <el-card v-for="i in 8" :key="i" shadow="never" class="skeleton-card">
        <el-skeleton :rows="4" animated />
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else-if="filteredList.length === 0"
      description="暂无符合条件的课程"
      class="empty-block"
    />

    <!-- 课程网格 -->
    <div v-else class="grid">
      <el-card
        v-for="row in pagedList"
        :key="row.id"
        shadow="hover"
        class="course-card"
        :class="{ 'is-disabled': row.stock <= 0 || isCourseExpired(row.startTime) }"
        @click="goDetail(row)"
      >
        <div class="card-top">
          <div class="course-icon">
            <el-icon :size="22"><Basketball /></el-icon>
          </div>
          <div class="card-top-right">
            <el-tag v-if="row.category" size="small" type="warning" effect="plain" class="category-tag">
              {{ row.category }}
            </el-tag>
            <el-tag v-if="row.stock <= 0" type="info" size="small">已售罄</el-tag>
            <el-tag
              v-else-if="isCourseExpired(row.startTime)"
              type="info"
              size="small"
            >已结束</el-tag>
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
            @click.stop="handleBook(row)"
          >
            {{ getBtnText(row) }}
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-if="filteredList.length > PAGE_SIZE" class="pagination-bar">
      <el-pagination
        :current-page="currentPage"
        :page-size="PAGE_SIZE"
        :total="filteredList.length"
        layout="prev, pager, next, total"
        background
        @current-change="(val) => (currentPage = val)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
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
import { COURSE_CATEGORIES, STATUS_OPTIONS, SORT_OPTIONS } from '../constants/course'

const router = useRouter()
const goDetail = (row) => router.push(`/course/${row.id}`)
const loading = ref(true)
const list = ref([])
const keyword = ref('')
const activeCategory = ref('')
const statusFilter = ref('')
const sortBy = ref('time')
const currentPage = ref(1)
const PAGE_SIZE = 8

const resetPage = () => { currentPage.value = 1 }

const filteredList = computed(() => {
  let result = list.value

  // 关键词
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    result = result.filter(
      (r) =>
        (r.name || '').toLowerCase().includes(kw) ||
        (r.coach || '').toLowerCase().includes(kw)
    )
  }

  // 分类
  if (activeCategory.value) {
    result = result.filter((r) => r.category === activeCategory.value)
  }

  // 状态
  if (statusFilter.value === 'available') {
    result = result.filter((r) => r.stock > 0 && !isCourseExpired(r.startTime))
  } else if (statusFilter.value === 'soldOut') {
    result = result.filter((r) => r.stock <= 0)
  } else if (statusFilter.value === 'expired') {
    result = result.filter((r) => isCourseExpired(r.startTime))
  }

  // 排序
  if (sortBy.value === 'price') {
    result = [...result].sort((a, b) => (a.price || 0) - (b.price || 0))
  } else {
    result = [...result].sort((a, b) => {
      if (!a.startTime) return 1
      if (!b.startTime) return -1
      return dayjs(a.startTime).unix() - dayjs(b.startTime).unix()
    })
  }

  return result
})

const pagedList = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredList.value.slice(start, start + PAGE_SIZE)
})

watch([keyword, activeCategory, statusFilter, sortBy], resetPage)

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
  margin-bottom: 12px;
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

/* 分类 Tab */
.category-bar {
  margin-bottom: 0;
  border-bottom: none;
}

.category-bar :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.category-bar :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.category-bar :deep(.el-tabs__item) {
  font-size: 14px;
  padding: 0 18px;
}

/* 状态 + 排序栏 */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 0 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

/* 课程网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.course-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  transition: all 0.25s;
  cursor: pointer;
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
  align-items: flex-start;
  margin-bottom: 14px;
}

.card-top-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.category-tag {
  font-size: 11px;
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
  flex-shrink: 0;
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

.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}
</style>

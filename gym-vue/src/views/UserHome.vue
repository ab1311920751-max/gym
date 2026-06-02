<template>
  <div class="user-home">
    <!-- Hero 轮播 -->
    <el-carousel height="360px" :interval="4000" arrow="always">
      <el-carousel-item v-for="(item, idx) in banners" :key="idx">
        <div class="carousel-slide" :style="{ backgroundImage: `url(${item.imageUrl})` }">
          <div class="slide-overlay"></div>
          <div class="slide-content">
            <h2 class="slide-title">{{ item.title }}</h2>
            <p class="slide-sub">{{ item.subtitle }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 特色亮点 -->
    <el-row :gutter="16" class="feature-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, idx) in features" :key="idx">
        <el-card shadow="hover" class="feature-card">
          <div class="feature-icon">{{ item.icon }}</div>
          <div class="feature-title">{{ item.title }}</div>
          <div class="feature-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 热门课程 -->
    <div class="section-header">
      <h3>热门课程推荐</h3>
      <el-button type="primary" link @click="goCourse">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
    </div>
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8" v-for="course in hotCourses" :key="course.id">
        <el-card shadow="hover" class="course-card" @click="goDetail(course)">
          <div class="course-cover">{{ course.emoji }}</div>
          <div class="course-body">
            <div class="course-name">{{ course.name }}</div>
            <div class="course-meta">
              <span><el-icon><User /></el-icon> {{ course.coach || '专业教练' }}</span>
              <span><el-icon><Clock /></el-icon> {{ course.time }}</span>
            </div>
            <div class="course-footer">
              <span class="course-price">￥{{ course.price }}</span>
              <el-tag v-if="course.stock > 0" type="success" size="small">余 {{ course.stock }} 位</el-tag>
              <el-tag v-else type="info" size="small">已约满</el-tag>
            </div>
            <el-button
              type="primary"
              class="booking-btn"
              :disabled="course.stock <= 0"
              @click.stop="goCourse"
            >
              立即预约
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, User, Clock } from '@element-plus/icons-vue'
import { listCourses } from '../api/course'
import { listBanners } from '../api/banner'

const router = useRouter()

const banners = ref([])

const features = [
  { icon: '💪', title: '专业器械', desc: '进口高端健身器材' },
  { icon: '👨‍🏫', title: '金牌教练', desc: '5年以上教学经验' },
  { icon: '📅', title: '灵活排课', desc: '早中晚多时段可选' },
  { icon: '💰', title: '超值会员', desc: 'VIP享专属折扣优惠' }
]

const hotCourses = ref([])

const goCourse = () => router.push('/course')
const goDetail = (course) => router.push(`/course/${course.id}`)

onMounted(async () => {
  try {
    const bRes = await listBanners()
    banners.value = bRes.data || []
  } catch (e) {
    console.error('加载轮播图失败', e)
  }
  try {
    const res = await listCourses()
    const courses = (res.data || []).filter(c => c.stock > 0).slice(0, 6)
    hotCourses.value = courses.map((c, i) => ({
      ...c,
      emoji: ['🏃', '🧘', '🚴', '🏊', '🥊', '🎯'][i % 6],
      time: formatTime(c.startTime)
    }))
  } catch (e) {
    console.error('加载课程失败', e)
  }
})


const formatTime = (str) => {
  if (!str) return ''
  const d = new Date(str)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.user-home {
  padding: 4px;
}

/* 轮播 */
.carousel-slide {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
  color: #fff;
}

.slide-overlay {
  position: absolute;
  inset: 0;
  background: rgba(20, 20, 28, 0.55);
}

.slide-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  animation: fadeInUp 0.6s ease;
}

.slide-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.slide-sub {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

:deep(.el-carousel__indicators) {
  bottom: 20px;
}

/* 特色卡片 */
.feature-row {
  margin-top: 20px;
}

.feature-card {
  text-align: center;
  border: none;
  border-radius: 10px;
  margin-bottom: 16px;
}

.feature-card :deep(.el-card__body) {
  padding: 28px 16px;
}

.feature-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 13px;
  color: #909399;
}

/* 课程列表 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24px 0 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
}

.course-card {
  border: none;
  border-radius: 10px;
  margin-bottom: 16px;
  overflow: hidden;
  cursor: pointer;
}

.course-card :deep(.el-card__body) {
  padding: 0;
}

.course-cover {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  background: linear-gradient(135deg, #fff5ee 0%, #ffe8d6 100%);
}

.course-body {
  padding: 16px;
}

.course-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 10px;
}

.course-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.course-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.course-price {
  font-size: 20px;
  font-weight: 700;
  color: #ff7a2f;
}

.booking-btn {
  width: 100%;
}
</style>

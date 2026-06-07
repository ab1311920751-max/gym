<template>
  <div class="detail-page">
    <!-- 加载骨架 -->
    <div v-if="loading" class="skeleton-wrap">
      <el-skeleton animated :rows="2" />
      <el-skeleton animated :rows="6" style="margin-top: 20px" />
    </div>

    <!-- 错误/不存在 -->
    <el-result
      v-else-if="error"
      icon="error"
      title="课程不存在"
      sub-title="该课程可能已下架或链接地址有误"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回课程列表</el-button>
      </template>
    </el-result>

    <!-- 正常内容 -->
    <template v-else-if="course">
      <!-- 返回栏 -->
      <div class="back-bar">
        <el-button text :icon="ArrowLeft" @click="goBack">返回课程列表</el-button>
      </div>

      <el-row :gutter="20">
        <!-- 左侧主内容 -->
        <el-col :xs="24" :md="16">
          <!-- 主卡片 -->
          <el-card shadow="never" class="main-card">
            <div class="hero">
              <div class="hero-icon">
                <span class="emoji">{{ emoji }}</span>
              </div>
              <div class="hero-info">
                <div class="title-row">
                  <h2 class="course-title">{{ course.name }}</h2>
                  <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
                </div>
                <p class="course-desc">{{ course.content || course.description || '暂无简介' }}</p>
                <div class="meta-row">
                  <span><el-icon><User /></el-icon> {{ course.coach || '待定' }}</span>
                  <span><el-icon><Clock /></el-icon> {{ formatTime(course.startTime) }}</span>
                  <span><el-icon><Histogram /></el-icon> {{ course.stock }}/{{ course.capacity }} 人</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 课程详情 -->
          <el-card shadow="never" class="content-card">
            <template #header>
              <span class="card-header-title">课程详情</span>
            </template>
            <div class="content-body">
              {{ course.content || course.description || '暂无详细介绍' }}
            </div>
          </el-card>

          <!-- 评论区 -->
          <el-card shadow="never" class="comment-card">
            <template #header>
              <span class="card-header-title">评论（{{ totalComments }} 条）</span>
            </template>

            <!-- 输入区 -->
            <div class="comment-input-wrap">
              <div v-if="replyTarget" class="reply-tip">
                回复 <strong>@{{ replyTarget.username }}</strong>
                <el-button text size="small" class="cancel-reply" @click="replyTarget = null">× 取消</el-button>
              </div>
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                :placeholder="replyTarget ? `回复 @${replyTarget.username}...` : '说点什么...'"
                maxlength="500"
                show-word-limit
              />
              <div class="input-actions">
                <el-button type="primary" size="small" @click="submitComment">发布</el-button>
              </div>
            </div>

            <!-- 评论列表 -->
            <div v-if="comments.length === 0" class="empty-comments">暂无评论，来发表第一条吧！</div>
            <div v-else class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <!-- 顶级评论 -->
                <div class="comment-meta">
                  <span class="comment-author">{{ comment.nickname || comment.username }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  <div class="comment-actions">
                    <el-button text size="small" @click="handleReply(comment)">回复</el-button>
                    <el-button
                      v-if="canDelete(comment)"
                      text
                      size="small"
                      type="danger"
                      @click="handleDelete(comment.id)"
                    >删除</el-button>
                  </div>
                </div>
                <div class="comment-content">{{ comment.content }}</div>

                <!-- 子评论 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="comment-meta">
                      <span class="comment-author">{{ reply.nickname || reply.username }}</span>
                      <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                      <div class="comment-actions">
                        <el-button text size="small" @click="handleReply(reply)">回复</el-button>
                        <el-button
                          v-if="canDelete(reply)"
                          text
                          size="small"
                          type="danger"
                          @click="handleDelete(reply.id)"
                        >删除</el-button>
                      </div>
                    </div>
                    <div class="comment-content">
                      <span v-if="reply.parentUsername" class="reply-at">@{{ reply.parentUsername }} </span>
                      {{ reply.content }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧预约栏 -->
        <el-col :xs="24" :md="8">
          <el-card shadow="never" class="booking-card">
            <div class="info-list">
              <div class="info-item">
                <span class="label">课程名称</span>
                <span class="value">{{ course.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">授课教练</span>
                <span class="value">{{ course.coach || '待定' }}</span>
              </div>
              <div class="info-item">
                <span class="label">上课时间</span>
                <span class="value">{{ formatTime(course.startTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">课程容量</span>
                <span class="value">{{ course.capacity }} 人</span>
              </div>
              <div class="info-item">
                <span class="label">剩余名额</span>
                <span class="value" :class="{ 'low-stock': isLowStock }">{{ course.stock }} 位</span>
              </div>
            </div>

            <!-- 名额进度条 -->
            <div class="stock-bar">
              <div
                class="stock-fill"
                :style="{ width: stockPercent + '%', background: stockColor }"
              ></div>
            </div>

            <!-- 价格与操作 -->
            <div class="price-row">
              <span class="price">
                <span class="price-symbol">￥</span>
                <span class="price-num">{{ course.price }}</span>
              </span>
              <span class="price-note">VIP 享折扣</span>
            </div>

            <el-button
              type="primary"
              size="large"
              class="book-btn"
              :loading="booking"
              :disabled="!isBookable"
              @click="handleBook"
            >
              {{ bookBtnText }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowLeft,
  User,
  Clock,
  Histogram
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getCourse } from '../api/course'
import { createBooking } from '../api/booking'
import { listComments, addComment, deleteComment } from '../api/comment'
import { LOW_STOCK_THRESHOLD } from '../constants/booking'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const error = ref(false)
const course = ref(null)
const booking = ref(false)

const comments = ref([])
const commentContent = ref('')
const replyTarget = ref(null)

const currentUser = JSON.parse(localStorage.getItem('user') || '{}')

const totalComments = computed(() => {
  return comments.value.reduce((sum, c) => sum + 1 + (c.replies?.length || 0), 0)
})

const canDelete = (comment) =>
  currentUser.role === 'admin' || comment.userId === currentUser.id

const courseId = computed(() => route.params.id)

const emoji = computed(() => {
  const list = ['🏃', '🧘', '🚴', '🏊', '🥊', '🎯']
  return list[(course.value?.id || 0) % list.length]
})

const statusText = computed(() => {
  if (!course.value) return ''
  if (course.value.stock <= 0) return '已售罄'
  if (isCourseExpired(course.value.startTime)) return '已结束'
  if (course.value.stock < LOW_STOCK_THRESHOLD) return `仅剩 ${course.value.stock} 位`
  return '可预约'
})

const statusTagType = computed(() => {
  if (!course.value) return 'info'
  if (course.value.stock <= 0 || isCourseExpired(course.value.startTime)) return 'info'
  if (course.value.stock < LOW_STOCK_THRESHOLD) return 'danger'
  return 'success'
})

const isLowStock = computed(() => {
  if (!course.value) return false
  return course.value.stock > 0 && course.value.stock < LOW_STOCK_THRESHOLD
})

const stockPercent = computed(() => {
  if (!course.value || !course.value.capacity || course.value.capacity <= 0) return 0
  const pct = (course.value.stock / course.value.capacity) * 100
  return Math.max(0, Math.min(100, pct))
})

const stockColor = computed(() => {
  if (!course.value) return '#dcdfe6'
  if (course.value.stock <= 0) return '#dcdfe6'
  if (course.value.stock < LOW_STOCK_THRESHOLD) return '#f56c6c'
  return '#ff7a2f'
})

const isBookable = computed(() => {
  if (!course.value) return false
  return course.value.stock > 0 && !isCourseExpired(course.value.startTime)
})

const bookBtnText = computed(() => {
  if (!course.value) return '立即抢购'
  if (course.value.stock <= 0) return '已售罄'
  if (isCourseExpired(course.value.startTime)) return '已结束'
  return '立即抢购'
})

const formatTime = (val) => (val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '时间待定')

const isCourseExpired = (timeStr) => {
  if (!timeStr) return false
  return dayjs(timeStr).isBefore(dayjs())
}

const loadCourse = async () => {
  loading.value = true
  error.value = false
  try {
    const res = await getCourse(courseId.value)
    if (res.code === '200' && res.data) {
      course.value = res.data
    } else {
      error.value = true
    }
  } catch (e) {
    console.error('加载课程详情失败', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

const handleBook = async () => {
  if (!localStorage.getItem('user')) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  booking.value = true
  try {
    const res = await createBooking({ courseId: course.value.id })
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
      loadCourse()
    }
  } catch (e) {
    console.error('抢课失败', e)
  } finally {
    booking.value = false
  }
}

const goBack = () => router.push('/course')

const loadComments = async () => {
  try {
    const res = await listComments(courseId.value)
    if (res.code === '200') {
      comments.value = res.data || []
    }
  } catch (e) {
    console.error('加载评论失败', e)
  }
}

const submitComment = async () => {
  if (!localStorage.getItem('user')) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  try {
    const res = await addComment({
      courseId: course.value.id,
      content: commentContent.value.trim(),
      parentId: replyTarget.value?.id || null
    })
    if (res.code === '200') {
      commentContent.value = ''
      replyTarget.value = null
      await loadComments()
    }
  } catch (e) {
    console.error('发布评论失败', e)
  }
}

const handleReply = (comment) => {
  replyTarget.value = { id: comment.id, username: comment.nickname || comment.username }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteComment(id)
    if (res.code === '200') {
      ElMessage.success('已删除')
      await loadComments()
    }
  } catch (_) {
    // 用户取消
  }
}

onMounted(() => {
  loadCourse()
  loadComments()
})
</script>

<style scoped>
.detail-page {
  padding: 4px;
}

.skeleton-wrap {
  max-width: 800px;
}

/* 返回栏 */
.back-bar {
  margin-bottom: 16px;
}

/* 主卡片 */
.main-card {
  border: none;
  border-radius: 12px;
  margin-bottom: 20px;
}

.main-card :deep(.el-card__body) {
  padding: 24px;
}

.hero {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.hero-icon {
  width: 72px;
  height: 72px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fff0e6, #ffe0cc);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.emoji {
  font-size: 36px;
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.course-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
}

.course-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 14px;
}

.meta-row {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.meta-row span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 课程详情卡片 */
.content-card {
  border: none;
  border-radius: 12px;
}

.content-card :deep(.el-card__body) {
  padding: 24px;
}

.card-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2d3d;
}

.content-body {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 预约侧栏 */
.booking-card {
  border: none;
  border-radius: 12px;
  position: sticky;
  top: 20px;
}

.booking-card :deep(.el-card__body) {
  padding: 24px;
}

.info-list {
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f7fa;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-size: 13px;
  color: #909399;
}

.info-item .value {
  font-size: 14px;
  color: #1f2d3d;
  font-weight: 500;
}

.info-item .value.low-stock {
  color: #f56c6c;
}

/* 进度条 */
.stock-bar {
  height: 6px;
  background: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 20px;
}

.stock-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

/* 价格 */
.price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.price {
  color: #ff7a2f;
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 16px;
  font-weight: 600;
}

.price-num {
  font-size: 28px;
  font-weight: 700;
  margin-left: 2px;
}

.price-note {
  font-size: 12px;
  color: #c0c4cc;
}

.book-btn {
  width: 100%;
}

/* 评论区 */
.comment-card {
  border: none;
  border-radius: 12px;
  margin-top: 20px;
}

.comment-card :deep(.el-card__body) {
  padding: 24px;
}

.comment-input-wrap {
  margin-bottom: 24px;
}

.reply-tip {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.cancel-reply {
  padding: 0;
  color: #909399;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.empty-comments {
  text-align: center;
  color: #c0c4cc;
  font-size: 14px;
  padding: 24px 0;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  border-bottom: 1px solid #f5f7fa;
  padding-bottom: 16px;
}

.comment-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
}

.comment-time {
  font-size: 12px;
  color: #c0c4cc;
  flex: 1;
}

.comment-actions {
  display: flex;
  gap: 4px;
}

.comment-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.reply-at {
  color: #409eff;
  font-weight: 500;
}

.reply-list {
  margin-top: 12px;
  margin-left: 16px;
  padding-left: 16px;
  border-left: 2px solid #f0f2f5;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .title-row {
    justify-content: center;
  }

  .meta-row {
    justify-content: center;
  }
}
</style>

<template>
  <div class="my-booking-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">
          <el-icon><List /></el-icon>
          <span>我的课程订单</span>
        </h2>
        <p class="page-subtitle">查看与管理你的预约、支付、退款</p>
      </div>
    </div>

    <el-card shadow="never" class="content-card">
      <el-tabs v-model="activeTab" class="status-tabs">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane :label="`待支付 (${counts[BOOKING_STATUS.PENDING]})`" :name="String(BOOKING_STATUS.PENDING)" />
        <el-tab-pane :label="`已预约 (${counts[BOOKING_STATUS.PAID]})`" :name="String(BOOKING_STATUS.PAID)" />
        <el-tab-pane :label="`已取消 (${counts[BOOKING_STATUS.CANCELLED]})`" :name="String(BOOKING_STATUS.CANCELLED)" />
      </el-tabs>

      <el-table
        v-loading="loading"
        :data="filteredList"
        stripe
        empty-text="暂无订单"
        style="width: 100%"
      >
        <el-table-column prop="bookingNo" label="订单号" width="200">
          <template #default="{ row }">
            <span class="booking-no">{{ row.bookingNo }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="courseName" label="课程" min-width="160">
          <template #default="{ row }">
            <div class="course-cell">{{ row.courseName }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="coach" label="教练" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ row.coach || '待定' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="开课时间" width="170">
          <template #default="{ row }">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ row.startTimeDisplay || formatTime(row.startTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">
            <span class="muted">{{ formatTime(row.bookingTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="实付" width="100">
          <template #default="{ row }">
            <span class="price">￥{{ row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="BOOKING_STATUS_TAG_TYPE[row.status]" effect="light">
              {{ BOOKING_STATUS_LABEL[row.status] }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === BOOKING_STATUS.PENDING">
              <el-button type="primary" size="small" @click="handlePay(row.id)">
                余额支付
              </el-button>
              <el-button size="small" plain @click="handleAlipay(row.bookingNo)">
                支付宝
              </el-button>
              <el-button size="small" text @click="handleCancel(row.id)">
                取消
              </el-button>
            </template>

            <template v-else-if="row.status === BOOKING_STATUS.PAID">
              <el-button type="danger" size="small" plain @click="handleCancel(row.id)">
                取消预约
              </el-button>
            </template>

            <span v-else class="muted">不可操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { List, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { listMyBookings, payBooking, cancelBooking } from '../api/booking'
import { buildPayUrl } from '../api/alipay'
import {
  BOOKING_STATUS,
  BOOKING_STATUS_LABEL,
  BOOKING_STATUS_TAG_TYPE
} from '../constants/booking'

const list = ref([])
const loading = ref(true)
const activeTab = ref('all')

const counts = computed(() => {
  const c = {
    [BOOKING_STATUS.PENDING]: 0,
    [BOOKING_STATUS.PAID]: 0,
    [BOOKING_STATUS.CANCELLED]: 0
  }
  list.value.forEach((row) => {
    if (c[row.status] != null) c[row.status]++
  })
  return c
})

const filteredList = computed(() => {
  if (activeTab.value === 'all') return list.value
  const target = Number(activeTab.value)
  return list.value.filter((row) => row.status === target)
})

const load = async () => {
  loading.value = true
  try {
    const res = await listMyBookings()
    list.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handlePay = async (id) => {
  try {
    const res = await payBooking(id)
    if (res.code === '200') {
      ElMessage.success('余额支付成功')
      load()
      window.dispatchEvent(new Event('refresh-user'))
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAlipay = (bookingNo) => {
  window.location.href = buildPayUrl({ bookingNo })
}

const handleCancel = (id) => {
  ElMessageBox.confirm('确定要取消/退订吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '再想想',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await cancelBooking(id)
        if (res.code === '200') {
          ElMessage.success('操作成功')
          load()
          window.dispatchEvent(new Event('refresh-user'))
        }
      } catch (e) {
        console.error(e)
      }
    })
    .catch(() => {})
}

const formatTime = (val) => (val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '—')

onMounted(() => load())
</script>

<style scoped>
.my-booking-page {
  padding: 4px;
}

.page-header {
  margin-bottom: 16px;
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

.content-card {
  border: none;
  border-radius: 10px;
}

.status-tabs :deep(.el-tabs__item.is-active) {
  color: #ff7a2f;
}

.status-tabs :deep(.el-tabs__active-bar) {
  background-color: #ff7a2f;
}

.booking-no {
  font-family: Consolas, Menlo, monospace;
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 3px 8px;
  border-radius: 4px;
}

.course-cell {
  font-weight: 600;
  color: #1f2d3d;
}

.time-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 13px;
}

.muted {
  color: #909399;
  font-size: 12px;
}

.price {
  color: #ff7a2f;
  font-weight: 700;
  font-size: 15px;
}
</style>

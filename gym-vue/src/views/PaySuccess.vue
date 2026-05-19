<template>
  <div class="pay-success-container">
    <el-card class="box-card" shadow="never">
      <div class="content">
        <el-icon
          v-if="loading"
          class="is-loading"
          :size="56"
          color="#ff7a2f"
        ><Loading /></el-icon>
        <el-icon
          v-else-if="success"
          :size="56"
          color="#67C23A"
        ><CircleCheckFilled /></el-icon>
        <el-icon
          v-else
          :size="56"
          color="#F56C6C"
        ><CircleCloseFilled /></el-icon>

        <h2 class="title">{{ msg }}</h2>

        <div v-if="success" class="info-block">
          <div class="info-row" v-if="bookingNo">
            <span class="info-label">订单号</span>
            <span class="info-value">{{ bookingNo }}</span>
          </div>
          <div class="info-row" v-if="tradeNo">
            <span class="info-label">支付宝交易号</span>
            <span class="info-value">{{ tradeNo }}</span>
          </div>
        </div>

        <div class="actions">
          <el-button type="primary" @click="goHome">返回首页</el-button>
          <el-button @click="goOrders">查看订单</el-button>
          <el-button v-if="!success && !loading" type="warning" plain @click="retry">
            重试支付
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import {
  CircleCheckFilled,
  CircleCloseFilled,
  Loading
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const success = ref(false)
const msg = ref('正在确认支付结果...')
const bookingNo = ref('')
const tradeNo = ref('')

onMounted(() => {
  const params = route.query
  bookingNo.value = params.out_trade_no || ''
  tradeNo.value = params.trade_no || ''

  if (!bookingNo.value) {
    loading.value = false
    success.value = false
    msg.value = '参数异常，无法确认订单'
    return
  }

  request
    .get('/alipay/return', { params })
    .then((res) => {
      loading.value = false
      if (res.code === '200') {
        success.value = true
        msg.value = '支付成功'
      } else {
        success.value = false
        msg.value = '支付确认失败：' + (res.msg || '未知错误')
      }
    })
    .catch((err) => {
      loading.value = false
      success.value = false
      msg.value = '网络请求失败'
      console.error(err)
    })
})

const goHome = () => router.push('/')
const goOrders = () => router.push('/my-booking')
const retry = () => router.push('/my-booking')
</script>

<style scoped>
.pay-success-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.box-card {
  max-width: 500px;
  width: 100%;
  text-align: center;
  border-radius: 12px;
  border: none;
}

.box-card :deep(.el-card__body) {
  padding: 48px 32px;
}

.title {
  margin: 20px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
}

.info-block {
  margin-top: 24px;
  background: #f7f8fa;
  border-radius: 8px;
  padding: 16px 20px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.info-label {
  color: #909399;
}

.info-value {
  color: #1f2d3d;
  font-weight: 600;
  font-family: Consolas, Menlo, monospace;
}

.actions {
  margin-top: 32px;
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}
</style>

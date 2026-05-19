<template>
  <div class="wallet-page">
    <!-- 顶部：用户档案 + 余额 -->
    <el-row :gutter="20">
      <el-col :xs="24" :md="9">
        <el-card shadow="never" class="profile-card">
          <div class="profile-row">
            <div class="profile-avatar">
              {{ (user.username || 'U').charAt(0).toUpperCase() }}
            </div>
            <div class="profile-meta">
              <div class="profile-name">{{ user.username || '—' }}</div>
              <el-tag size="small" :type="VIP_TAG_TYPE[user.vipType || 0]" effect="light">
                {{ VIP_LABEL[user.vipType || 0] }}
              </el-tag>
            </div>
          </div>

          <div v-if="user.vipType > 0" class="vip-expire" :class="{ warn: expiring }">
            <el-icon><AlarmClock /></el-icon>
            <div>
              <div>到期时间</div>
              <div class="expire-time">{{ formatDate(user.vipExpireTime) }}</div>
              <div v-if="expiring" class="warn-text">即将过期，建议尽快续费</div>
            </div>
          </div>
          <div v-else class="vip-tip">
            暂未开通 VIP，开通后可享 9 折 / 8 折购课优惠
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="15">
        <el-card shadow="never" class="balance-card">
          <div class="balance-bg"></div>
          <div class="balance-content">
            <div class="balance-label">账户余额</div>
            <div class="balance-amount">
              <span class="balance-symbol">￥</span>
              <span class="balance-num">{{ formatMoney(user.balance) }}</span>
            </div>
            <el-button class="recharge-btn" size="large" @click="dialogVisible = true">
              <el-icon><Wallet /></el-icon>
              <span>支付宝充值</span>
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- VIP 卡片 -->
    <div class="vip-section">
      <h3 class="section-title">
        <el-icon><Medal /></el-icon>
        <span>会员专区</span>
        <span class="section-sub">支持续费叠加</span>
      </h3>

      <el-row :gutter="20">
        <el-col :xs="24" :md="12">
          <div
            class="vip-card vip-monthly"
            :class="{ 'is-active': user.vipType === VIP_TYPE.MONTHLY }"
          >
            <el-tag
              v-if="user.vipType === VIP_TYPE.MONTHLY"
              type="success"
              effect="dark"
              class="active-badge"
            >
              当前等级
            </el-tag>
            <div class="vip-card-head">
              <el-icon :size="32"><Calendar /></el-icon>
              <div class="vip-card-title">月卡 VIP</div>
            </div>
            <div class="vip-price">
              <span class="vip-price-symbol">￥</span>
              <span class="vip-price-num">{{ VIP_PRICE[VIP_TYPE.MONTHLY] }}</span>
              <span class="vip-price-unit">/ 月</span>
            </div>
            <ul class="vip-features">
              <li>全场课程 9 折优惠</li>
              <li>专属 VIP 标识</li>
              <li>优先抢课通道</li>
            </ul>
            <el-button class="vip-btn vip-btn-monthly" @click="handleBuyVip(VIP_TYPE.MONTHLY)">
              {{ user.vipType === VIP_TYPE.MONTHLY ? '立即续费 (+30天)' : '立即开通' }}
            </el-button>
          </div>
        </el-col>

        <el-col :xs="24" :md="12">
          <div
            class="vip-card vip-yearly"
            :class="{ 'is-active': user.vipType === VIP_TYPE.YEARLY }"
          >
            <el-tag
              v-if="user.vipType === VIP_TYPE.YEARLY"
              type="success"
              effect="dark"
              class="active-badge"
            >
              当前等级
            </el-tag>
            <div class="vip-card-head">
              <el-icon :size="32"><Trophy /></el-icon>
              <div class="vip-card-title">年卡 VIP</div>
            </div>
            <div class="vip-price">
              <span class="vip-price-symbol">￥</span>
              <span class="vip-price-num">{{ VIP_PRICE[VIP_TYPE.YEARLY] }}</span>
              <span class="vip-price-unit">/ 年</span>
            </div>
            <ul class="vip-features">
              <li>全场课程 8 折钜惠</li>
              <li>尊贵皇冠标识</li>
              <li>所有月卡权益</li>
            </ul>
            <el-button class="vip-btn vip-btn-yearly" @click="handleBuyVip(VIP_TYPE.YEARLY)">
              {{ user.vipType === VIP_TYPE.YEARLY ? '立即续费 (+365天)' : '升级 / 开通' }}
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="dialogVisible" title="支付宝充值" width="420px" align-center>
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-position="top">
        <el-form-item label="充值金额" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :max="9999"
            :step="100"
            size="large"
            style="width: 100%"
          />
        </el-form-item>
        <div class="recharge-hint">
          点击「前往支付」后将打开新窗口跳转支付宝沙箱付款页。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAlipay">前往支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Wallet,
  Medal,
  Calendar,
  Trophy,
  AlarmClock
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getUserById, buyVip } from '../api/user'
import { buildPayUrl, confirmRecharge } from '../api/alipay'
import { VIP_TYPE, VIP_LABEL, VIP_TAG_TYPE, VIP_PRICE } from '../constants/vip'

const route = useRoute()
const router = useRouter()
const user = ref({})
const dialogVisible = ref(false)
const rechargeFormRef = ref(null)

const rechargeForm = reactive({ amount: 100 })
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (!value || value <= 0) cb(new Error('金额必须大于 0'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

const expiring = computed(() => isExpiringSoon(user.value.vipExpireTime))

const formatMoney = (val) => (val == null ? '0.00' : Number(val).toFixed(2))

const formatDate = (dateStr) => {
  if (!dateStr) return '永久有效'
  return dayjs(dateStr).format('YYYY年MM月DD日 HH:mm')
}

const isExpiringSoon = (dateStr) => {
  if (!dateStr) return false
  const expireTime = dayjs(dateStr)
  const now = dayjs()
  return expireTime.diff(now, 'day') <= 7 && expireTime.isAfter(now)
}

const loadUser = async () => {
  const localUser = JSON.parse(localStorage.getItem('user') || '{}')
  if (!localUser.id) return
  try {
    const res = await getUserById(localUser.id)
    if (res.code === '200') {
      user.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAlipay = async () => {
  try {
    await rechargeFormRef.value.validate()
  } catch {
    return
  }
  const traceNo = Date.now() + Math.floor(Math.random() * 1000)
  const payUrl = buildPayUrl({
    subject: '健身房充值',
    traceNo,
    totalAmount: rechargeForm.amount
  })
  window.open(payUrl, '_blank')
  dialogVisible.value = false

  try {
    await ElMessageBox.confirm('支付完成后请点击确认刷新余额', '支付确认', {
      confirmButtonText: '我已支付',
      cancelButtonText: '遇到问题',
      type: 'success'
    })
    loadUser()
    window.dispatchEvent(new Event('refresh-user'))
  } catch {
    /* user cancelled */
  }
}

const checkPayCallback = async () => {
  if (route.query.pay !== 'success') return
  try {
    const res = await confirmRecharge({
      out_trade_no: route.query.out_trade_no,
      total_amount: route.query.total_amount
    })
    if (res.code === '200') {
      ElMessage.success(`充值 ${route.query.total_amount} 元到账成功`)
      loadUser()
      window.dispatchEvent(new Event('refresh-user'))
      router.replace('/wallet')
    }
  } catch (e) {
    console.error(e)
  }
}

const handleBuyVip = (type) => {
  const price = VIP_PRICE[type]
  if (Number(user.value.balance) < price) {
    ElMessage.warning('余额不足，请先充值')
    dialogVisible.value = true
    return
  }
  const actionText = user.value.vipType === type ? '续费' : '开通'

  ElMessageBox.confirm(`确认花费 ${price} 元${actionText}吗？`, '提示', {
    confirmButtonText: '确定扣款',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await buyVip({ vipType: type })
        ElMessage.success(`${actionText}成功`)
        loadUser()
        window.dispatchEvent(new Event('refresh-user'))
      } catch (e) {
        console.error(e)
      }
    })
    .catch(() => {})
}

onMounted(async () => {
  const localUser = JSON.parse(localStorage.getItem('user') || '{}')
  user.value = localUser

  if (route.query.pay === 'success' && localUser.id) {
    await checkPayCallback()
  }
  loadUser()
})
</script>

<style scoped>
.wallet-page {
  padding: 4px;
}

/* 用户档案卡片 */
.profile-card {
  border: none;
  border-radius: 10px;
  height: 100%;
  min-height: 220px;
}

.profile-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.profile-avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 107, 26, 0.3);
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
  margin-bottom: 4px;
}

.vip-expire {
  background: #fff5ee;
  border: 1px solid #ffe0cc;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 13px;
  color: #606266;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.vip-expire.warn {
  background: #fef0f0;
  border-color: #fbc4c4;
  color: #f56c6c;
}

.expire-time {
  font-weight: 600;
  color: #1f2d3d;
  margin-top: 2px;
}

.vip-expire.warn .expire-time {
  color: #f56c6c;
}

.warn-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}

.vip-tip {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px 14px;
  font-size: 13px;
  color: #909399;
}

/* 余额卡片 */
.balance-card {
  border: none;
  border-radius: 10px;
  height: 100%;
  min-height: 220px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
}

.balance-card :deep(.el-card__body) {
  padding: 32px !important;
  position: relative;
  z-index: 1;
}

.balance-bg {
  position: absolute;
  right: -40px;
  top: -40px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.balance-content {
  color: #fff;
}

.balance-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.balance-amount {
  display: flex;
  align-items: baseline;
  margin-bottom: 24px;
}

.balance-symbol {
  font-size: 22px;
  font-weight: 600;
}

.balance-num {
  font-size: 42px;
  font-weight: 700;
  margin-left: 4px;
  letter-spacing: 1px;
}

.recharge-btn {
  background: #fff;
  color: #ff7a2f;
  border: none;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.recharge-btn:hover {
  background: #fff0e6;
  color: #ff6b1a;
}

/* VIP 区 */
.vip-section {
  margin-top: 24px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2d3d;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-sub {
  font-size: 13px;
  color: #909399;
  font-weight: 400;
  margin-left: 4px;
}

.vip-card {
  position: relative;
  padding: 28px;
  border-radius: 12px;
  transition: all 0.3s;
  background: #fff;
  border: 1px solid #f0f0f0;
  margin-bottom: 16px;
  overflow: hidden;
}

.vip-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.vip-card.is-active {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9eb 0%, #fff 100%);
}

.active-badge {
  position: absolute;
  top: 16px;
  right: 16px;
}

.vip-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.vip-monthly .vip-card-head {
  color: #ff7a2f;
}

.vip-yearly .vip-card-head {
  color: #e6a23c;
}

.vip-card-title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2d3d;
}

.vip-price {
  display: flex;
  align-items: baseline;
  color: #1f2d3d;
  margin-bottom: 14px;
}

.vip-price-symbol {
  font-size: 18px;
  font-weight: 600;
}

.vip-price-num {
  font-size: 36px;
  font-weight: 700;
  margin: 0 4px;
}

.vip-price-unit {
  font-size: 14px;
  color: #909399;
}

.vip-features {
  list-style: none;
  padding: 0;
  margin: 0 0 18px;
  color: #606266;
  font-size: 14px;
}

.vip-features li {
  padding: 6px 0 6px 18px;
  position: relative;
}

.vip-features li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #67c23a;
  font-weight: 700;
}

.vip-btn {
  width: 100%;
  height: 40px;
}

.vip-btn-monthly {
  background: linear-gradient(135deg, #ff8c42 0%, #ff6b1a 100%);
  color: #fff;
  border: none;
}

.vip-btn-monthly:hover {
  opacity: 0.92;
  color: #fff;
}

.vip-btn-yearly {
  background: linear-gradient(135deg, #f0c14b 0%, #e6a23c 100%);
  color: #fff;
  border: none;
}

.vip-btn-yearly:hover {
  opacity: 0.92;
  color: #fff;
}

.recharge-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
</style>

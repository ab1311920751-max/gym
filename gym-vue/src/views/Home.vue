<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2 class="page-title">运营数据驾驶舱</h2>
      <p class="page-subtitle">实时监控系统核心指标</p>
    </div>

    <!-- KPI 卡片 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, idx) in kpiList" :key="idx">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-strip" :style="{ background: item.color }"></div>
          <div class="kpi-body">
            <div class="kpi-icon" :style="{ color: item.color }">
              <el-icon :size="26"><component :is="item.icon" /></el-icon>
            </div>
            <div class="kpi-content">
              <div class="kpi-title">{{ item.title }}</div>
              <div class="kpi-num">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 会员分布 + 业务概览 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <el-icon><PieChart /></el-icon>
              <span>会员等级分布</span>
            </div>
          </template>
          <div v-if="loading" class="chart-skeleton">
            <el-skeleton :rows="6" animated />
          </div>
          <el-empty
            v-else-if="!report.vipData || report.vipData.length === 0"
            description="暂无会员数据"
          />
          <div v-else ref="pieRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>业务概览</span>
            </div>
          </template>
          <div class="summary-list">
            <div class="summary-row" v-for="row in summaryRows" :key="row.label">
              <span class="summary-label">{{ row.label }}</span>
              <span class="summary-value">{{ row.value }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, markRaw } from 'vue'
import {
  Money,
  User,
  Tickets,
  Medal,
  PieChart,
  DataAnalysis
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboard } from '../api/report'
import { CHART_COLORS } from '../constants/theme'

const report = ref({})
const loading = ref(true)
const pieRef = ref(null)
let pieInstance = null

const kpiList = computed(() => [
  {
    title: '总营收',
    value: `￥${Number(report.value.totalRevenue || 0).toLocaleString()}`,
    icon: markRaw(Money),
    color: '#ff7a2f'
  },
  {
    title: '总用户数',
    value: `${report.value.userCount || 0} 人`,
    icon: markRaw(User),
    color: '#409eff'
  },
  {
    title: '订单总量',
    value: `${report.value.orderCount || 0} 单`,
    icon: markRaw(Tickets),
    color: '#67c23a'
  },
  {
    title: 'VIP 用户',
    value: `${vipUserCount.value} 人`,
    icon: markRaw(Medal),
    color: '#e6a23c'
  }
])

const vipUserCount = computed(() => {
  const list = report.value.vipData || []
  return list
    .filter((v) => v.name && v.name !== '普通会员')
    .reduce((sum, v) => sum + (Number(v.value) || 0), 0)
})

const summaryRows = computed(() => {
  const total = report.value.userCount || 0
  const vipRate = total > 0 ? ((vipUserCount.value / total) * 100).toFixed(1) : '0.0'
  const aov =
    report.value.orderCount > 0
      ? (Number(report.value.totalRevenue || 0) / report.value.orderCount).toFixed(2)
      : '0.00'
  return [
    { label: 'VIP 渗透率', value: `${vipRate} %` },
    { label: '客单价', value: `￥${aov}` },
    { label: '累计订单', value: `${report.value.orderCount || 0} 单` },
    { label: '累计营收', value: `￥${Number(report.value.totalRevenue || 0).toLocaleString()}` }
  ]
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    report.value = res.data || {}
    await nextTick()
    renderPie()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const renderPie = () => {
  if (!pieRef.value) return
  if (pieInstance) {
    pieInstance.dispose()
  }
  pieInstance = echarts.init(pieRef.value)
  pieInstance.setOption({
    color: CHART_COLORS,
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    series: [
      {
        name: '会员分布',
        type: 'pie',
        radius: ['45%', '72%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 12
        },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold' }
        },
        data: report.value.vipData || []
      }
    ]
  })
}

const handleResize = () => {
  pieInstance?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieInstance?.dispose()
  pieInstance = null
})
</script>

<style scoped>
.dashboard-container {
  padding: 4px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
}

.page-subtitle {
  color: #909399;
  font-size: 13px;
  margin-top: 6px;
}

/* KPI 卡 */
.kpi-card {
  border: none;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 16px;
  position: relative;
}

.kpi-card :deep(.el-card__body) {
  padding: 0 !important;
}

.kpi-strip {
  height: 4px;
  width: 100%;
}

.kpi-body {
  display: flex;
  align-items: center;
  padding: 22px 24px;
}

.kpi-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: #fff5ee;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.kpi-content {
  flex: 1;
  min-width: 0;
}

.kpi-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.kpi-num {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
  line-height: 1.2;
}

/* 图表区 */
.chart-row {
  margin-top: 8px;
}

.chart-card {
  border: none;
  border-radius: 10px;
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2d3d;
  font-size: 15px;
}

.chart-box {
  height: 360px;
  width: 100%;
}

.chart-skeleton {
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-list {
  display: flex;
  flex-direction: column;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 4px;
  border-bottom: 1px dashed #f0f0f0;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-label {
  color: #606266;
  font-size: 14px;
}

.summary-value {
  color: #ff7a2f;
  font-size: 18px;
  font-weight: 700;
}
</style>

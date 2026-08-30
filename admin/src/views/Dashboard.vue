<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getOverview,
  getOrderStatus,
  getPointsTrend,
  getTopProducts,
} from '../api/dashboard'

const loading = ref(false)

const overview = reactive({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  pendingShipCount: 0,
  todayNewUsers: 0,
  todayOrders: 0,
  totalPointsIssued: 0,
})

const topProducts = ref([])

const pieChartRef = ref()
const lineChartRef = ref()
let pieChart = null
let lineChart = null

const statCards = [
  { key: 'userCount', label: '用户总数', icon: 'User', color: '#409eff' },
  { key: 'productCount', label: '商品总数', icon: 'Goods', color: '#67c23a' },
  { key: 'orderCount', label: '订单总数', icon: 'List', color: '#e6a23c' },
  { key: 'pendingShipCount', label: '待发货', icon: 'Box', color: '#f56c6c' },
  { key: 'todayNewUsers', label: '今日新增用户', icon: 'UserFilled', color: '#909399' },
  { key: 'todayOrders', label: '今日订单', icon: 'ShoppingCart', color: '#9c27b0' },
]

function renderPieChart(data) {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  const source = [
    { name: '待发货', value: data.pendingShip || 0 },
    { name: '已发货', value: data.shipped || 0 },
    { name: '已完成', value: data.completed || 0 },
    { name: '已取消', value: data.cancelled || 0 },
  ]
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    color: ['#e6a23c', '#409eff', '#67c23a', '#909399'],
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        data: source,
      },
    ],
  })
}

function renderLineChart(data) {
  if (!lineChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)
  const trend = data.trend || []
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trend.map((i) => i.d),
      axisLabel: { fontSize: 10 },
    },
    yAxis: { type: 'value', name: '积分' },
    series: [
      {
        name: '发放积分',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: trend.map((i) => i.pts),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.5)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' },
          ]),
        },
        lineStyle: { color: '#409eff', width: 2 },
      },
    ],
  })
}

function handleResize() {
  pieChart && pieChart.resize()
  lineChart && lineChart.resize()
}

async function loadData() {
  loading.value = true
  try {
    const [ov, os, pt, tp] = await Promise.all([
      getOverview(),
      getOrderStatus(),
      getPointsTrend(30),
      getTopProducts(10),
    ])
    Object.assign(overview, ov.data || {})
    await nextTick()
    renderPieChart(os.data || {})
    renderLineChart(pt.data || {})
    topProducts.value = tp.data || []
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  pieChart && pieChart.dispose()
  lineChart && lineChart.dispose()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col
        v-for="card in statCards"
        :key="card.key"
        :xs="12"
        :sm="12"
        :md="8"
        :lg="4"
      >
        <el-card class="stat-card" shadow="hover">
          <div class="stat-inner">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ overview[card.key] }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 累计积分 -->
    <el-card class="page-card" shadow="never">
      <div class="total-points">
        <el-icon class="tp-icon"><Coin /></el-icon>
        <div>
          <div class="tp-label">累计发放积分</div>
          <div class="tp-value">{{ overview.totalPointsIssued }}</div>
        </div>
      </div>
    </el-card>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :xs="24" :md="10">
        <el-card class="page-card" shadow="never">
          <template #header>订单状态分布</template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="14">
        <el-card class="page-card" shadow="never">
          <template #header>近30天积分发放趋势</template>
          <div ref="lineChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 销量 TOP10 -->
    <el-card class="page-card" shadow="never">
      <template #header>商品销量 TOP10</template>
      <el-table :data="topProducts" stripe>
        <el-table-column type="index" label="排名" width="70" align="center" />
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="sales" label="销量" width="100" align="center" sortable />
        <el-table-column label="操作" width="120" align="center">
          <template #default>
            <el-tag type="info" size="small">TOP 商品</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.stat-row {
  margin-bottom: 0;
}

.stat-row .el-col {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.total-points {
  display: flex;
  align-items: center;
  gap: 16px;
}

.tp-icon {
  font-size: 40px;
  color: #e6a23c;
}

.tp-label {
  font-size: 13px;
  color: #909399;
}

.tp-value {
  font-size: 24px;
  font-weight: 600;
  color: #e6a23c;
}
</style>

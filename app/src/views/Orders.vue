<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getOrders, cancelOrder, confirmOrder } from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const tabs = [
  { name: '全部', value: '' },
  { name: '待发货', value: 0 },
  { name: '已发货', value: 1 },
  { name: '已完成', value: 2 },
  { name: '已取消', value: 3 },
]
const statusMap = {
  0: { text: '待发货', color: '#ff976a' },
  1: { text: '已发货', color: '#1989fa' },
  2: { text: '已完成', color: '#07c160' },
  3: { text: '已取消', color: '#c8c9cc' },
}

const active = ref(route.query.status !== undefined && route.query.status !== '' ? Number(route.query.status) : '')
const list = ref([])
const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

function priceText(o) {
  if (o.cashPaid && o.cashPaid > 0) {
    return `${o.pointsUsed || 0}积分 + ¥${o.cashPaid}`
  }
  return `${o.pointsUsed || 0}积分`
}

function addressText(o) {
  if (!o.addressSnapshot) return ''
  const s = o.addressSnapshot
  // 后端快照格式：name|phone|fullAddress
  if (s.startsWith('{')) {
    try {
      const a = JSON.parse(s)
      return `${a.province || ''}${a.city || ''}${a.district || ''}${a.detail || ''}（${a.name || ''} ${a.phone || ''}）`
    } catch (e) {
      return s
    }
  }
  const parts = s.split('|')
  if (parts.length >= 3) {
    return `${parts[2]}（${parts[0]} ${parts[1]}）`
  }
  return s
}

async function onLoad() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (active.value !== '') params.status = active.value
    const data = await getOrders(params)
    const records = data.records || []
    list.value.push(...records)
    total.value = data.total || 0
    if (list.value.length >= total.value || records.length === 0) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onRefresh() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = false
  onLoad()
}

function onTab(name) {
  active.value = name
  onRefresh()
}

function onCancel(o) {
  showConfirmDialog({
    title: '提示',
    message: '确定要取消该订单吗？',
  })
    .then(async () => {
      try {
        await cancelOrder(o.id)
        showToast('已取消')
        onRefresh()
      } catch (e) {
        // ignore
      }
    })
    .catch(() => {})
}

function onConfirm(o) {
  showConfirmDialog({
    title: '提示',
    message: '确认已收到商品？',
  })
    .then(async () => {
      try {
        await confirmOrder(o.id)
        showToast('确认成功')
        onRefresh()
      } catch (e) {
        // ignore
      }
    })
    .catch(() => {})
}

onMounted(() => {
  if (userStore.isLogin) onRefresh()
})
</script>

<template>
  <div class="orders-page page">
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.back()" />
    <van-tabs v-model:active="active" @change="onTab" sticky>
      <van-tab v-for="t in tabs" :key="t.value" :name="t.value" :title="t.name" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="o in list" :key="o.id" class="order-card">
          <div class="order-head">
            <span class="order-no">订单号：{{ o.orderNo }}</span>
            <span class="status" :style="{ color: statusMap[o.status].color }">
              {{ statusMap[o.status].text }}
            </span>
          </div>
          <div class="order-body">
            <van-image class="img" fit="cover" :src="o.productImage" />
            <div class="info">
              <div class="name text-ellipsis-2">{{ o.productName }}</div>
              <div class="meta">
                <span class="price points-color">{{ priceText(o) }}</span>
                <span class="qty">x{{ o.quantity }}</span>
              </div>
              <div v-if="o.shipNo && o.status === 1" class="ship">快递单号：{{ o.shipNo }}</div>
            </div>
          </div>
          <div v-if="addressText(o)" class="order-addr">
            <van-icon name="location-o" /> {{ addressText(o) }}
          </div>
          <div v-if="o.status === 0 || o.status === 1" class="order-actions">
            <van-button
              v-if="o.status === 0"
              size="small"
              plain
              @click="onCancel(o)"
            >取消订单</van-button>
            <van-button
              v-if="o.status === 1"
              size="small"
              type="primary"
              @click="onConfirm(o)"
            >确认收货</van-button>
          </div>
        </div>
        <van-empty v-if="finished && !list.length" description="暂无订单" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding-bottom: 20px;
}
.order-card {
  background: #fff;
  margin: 10px 12px;
  border-radius: 10px;
  padding: 12px;
}
.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #969799;
}
.order-head .status {
  font-size: 13px;
  font-weight: 600;
}
.order-body {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  background: #f7f8fa;
  flex: 0 0 80px;
}
.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.name {
  font-size: 14px;
  line-height: 20px;
}
.meta {
  margin-top: 6px;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 14px;
}
.qty {
  color: #969799;
  font-size: 13px;
}
.ship {
  margin-top: 4px;
  font-size: 12px;
  color: #1989fa;
}
.order-addr {
  margin-top: 8px;
  font-size: 12px;
  color: #969799;
  line-height: 18px;
}
.order-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

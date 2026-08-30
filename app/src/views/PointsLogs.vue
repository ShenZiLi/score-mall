<script setup>
import { ref, onMounted } from 'vue'
import { getPointsLogs } from '../api'

const typeMap = {
  SIGN: '签到',
  ORDER_EARN: '订单获取',
  EXCHANGE: '兑换消耗',
  REFUND: '退款返还',
  ADMIN: '系统调整',
}

const list = ref([])
const page = ref(1)
const size = 20
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

async function onLoad() {
  loading.value = true
  try {
    const data = await getPointsLogs({ page: page.value, size })
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
  }
}

function changeColor(change) {
  return change >= 0 ? '#07c160' : '#ee0a24'
}

function formatChange(change) {
  return change >= 0 ? `+${change}` : `${change}`
}

onMounted(() => {})
</script>

<template>
  <div class="logs-page page">
    <van-nav-bar title="积分明细" left-arrow @click-left="$router.back()" />
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div v-for="log in list" :key="log.id" class="log-item">
        <div class="log-info">
          <div class="log-type">
            {{ typeMap[log.type] || log.type }}
            <span v-if="log.remark" class="log-remark">{{ log.remark }}</span>
          </div>
          <div class="log-time">{{ log.createTime }}</div>
        </div>
        <div class="log-change" :style="{ color: changeColor(log.changePoints) }">
          {{ formatChange(log.changePoints) }}
        </div>
      </div>
      <van-empty v-if="finished && !list.length" description="暂无积分记录" />
    </van-list>
  </div>
</template>

<style scoped>
.logs-page {
  min-height: 100vh;
  padding-bottom: 20px;
}
.log-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  margin: 8px 12px;
  border-radius: 8px;
  padding: 12px 14px;
}
.log-info {
  flex: 1;
  min-width: 0;
}
.log-type {
  font-size: 14px;
  color: #323233;
  display: flex;
  align-items: center;
  gap: 6px;
}
.log-remark {
  font-size: 12px;
  color: #969799;
}
.log-time {
  margin-top: 4px;
  font-size: 12px;
  color: #969799;
}
.log-change {
  font-size: 16px;
  font-weight: 600;
}
</style>

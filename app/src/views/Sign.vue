<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import { getSignStatus, sign } from '../api'
import { useUserStore } from '../store/user'
import TabBar from '../components/TabBar.vue'

const router = useRouter()
const userStore = useUserStore()

const signedToday = ref(false)
const recent = ref([])
const loading = ref(false)
const signing = ref(false)

// 构建近30天日历
const calendar = computed(() => {
  const signedSet = new Set(recent.value.map((r) => r.signDate))
  const today = new Date()
  const days = []
  for (let i = 29; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const dateStr = `${y}-${m}-${day}`
    days.push({
      date: dateStr,
      label: `${m}/${day}`,
      weekday: '日一二三四五六'[d.getDay()],
      signed: signedSet.has(dateStr),
      isToday: i === 0,
    })
  }
  return days
})

async function loadStatus() {
  loading.value = true
  try {
    const data = await getSignStatus()
    signedToday.value = data.signedToday
    recent.value = data.recent || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

async function onSign() {
  if (signedToday.value) return
  signing.value = true
  try {
    const data = await sign()
    signedToday.value = true
    showSuccessToast(`签到成功 +${data.points}积分`)
    // 更新积分
    if (userStore.userInfo) {
      userStore.setUserInfo({ ...userStore.userInfo, points: userStore.userInfo.points + data.points })
    }
    await loadStatus()
  } catch (e) {
    // ignore
  } finally {
    signing.value = false
  }
}

onMounted(loadStatus)
</script>

<template>
  <div class="sign-page page">
    <van-nav-bar title="每日签到" />
    <div class="sign-hero">
      <div class="hero-bg">
        <div class="points">
          <span class="num">{{ userStore.points }}</span>
          <span class="unit">积分</span>
        </div>
        <div class="tip">每日签到，积分不断累积</div>
        <van-button
          class="sign-btn"
          round
          :type="signedToday ? 'default' : 'primary'"
          :loading="signing"
          :disabled="signedToday"
          @click="onSign"
        >
          {{ signedToday ? '今日已签到' : '立即签到' }}
        </van-button>
      </div>
    </div>

    <div class="entry" @click="router.push('/points/logs')">
      <van-icon name="balance-o" />
      <span>积分明细</span>
      <van-icon name="arrow" class="arrow" />
    </div>

    <div class="calendar-card">
      <div class="card-title">近30天签到记录</div>
      <div v-if="loading" class="loading">
        <van-loading type="spinner" />
      </div>
      <div v-else class="calendar-grid">
        <div
          v-for="d in calendar"
          :key="d.date"
          class="cal-item"
          :class="{ signed: d.signed, today: d.isToday }"
        >
          <span class="label">{{ d.label }}</span>
          <span class="weekday">周{{ d.weekday }}</span>
          <van-icon v-if="d.signed" name="success" class="check" />
        </div>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<style scoped>
.sign-page {
  padding-bottom: 60px;
  min-height: 100vh;
}
.sign-hero {
  padding: 16px 12px 0;
}
.hero-bg {
  background: linear-gradient(135deg, #ff6b3b, #ff8f5e);
  border-radius: 14px;
  padding: 24px 16px;
  text-align: center;
  color: #fff;
}
.points {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}
.points .num {
  font-size: 34px;
  font-weight: 700;
}
.points .unit {
  font-size: 14px;
}
.tip {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.9;
}
.sign-btn {
  margin-top: 16px;
  width: 60%;
}
.sign-btn :deep(.van-button--default) {
  color: #ff6b3b;
}
.entry {
  margin: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.entry .arrow {
  margin-left: auto;
  color: #c8c9cc;
}
.calendar-card {
  margin: 0 12px 12px;
  background: #fff;
  border-radius: 10px;
  padding: 14px;
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}
.loading {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
}
.cal-item {
  border: 1px solid #f2f3f5;
  border-radius: 6px;
  padding: 6px 2px;
  text-align: center;
  position: relative;
  background: #fafafa;
}
.cal-item.signed {
  background: #fff5ef;
  border-color: #ffd4c2;
}
.cal-item.today {
  border-color: #ff6b3b;
}
.cal-item .label {
  display: block;
  font-size: 11px;
  color: #323233;
}
.cal-item .weekday {
  display: block;
  font-size: 10px;
  color: #969799;
  margin-top: 2px;
}
.cal-item .check {
  position: absolute;
  top: 1px;
  right: 1px;
  color: #ff6b3b;
  font-size: 12px;
}
</style>

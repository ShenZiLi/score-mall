<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useUserStore } from '../store/user'
import TabBar from '../components/TabBar.vue'

const router = useRouter()
const userStore = useUserStore()

const orderTabs = [
  { status: 0, name: '待发货', icon: 'logistics' },
  { status: 1, name: '已发货', icon: 'send-gift-o' },
  { status: 2, name: '已完成', icon: 'passed' },
  { status: 3, name: '已取消', icon: 'close' },
]

const menuGroups = [
  [
    { name: '我的订单', icon: 'orders-o', to: '/orders' },
    { name: '收货地址', icon: 'location-o', to: '/addresses' },
  ],
  [
    { name: '积分明细', icon: 'balance-o', to: '/points/logs' },
    { name: '每日签到', icon: 'calendar-o', to: '/sign' },
  ],
  [
    { name: '修改资料', icon: 'edit', to: '/profile/edit' },
    { name: '修改密码', icon: 'lock', to: '/profile/password' },
  ],
]

function goOrder(status) {
  router.push({ path: '/orders', query: { status } })
}

function onMenu(item) {
  router.push(item.to)
}

function onLogout() {
  showConfirmDialog({ title: '提示', message: '确定退出登录吗？' })
    .then(() => {
      userStore.clear()
      router.replace('/login')
    })
    .catch(() => {})
}

function avatarSrc() {
  return userStore.userInfo && userStore.userInfo.avatar
    ? userStore.userInfo.avatar
    : ''
}

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      // ignore
    }
  }
})
</script>

<template>
  <div class="profile-page page">
    <div class="header">
      <div class="user">
        <van-image
          round
          width="56"
          height="56"
          fit="cover"
          :src="avatarSrc()"
        >
          <template #error>
            <div class="avatar-fallback"><van-icon name="user-o" /></div>
          </template>
        </van-image>
        <div class="user-info">
          <div class="nickname">
            {{ userStore.userInfo ? userStore.userInfo.nickname : '未登录' }}
          </div>
          <div class="phone">
            {{ userStore.userInfo ? userStore.userInfo.phone : '' }}
          </div>
        </div>
      </div>
      <div class="stats">
        <div class="stat">
          <span class="num">{{ userStore.points }}</span>
          <span class="label">可用积分</span>
        </div>
        <div class="stat">
          <span class="num">{{ userStore.userInfo ? userStore.userInfo.totalPoints : 0 }}</span>
          <span class="label">累计积分</span>
        </div>
        <div class="stat">
          <span class="num">{{ userStore.userInfo ? userStore.userInfo.levelName : '-' }}</span>
          <span class="label">会员等级</span>
        </div>
      </div>
    </div>

    <!-- 我的订单快捷入口 -->
    <div class="order-card">
      <div class="card-head">
        <span class="title">我的订单</span>
        <span class="all" @click="goOrder('')">全部订单 <van-icon name="arrow" /></span>
      </div>
      <div class="order-tabs">
        <div v-for="t in orderTabs" :key="t.status" class="otab" @click="goOrder(t.status)">
          <van-icon :name="t.icon" />
          <span>{{ t.name }}</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div v-for="(group, gi) in menuGroups" :key="gi" class="menu-group">
      <van-cell-group inset>
        <van-cell
          v-for="item in group"
          :key="item.name"
          :title="item.name"
          is-link
          @click="onMenu(item)"
        >
          <template #icon>
            <van-icon :name="item.icon" class="menu-icon" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <div class="logout">
      <van-button block plain type="danger" @click="onLogout">退出登录</van-button>
    </div>

    <TabBar />
  </div>
</template>

<style scoped>
.profile-page {
  padding-bottom: 60px;
  min-height: 100vh;
}
.header {
  background: linear-gradient(135deg, #ff6b3b, #ff8f5e);
  padding: 30px 16px 20px;
  color: #fff;
}
.user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: #ff6b3b;
  font-size: 28px;
  border-radius: 50%;
}
.user-info {
  flex: 1;
}
.nickname {
  font-size: 17px;
  font-weight: 600;
}
.phone {
  margin-top: 4px;
  font-size: 13px;
  opacity: 0.9;
}
.stats {
  margin-top: 18px;
  display: flex;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 10px;
  padding: 12px 0;
}
.stat {
  flex: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.stat .num {
  font-size: 17px;
  font-weight: 700;
}
.stat .label {
  font-size: 12px;
  opacity: 0.9;
}
.order-card {
  background: #fff;
  margin: 10px 12px;
  border-radius: 10px;
  padding: 12px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-head .title {
  font-size: 15px;
  font-weight: 600;
}
.card-head .all {
  font-size: 12px;
  color: #969799;
  display: flex;
  align-items: center;
}
.order-tabs {
  margin-top: 14px;
  display: flex;
}
.otab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #646566;
}
.otab .van-icon {
  font-size: 24px;
  color: #ff6b3b;
}
.menu-group {
  margin-top: 10px;
}
.menu-icon {
  margin-right: 8px;
  color: #ff6b3b;
  font-size: 18px;
}
.logout {
  margin: 20px 16px;
}
</style>

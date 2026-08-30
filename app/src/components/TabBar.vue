<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const tabs = [
  { name: 'home', title: '首页', icon: 'wap-home-o', to: '/home' },
  { name: 'category', title: '分类', icon: 'apps-o', to: '/category' },
  { name: 'sign', title: '签到', icon: 'calendar-o', to: '/sign' },
  { name: 'profile', title: '我的', icon: 'user-o', to: '/profile' },
]

const active = computed(() => route.meta.tab || '')

function go(to) {
  // 签到、我的需要登录
  if ((to === '/sign' || to === '/profile') && !userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: to } })
    return
  }
  router.push(to)
}
</script>

<template>
  <van-tabbar :model-value="active" @change="(n) => {
    const t = tabs.find((x) => x.name === n)
    if (t) go(t.to)
  }">
    <van-tabbar-item v-for="t in tabs" :key="t.name" :name="t.name" :icon="t.icon">
      {{ t.title }}
    </van-tabbar-item>
  </van-tabbar>
</template>

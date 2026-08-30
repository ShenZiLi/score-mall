<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute, RouterView } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '../stores/admin'
import { getAdminInfo } from '../api/auth'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const isCollapse = ref(false)

// 菜单结构
const menus = [
  { index: '/dashboard', title: '仪表盘', icon: 'DataLine' },
  { index: '/users', title: '用户管理', icon: 'User' },
  {
    title: '商品管理',
    icon: 'Goods',
    children: [
      { index: '/categories', title: '商品分类', icon: 'Menu' },
      { index: '/products', title: '商品列表', icon: 'ShoppingCart' },
    ],
  },
  { index: '/orders', title: '订单管理', icon: 'List' },
  {
    title: '营销管理',
    icon: 'Promotion',
    children: [{ index: '/banners', title: '轮播图管理', icon: 'Picture' }],
  },
  {
    title: '积分管理',
    icon: 'Coin',
    children: [{ index: '/points/logs', title: '积分流水', icon: 'Tickets' }],
  },
]

const activeMenu = computed(() => route.path)

// 面包屑
const breadcrumbs = computed(() => {
  const list = []
  if (route.meta.parent) {
    list.push({ title: route.meta.parent })
  }
  if (route.meta.title) {
    list.push({ title: route.meta.title })
  }
  return list
})

function handleMenuSelect(index) {
  router.push(index)
}

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

function goProfile() {
  router.push('/profile')
}

async function loadAdminInfo() {
  try {
    const res = await getAdminInfo()
    if (res.data) {
      adminStore.setAdminInfo(res.data)
    }
  } catch (e) {
    // ignore
  }
}

// 进入布局时拉取最新管理员信息
loadAdminInfo()

function handleLogout() {
  ElMessageBox.confirm('确认退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      adminStore.clearAuth()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<template>
  <el-container class="layout-root">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-bar">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span v-show="!isCollapse" class="logo-text">积分商城后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
        @select="handleMenuSelect"
      >
        <template v-for="menu in menus" :key="menu.index || menu.title">
          <!-- 一级菜单 -->
          <el-menu-item v-if="!menu.children" :index="menu.index">
            <el-icon><component :is="menu.icon" /></el-icon>
            <template #title>{{ menu.title }}</template>
          </el-menu-item>
          <!-- 二级菜单 -->
          <el-sub-menu v-else :index="menu.title">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.index"
              :index="child.index"
            >
              <el-icon><component :is="child.icon" /></el-icon>
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.title">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="(cmd) => (cmd === 'profile' ? goProfile() : handleLogout())">
            <span class="admin-info">
              <el-avatar :size="32" :src="adminStore.avatar">
                {{ adminStore.nickname.charAt(0) }}
              </el-avatar>
              <span class="admin-name">{{ adminStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <RouterView v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-root {
  height: 100%;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.28s;
  overflow-x: hidden;
}

.logo-bar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  background-color: #2b3a4d;
  overflow: hidden;
  white-space: nowrap;
}

.logo-icon {
  font-size: 24px;
  color: #409eff;
}

.logo-text {
  font-size: 17px;
  font-weight: 600;
}

.layout-aside .el-menu {
  border-right: none;
}

.layout-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #5a5e66;
}

.collapse-btn:hover {
  color: #409eff;
}

.breadcrumb {
  line-height: 60px;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.admin-name {
  font-size: 14px;
  color: #303133;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 0;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

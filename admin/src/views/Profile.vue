<script setup>
import { computed } from 'vue'
import { useAdminStore } from '../stores/admin'

const adminStore = useAdminStore()
const info = computed(() => adminStore.adminInfo || {})

const roleText = computed(() => {
  const r = String(info.value.role || '').toUpperCase()
  if (r.includes('SUPER')) return '超级管理员'
  if (r.includes('ADMIN')) return '管理员'
  return info.value.role || '管理员'
})
</script>

<template>
  <div class="page-container">
    <el-card class="page-card" shadow="never">
      <div class="profile-header">
        <el-avatar :size="80" :src="adminStore.avatar">
          {{ adminStore.nickname.charAt(0) }}
        </el-avatar>
        <div class="profile-meta">
          <h2 class="profile-name">{{ adminStore.nickname }}</h2>
          <el-tag type="primary" size="small">{{ roleText }}</el-tag>
        </div>
      </div>
    </el-card>

    <el-card class="page-card" shadow="never">
      <template #header>基本信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="管理员ID">{{ info.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ info.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ info.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleText }}</el-descriptions-item>
        <el-descriptions-item label="头像" :span="2">
          <el-avatar :size="48" :src="adminStore.avatar">
            {{ adminStore.nickname.charAt(0) }}
          </el-avatar>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<style scoped>
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-name {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
</style>

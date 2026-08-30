<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useAdminStore } from '../stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function handleLogin() {
  loginFormRef.value.validate((valid) => {
    if (!valid) return
    loading.value = true
    login(loginForm)
      .then((res) => {
        const { token, adminInfo } = res.data || {}
        if (!token) {
          ElMessage.error('登录失败：未返回 token')
          return
        }
        adminStore.setAuth(token, adminInfo)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      })
      .catch(() => {})
      .finally(() => {
        loading.value = false
      })
  })
}
</script>

<template>
  <div class="login-bg">
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <el-icon class="login-logo"><Shop /></el-icon>
        <h2 class="login-title">积分商城管理后台</h2>
        <p class="login-subtitle">Admin Console</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="'User'"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            :prefix-icon="'Lock'"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">默认账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<style scoped>
.login-card {
  width: 400px;
  border-radius: 12px;
  padding: 20px 10px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-logo {
  font-size: 48px;
  color: #409eff;
}

.login-title {
  margin: 12px 0 4px;
  font-size: 22px;
  color: #303133;
}

.login-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
  letter-spacing: 1px;
}

.login-btn {
  width: 100%;
}

.login-tip {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>

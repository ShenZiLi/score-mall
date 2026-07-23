<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { login } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)

async function onSubmit() {
  if (!form.username) return showFailToast('请输入手机号')
  if (!form.password) return showFailToast('请输入密码')
  loading.value = true
  try {
    const data = await login(form)
    userStore.setToken(data.token)
    userStore.setUserInfo(data.userInfo)
    showSuccessToast('登录成功')
    const redirect = route.query.redirect || '/home'
    router.replace(redirect)
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page page">
    <div class="header">
      <div class="logo">积分商城</div>
      <div class="sub">欢迎回来，请登录账号</div>
    </div>
    <van-form @submit="onSubmit" class="form">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          maxlength="11"
          clearable
          :rules="[{ required: true, message: '请输入手机号' }]"
        />
        <van-field
          v-model="form.password"
          label="密码"
          placeholder="请输入密码"
          type="password"
          clearable
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>
      <div class="actions">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
        >
          登录
        </van-button>
        <div class="links">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">去注册</router-link>
        </div>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.login-page {
  padding: 0 0 40px;
}
.header {
  padding: 60px 24px 30px;
  background: linear-gradient(135deg, #ff6b3b, #ff8f5e);
  color: #fff;
}
.logo {
  font-size: 26px;
  font-weight: 700;
}
.sub {
  margin-top: 8px;
  font-size: 13px;
  opacity: 0.9;
}
.form {
  margin-top: 20px;
}
.actions {
  padding: 20px 16px 0;
}
.links {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #969799;
}
.link {
  color: #ff6b3b;
  margin-left: 4px;
}
</style>

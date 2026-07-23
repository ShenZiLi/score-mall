<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { register } from '../api'

const router = useRouter()

const form = reactive({
  phone: '',
  password: '',
  nickname: '',
})
const loading = ref(false)

async function onSubmit() {
  if (!/^1\d{10}$/.test(form.phone)) return showFailToast('请输入正确的手机号')
  if (form.password.length < 6) return showFailToast('密码至少6位')
  if (!form.nickname) return showFailToast('请输入昵称')
  loading.value = true
  try {
    await register(form)
    showSuccessToast('注册成功，请登录')
    router.replace('/login')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page page">
    <van-nav-bar title="注册账号" left-arrow @click-left="$router.back()" />
    <van-form @submit="onSubmit" class="form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
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
          placeholder="请输入密码(至少6位)"
          type="password"
          clearable
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <van-field
          v-model="form.nickname"
          label="昵称"
          placeholder="请输入昵称"
          clearable
          :rules="[{ required: true, message: '请输入昵称' }]"
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
          注册
        </van-button>
        <div class="links">
          <span>已有账号？</span>
          <router-link to="/login" class="link">去登录</router-link>
        </div>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.form {
  margin-top: 16px;
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

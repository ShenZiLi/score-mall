<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import { changePassword } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const saving = ref(false)

async function onSubmit() {
  if (!form.oldPassword) return showFailToast('请输入原密码')
  if (form.newPassword.length < 6) return showFailToast('新密码至少6位')
  if (form.newPassword !== form.confirmPassword)
    return showFailToast('两次输入的密码不一致')
  saving.value = true
  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    })
    showSuccessToast('修改成功，请重新登录')
    userStore.clear()
    router.replace('/login')
  } catch (e) {
    // ignore
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="pwd-page page">
    <van-nav-bar title="修改密码" left-arrow @click-left="$router.back()" />
    <van-form @submit="onSubmit" class="form">
      <van-cell-group inset>
        <van-field
          v-model="form.oldPassword"
          label="原密码"
          placeholder="请输入原密码"
          type="password"
          clearable
          :rules="[{ required: true, message: '请输入原密码' }]"
        />
        <van-field
          v-model="form.newPassword"
          label="新密码"
          placeholder="请输入新密码(至少6位)"
          type="password"
          clearable
          :rules="[{ required: true, message: '请输入新密码' }]"
        />
        <van-field
          v-model="form.confirmPassword"
          label="确认密码"
          placeholder="请再次输入新密码"
          type="password"
          clearable
          :rules="[{ required: true, message: '请确认密码' }]"
        />
      </van-cell-group>
      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="saving">
          确认修改
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.pwd-page {
  min-height: 100vh;
}
.form {
  margin-top: 10px;
}
.actions {
  padding: 20px 16px;
}
</style>

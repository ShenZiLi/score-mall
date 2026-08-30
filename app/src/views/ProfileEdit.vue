<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import { updateProfile, uploadFile } from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  nickname: '',
  avatar: '',
})
const saving = ref(false)
const uploading = ref(false)

async function onChooseAvatar({ file }) {
  uploading.value = true
  try {
    const data = await uploadFile(file)
    form.avatar = data.url
  } catch (e) {
    // ignore
  } finally {
    uploading.value = false
  }
}

async function onSave() {
  if (!form.nickname) return showToast('请输入昵称')
  saving.value = true
  try {
    await updateProfile({ nickname: form.nickname, avatar: form.avatar })
    // 更新本地用户信息
    if (userStore.userInfo) {
      userStore.setUserInfo({
        ...userStore.userInfo,
        nickname: form.nickname,
        avatar: form.avatar,
      })
    }
    showSuccessToast('保存成功')
    router.back()
  } catch (e) {
    // ignore
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (userStore.userInfo) {
    form.nickname = userStore.userInfo.nickname || ''
    form.avatar = userStore.userInfo.avatar || ''
  }
})
</script>

<template>
  <div class="edit-page page">
    <van-nav-bar title="修改资料" left-arrow @click-left="$router.back()" />
    <div class="avatar-area">
      <van-uploader :after-read="onChooseAvatar" :show-upload="true" :max-count="1">
        <van-image round width="80" height="80" fit="cover" :src="form.avatar">
          <template #error>
            <div class="avatar-ph"><van-icon name="user-o" /></div>
          </template>
        </van-image>
      </van-uploader>
      <div class="avatar-tip">
        {{ uploading ? '上传中...' : '点击头像更换' }}
      </div>
    </div>

    <van-cell-group inset>
      <van-field v-model="form.nickname" label="昵称" placeholder="请输入昵称" clearable />
      <van-field
        :model-value="userStore.userInfo ? userStore.userInfo.phone : ''"
        label="手机号"
        readonly
      />
    </van-cell-group>

    <div class="actions">
      <van-button round block type="primary" :loading="saving" @click="onSave">
        保存
      </van-button>
    </div>
  </div>
</template>

<style scoped>
.edit-page {
  min-height: 100vh;
}
.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  background: #fff;
  margin: 10px 0;
}
.avatar-ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  color: #c8c9cc;
  font-size: 36px;
  border-radius: 50%;
}
.avatar-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #969799;
}
.actions {
  padding: 20px 16px;
}
</style>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showSuccessToast, showFailToast } from 'vant'
import {
  getAddressDetail,
  addAddress,
  updateAddress,
} from '../api'

const route = useRoute()
const router = useRouter()

const id = route.query.id ? Number(route.query.id) : null
const from = route.query.from || '/addresses'
const isEdit = computed(() => !!id)

const form = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false,
})
const saving = ref(false)

async function loadDetail() {
  if (!id) return
  const data = await getAddressDetail(id)
  Object.assign(form, {
    name: data.name || '',
    phone: data.phone || '',
    province: data.province || '',
    city: data.city || '',
    district: data.district || '',
    detail: data.detail || '',
    isDefault: !!data.isDefault,
  })
}

function validate() {
  if (!form.name) return '请输入收货人姓名'
  if (!/^1\d{10}$/.test(form.phone)) return '请输入正确的手机号'
  if (!form.province || !form.city) return '请输入所在地区'
  if (!form.detail) return '请输入详细地址'
  return ''
}

async function onSave() {
  const err = validate()
  if (err) return showFailToast(err)
  saving.value = true
  try {
    const payload = { ...form, isDefault: form.isDefault ? 1 : 0 }
    // 后端可能期望布尔/数字，按数字传更通用
    if (id) {
      await updateAddress(id, { ...form })
    } else {
      await addAddress({ ...form })
    }
    showSuccessToast('保存成功')
    router.replace(from)
  } catch (e) {
    // ignore
  } finally {
    saving.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <div class="addr-edit-page page">
    <van-nav-bar :title="isEdit ? '编辑地址' : '新增地址'" left-arrow @click-left="$router.back()" />
    <van-form @submit="onSave" class="form">
      <van-cell-group inset>
        <van-field v-model="form.name" label="收货人" placeholder="请输入姓名" clearable />
        <van-field
          v-model="form.phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          maxlength="11"
          clearable
        />
        <van-field
          v-model="form.province"
          label="省份"
          placeholder="请输入省份"
          clearable
        />
        <van-field
          v-model="form.city"
          label="城市"
          placeholder="请输入城市"
          clearable
        />
        <van-field
          v-model="form.district"
          label="区/县"
          placeholder="请输入区/县"
          clearable
        />
        <van-field
          v-model="form.detail"
          label="详细地址"
          type="textarea"
          rows="2"
          placeholder="请输入详细地址"
          clearable
          autosize
        />
        <van-cell title="设为默认地址">
          <template #right-icon>
            <van-switch v-model="form.isDefault" />
          </template>
        </van-cell>
      </van-cell-group>
      <div class="actions">
        <van-button round block type="primary" native-type="submit" :loading="saving">
          保存
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<style scoped>
.addr-edit-page {
  min-height: 100vh;
}
.form {
  margin-top: 10px;
}
.actions {
  padding: 20px 16px;
}
</style>

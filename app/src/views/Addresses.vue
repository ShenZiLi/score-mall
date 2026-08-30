<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getAddresses, deleteAddress, setDefaultAddress } from '../api'

const route = useRoute()
const router = useRouter()

const list = ref([])
const loading = ref(false)
const from = route.query.from

async function load() {
  loading.value = true
  try {
    list.value = (await getAddresses()) || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function onEdit(a) {
  router.push({ path: '/address/edit', query: { id: a.id, from: route.fullPath } })
}

function onAdd() {
  router.push({ path: '/address/edit', query: { from: route.fullPath } })
}

function onDelete(a) {
  showConfirmDialog({ title: '提示', message: '确定删除该地址？' })
    .then(async () => {
      try {
        await deleteAddress(a.id)
        showToast('已删除')
        load()
      } catch (e) {
        // ignore
      }
    })
    .catch(() => {})
}

async function onSetDefault(a) {
  try {
    await setDefaultAddress(a.id)
    showToast('已设为默认')
    load()
  } catch (e) {
    // ignore
  }
}

onMounted(load)
</script>

<template>
  <div class="addresses-page page">
    <van-nav-bar
      title="收货地址"
      left-arrow
      @click-left="() => (from ? router.replace(from) : $router.back())"
    />
    <van-loading v-if="loading" class="loading" type="spinner" />
    <template v-else>
      <div v-if="list.length" class="list">
        <div v-for="a in list" :key="a.id" class="addr-card">
          <div class="addr-top">
            <span class="name">{{ a.name }}</span>
            <span class="phone">{{ a.phone }}</span>
            <van-tag v-if="a.isDefault" type="danger" size="mini">默认</van-tag>
          </div>
          <div class="addr-text">
            {{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}
          </div>
          <div class="addr-actions">
            <span v-if="!a.isDefault" class="act" @click="onSetDefault(a)">设为默认</span>
            <span class="act" @click="onEdit(a)">编辑</span>
            <span class="act danger" @click="onDelete(a)">删除</span>
          </div>
        </div>
      </div>
      <van-empty v-else description="还没有收货地址" />
    </template>

    <div class="add-bar">
      <van-button round block type="primary" @click="onAdd">新增收货地址</van-button>
    </div>
  </div>
</template>

<style scoped>
.addresses-page {
  min-height: 100vh;
  padding-bottom: 80px;
}
.loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.list {
  padding: 10px 12px;
}
.addr-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
}
.addr-top {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}
.addr-top .name {
  font-weight: 600;
}
.addr-top .phone {
  color: #969799;
  font-size: 13px;
}
.addr-text {
  margin-top: 6px;
  font-size: 13px;
  color: #646566;
  line-height: 20px;
}
.addr-actions {
  margin-top: 10px;
  display: flex;
  gap: 18px;
  justify-content: flex-end;
  border-top: 1px solid #f2f3f5;
  padding-top: 10px;
}
.act {
  font-size: 13px;
  color: #646566;
}
.act.danger {
  color: #ee0a24;
}
.add-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  max-width: 750px;
  margin: 0 auto;
}
</style>

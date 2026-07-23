<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showSuccessToast, showToast } from 'vant'
import { getProductDetail, getAddresses, createOrder } from '../api'

const route = useRoute()
const router = useRouter()

const productId = route.query.productId
const quantity = Number(route.query.quantity || 1)
const payType = route.query.payType || 'POINTS_ONLY'

const product = ref(null)
const addresses = ref([])
const selectedAddress = ref(null)
const loading = ref(false)
const submitting = ref(false)
const showAddressSheet = ref(false)

const totalPoints = computed(() =>
  product.value ? (product.value.pointsPrice || 0) * quantity : 0,
)
const totalCash = computed(() =>
  product.value ? (product.value.cashPrice || 0) * quantity : 0,
)

const addressText = computed(() => {
  if (!selectedAddress.value) return '请选择收货地址'
  const a = selectedAddress.value
  return `${a.province}${a.city}${a.district}${a.detail}（${a.name} ${a.phone}）`
})

async function loadProduct() {
  product.value = await getProductDetail(productId)
}

async function loadAddresses() {
  const data = await getAddresses()
  addresses.value = data || []
  selectedAddress.value =
    addresses.value.find((a) => a.isDefault) || addresses.value[0] || null
}

function onSelectAddress(a) {
  selectedAddress.value = a
  showAddressSheet.value = false
}

function goAddressManage() {
  router.push({ path: '/addresses', query: { from: route.fullPath } })
}

async function onSubmit() {
  if (!selectedAddress.value) return showFailToast('请选择收货地址')
  if (!product.value) return
  if (product.value.stock <= 0) return showFailToast('库存不足')
  submitting.value = true
  try {
    const order = await createOrder({
      productId: product.value.id,
      quantity,
      addressId: selectedAddress.value.id,
      payType,
    })
    showSuccessToast('兑换成功')
    // 刷新用户积分
    router.replace('/orders')
  } catch (e) {
    // 错误已在拦截器提示
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!productId) {
    showToast('参数错误')
    router.back()
    return
  }
  loading.value = true
  try {
    await Promise.all([loadProduct(), loadAddresses()])
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="confirm-page page">
    <van-nav-bar title="确认订单" left-arrow @click-left="$router.back()" />
    <van-loading v-if="loading" class="loading" type="spinner" />

    <template v-else-if="product">
      <!-- 地址 -->
      <div class="address-card" @click="showAddressSheet = true">
        <van-icon name="location-o" class="addr-icon" />
        <div class="addr-info">
          <template v-if="selectedAddress">
            <div class="addr-name">
              {{ selectedAddress.name }}
              <span class="addr-phone">{{ selectedAddress.phone }}</span>
              <van-tag v-if="selectedAddress.isDefault" type="danger" size="mini">默认</van-tag>
            </div>
            <div class="addr-text">
              {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detail }}
            </div>
          </template>
          <div v-else class="addr-empty">请选择收货地址</div>
        </div>
        <van-icon name="arrow" />
      </div>

      <!-- 商品信息 -->
      <div class="goods-card">
        <van-image class="goods-img" fit="cover" :src="product.coverImage" />
        <div class="goods-info">
          <div class="goods-name text-ellipsis-2">{{ product.name }}</div>
          <div v-if="product.subtitle" class="goods-sub text-ellipsis">{{ product.subtitle }}</div>
          <div class="goods-bottom">
            <span class="points-color">
              {{ product.cashPrice && product.cashPrice > 0
                ? `${product.pointsPrice || 0}积分 + ¥${product.cashPrice}`
                : `${product.pointsPrice || 0}积分` }}
            </span>
            <span class="qty">x{{ quantity }}</span>
          </div>
        </div>
      </div>

      <!-- 明细 -->
      <van-cell-group inset class="bill">
        <van-cell title="积分抵扣">
          <span class="points-color">{{ totalPoints }} 积分</span>
        </van-cell>
        <van-cell v-if="totalCash > 0" title="现金支付">
          <span>¥{{ totalCash.toFixed(2) }}</span>
        </van-cell>
        <van-cell title="实付">
          <span class="pay-amount">
            {{ totalPoints }}积分<span v-if="totalCash > 0"> + ¥{{ totalCash.toFixed(2) }}</span>
          </span>
        </van-cell>
      </van-cell-group>

      <div class="submit-bar">
        <van-button round block type="primary" :loading="submitting" @click="onSubmit">
          提交订单
        </van-button>
      </div>
    </template>

    <!-- 地址选择 -->
    <van-action-sheet v-model:show="showAddressSheet" title="选择收货地址">
      <div class="addr-list">
        <div
          v-for="a in addresses"
          :key="a.id"
          class="addr-item"
          :class="{ active: selectedAddress && selectedAddress.id === a.id }"
          @click="onSelectAddress(a)"
        >
          <div class="addr-item-top">
            <span class="name">{{ a.name }}</span>
            <span class="phone">{{ a.phone }}</span>
            <van-tag v-if="a.isDefault" type="danger" size="mini">默认</van-tag>
          </div>
          <div class="addr-item-text">
            {{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail }}
          </div>
        </div>
        <div v-if="!addresses.length" class="addr-list-empty">
          暂无地址
          <van-button size="small" type="primary" @click="goAddressManage">去添加</van-button>
        </div>
        <div v-if="addresses.length" class="addr-list-manage" @click="goAddressManage">
          管理收货地址
        </div>
      </div>
    </van-action-sheet>
  </div>
</template>

<style scoped>
.confirm-page {
  padding-bottom: 80px;
  min-height: 100vh;
}
.loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.address-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  margin: 10px 12px;
  padding: 14px;
  border-radius: 10px;
}
.addr-icon {
  font-size: 22px;
  color: #ff6b3b;
}
.addr-info {
  flex: 1;
  min-width: 0;
}
.addr-name {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.addr-phone {
  font-weight: 400;
  color: #969799;
  font-size: 13px;
}
.addr-text {
  margin-top: 4px;
  font-size: 13px;
  color: #646566;
}
.addr-empty {
  color: #969799;
  font-size: 14px;
}
.goods-card {
  display: flex;
  gap: 10px;
  background: #fff;
  margin: 10px 12px;
  padding: 12px;
  border-radius: 10px;
}
.goods-img {
  width: 90px;
  height: 90px;
  border-radius: 6px;
  background: #f7f8fa;
  flex: 0 0 90px;
}
.goods-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.goods-name {
  font-size: 14px;
  line-height: 20px;
}
.goods-sub {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}
.goods-bottom {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 14px;
}
.qty {
  color: #969799;
  font-size: 13px;
}
.bill {
  margin-top: 10px;
}
.pay-amount {
  color: #ff6b3b;
  font-weight: 600;
}
.submit-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  max-width: 750px;
  margin: 0 auto;
}
.addr-list {
  padding: 8px 0 16px;
  max-height: 60vh;
  overflow-y: auto;
}
.addr-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f2f3f5;
}
.addr-item.active {
  background: #fff5ef;
}
.addr-item-top {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.addr-item-top .name {
  font-weight: 600;
}
.addr-item-top .phone {
  color: #969799;
  font-size: 13px;
}
.addr-item-text {
  margin-top: 4px;
  font-size: 13px;
  color: #646566;
}
.addr-list-empty {
  padding: 24px;
  text-align: center;
  color: #969799;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}
.addr-list-manage {
  text-align: center;
  padding: 14px;
  color: #ff6b3b;
  font-size: 14px;
}
</style>

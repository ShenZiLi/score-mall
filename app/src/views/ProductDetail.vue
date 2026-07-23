<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showFailToast, showToast } from 'vant'
import { getProductDetail } from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const currentImage = ref(0)
const showSku = ref(false)
const quantity = ref(1)

const images = computed(() => {
  if (!product.value) return []
  if (product.value.images && product.value.images.length) return product.value.images
  return product.value.coverImage ? [product.value.coverImage] : []
})

const priceText = computed(() => {
  if (!product.value) return ''
  const p = product.value
  if (p.cashPrice && p.cashPrice > 0) {
    return `${p.pointsPrice || 0}积分 + ¥${p.cashPrice}`
  }
  return `${p.pointsPrice || 0}积分`
})

const payType = computed(() => {
  if (!product.value) return 'POINTS_ONLY'
  return product.value.cashPrice && product.value.cashPrice > 0
    ? 'POINTS_CASH'
    : 'POINTS_ONLY'
})

async function loadProduct() {
  loading.value = true
  try {
    product.value = await getProductDetail(route.params.id)
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function onBuy() {
  if (!product.value) return
  if (product.value.stock <= 0) return showFailToast('库存不足')
  if (!userStore.isLogin) {
    showToast('请先登录')
    router.push({
      path: '/login',
      query: { redirect: `/product/${route.params.id}` },
    })
    return
  }
  router.push({
    path: '/order/confirm',
    query: {
      productId: product.value.id,
      quantity: quantity.value,
      payType: payType.value,
    },
  })
}

onMounted(loadProduct)
</script>

<template>
  <div class="detail-page page">
    <van-nav-bar title="商品详情" left-arrow @click-left="$router.back()" fixed placeholder />
    <van-loading v-if="loading" class="loading" type="spinner" />
    <template v-else-if="product">
      <van-swipe class="banner" :autoplay="3000" @change="(i) => (currentImage = i)">
        <van-swipe-item v-for="(img, i) in images" :key="i">
          <van-image fit="cover" :src="img" class="banner-img" />
        </van-swipe-item>
        <template #indicator>
          <div class="indicator">{{ currentImage + 1 }}/{{ images.length }}</div>
        </template>
      </van-swipe>

      <div class="price-box">
        <div class="price">{{ priceText }}</div>
        <div v-if="product.originalPrice" class="origin">原价 ¥{{ product.originalPrice }}</div>
      </div>

      <div class="info-card">
        <div class="name">{{ product.name }}</div>
        <div v-if="product.subtitle" class="sub">{{ product.subtitle }}</div>
        <div class="meta">
          <span>已兑{{ product.sales || 0 }}件</span>
          <span>库存{{ product.stock }}件</span>
        </div>
      </div>

      <div class="detail-card">
        <div class="block-title">商品详情</div>
        <div class="detail-html" v-html="product.detail || '暂无详情'"></div>
      </div>

      <van-action-bar>
        <van-action-bar-icon icon="chat-o" text="客服" />
        <van-action-bar-icon icon="cart-o" text="订单" @click="router.push('/orders')" />
        <van-action-bar-button type="warning" text="立即兑换" @click="onBuy" />
      </van-action-bar>
    </template>
    <van-empty v-else description="商品不存在" />
  </div>
</template>

<style scoped>
.detail-page {
  padding-bottom: 60px;
  min-height: 100vh;
  background: #f7f8fa;
}
.loading {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.banner {
  background: #fff;
}
.banner-img {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #f7f8fa;
}
.indicator {
  position: absolute;
  right: 12px;
  bottom: 12px;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}
.price-box {
  background: linear-gradient(135deg, #ff6b3b, #ff8f5e);
  color: #fff;
  padding: 14px 16px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.price-box .price {
  font-size: 22px;
  font-weight: 700;
}
.price-box .origin {
  font-size: 13px;
  text-decoration: line-through;
  opacity: 0.85;
}
.info-card {
  background: #fff;
  padding: 12px 16px;
  margin-bottom: 10px;
}
.name {
  font-size: 16px;
  font-weight: 600;
  line-height: 22px;
}
.sub {
  margin-top: 6px;
  font-size: 13px;
  color: #969799;
}
.meta {
  margin-top: 10px;
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: #969799;
}
.detail-card {
  background: #fff;
  padding: 12px 16px;
}
.block-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
}
.detail-html {
  font-size: 14px;
  color: #323233;
  line-height: 1.7;
  word-break: break-all;
}
.detail-html :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 8px 0;
}
</style>

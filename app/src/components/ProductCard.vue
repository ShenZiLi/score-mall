<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: { type: Object, required: true },
})

const router = useRouter()

function goDetail() {
  router.push(`/product/${props.product.id}`)
}

function priceText(p) {
  if (p.cashPrice && p.cashPrice > 0) {
    return `${p.pointsPrice || 0}积分 + ¥${p.cashPrice}`
  }
  return `${p.pointsPrice || 0}积分`
}
</script>

<template>
  <div class="product-card" @click="goDetail">
    <van-image
      class="cover"
      fit="cover"
      :src="product.coverImage"
      lazy-load
    >
      <template #error>
        <div class="cover-fallback">暂无图片</div>
      </template>
    </van-image>
    <div class="info">
      <div class="name text-ellipsis-2">{{ product.name }}</div>
      <div v-if="product.subtitle" class="sub text-ellipsis">{{ product.subtitle }}</div>
      <div class="bottom">
        <span class="price points-color">{{ priceText(product) }}</span>
        <span v-if="product.originalPrice" class="origin">¥{{ product.originalPrice }}</span>
      </div>
      <div class="sales">已兑{{ product.sales || 0 }}件</div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.cover {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: #f7f8fa;
}
.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #c8c9cc;
  font-size: 12px;
}
.info {
  padding: 6px 8px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.name {
  font-size: 13px;
  line-height: 18px;
  color: #323233;
  min-height: 36px;
}
.sub {
  font-size: 11px;
  color: #969799;
}
.bottom {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 2px;
}
.price {
  font-size: 14px;
}
.origin {
  font-size: 11px;
  color: #c8c9cc;
  text-decoration: line-through;
}
.sales {
  font-size: 11px;
  color: #969799;
}
</style>

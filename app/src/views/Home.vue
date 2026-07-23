<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHome } from '../api'
import { useUserStore } from '../store/user'
import ProductCard from '../components/ProductCard.vue'
import TabBar from '../components/TabBar.vue'

defineOptions({ name: 'HomePage' })

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const banners = ref([])
const categories = ref([])
const hotProducts = ref([])
const recommendProducts = ref([])

const keyword = ref('')

function onSearch() {
  router.push({ path: '/products', query: { keyword: keyword.value } })
}

function goCategory(id) {
  router.push({ path: '/products', query: { categoryId: id } })
}

function goAllProducts() {
  router.push('/products')
}

function goSign() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: '/sign' } })
    return
  }
  router.push('/sign')
}

async function loadData() {
  loading.value = true
  try {
    const data = await getHome()
    banners.value = data.banners || []
    categories.value = data.categories || []
    hotProducts.value = data.hotProducts || []
    recommendProducts.value = data.recommendProducts || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="home-page page">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <van-search
        v-model="keyword"
        placeholder="搜索商品"
        shape="round"
        @search="onSearch"
        @click-input="onSearch"
      />
    </div>

    <van-loading v-if="loading" class="page-loading" type="spinner" />

    <div v-else class="content">
      <!-- 轮播图 -->
      <div v-if="banners.length" class="banner-wrap">
        <van-swipe class="banner-swipe" :autoplay="3000" indicator-color="#ff6b3b">
          <van-swipe-item v-for="b in banners" :key="b.id">
            <van-image class="banner-img" fit="cover" :src="b.image" />
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- 签到入口 -->
      <div class="sign-entry" @click="goSign">
        <van-icon name="medal-o" />
        <span>{{ userStore.isLogin ? '每日签到领积分' : '登录后签到领积分' }}</span>
        <van-icon name="arrow" />
      </div>

      <!-- 金刚区分类 -->
      <div v-if="categories.length" class="categories">
        <div class="scroll">
          <div
            v-for="c in categories"
            :key="c.id"
            class="cat-item"
            @click="goCategory(c.id)"
          >
            <div class="cat-icon">
              <van-icon :name="c.icon || 'gift-o'" />
            </div>
            <span class="cat-name text-ellipsis">{{ c.name }}</span>
          </div>
          <div class="cat-item" @click="goAllProducts">
            <div class="cat-icon"><van-icon name="apps-o" /></div>
            <span class="cat-name">全部</span>
          </div>
        </div>
      </div>

      <!-- 热门商品 -->
      <div v-if="hotProducts.length" class="section">
        <div class="section-head">
          <span class="title">热门兑换</span>
          <span class="more" @click="goAllProducts">查看全部 <van-icon name="arrow" /></span>
        </div>
        <div class="hot-scroll">
          <ProductCard
            v-for="p in hotProducts"
            :key="p.id"
            :product="p"
            class="hot-card"
          />
        </div>
      </div>

      <!-- 推荐商品 -->
      <div v-if="recommendProducts.length" class="section">
        <div class="section-head">
          <span class="title">为你推荐</span>
        </div>
        <div class="rec-grid">
          <ProductCard
            v-for="p in recommendProducts"
            :key="p.id"
            :product="p"
          />
        </div>
      </div>

      <van-empty v-if="!banners.length && !recommendProducts.length" description="暂无商品" />
    </div>

    <TabBar />
  </div>
</template>

<style scoped>
.home-page {
  padding-bottom: 60px;
}
.search-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: linear-gradient(135deg, #ff6b3b, #ff8f5e);
}
.search-bar :deep(.van-search) {
  background: transparent;
  padding: 8px 12px;
}
.search-bar :deep(.van-search__content) {
  background: #fff;
}
.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.banner-wrap {
  padding: 10px 12px 0;
}
.banner-swipe {
  border-radius: 10px;
  overflow: hidden;
}
.banner-img {
  width: 100%;
  aspect-ratio: 2.4 / 1;
  background: #f7f8fa;
}
.sign-entry {
  margin: 12px;
  background: linear-gradient(90deg, #fff5ef, #ffe9df);
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #ff6b3b;
}
.sign-entry .van-icon:last-child {
  margin-left: auto;
}
.categories {
  background: #fff;
  margin: 0 12px;
  border-radius: 10px;
  padding: 12px 0;
}
.categories .scroll {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding: 0 8px;
  scrollbar-width: none;
}
.categories .scroll::-webkit-scrollbar {
  display: none;
}
.cat-item {
  flex: 0 0 auto;
  width: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.cat-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff5ef;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b3b;
  font-size: 22px;
}
.cat-name {
  font-size: 12px;
  color: #323233;
  max-width: 64px;
}
.section {
  margin-top: 14px;
  padding: 0 12px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.section-head .title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}
.section-head .more {
  font-size: 12px;
  color: #969799;
  display: flex;
  align-items: center;
}
.hot-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
  padding-bottom: 4px;
}
.hot-scroll::-webkit-scrollbar {
  display: none;
}
.hot-card {
  flex: 0 0 130px;
  width: 130px;
}
.rec-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
</style>

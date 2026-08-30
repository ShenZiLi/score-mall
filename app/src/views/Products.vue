<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts, getCategories } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref(route.query.keyword || '')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)
const order = ref('') // 空/sales/points

const categories = ref([])
const list = ref([])
const page = ref(1)
const size = 20
const total = ref(0)
const loading = ref(false)
const finished = ref(false)

const orders = [
  { text: '综合', value: '' },
  { text: '销量优先', value: 'sales' },
  { text: '积分优先', value: 'points' },
]

async function loadCategories() {
  try {
    const data = await getCategories()
    categories.value = [{ id: null, name: '全部' }, ...(data || [])]
  } catch (e) {
    // ignore
  }
}

async function onLoad() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (categoryId.value) params.categoryId = categoryId.value
    if (keyword.value) params.keyword = keyword.value
    if (order.value) params.order = order.value
    const data = await getProducts(params)
    const records = data.records || []
    list.value.push(...records)
    total.value = data.total || 0
    if (list.value.length >= total.value || records.length === 0) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

function refresh() {
  list.value = []
  page.value = 1
  finished.value = false
  loading.value = false
  onLoad()
}

function onSearch() {
  refresh()
}

function selectCategory(id) {
  categoryId.value = id
  refresh()
}

function selectOrder(val) {
  order.value = val
  refresh()
}

onMounted(() => {
  loadCategories()
})
</script>

<template>
  <div class="products-page page">
    <van-nav-bar title="商品列表" left-arrow @click-left="$router.back()" />
    <van-search
      v-model="keyword"
      placeholder="搜索商品"
      shape="round"
      @search="onSearch"
      @clear="onSearch"
    />

    <!-- 分类筛选 -->
    <div class="cat-filter">
      <div class="scroll">
        <span
          v-for="c in categories"
          :key="c.id"
          class="chip"
          :class="{ active: categoryId === c.id }"
          @click="selectCategory(c.id)"
        >{{ c.name }}</span>
      </div>
    </div>

    <!-- 排序 -->
    <div class="order-bar">
      <span
        v-for="o in orders"
        :key="o.value"
        class="order-item"
        :class="{ active: order === o.value }"
        @click="selectOrder(o.value)"
      >{{ o.text }}</span>
    </div>

    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div v-if="list.length" class="grid">
        <ProductCard v-for="p in list" :key="p.id" :product="p" />
      </div>
      <van-empty v-if="finished && !list.length" description="暂无商品" />
    </van-list>
  </div>
</template>

<style scoped>
.cat-filter {
  background: #fff;
  border-bottom: 1px solid #f2f3f5;
}
.cat-filter .scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 10px 12px;
  scrollbar-width: none;
}
.cat-filter .scroll::-webkit-scrollbar {
  display: none;
}
.chip {
  flex: 0 0 auto;
  padding: 4px 14px;
  border-radius: 14px;
  background: #f7f8fa;
  font-size: 13px;
  color: #646566;
}
.chip.active {
  background: #ff6b3b;
  color: #fff;
}
.order-bar {
  display: flex;
  background: #fff;
  padding: 8px 12px;
  gap: 24px;
  border-bottom: 1px solid #f2f3f5;
  position: sticky;
  top: 54px;
  z-index: 5;
}
.order-item {
  font-size: 13px;
  color: #646566;
}
.order-item.active {
  color: #ff6b3b;
  font-weight: 600;
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 10px 12px;
}
</style>

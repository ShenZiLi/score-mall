<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getProducts } from '../api'
import ProductCard from '../components/ProductCard.vue'
import TabBar from '../components/TabBar.vue'

const router = useRouter()

const categories = ref([])
const activeId = ref(null)
const products = ref([])
const loading = ref(false)

async function loadCategories() {
  const data = await getCategories()
  categories.value = data || []
  if (categories.value.length) {
    activeId.value = categories.value[0].id
  }
}

async function loadProducts() {
  if (!activeId.value) return
  loading.value = true
  try {
    const data = await getProducts({ page: 1, size: 50, categoryId: activeId.value })
    products.value = data.records || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

watch(activeId, loadProducts)

onMounted(async () => {
  await loadCategories()
})
</script>

<template>
  <div class="category-page page">
    <van-nav-bar title="商品分类" />
    <div class="body">
      <van-sidebar v-model="activeId" class="side" @change="() => {}">
        <van-sidebar-item
          v-for="c in categories"
          :key="c.id"
          :title="c.name"
        />
      </van-sidebar>
      <div class="right">
        <van-loading v-if="loading" class="loading" type="spinner" />
        <template v-else>
          <div v-if="products.length" class="grid">
            <ProductCard v-for="p in products" :key="p.id" :product="p" />
          </div>
          <van-empty v-else description="该分类下暂无商品" />
        </template>
      </div>
    </div>
    <TabBar />
  </div>
</template>

<style scoped>
.category-page {
  padding-bottom: 60px;
}
.body {
  display: flex;
  height: calc(100vh - 96px);
}
.side {
  flex: 0 0 90px;
  height: 100%;
  overflow-y: auto;
}
.side :deep(.van-sidebar-item) {
  padding: 16px 8px;
  font-size: 13px;
}
.right {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #f7f8fa;
}
.loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
</style>

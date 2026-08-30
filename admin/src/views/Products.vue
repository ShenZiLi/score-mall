<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listProducts,
  getProduct,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStock,
} from '../api/product'
import { listCategories } from '../api/category'
import { uploadFile } from '../api/upload'
import ImageUpload from '../components/ImageUpload.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const query = reactive({
  page: 1,
  size: 20,
  keyword: '',
  categoryId: '',
  status: '',
})

const categoryMap = computed(() => {
  const map = {}
  categories.value.forEach((c) => (map[c.id] = c.name))
  return map
})

const statusOptions = [
  { value: 1, label: '上架' },
  { value: 0, label: '下架' },
]

function statusText(status) {
  return status === 1 ? '上架' : '下架'
}

function statusType(status) {
  return status === 1 ? 'success' : 'info'
}

async function loadCategories() {
  try {
    const res = await listCategories()
    categories.value = res.data || []
  } catch (e) {
    // ignore
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await listProducts(query)
    const data = res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadData()
}

function handleReset() {
  query.keyword = ''
  query.categoryId = ''
  query.status = ''
  query.page = 1
  loadData()
}

function handlePageChange(page) {
  query.page = page
  loadData()
}

function handleSizeChange(size) {
  query.size = size
  query.page = 1
  loadData()
}

// 新增/编辑弹窗
const dialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const defaultForm = () => ({
  id: null,
  categoryId: '',
  name: '',
  subtitle: '',
  coverImage: '',
  images: '',
  detail: '',
  originalPrice: 0,
  pointsPrice: 0,
  cashPrice: 0,
  stock: 0,
  status: 1,
  isHot: 0,
  isRecommend: 0,
  sort: 0,
})
const form = reactive(defaultForm())

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  pointsPrice: [{ required: true, message: '请输入积分价', trigger: 'blur' }],
}

// 多图列表（拆分 images 字符串）
const imageList = ref([])
const uploadingImages = ref(false)

function syncImageListFromForm() {
  imageList.value = form.images
    ? form.images.split(',').filter((u) => u)
    : []
}

function syncFormFromImageList() {
  form.images = imageList.value.join(',')
}

async function customImageRequest({ file }) {
  uploadingImages.value = true
  try {
    const res = await uploadFile(file)
    const url = res.data?.url || res.data
    imageList.value.push(url)
    syncFormFromImageList()
  } catch (e) {
    ElMessage.error('图片上传失败')
  } finally {
    uploadingImages.value = false
  }
}

function removeImage(idx) {
  imageList.value.splice(idx, 1)
  syncFormFromImageList()
}

function openCreate() {
  Object.assign(form, defaultForm())
  imageList.value = []
  isEdit.value = false
  dialog.value = true
}

async function openEdit(row) {
  try {
    const res = await getProduct(row.id)
    const data = res.data || row
    Object.assign(form, defaultForm(), data)
    syncImageListFromForm()
    isEdit.value = true
    dialog.value = true
  } catch (e) {
    // ignore
  }
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    const payload = { ...form }
    // 数字字段兜底
    payload.originalPrice = Number(payload.originalPrice) || 0
    payload.pointsPrice = Number(payload.pointsPrice) || 0
    payload.cashPrice = Number(payload.cashPrice) || 0
    payload.stock = Number(payload.stock) || 0
    payload.sort = Number(payload.sort) || 0
    try {
      if (isEdit.value) {
        await updateProduct(form.id, payload)
        ElMessage.success('修改成功')
      } else {
        await createProduct(payload)
        ElMessage.success('新增成功')
      }
      dialog.value = false
      loadData()
    } catch (e) {
      // ignore
    } finally {
      submitting.value = false
    }
  })
}

function handleToggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确认${action}商品「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await updateProduct(row.id, { ...row, status: next })
      ElMessage.success(`${action}成功`)
      loadData()
    })
    .catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除商品「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

// 修改库存弹窗
const stockDialog = ref(false)
const stockForm = reactive({ id: null, name: '', stock: 0 })

function openStockDialog(row) {
  stockForm.id = row.id
  stockForm.name = row.name
  stockForm.stock = row.stock
  stockDialog.value = true
}

async function submitStock() {
  try {
    await updateProductStock(stockForm.id, stockForm.stock)
    ElMessage.success('库存修改成功')
    stockDialog.value = false
    loadData()
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})
</script>

<template>
  <div class="page-container">
    <el-card class="page-card" shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="商品名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="query.categoryId"
          placeholder="分类"
          clearable
          style="width: 160px"
        >
          <el-option
            v-for="c in categories"
            :key="c.id"
            :label="c.name"
            :value="c.id"
          />
        </el-select>
        <el-select
          v-model="query.status"
          placeholder="状态"
          clearable
          style="width: 120px"
        >
          <el-option
            v-for="opt in statusOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-button type="primary" :icon="'Search'" @click="handleSearch">搜索</el-button>
        <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
        <div class="flex-grow"></div>
        <el-button type="primary" :icon="'Plus'" @click="openCreate">新增商品</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <img
              v-if="row.coverImage"
              :src="row.coverImage"
              class="table-cover"
              alt="cover"
            />
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="分类" width="120" align="center">
          <template #default="{ row }">
            {{ categoryMap[row.categoryId] || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="pointsPrice" label="积分价" width="100" align="center" sortable />
        <el-table-column prop="originalPrice" label="原价(¥)" width="100" align="center" sortable />
        <el-table-column prop="stock" label="库存" width="90" align="center" sortable />
        <el-table-column prop="sales" label="销量" width="90" align="center" sortable />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="openStockDialog(row)">改库存</el-button>
            <el-button
              :type="row.status === 1 ? 'info' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialog"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="720px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="c in categories"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="封面图">
          <ImageUpload v-model="form.coverImage" :width="120" :height="120" />
        </el-form-item>
        <el-form-item label="商品图集">
          <el-upload
            list-type="picture-card"
            :show-file-list="true"
            :http-request="customImageRequest"
            accept="image/*"
            :file-list="[]"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="image-list" v-loading="uploadingImages">
            <div
              v-for="(img, idx) in imageList"
              :key="idx"
              class="image-item"
            >
              <img :src="img" />
              <span class="remove-btn" @click="removeImage(idx)">×</span>
            </div>
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="原价(¥)">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="积分价" prop="pointsPrice">
              <el-input-number v-model="form.pointsPrice" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="现金价(¥)">
              <el-input-number v-model="form.cashPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">上架</el-radio>
                <el-radio :value="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="热门">
              <el-switch v-model="form.isHot" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="推荐">
          <el-switch v-model="form.isRecommend" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="商品详情">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="4"
            placeholder="请输入商品详情"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改库存弹窗 -->
    <el-dialog v-model="stockDialog" title="修改库存" width="420px">
      <el-form label-width="80px">
        <el-form-item label="商品">
          <span>{{ stockForm.name }}</span>
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="stockForm.stock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialog = false">取消</el-button>
        <el-button type="primary" @click="submitStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.muted {
  color: #c0c4cc;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 2px;
  right: 4px;
  color: #f56c6c;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
  text-shadow: 0 0 2px #fff;
}
</style>

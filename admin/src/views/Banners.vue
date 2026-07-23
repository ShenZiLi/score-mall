<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listBanners,
  createBanner,
  updateBanner,
  deleteBanner,
} from '../api/banner'
import ImageUpload from '../components/ImageUpload.vue'

const loading = ref(false)
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await listBanners()
    tableData.value = res.data || []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function statusText(status) {
  return status === 1 ? '启用' : '禁用'
}

function statusType(status) {
  return status === 1 ? 'success' : 'info'
}

const dialog = ref(false)
const isEdit = ref(false)
const formRef = ref()
const defaultForm = () => ({
  id: null,
  title: '',
  image: '',
  link: '',
  sort: 0,
  status: 1,
})
const form = reactive(defaultForm())

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }],
}

function openCreate() {
  Object.assign(form, defaultForm())
  isEdit.value = false
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, defaultForm(), row)
  isEdit.value = true
  dialog.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const payload = {
      title: form.title,
      image: form.image,
      link: form.link,
      sort: Number(form.sort) || 0,
      status: form.status,
    }
    try {
      if (isEdit.value) {
        await updateBanner(form.id, payload)
        ElMessage.success('修改成功')
      } else {
        await createBanner(payload)
        ElMessage.success('新增成功')
      }
      dialog.value = false
      loadData()
    } catch (e) {
      // ignore
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除轮播图「${row.title}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteBanner(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
    .catch(() => {})
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-card class="page-card" shadow="never">
      <div class="toolbar">
        <span class="page-title">轮播图管理</span>
        <div class="flex-grow"></div>
        <el-button type="primary" :icon="'Plus'" @click="openCreate">新增轮播图</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column label="图片" width="120" align="center">
          <template #default="{ row }">
            <img
              v-if="row.image"
              :src="row.image"
              class="banner-thumb"
              alt="banner"
            />
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="link" label="链接" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="90" align="center" sortable />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" :title="isEdit ? '编辑轮播图' : '新增轮播图'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <ImageUpload v-model="form.image" :width="200" :height="100" />
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="form.link" placeholder="请输入跳转链接" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 16px;
  font-weight: 600;
}

.muted {
  color: #c0c4cc;
}

.banner-thumb {
  width: 100px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
</style>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from '../api/category'

const loading = ref(false)
const tableData = ref([])

async function loadData() {
  loading.value = true
  try {
    const res = await listCategories()
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

// 新增/编辑弹窗
const dialog = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1,
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

function resetForm() {
  form.id = null
  form.name = ''
  form.icon = ''
  form.sort = 0
  form.status = 1
}

function openCreate() {
  resetForm()
  isEdit.value = false
  dialog.value = true
}

function openEdit(row) {
  resetForm()
  Object.assign(form, row)
  isEdit.value = true
  dialog.value = true
}

async function submit() {
  if (!form.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  const payload = {
    name: form.name,
    icon: form.icon,
    sort: form.sort,
    status: form.status,
  }
  try {
    if (isEdit.value) {
      await updateCategory(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createCategory(payload)
      ElMessage.success('新增成功')
    }
    dialog.value = false
    loadData()
  } catch (e) {
    // ignore
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除分类「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await deleteCategory(row.id)
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
        <span class="page-title">商品分类</span>
        <div class="flex-grow"></div>
        <el-button type="primary" :icon="'Plus'" @click="openCreate">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="icon" label="图标" width="140" align="center">
          <template #default="{ row }">
            <span v-if="row.icon">{{ row.icon }}</span>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" align="center" sortable />
        <el-table-column label="状态" width="100" align="center">
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

    <el-dialog v-model="dialog" :title="isEdit ? '编辑分类' : '新增分类'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="图标名称或标识" />
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
</style>

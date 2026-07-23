<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, updateUserStatus, adjustUserPoints } from '../api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: '',
})

const levelOptions = [
  { value: 0, label: '普通', type: 'info' },
  { value: 1, label: '银卡', type: '' },
  { value: 2, label: '金卡', type: 'warning' },
  { value: 3, label: '钻石', type: 'danger' },
]

const statusOptions = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' },
]

function levelTag(level) {
  return levelOptions.find((i) => i.value === level) || levelOptions[0]
}

function statusTagType(status) {
  return status === 1 ? 'success' : 'danger'
}

function statusText(status) {
  return status === 1 ? '正常' : '禁用'
}

async function loadData() {
  loading.value = true
  try {
    const res = await listUsers(query)
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

function handleToggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const action = next === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确认${action}用户「${row.nickname || row.phone}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      await updateUserStatus(row.id, next)
      ElMessage.success(`${action}成功`)
      loadData()
    })
    .catch(() => {})
}

// 调整积分弹窗
const pointsDialog = ref(false)
const pointsForm = reactive({
  id: null,
  nickname: '',
  points: 0,
  remark: '',
})
const pointsFormRef = ref()

function openPointsDialog(row) {
  pointsForm.id = row.id
  pointsForm.nickname = row.nickname || row.phone
  pointsForm.points = 0
  pointsForm.remark = ''
  pointsDialog.value = true
}

async function submitPoints() {
  if (pointsForm.points === 0 || pointsForm.points === '' || pointsForm.points === null) {
    ElMessage.warning('请输入变动积分（正数加，负数减）')
    return
  }
  try {
    await adjustUserPoints(pointsForm.id, pointsForm.points, pointsForm.remark)
    ElMessage.success('积分调整成功')
    pointsDialog.value = false
    loadData()
  } catch (e) {
    // ignore
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-card class="page-card" shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="手机号/昵称"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="query.status"
          placeholder="状态"
          clearable
          style="width: 140px"
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
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="昵称" min-width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="30" :src="row.avatar">{{ (row.nickname || '?').charAt(0) }}</el-avatar>
              <span>{{ row.nickname || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="当前积分" width="110" align="center" sortable />
        <el-table-column prop="totalPoints" label="累计积分" width="110" align="center" sortable />
        <el-table-column label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.level).type" size="small">
              {{ levelTag(row.level).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openPointsDialog(row)">
              调整积分
            </el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 调整积分弹窗 -->
    <el-dialog v-model="pointsDialog" title="调整积分" width="440px">
      <el-form ref="pointsFormRef" :model="pointsForm" label-width="80px">
        <el-form-item label="用户">
          <span>{{ pointsForm.nickname }}</span>
        </el-form-item>
        <el-form-item label="变动积分" required>
          <el-input-number v-model="pointsForm.points" :step="100" />
          <div class="form-tip">正数增加，负数扣减</div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="pointsForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入调整备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPoints">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>

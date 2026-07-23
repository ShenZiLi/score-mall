<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listPointsLogs } from '../api/points'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 20,
  userId: '',
})

// 积分类型映射
const typeMap = {
  sign: { label: '签到', type: 'success' },
  order: { label: '下单消费', type: 'warning' },
  refund: { label: '退款返还', type: '' },
  admin: { label: '管理员调整', type: 'danger' },
  register: { label: '注册赠送', type: 'info' },
}

function typeInfo(type) {
  return typeMap[type] || { label: type || '其他', type: 'info' }
}

function changeColor(changePoints) {
  if (changePoints > 0) return '#67c23a'
  if (changePoints < 0) return '#f56c6c'
  return '#909399'
}

async function loadData() {
  loading.value = true
  try {
    const res = await listPointsLogs(query)
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
  query.userId = ''
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

onMounted(loadData)
</script>

<template>
  <div class="page-container">
    <el-card class="page-card" shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.userId"
          placeholder="用户ID"
          clearable
          style="width: 180px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="'Search'" @click="handleSearch">搜索</el-button>
        <el-button :icon="'Refresh'" @click="handleReset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column label="变动积分" width="120" align="center">
          <template #default="{ row }">
            <span :style="{ color: changeColor(row.changePoints), fontWeight: 600 }">
              {{ row.changePoints > 0 ? '+' : '' }}{{ row.changePoints }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforePoints" label="变动前" width="110" align="center" />
        <el-table-column prop="afterPoints" label="变动后" width="110" align="center" />
        <el-table-column label="类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="typeInfo(row.type).type" size="small">
              {{ typeInfo(row.type).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
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
  </div>
</template>

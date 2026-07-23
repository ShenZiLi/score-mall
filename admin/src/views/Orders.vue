<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listOrders, getOrder, shipOrder, updateOrderRemark } from '../api/order'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: '',
  userId: '',
})

const statusOptions = [
  { value: 0, label: '待发货', type: 'warning' },
  { value: 1, label: '已发货', type: '' },
  { value: 2, label: '已完成', type: 'success' },
  { value: 3, label: '已取消', type: 'info' },
]

function statusInfo(status) {
  return statusOptions.find((i) => i.value === status) || statusOptions[0]
}

async function loadData() {
  loading.value = true
  try {
    const res = await listOrders(query)
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

// 详情弹窗
const detailDialog = ref(false)
const detail = ref({})

async function openDetail(row) {
  try {
    const res = await getOrder(row.id)
    detail.value = res.data || row
    detailDialog.value = true
  } catch (e) {
    // ignore
  }
}

// 发货弹窗
const shipDialog = ref(false)
const shipForm = reactive({ id: null, orderNo: '', shipNo: '' })

function openShipDialog(row) {
  shipForm.id = row.id
  shipForm.orderNo = row.orderNo
  shipForm.shipNo = ''
  shipDialog.value = true
}

async function submitShip() {
  if (!shipForm.shipNo) {
    ElMessage.warning('请输入物流单号')
    return
  }
  try {
    await shipOrder(shipForm.id, shipForm.shipNo)
    ElMessage.success('发货成功')
    shipDialog.value = false
    loadData()
  } catch (e) {
    // ignore
  }
}

// 备注弹窗
const remarkDialog = ref(false)
const remarkForm = reactive({ id: null, orderNo: '', remark: '' })

function openRemarkDialog(row) {
  remarkForm.id = row.id
  remarkForm.orderNo = row.orderNo
  remarkForm.remark = row.remark || ''
  remarkDialog.value = true
}

async function submitRemark() {
  try {
    await updateOrderRemark(remarkForm.id, remarkForm.remark)
    ElMessage.success('备注成功')
    remarkDialog.value = false
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
          placeholder="订单号/商品名"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="query.userId"
          placeholder="用户ID"
          clearable
          style="width: 130px"
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
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <img v-if="row.productImage" :src="row.productImage" class="table-cover" alt="" />
              <div class="product-info">
                <div class="product-name">{{ row.productName }}</div>
                <div class="product-qty">x{{ row.quantity }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="90" align="center" />
        <el-table-column prop="pointsUsed" label="积分" width="90" align="center" />
        <el-table-column prop="cashPaid" label="现金(¥)" width="100" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusInfo(row.status).type" size="small">
              {{ statusInfo(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 0"
              type="warning"
              link
              size="small"
              @click="openShipDialog(row)"
            >
              发货
            </el-button>
            <el-button type="info" link size="small" @click="openRemarkDialog(row)">备注</el-button>
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialog" title="订单详情" width="640px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusInfo(detail.status).type" size="small">
            {{ statusInfo(detail.status).label }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品">{{ detail.productName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="使用积分">{{ detail.pointsUsed }}</el-descriptions-item>
        <el-descriptions-item label="支付现金">¥{{ detail.cashPaid }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ detail.shipNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ detail.shipTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detail.completeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">
          {{ detail.addressSnapshot }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipDialog" title="订单发货" width="440px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ shipForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.shipNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialog = false">取消</el-button>
        <el-button type="primary" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 备注弹窗 -->
    <el-dialog v-model="remarkDialog" title="订单备注" width="440px">
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ remarkForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="remarkForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="remarkDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRemark">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 13px;
  line-height: 1.4;
  word-break: break-all;
}

.product-qty {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>

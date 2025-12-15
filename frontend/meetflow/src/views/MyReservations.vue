<template>
  <div class="reservations-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的预约</h2>
          <el-select
            v-model="statusFilter"
            placeholder="筛选状态"
            style="width: 200px"
            clearable
            @change="handleStatusChange"
          >
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
            <el-option label="已取消" :value="3" />
            <el-option label="已完成" :value="4" />
          </el-select>
        </div>
      </template>
      
      <el-table
        :data="reservations"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column label="会议室" width="150">
          <template #default="{ row }">
            {{ row.meetingRoomName || row.meetingRoom?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="meetingTitle" label="会议主题" min-width="150" />
        <el-table-column label="预约日期" width="120">
          <template #default="{ row }">
            {{ row.reservationDate }}
          </template>
        </el-table-column>
        <el-table-column label="时间段" width="180">
          <template #default="{ row }">
            {{ row.startTime }}:00 - {{ row.endTime }}:00
          </template>
        </el-table-column>
        <el-table-column prop="attendeeCount" label="参会人数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.approveTime || row.approvalTime) }}
          </template>
        </el-table-column>
        <el-table-column label="驳回理由" min-width="150">
          <template #default="{ row }">
            {{ row.rejectReason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleCancel(row.id)"
            >
              取消
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && reservations.length === 0" description="暂无预约记录" />
      
      <!-- 分页组件 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyReservationsPage, cancelReservation } from '@/api/reservation'
import dayjs from 'dayjs'

const reservations = ref([])
const loading = ref(false)
const statusFilter = ref(null)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const statusMap = {
  0: { text: '待审批', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已驳回', type: 'danger' },
  3: { text: '已取消', type: 'info' },
  4: { text: '已完成', type: '' }
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

const getStatusType = (status) => {
  return statusMap[status]?.type || ''
}

const fetchReservations = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (statusFilter.value !== null) {
      params.status = statusFilter.value
    }
    const res = await getMyReservationsPage(params)
    if (res.data) {
      reservations.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      reservations.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取预约记录失败:', error)
    reservations.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchReservations()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchReservations()
}

const handleStatusChange = () => {
  currentPage.value = 1
  fetchReservations()
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelReservation(id)
    ElMessage.success('取消成功')
    fetchReservations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
    }
  }
}

onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.reservations-container {
  max-width: 1400px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}
</style>


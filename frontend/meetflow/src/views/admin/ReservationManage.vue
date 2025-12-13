<template>
  <div class="manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>预约管理</h2>
          <el-select
            v-model="statusFilter"
            placeholder="筛选状态"
            style="width: 200px"
            clearable
            @change="fetchReservations"
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="会议室" width="150">
          <template #default="{ row }">
            {{ getMeetingRoomName(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="meetingTitle" label="会议主题" min-width="150" />
        <el-table-column label="预约人" width="120">
          <template #default="{ row }">
            {{ getUserName(row) }}
          </template>
        </el-table-column>
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
            {{ row.approvalTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="驳回理由" min-width="150">
          <template #default="{ row }">
            {{ row.rejectReason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handleApprove(row.id)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleReject(row.id)"
            >
              驳回
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && reservations.length === 0" description="暂无预约记录" />
    </el-card>
    
    <!-- 驳回对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回预约"
      width="500px"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectForm"
        :rules="rejectRules"
        label-width="100px"
      >
        <el-form-item label="驳回理由" prop="rejectReason">
          <el-input
            v-model="rejectForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回理由"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReject">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllReservations, approveReservation } from '@/api/reservation'
import { getMeetingRooms } from '@/api/meetingRoom'

const reservations = ref([])
const meetingRooms = ref([])
const loading = ref(false)
const statusFilter = ref(null)
const rejectDialogVisible = ref(false)
const submitting = ref(false)
const rejectFormRef = ref(null)
const currentRejectId = ref(null)

const rejectForm = reactive({
  rejectReason: ''
})

const rejectRules = {
  rejectReason: [
    { required: true, message: '请输入驳回理由', trigger: 'blur' },
    { min: 5, message: '驳回理由至少5个字符', trigger: 'blur' }
  ]
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
    const params = statusFilter.value !== null ? { status: statusFilter.value } : {}
    const res = await getAllReservations(params)
    reservations.value = res.data || []
    // 调试：打印第一条数据查看结构
    if (reservations.value.length > 0) {
      console.log('预约数据示例:', reservations.value[0])
    }
  } catch (error) {
    console.error('获取预约记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取会议室名称（兼容多种数据结构）
const getMeetingRoomName = (row) => {
  if (!row) return '-'
  
  // 尝试多种可能的字段名和数据结构
  if (row.meetingRoom) {
    if (typeof row.meetingRoom === 'string') {
      // 如果是字符串，可能是名称
      return row.meetingRoom
    }
    if (row.meetingRoom.name) {
      return row.meetingRoom.name
    }
  }
  
  if (row.meetingRoomName) return row.meetingRoomName
  if (row.roomName) return row.roomName
  if (row.room?.name) return row.room.name
  
  // 如果只有ID，尝试从列表中查找
  const roomId = row.meetingRoomId || row.roomId || (row.meetingRoom?.id)
  if (roomId) {
    const room = meetingRooms.value.find(r => r.id === roomId || r.id === parseInt(roomId))
    if (room) return room.name
  }
  
  return '-'
}

// 获取用户名（兼容多种数据结构）
const getUserName = (row) => {
  if (!row) return '-'
  
  // 尝试多种可能的字段名和数据结构
  if (row.user) {
    if (typeof row.user === 'string') {
      // 如果是字符串，可能是用户名
      return row.user
    }
    if (row.user.name) return row.user.name
    if (row.user.username) return row.user.username
  }
  
  if (row.userName) return row.userName
  if (row.bookerName) return row.bookerName
  if (row.booker?.name) return row.booker.name
  if (row.booker?.username) return row.booker.username
  
  // 如果只有ID，显示ID（实际项目中可能需要查询用户信息）
  const userId = row.userId || row.bookerId || (row.user?.id)
  if (userId) {
    // 可以尝试从用户列表中查找，这里先显示ID
    return `用户${userId}`
  }
  
  return '-'
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确定要通过该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await approveReservation(id, {})
    ElMessage.success('审批通过')
    fetchReservations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批失败:', error)
    }
  }
}

const handleReject = (id) => {
  currentRejectId.value = id
  rejectForm.rejectReason = ''
  rejectDialogVisible.value = true
}

const submitReject = async () => {
  if (!rejectFormRef.value) return
  
  await rejectFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await approveReservation(currentRejectId.value, {
          rejectReason: rejectForm.rejectReason
        })
        ElMessage.success('已驳回')
        rejectDialogVisible.value = false
        fetchReservations()
      } catch (error) {
        console.error('驳回失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 获取会议室列表（用于通过ID查找名称）
const fetchMeetingRooms = async () => {
  try {
    const res = await getMeetingRooms()
    meetingRooms.value = res.data || []
  } catch (error) {
    console.error('获取会议室列表失败:', error)
  }
}

onMounted(() => {
  fetchMeetingRooms()
  fetchReservations()
})
</script>

<style scoped>
.manage-container {
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


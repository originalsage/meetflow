<template>
  <div class="manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>会议室管理</h2>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加会议室
          </el-button>
        </div>
      </template>
      
      <el-table
        :data="meetingRooms"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column label="照片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.photoUrl"
              :src="row.photoUrl"
              style="width: 60px; height: 60px"
              fit="cover"
              :preview-src-list="[row.photoUrl]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="roomNumber" label="房号" width="120" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column prop="purpose" label="用途" min-width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '可预约' : '不可预约' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '设为不可预约' : '设为可预约' }}
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="meeting-room-form"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="房号" prop="roomNumber">
          <el-input v-model="form.roomNumber" placeholder="请输入房号" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number
            v-model="form.capacity"
            :min="1"
            :max="1000"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="area">
          <el-input-number
            v-model="form.area"
            :min="1"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="用途" prop="purpose">
          <el-input
            v-model="form.purpose"
            type="textarea"
            :rows="3"
            placeholder="请输入用途说明"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">不可预约</el-radio>
            <el-radio :label="1">可预约</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="照片">
          <el-upload
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            :show-file-list="false"
          >
            <el-button type="primary" :loading="uploading">上传照片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持jpg/png格式，大小不超过5MB
              </div>
            </template>
          </el-upload>
          <el-image
            v-if="form.photoUrl"
            :src="form.photoUrl"
            style="width: 200px; height: 150px; margin-top: 10px; border-radius: 4px"
            fit="cover"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMeetingRooms,
  addMeetingRoom,
  updateMeetingRoom,
  deleteMeetingRoom,
  setMeetingRoomStatus,
  uploadMeetingRoomPhoto
} from '@/api/meetingRoom'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const meetingRooms = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)

const form = reactive({
  name: '',
  roomNumber: '',
  capacity: 1,
  area: 0,
  purpose: '',
  status: 1,
  photoUrl: ''
})

const rules = {
  name: [{ required: true, message: '请输入会议室名称', trigger: 'blur' }],
  roomNumber: [{ required: true, message: '请输入房号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'blur' }],
  purpose: [{ required: true, message: '请输入用途', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑会议室' : '添加会议室')

const handleUpload = async (options) => {
  uploading.value = true
  try {
    const res = await uploadMeetingRoomPhoto(options.file)
    form.photoUrl = res.data
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
  }
}

const fetchMeetingRooms = async () => {
  loading.value = true
  try {
    const res = await getMeetingRooms()
    meetingRooms.value = res.data || []
  } catch (error) {
    console.error('获取会议室列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    name: row.name,
    roomNumber: row.roomNumber,
    capacity: row.capacity,
    area: row.area,
    purpose: row.purpose,
    status: row.status,
    photoUrl: row.photoUrl || ''
  })
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该会议室吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteMeetingRoom(id)
    ElMessage.success('删除成功')
    fetchMeetingRooms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await setMeetingRoomStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchMeetingRooms()
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

const resetForm = () => {
  Object.assign(form, {
    name: '',
    roomNumber: '',
    capacity: 1,
    area: 0,
    purpose: '',
    status: 1,
    photoUrl: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}


const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await updateMeetingRoom(currentId.value, form)
          ElMessage.success('更新成功')
        } else {
          await addMeetingRoom(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchMeetingRooms()
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  fetchMeetingRooms()
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

.meeting-room-form {
  padding: 10px 0;
}

.meeting-room-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.meeting-room-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
</style>


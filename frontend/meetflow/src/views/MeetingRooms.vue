<template>
  <div class="meeting-rooms-container">
    <el-card class="page-header">
      <h2>会议室列表</h2>
    </el-card>
    
    <!-- 查询表单 -->
    <el-card class="search-card" style="margin-bottom: 20px">
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item label="名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入会议室名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="房号">
          <el-input
            v-model="queryForm.roomNumber"
            placeholder="请输入房号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number
            v-model="queryForm.minCapacity"
            :min="1"
            placeholder="min"
            style="width: 130px"
          />
          <span style="margin: 0 8px">-</span>
          <el-input-number
            v-model="queryForm.maxCapacity"
            :min="1"
            placeholder="max"
            style="width: 130px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="可预约" :value="1" />
            <el-option label="不可预约" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="rooms-card" v-loading="loading">
      <el-row :gutter="20">
        <el-col
          v-for="room in filteredRooms"
          :key="room.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          style="margin-bottom: 20px"
        >
          <el-card
            class="room-card"
            shadow="hover"
            @click="goToDetail(room.id)"
            style="cursor: pointer"
          >
            <div class="room-image">
              <el-image
                v-if="room.photoUrl"
                :src="room.photoUrl"
                fit="cover"
                style="width: 100%; height: 200px"
                :preview-src-list="[room.photoUrl]"
              />
              <div v-else class="no-image">
                <el-icon :size="60"><Picture /></el-icon>
                <p>暂无图片</p>
              </div>
            </div>
            <div class="room-info">
              <h3>{{ room.name }}</h3>
              <p class="room-number">房号：{{ room.roomNumber }}</p>
              <div class="room-details">
                <span><el-icon><User /></el-icon> 容量：{{ room.capacity }}人</span>
                <span><el-icon><House /></el-icon> 面积：{{ room.area }}㎡</span>
              </div>
              <p class="room-purpose">{{ room.purpose }}</p>
              <el-tag
                :type="room.status === 1 ? 'success' : 'danger'"
                style="margin-top: 10px"
              >
                {{ room.status === 1 ? '可预约' : '不可预约' }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-empty v-if="!loading && filteredRooms.length === 0" description="暂无符合条件的会议室" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMeetingRooms } from '@/api/meetingRoom'
import { Picture, User, House, Search, Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const meetingRooms = ref([])
const allMeetingRooms = ref([]) // 保存所有原始数据
const loading = ref(false)

// 查询表单
const queryForm = reactive({
  name: '',
  roomNumber: '',
  minCapacity: null,
  maxCapacity: null,
  status: null
})

// 过滤后的会议室列表
const filteredRooms = computed(() => {
  let result = [...allMeetingRooms.value]
  
  // 按名称过滤
  if (queryForm.name) {
    result = result.filter(room => 
      room.name && room.name.toLowerCase().includes(queryForm.name.toLowerCase())
    )
  }
  
  // 按房号过滤
  if (queryForm.roomNumber) {
    result = result.filter(room => 
      room.roomNumber && room.roomNumber.toLowerCase().includes(queryForm.roomNumber.toLowerCase())
    )
  }
  
  // 按容量范围过滤
  if (queryForm.minCapacity !== null && queryForm.minCapacity > 0) {
    result = result.filter(room => room.capacity >= queryForm.minCapacity)
  }
  if (queryForm.maxCapacity !== null && queryForm.maxCapacity > 0) {
    result = result.filter(room => room.capacity <= queryForm.maxCapacity)
  }
  
  // 按状态过滤
  if (queryForm.status !== null) {
    result = result.filter(room => room.status === queryForm.status)
  }
  
  return result
})

const fetchMeetingRooms = async () => {
  loading.value = true
  try {
    const res = await getMeetingRooms()
    allMeetingRooms.value = res.data || []
    meetingRooms.value = res.data || []
  } catch (error) {
    console.error('获取会议室列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  // 使用computed属性自动过滤，这里可以添加其他逻辑
  console.log('查询条件:', queryForm)
}

// 重置
const handleReset = () => {
  queryForm.name = ''
  queryForm.roomNumber = ''
  queryForm.minCapacity = null
  queryForm.maxCapacity = null
  queryForm.status = null
}

const goToDetail = (id) => {
  router.push(`/meeting-rooms/${id}`)
}

onMounted(() => {
  fetchMeetingRooms()
})
</script>

<style scoped>
.meeting-rooms-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.rooms-card {
  min-height: 400px;
}

.room-card {
  height: 100%;
  transition: transform 0.3s, box-shadow 0.3s;
}

.room-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.room-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 15px;
}

.no-image {
  width: 100%;
  height: 200px;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.no-image p {
  margin-top: 10px;
  font-size: 14px;
}

.room-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 18px;
}

.room-number {
  color: #666;
  font-size: 14px;
  margin: 5px 0;
}

.room-details {
  display: flex;
  justify-content: space-between;
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

.room-details span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.room-purpose {
  color: #909399;
  font-size: 13px;
  margin: 10px 0 0 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>


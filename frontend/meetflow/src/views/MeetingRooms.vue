<template>
  <div class="meeting-rooms-container">
    <el-card class="page-header">
      <h2>会议室列表</h2>
    </el-card>
    
    <el-card class="rooms-card" v-loading="loading">
      <el-row :gutter="20">
        <el-col
          v-for="room in meetingRooms"
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
      
      <el-empty v-if="!loading && meetingRooms.length === 0" description="暂无会议室" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMeetingRooms } from '@/api/meetingRoom'
import { Picture, User, House } from '@element-plus/icons-vue'

const router = useRouter()
const meetingRooms = ref([])
const loading = ref(false)

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


<template>
  <div class="detail-container" v-loading="loading">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button @click="$router.back()" :icon="ArrowLeft">返回</el-button>
          <h2>会议室详情</h2>
        </div>
      </template>
      
      <div v-if="roomDetail" class="detail-content">
        <el-row :gutter="30">
          <el-col :xs="24" :md="12">
            <div class="room-image-large">
              <el-image
                v-if="roomDetail.photoUrl"
                :src="roomDetail.photoUrl"
                fit="cover"
                style="width: 100%; height: 400px; border-radius: 8px"
                :preview-src-list="[roomDetail.photoUrl]"
              />
              <div v-else class="no-image-large">
                <el-icon :size="80"><Picture /></el-icon>
                <p>暂无图片</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :md="12">
            <div class="room-info-detail">
              <h1>{{ roomDetail.name }}</h1>
              <el-divider />
              <div class="info-item">
                <span class="label">房号：</span>
                <span class="value">{{ roomDetail.roomNumber }}</span>
              </div>
              <div class="info-item">
                <span class="label">容量：</span>
                <span class="value">{{ roomDetail.capacity }}人</span>
              </div>
              <div class="info-item">
                <span class="label">面积：</span>
                <span class="value">{{ roomDetail.area }}㎡</span>
              </div>
              <div class="info-item">
                <span class="label">用途：</span>
                <span class="value">{{ roomDetail.purpose }}</span>
              </div>
              <div class="info-item">
                <span class="label">状态：</span>
                <el-tag :type="roomDetail.status === 1 ? 'success' : 'danger'">
                  {{ roomDetail.status === 1 ? '可预约' : '不可预约' }}
                </el-tag>
              </div>
              <el-divider />
              <el-button
                type="primary"
                size="large"
                :disabled="roomDetail.status === 0"
                @click="goToReserve"
                style="width: 100%"
              >
                立即预约
              </el-button>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMeetingRoomDetail } from '@/api/meetingRoom'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const roomDetail = ref(null)
const loading = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getMeetingRoomDetail(route.params.id)
    roomDetail.value = res.data
  } catch (error) {
    console.error('获取会议室详情失败:', error)
  } finally {
    loading.value = false
  }
}

const goToReserve = () => {
  router.push({
    path: '/reserve',
    query: { roomId: roomDetail.value.id }
  })
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.detail-content {
  padding: 20px 0;
}

.room-image-large {
  margin-bottom: 20px;
}

.no-image-large {
  width: 100%;
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.no-image-large p {
  margin-top: 15px;
  font-size: 16px;
}

.room-info-detail h1 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 28px;
}

.info-item {
  margin: 20px 0;
  font-size: 16px;
}

.info-item .label {
  color: #666;
  font-weight: 500;
  margin-right: 10px;
}

.info-item .value {
  color: #333;
}
</style>


<template>
  <div class="confirm-usage-container">
    <el-card class="confirm-card">
      <div class="confirm-content">
        <el-result
          v-if="result"
          :icon="result.icon"
          :title="result.title"
          :sub-title="result.subTitle"
        />
        <div v-else class="confirm-pending-content">
          <el-icon class="confirm-icon" :size="60"><Check /></el-icon>
          <h2 class="confirm-title">确认使用会议室</h2>
          <p class="confirm-subtitle">请确认您要使用该会议室</p>
          <div class="confirm-info" v-if="reservationInfo">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="会议室">
                <strong>{{ reservationInfo.meetingRoomName }}</strong>
              </el-descriptions-item>
              <el-descriptions-item label="会议主题">
                {{ reservationInfo.meetingTitle }}
              </el-descriptions-item>
              <el-descriptions-item label="会议日期">
                {{ formatDate(reservationInfo.reservationDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="会议时间">
                {{ reservationInfo.startTime }}:00 - {{ reservationInfo.endTime }}:00
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="confirm-buttons">
            <el-button 
              type="primary" 
              size="large" 
              :loading="confirming"
              @click="handleConfirm"
              style="border-radius: 16px; padding: 12px 40px;"
            >
              确认使用
            </el-button>
            <el-button 
              size="large" 
              @click="goBack"
              style="border-radius: 16px; padding: 12px 40px; margin-left: 12px;"
            >
              取消
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, SuccessFilled, CircleClose } from '@element-plus/icons-vue'
import { completeReservation, getReservationDetail } from '@/api/reservation'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const result = ref(null)
const confirming = ref(false)
const reservationInfo = ref(null)

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取预约信息
const fetchReservationInfo = async () => {
  const reservationId = route.query.id

  if (!reservationId) {
    result.value = {
      icon: CircleClose,
      title: '参数错误',
      subTitle: '缺少必要的参数，请重新扫描二维码'
    }
    return
  }

  // 检查设备2是否已登录
  if (!userStore.token || !userStore.userInfo) {
    ElMessage.warning('请先登录')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
    result.value = {
      icon: CircleClose,
      title: '请先登录',
      subTitle: '您需要先登录才能确认使用会议室'
    }
    return
  }

  try {
    // 获取预约详情
    const res = await getReservationDetail(reservationId)
    reservationInfo.value = res.data
    
    // 验证设备2的用户是否与预约用户一致
    if (res.data.userId !== userStore.userInfo.id) {
      result.value = {
        icon: CircleClose,
        title: '用户不一致',
        subTitle: '您不是该预约的创建者，无法确认使用'
      }
      return
    }
  } catch (error) {
    console.error('获取预约信息失败:', error)
    const errorCode = error.response?.data?.code
    const errorStatus = error.response?.status
    const errorMessage = error.response?.data?.message || error.message || ''
    
    if (errorStatus === 401 || errorCode === 401 || 
        errorMessage.includes('未登录') || errorMessage.includes('Token') || 
        errorMessage.includes('token') || errorMessage.includes('登录')) {
      ElMessage.warning('请先登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      result.value = {
        icon: CircleClose,
        title: '请先登录',
        subTitle: '您需要先登录才能确认使用会议室'
      }
    } else {
      result.value = {
        icon: CircleClose,
        title: '获取预约信息失败',
        subTitle: '请重新扫描二维码'
      }
    }
  }
}

// 确认使用
const handleConfirm = async () => {
  const reservationId = route.query.id

  if (!reservationId) {
    ElMessage.error('参数错误')
    return
  }

  // 再次检查设备2是否已登录
  if (!userStore.token || !userStore.userInfo) {
    ElMessage.warning('请先登录')
    setTimeout(() => {
      router.push('/login')
    }, 1500)
    result.value = {
      icon: CircleClose,
      title: '请先登录',
      subTitle: '您需要先登录才能确认使用会议室'
    }
    return
  }

  // 再次验证用户是否一致
  if (reservationInfo.value && reservationInfo.value.userId !== userStore.userInfo.id) {
    result.value = {
      icon: CircleClose,
      title: '用户不一致',
      subTitle: '您不是该预约的创建者，无法确认使用'
    }
    return
  }

  confirming.value = true
  try {
    // 使用设备2的登录token来确认使用
    await completeReservation(reservationId)
    result.value = {
      icon: SuccessFilled,
      title: '确认使用成功',
      subTitle: '会议室使用已确认，预约状态已更新为已完成'
    }
  } catch (error) {
    console.error('确认使用失败:', error)
    const errorCode = error.response?.data?.code
    const errorStatus = error.response?.status
    const errorMessage = error.response?.data?.message || error.message || ''
    
    if (errorStatus === 401 || errorCode === 401 || 
        errorMessage.includes('未找到Token') || errorMessage.includes('未登录') || 
        errorMessage.includes('Token') || errorMessage.includes('token') || 
        errorMessage.includes('登录') || errorMessage.includes('过期')) {
      ElMessage.warning('请先登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
      result.value = {
        icon: CircleClose,
        title: '请先登录',
        subTitle: '您需要先登录才能确认使用会议室'
      }
    } else if (errorMessage.includes('无权限') || errorMessage.includes('用户不一致')) {
      result.value = {
        icon: CircleClose,
        title: '用户不一致',
        subTitle: '您不是该预约的创建者，无法确认使用'
      }
    } else {
      result.value = {
        icon: CircleClose,
        title: '确认使用失败',
        subTitle: errorMessage || '确认使用失败，请重试'
      }
    }
  } finally {
    confirming.value = false
  }
}

// 返回
const goBack = () => {
  // 直接返回上一页，如果没有上一页则跳转到登录页
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/login')
  }
}

onMounted(() => {
  fetchReservationInfo()
})
</script>

<style scoped>
.confirm-usage-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.confirm-card {
  max-width: 500px;
  width: 100%;
}

.confirm-content {
  padding: 40px 20px;
}

.confirm-pending-content {
  text-align: center;
  padding: 40px 20px;
}

.confirm-icon {
  color: #409eff;
  margin-bottom: 20px;
}

.confirm-title {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.confirm-subtitle {
  margin: 0 0 30px 0;
  font-size: 14px;
  color: #909399;
}

.confirm-info {
  margin: 30px 0;
  text-align: left;
}

.confirm-info :deep(.el-descriptions-item__label) {
  font-weight: 500;
  width: 100px;
}

.confirm-buttons {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}
</style>


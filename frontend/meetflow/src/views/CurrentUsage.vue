<template>
  <div class="current-usage-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>会议室使用状态</h2>
          <el-button @click="refresh" :icon="Refresh" circle />
        </div>
      </template>

      <!-- 当前使用中的会议室 -->
      <div v-if="usageData?.hasCurrentUsage" class="usage-card">
        <el-row :gutter="30">
          <!-- 左侧：会议室图片 -->
          <el-col :xs="24" :md="10">
            <div class="room-image-container">
              <div class="room-image-wrapper">
                <el-image
                  v-if="hasPhotoUrl(usageData.reservation?.photoUrl)"
                  :src="usageData.reservation.photoUrl"
                  fit="cover"
                  class="room-image"
                  :lazy="false"
                  :preview-src-list="[]"
                >
                  <template #error>
                    <div class="no-image">
                      <el-icon :size="80"><Picture /></el-icon>
                      <p>图片加载失败</p>
                    </div>
                  </template>
                </el-image>
                <div v-else class="no-image">
                  <el-icon :size="80"><Picture /></el-icon>
                  <p>暂无图片</p>
                  <p style="font-size: 12px; margin-top: 5px; color: #909399;">
                    URL: {{ usageData.reservation?.photoUrl || 'null' }}
                  </p>
                </div>
                <div class="image-overlay">
                  <el-tag :type="getStatusType(usageData.status)" size="large" class="status-badge">
                    {{ getStatusText(usageData.status) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-col>
          
          <!-- 右侧：会议信息 -->
          <el-col :xs="24" :md="14">
            <div class="meeting-content">
              <div class="meeting-header">
                <div class="room-name-row">
                  <h3 class="room-name">{{ usageData.reservation?.meetingRoomName }}</h3>
                  <el-tag 
                    v-if="usageData.reservation?.status === 1" 
                    type="warning" 
                    size="large" 
                    class="confirm-status-tag"
                  >
                    待确认使用
                  </el-tag>
                  <el-tag 
                    v-if="usageData.reservation?.status === 4" 
                    type="success" 
                    size="large" 
                    class="confirm-status-tag"
                  >
                    已确认使用
                  </el-tag>
                </div>
                <p class="room-number">
                  <el-icon><Location /></el-icon>
                  <span>{{ usageData.reservation?.roomNumber }}</span>
                </p>
              </div>
              
              <div class="meeting-info">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="会议主题">
                    <span class="meeting-title">{{ usageData.reservation?.meetingTitle }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="会议日期">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(usageData.reservation?.reservationDate) }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="会议时间">
                    <el-icon><Clock /></el-icon>
                    <span>{{ usageData.reservation?.startTime }}:00 - {{ usageData.reservation?.endTime }}:00</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="参会人数">
                    <el-icon><User /></el-icon>
                    <span>{{ usageData.reservation?.attendeeCount }} 人</span>
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="time-display">
                <div class="time-card" :class="getStatusClass(usageData.status)">
                  <div class="time-label">{{ getTimeLabel(usageData.status) }}</div>
                  <div class="time-value">{{ formatTimeDiff(usageData.timeDiffSeconds, usageData.status) }}</div>
                </div>
                <!-- 获取二维码按钮：仅在会议进行中且状态为已通过时显示（已完成状态不显示） -->
                <div v-if="usageData.status === 'ongoing' && usageData.reservation?.status === 1" class="confirm-button-wrapper">
                  <el-button 
                    type="primary" 
                    size="large" 
                    :icon="Link" 
                    @click="showQrCodeDialog"
                    class="confirm-button"
                  >
                    获取二维码
                  </el-button>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 下一个待使用的会议室 -->
      <div v-else-if="usageData?.nextReservation" class="next-usage-card">
        <div class="empty-state">
          <el-icon :size="80" color="#909399" class="empty-icon"><Clock /></el-icon>
          <h3>当前没有使用中的会议室</h3>
        </div>
        
        <div class="next-meeting-card">
          <h3 class="next-title">下一个待使用会议室</h3>
          <el-row :gutter="30">
            <el-col :xs="24" :md="10">
              <div class="next-room-image-wrapper">
                <el-image
                  v-if="hasPhotoUrl(usageData.nextReservation?.photoUrl)"
                  :src="usageData.nextReservation.photoUrl"
                  fit="cover"
                  class="next-room-image"
                  :lazy="false"
                  :preview-src-list="[]"
                >
                  <template #error>
                    <div class="no-image">
                      <el-icon :size="60"><Picture /></el-icon>
                      <p>图片加载失败</p>
                    </div>
                  </template>
                </el-image>
                <div v-else class="no-image">
                  <el-icon :size="60"><Picture /></el-icon>
                  <p>暂无图片</p>
                  <p style="font-size: 12px; margin-top: 5px; color: #909399;">
                    URL: {{ usageData.nextReservation?.photoUrl || 'null' }}
                  </p>
                </div>
              </div>
            </el-col>
            <el-col :xs="24" :md="14">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="会议室">
                  <strong>{{ usageData.nextReservation?.meetingRoomName }}</strong>
                </el-descriptions-item>
                <el-descriptions-item label="房号">
                  <el-icon><Location /></el-icon>
                  <span>{{ usageData.nextReservation?.roomNumber }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="会议主题">
                  {{ usageData.nextReservation?.meetingTitle }}
                </el-descriptions-item>
                <el-descriptions-item label="会议日期">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(usageData.nextReservation?.reservationDate) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="会议时间">
                  <el-icon><Clock /></el-icon>
                  <span>{{ usageData.nextReservation?.startTime }}:00 - {{ usageData.nextReservation?.endTime }}:00</span>
                </el-descriptions-item>
                <el-descriptions-item label="参会人数">
                  <el-icon><User /></el-icon>
                  <span>{{ usageData.nextReservation?.attendeeCount }} 人</span>
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 没有预约 -->
      <el-empty v-else description="暂无预约记录" />
    </el-card>

    <!-- 二维码对话框 -->
    <el-dialog
      v-model="qrCodeDialogVisible"
      title="扫描二维码确认使用"
      width="420px"
      :close-on-click-modal="false"
      class="qr-code-dialog"
    >
      <div class="qr-code-container">
        <div class="qr-code-header">
          <el-icon class="qr-icon"><Link /></el-icon>
          <h3 class="qr-title">扫描二维码</h3>
          <p class="qr-subtitle">使用手机扫描下方二维码即可确认使用会议室</p>
          <div class="qr-link-copy" @click="copyQrCodeUrl" title="点击复制链接">
            <el-icon class="copy-icon"><Link /></el-icon>
            <span>点击复制链接</span>
          </div>
        </div>
        <div class="qr-code-wrapper">
          <div class="qr-code-border">
            <canvas ref="qrCodeCanvas" class="qr-code-canvas"></canvas>
          </div>
        </div>
        <div class="qr-code-footer">
          <el-icon><Phone /></el-icon>
          <span>请使用手机扫描</span>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleCloseQrCodeDialog" style="border-radius: 8px;">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Clock, Picture, Location, Calendar, User, Link, Phone } from '@element-plus/icons-vue'
import { getCurrentUsage } from '@/api/reservation'
import { useUserStore } from '@/stores/user'
import QRCode from 'qrcode'
import dayjs from 'dayjs'

const loading = ref(false)
const usageData = ref(null)
const qrCodeDialogVisible = ref(false)
const qrCodeCanvas = ref(null)
const qrCodeUrl = ref('')
const userStore = useUserStore()
let timer = null

// 获取使用状态
const fetchUsage = async () => {
  loading.value = true
  try {
    const res = await getCurrentUsage()
    usageData.value = res.data
    // 调试：打印完整数据
    console.log('=== 使用状态数据 ===')
    console.log('完整响应:', JSON.stringify(res.data, null, 2))
    if (res.data?.reservation) {
      console.log('预约信息:', res.data.reservation)
      console.log('图片URL:', res.data.reservation.photoUrl)
      console.log('图片URL类型:', typeof res.data.reservation.photoUrl)
      console.log('图片URL是否为空:', !res.data.reservation.photoUrl)
      console.log('图片URL长度:', res.data.reservation.photoUrl?.length)
    }
    if (res.data?.nextReservation) {
      console.log('下一个预约信息:', res.data.nextReservation)
      console.log('下一个预约图片URL:', res.data.nextReservation.photoUrl)
      console.log('下一个预约图片URL类型:', typeof res.data.nextReservation.photoUrl)
    }
    console.log('==================')
  } catch (error) {
    console.error('获取使用状态失败:', error)
    ElMessage.error(error.response?.data?.message || '获取使用状态失败')
  } finally {
    loading.value = false
  }
}

// 刷新
const refresh = () => {
  fetchUsage()
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待开始',
    ongoing: '进行中',
    ended: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    ongoing: 'success',
    ended: 'info'
  }
  return typeMap[status] || ''
}

// 获取时间标签
const getTimeLabel = (status) => {
  const labelMap = {
    pending: '距离会议开始',
    ongoing: '会议已进行',
    ended: '会议已结束'
  }
  return labelMap[status] || ''
}

// 格式化时间差
const formatTimeDiff = (seconds, status) => {
  if (!seconds && seconds !== 0) return '-'
  
  const absSeconds = Math.abs(seconds)
  const hours = Math.floor(absSeconds / 3600)
  const minutes = Math.floor((absSeconds % 3600) / 60)
  const secs = absSeconds % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟${secs}秒`
  } else if (minutes > 0) {
    return `${minutes}分钟${secs}秒`
  } else {
    return `${secs}秒`
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取状态对应的样式类
const getStatusClass = (status) => {
  const classMap = {
    pending: 'status-pending',
    ongoing: 'status-ongoing',
    ended: 'status-ended'
  }
  return classMap[status] || ''
}

// 检查是否有有效的图片URL
const hasPhotoUrl = (photoUrl) => {
  return photoUrl && typeof photoUrl === 'string' && photoUrl.trim().length > 0
}

// 显示二维码对话框
const showQrCodeDialog = async () => {
  try {
    if (!usageData.value?.reservation?.id) {
      ElMessage.error('预约信息不存在')
      return
    }

    // 生成二维码URL（包含预约ID和token）
    const reservationId = usageData.value.reservation.id
    const token = userStore.token
    if (!token) {
      ElMessage.error('请先登录')
      return
    }
    // 使用当前页面的基础URL
    const baseUrl = window.location.origin
    qrCodeUrl.value = `${baseUrl}/confirm-usage?id=${reservationId}&token=${encodeURIComponent(token)}`
    
    qrCodeDialogVisible.value = true
    
    // 等待DOM更新后生成二维码
    await nextTick()
    if (qrCodeCanvas.value) {
      try {
        await QRCode.toCanvas(qrCodeCanvas.value, qrCodeUrl.value, {
          width: 300,
          margin: 2,
          color: {
            dark: '#000000',
            light: '#FFFFFF'
          }
        })
      } catch (error) {
        console.error('生成二维码失败:', error)
        ElMessage.error('生成二维码失败')
      }
    }
  } catch (error) {
    console.error('显示二维码对话框失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 复制二维码链接
const copyQrCodeUrl = async () => {
  if (!qrCodeUrl.value) {
    ElMessage.warning('链接未生成')
    return
  }
  
  try {
    // 使用 Clipboard API 复制链接
    await navigator.clipboard.writeText(qrCodeUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    // 如果 Clipboard API 不可用，使用传统方法
    try {
      const textArea = document.createElement('textarea')
      textArea.value = qrCodeUrl.value
      textArea.style.position = 'fixed'
      textArea.style.opacity = '0'
      document.body.appendChild(textArea)
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
      ElMessage.success('链接已复制到剪贴板')
    } catch (err) {
      console.error('复制失败:', err)
      ElMessage.error('复制失败，请手动复制')
    }
  }
}

// 关闭二维码对话框
const handleCloseQrCodeDialog = () => {
  qrCodeDialogVisible.value = false
  // 刷新使用状态
  fetchUsage()
}

// 更新倒计时
const updateCountdown = () => {
  if (usageData.value?.hasCurrentUsage && usageData.value?.reservation) {
    const now = dayjs()
    const reservation = usageData.value.reservation
    const startDateTime = dayjs(reservation.reservationDate).hour(reservation.startTime).minute(0).second(0)
    const endDateTime = dayjs(reservation.reservationDate).hour(reservation.endTime).minute(0).second(0)
    
    // 开始前1小时和结束后1小时的时间范围
    const rangeStart = startDateTime.subtract(1, 'hour')
    const rangeEnd = endDateTime.add(1, 'hour')
    
    // 检查是否还在范围内
    if (now.isBefore(rangeStart) || now.isAfter(rangeEnd)) {
      // 超出范围，重新获取
      fetchUsage()
      return
    }
    
    if (now.isBefore(startDateTime)) {
      // 待开始
      usageData.value.status = 'pending'
      usageData.value.timeDiffSeconds = startDateTime.diff(now, 'second')
    } else if (now.isBefore(endDateTime)) {
      // 进行中
      usageData.value.status = 'ongoing'
      usageData.value.timeDiffSeconds = now.diff(startDateTime, 'second')
    } else {
      // 已结束（在结束后1小时内）
      usageData.value.status = 'ended'
      usageData.value.timeDiffSeconds = now.diff(endDateTime, 'second')
    }
  }
}

onMounted(() => {
  fetchUsage()
  // 每秒更新一次倒计时
  timer = setInterval(() => {
    updateCountdown()
  }, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.current-usage-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.usage-card {
  padding: 30px;
}

/* 会议室图片容器 */
.room-image-container {
  margin-bottom: 20px;
}

.room-image-wrapper {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.room-image-wrapper:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}

.room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-image :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #909399;
}

.no-image p {
  margin-top: 10px;
  font-size: 14px;
}

.image-overlay {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 10;
}

.status-badge {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* 会议内容 */
.meeting-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.meeting-header {
  margin-bottom: 25px;
}

.room-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.room-name {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.confirm-status-tag {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 16px;
  border-radius: 16px;
}

.room-number {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 16px;
}

.meeting-info {
  margin-bottom: 25px;
  flex: 1;
}

.meeting-title {
  font-weight: 500;
  color: #303133;
}

.meeting-info :deep(.el-descriptions-item__label) {
  font-weight: 500;
  width: 100px;
}

.meeting-info :deep(.el-descriptions-item__content) {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 时间显示 */
.time-display {
  margin-top: auto;
}

.confirm-button-wrapper {
  margin-top: 20px;
  text-align: center;
}

.confirm-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 16px;
}

.time-card {
  text-align: center;
  padding: 35px 40px;
  border-radius: 16px;
  color: white;
  min-width: 100%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.time-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
}

.status-pending {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.status-ongoing {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.status-ended {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.time-label {
  font-size: 18px;
  margin-bottom: 20px;
  opacity: 0.95;
  font-weight: 500;
}

.time-value {
  font-size: 42px;
  font-weight: bold;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 下一个会议室 */
.next-usage-card {
  padding: 30px;
}

.empty-state {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px;
}

.empty-icon {
  margin-bottom: 15px;
}

.empty-state h3 {
  margin: 0;
  font-size: 20px;
  color: #909399;
  font-weight: 500;
}

.next-meeting-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.next-title {
  margin: 0 0 25px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  padding-bottom: 15px;
  border-bottom: 2px solid #e4e7ed;
}

.next-room-image-wrapper {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.next-room-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.next-room-image :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.next-meeting-card :deep(.el-descriptions-item__label) {
  font-weight: 500;
  width: 100px;
}

.next-meeting-card :deep(.el-descriptions-item__content) {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .room-image-wrapper {
    height: 300px;
  }
  
  .room-name {
    font-size: 24px;
  }
  
  .room-name-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .confirm-status-tag {
    font-size: 12px;
    padding: 4px 12px;
  }
  
  .time-value {
    font-size: 32px;
  }
  
  .next-room-image-wrapper {
    height: 250px;
    margin-bottom: 20px;
  }
}

/* 二维码对话框样式 */
.qr-code-dialog :deep(.el-dialog__header) {
  text-align: center;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.qr-code-dialog :deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.qr-code-container {
  padding: 30px 20px;
}

.qr-code-header {
  text-align: center;
  margin-bottom: 30px;
}

.qr-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 12px;
}

.qr-link-copy {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
  padding: 10px 16px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #409eff;
  font-size: 14px;
}

.qr-link-copy:hover {
  background: #e1f3ff;
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.qr-link-copy .copy-icon {
  font-size: 18px;
}

.qr-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.qr-subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
}

.qr-code-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.qr-code-border {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 2px solid #e4e7ed;
  display: inline-block;
}

.qr-code-canvas {
  display: block;
  border-radius: 8px;
  background: white;
  padding: 8px;
}

.qr-code-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #606266;
  font-size: 14px;
}

.qr-code-footer .el-icon {
  font-size: 18px;
  color: #409eff;
}
</style>


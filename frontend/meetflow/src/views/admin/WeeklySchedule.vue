<template>
  <div class="schedule-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <el-icon class="title-icon"><Calendar /></el-icon>
            <h2>每周预约情况</h2>
          </div>
          <div class="header-controls">
            <el-date-picker
              v-model="weekStartDate"
              type="date"
              placeholder="选择周开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="fetchWeeklyData"
              style="margin-right: 10px"
              size="default"
            />
            <el-button-group>
              <el-button :icon="ArrowLeft" @click="prevWeek">上一周</el-button>
              <el-button @click="currentWeek" type="primary">本周</el-button>
              <el-button :icon="ArrowRight" @click="nextWeek">下一周</el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <div v-loading="loading" class="schedule-content">
        <div class="schedule-table-wrapper">
          <table class="schedule-table">
            <thead>
              <tr>
                <th class="room-header">会议室</th>
                <th
                  v-for="day in days"
                  :key="day.date"
                  class="day-header"
                  :class="{ 'is-today': day.isToday, 'is-weekend': day.isWeekend }"
                >
                  <div class="day-label">{{ day.label }}</div>
                  <div class="day-date">{{ day.date }}</div>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="room in meetingRooms"
                :key="room.id"
                class="room-row"
              >
                <td class="room-name-cell">
                  <div class="room-name">{{ room.name }}</div>
                  <div class="room-number">{{ room.roomNumber }}</div>
                </td>
                <td
                  v-for="day in days"
                  :key="`${room.id}-${day.date}`"
                  class="day-cell"
                  :class="{ 'is-today': day.isToday, 'is-weekend': day.isWeekend }"
                >
                  <div class="reservations-container">
                    <div
                      v-for="(reservation, index) in getReservationsForCell(room.id, day.date)"
                      :key="index"
                      class="reservation-block"
                      :class="getReservationClass(reservation.status)"
                      :style="getReservationStyle(reservation)"
                      :title="getReservationTooltip(reservation)"
                    >
                      <div class="reservation-header">
                        <div class="reservation-time">
                          <el-icon class="time-icon"><Clock /></el-icon>
                          {{ reservation.startTime }}:00-{{ reservation.endTime }}:00
                        </div>
                        <div class="reservation-status-badge" :class="getReservationClass(reservation.status)">
                          {{ getStatusText(reservation.status) }}
                        </div>
                      </div>
                      <div class="reservation-title">{{ reservation.meetingTitle }}</div>
                      <div class="reservation-footer">
                        <div class="reservation-user">
                          <el-icon class="user-icon"><User /></el-icon>
                          {{ getUserName(reservation) }}
                        </div>
                        <div class="reservation-count">
                          <el-icon class="count-icon"><UserFilled /></el-icon>
                          {{ reservation.attendeeCount }}人
                        </div>
                      </div>
                    </div>
                    <div v-if="getReservationsForCell(room.id, day.date).length === 0" class="empty-hint">
                      <el-icon><CircleCheck /></el-icon>
                      <span>空闲</span>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 图例说明 -->
        <div class="legend">
          <div class="legend-title">状态说明：</div>
          <div class="legend-items">
            <div class="legend-item status-pending">
              <span class="legend-color"></span>
              <span>待审批</span>
            </div>
            <div class="legend-item status-approved">
              <span class="legend-color"></span>
              <span>已通过</span>
            </div>
            <div class="legend-item status-completed">
              <span class="legend-color"></span>
              <span>已完成</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWeeklyReservations } from '@/api/reservation'
import { getMeetingRooms } from '@/api/meetingRoom'
import { Calendar, ArrowLeft, ArrowRight, Clock, User, UserFilled, CircleCheck } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const weekStartDate = ref(dayjs().startOf('week').format('YYYY-MM-DD'))
const meetingRooms = ref([])
const weeklyReservations = ref([])

const days = computed(() => {
  const start = dayjs(weekStartDate.value)
  const today = dayjs()
  return Array.from({ length: 7 }, (_, i) => {
    const date = start.add(i, 'day')
    const dayOfWeek = date.day()
    return {
      date: date.format('YYYY-MM-DD'),
      label: `${date.format('MM-DD')} ${['日', '一', '二', '三', '四', '五', '六'][dayOfWeek]}`,
      isToday: date.isSame(today, 'day'),
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6
    }
  })
})

const getReservationsForCell = (roomId, date) => {
  return weeklyReservations.value.filter(r => {
    const reservationRoomId = r.meetingRoom?.id || r.meetingRoomId
    return reservationRoomId === roomId && r.reservationDate === date
  }).sort((a, b) => a.startTime - b.startTime)
}

const getReservationClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-approved',
    2: 'status-rejected',
    3: 'status-cancelled',
    4: 'status-completed'
  }
  return classMap[status] || ''
}

const getReservationStyle = (reservation) => {
  // 固定大小，不再根据时间段动态计算
  return {}
}

const getReservationTooltip = (reservation) => {
  const userName = getUserName(reservation)
  const statusText = getStatusText(reservation.status)
  return `${reservation.meetingTitle}\n预约人：${userName}\n时间：${reservation.startTime}:00-${reservation.endTime}:00\n状态：${statusText}\n人数：${reservation.attendeeCount}人`
}

const getUserName = (reservation) => {
  if (reservation.user?.name) return reservation.user.name
  if (reservation.user?.username) return reservation.user.username
  if (reservation.userName) return reservation.userName
  if (reservation.bookerName) return reservation.bookerName
  return '未知'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待审批',
    1: '已通过',
    2: '已驳回',
    3: '已取消',
    4: '已完成'
  }
  return statusMap[status] || '未知'
}

const fetchWeeklyData = async () => {
  loading.value = true
  try {
    // 获取会议室列表
    const roomsRes = await getMeetingRooms()
    meetingRooms.value = roomsRes.data || []
    
    // 获取预约数据
    const res = await getWeeklyReservations(weekStartDate.value)
    weeklyReservations.value = res.data || []
  } catch (error) {
    console.error('获取每周预约情况失败:', error)
  } finally {
    loading.value = false
  }
}

const prevWeek = () => {
  weekStartDate.value = dayjs(weekStartDate.value).subtract(7, 'day').format('YYYY-MM-DD')
  fetchWeeklyData()
}

const nextWeek = () => {
  weekStartDate.value = dayjs(weekStartDate.value).add(7, 'day').format('YYYY-MM-DD')
  fetchWeeklyData()
}

const currentWeek = () => {
  weekStartDate.value = dayjs().startOf('week').format('YYYY-MM-DD')
  fetchWeeklyData()
}

onMounted(() => {
  fetchWeeklyData()
})
</script>

<style scoped>
.schedule-container {
  max-width: 1800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
  color: #818cf8;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

.header-controls {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.schedule-content {
  overflow-x: auto;
  margin-top: 20px;
}

.schedule-table-wrapper {
  overflow-x: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  table-layout: auto;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #e4e7ed;
  padding: 8px;
  vertical-align: top;
}

.schedule-table thead {
  background: linear-gradient(135deg, #a5b4fc 0%, #c4b5fd 100%);
  color: white;
}

.room-header {
  width: 160px;
  text-align: center;
  font-weight: 600;
  background: linear-gradient(135deg, #a5b4fc 0%, #c4b5fd 100%);
  color: white;
  position: sticky;
  left: 0;
  z-index: 10;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.1);
}

.day-header {
  width: 180px;
  text-align: center;
  font-weight: 600;
  background: linear-gradient(135deg, #a5b4fc 0%, #c4b5fd 100%);
  color: white;
  transition: all 0.3s;
}

.day-header.is-today {
  background: linear-gradient(135deg, #fca5a5 0%, #f87171 100%);
  box-shadow: 0 2px 8px rgba(248, 113, 113, 0.2);
}

.day-header.is-weekend {
  background: linear-gradient(135deg, #bbf7d0 0%, #86efac 100%);
}

.day-label {
  font-size: 15px;
  color: white;
  margin-bottom: 4px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.day-date {
  font-size: 12px;
  color: white;
  opacity: 0.95;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.room-name-cell {
  background: #f8f9fa;
  position: sticky;
  left: 0;
  z-index: 5;
  text-align: center;
  padding: 12px 8px;
  border-right: 2px solid #e4e7ed;
  transition: all 0.3s;
}

.room-name-cell:hover {
  background: #f0f2f5;
}

.room-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  font-size: 14px;
}

.room-number {
  font-size: 12px;
  color: #818cf8;
  font-weight: 500;
  padding: 2px 8px;
  background: rgba(129, 140, 248, 0.1);
  border-radius: 12px;
  display: inline-block;
}

.day-cell {
  width: 180px;
  max-width: 180px;
  background: white;
  transition: all 0.3s;
  position: relative;
}

.day-cell.is-today {
  background: #fff5f5;
}

.day-cell.is-weekend {
  background: #f0fdf4;
}

.day-cell:hover {
  background: #f8f9fa;
}

.reservations-container {
  position: relative;
  padding: 3px;
}

.reservation-block {
  margin-bottom: 4px;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 3px solid;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  min-height: 75px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.reservation-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  flex-shrink: 0;
}

.reservation-time {
  font-weight: 600;
  font-size: 11px;
  color: #4b5563;
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
}

.time-icon {
  font-size: 12px;
}

.reservation-status-badge {
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 8px;
  font-weight: 600;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  letter-spacing: 0.3px;
}

.reservation-title {
  font-size: 12px;
  color: #4b5563;
  font-weight: 600;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
  flex-shrink: 0;
}

.reservation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
  margin-top: auto;
  padding-top: 4px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.reservation-user {
  font-size: 10px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 3px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-icon {
  font-size: 11px;
  color: #667eea;
}

.reservation-count {
  font-size: 10px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 3px;
}

.count-icon {
  font-size: 11px;
  color: #52c41a;
}

.empty-hint {
  text-align: center;
  color: #d1d5db;
  font-size: 11px;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  opacity: 0.6;
}

.empty-hint .el-icon {
  font-size: 18px;
}

.status-pending {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-left-color: #f59e0b;
  color: #78350f;
}

.status-pending .reservation-status-badge {
  background: linear-gradient(135deg, #d97706 0%, #f59e0b 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(245, 158, 11, 0.2);
}

.status-approved {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-left-color: #10b981;
  color: #064e3b;
}

.status-approved .reservation-status-badge {
  background: linear-gradient(135deg, #059669 0%, #10b981 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(16, 185, 129, 0.2);
}

.status-completed {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-left-color: #3b82f6;
  color: #1e3a8a;
}

.status-completed .reservation-status-badge {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
  box-shadow: 0 1px 3px rgba(59, 130, 246, 0.2);
}

.legend {
  margin-top: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.legend-title {
  font-weight: 600;
  color: #333;
}

.legend-items {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  border-left: 4px solid;
}

.legend-item.status-pending .legend-color {
  background-color: #fef3c7;
  border-left-color: #f59e0b;
}

.legend-item.status-approved .legend-color {
  background-color: #d1fae5;
  border-left-color: #10b981;
}

.legend-item.status-completed .legend-color {
  background-color: #dbeafe;
  border-left-color: #3b82f6;
}
</style>

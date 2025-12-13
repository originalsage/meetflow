<template>
  <div class="schedule-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>每周预约情况</h2>
          <div class="header-controls">
            <el-date-picker
              v-model="weekStartDate"
              type="date"
              placeholder="选择周开始日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="fetchWeeklyData"
              style="margin-right: 10px"
            />
            <el-button @click="prevWeek">上一周</el-button>
            <el-button @click="nextWeek">下一周</el-button>
            <el-button @click="currentWeek">本周</el-button>
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
                      <div class="reservation-time">
                        {{ reservation.startTime }}:00-{{ reservation.endTime }}:00
                      </div>
                      <div class="reservation-title">{{ reservation.meetingTitle }}</div>
                      <div class="reservation-user">
                        {{ getUserName(reservation) }}
                      </div>
                      <div class="reservation-status">
                        {{ getStatusText(reservation.status) }}
                      </div>
                    </div>
                    <div v-if="getReservationsForCell(room.id, day.date).length === 0" class="empty-hint">
                      空闲
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
            <div class="legend-item status-rejected">
              <span class="legend-color"></span>
              <span>已驳回</span>
            </div>
            <div class="legend-item status-cancelled">
              <span class="legend-color"></span>
              <span>已取消</span>
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
import dayjs from 'dayjs'

const loading = ref(false)
const weekStartDate = ref(dayjs().startOf('week').format('YYYY-MM-DD'))
const meetingRooms = ref([])
const weeklyReservations = ref([])

const days = computed(() => {
  const start = dayjs(weekStartDate.value)
  return Array.from({ length: 7 }, (_, i) => {
    const date = start.add(i, 'day')
    return {
      date: date.format('YYYY-MM-DD'),
      label: `${date.format('MM-DD')} ${['日', '一', '二', '三', '四', '五', '六'][date.day()]}`
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
  const duration = reservation.endTime - reservation.startTime
  return {
    minHeight: `${Math.max(60, duration * 20)}px`
  }
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
  gap: 10px;
}

.card-header h2 {
  margin: 0;
  color: #333;
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
  border-radius: 4px;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  min-width: 1000px;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #e4e7ed;
  padding: 8px;
  vertical-align: top;
}

.schedule-table thead {
  background: #f5f7fa;
}

.room-header {
  width: 150px;
  text-align: center;
  font-weight: 600;
  background: #fafafa;
  position: sticky;
  left: 0;
  z-index: 10;
}

.day-header {
  min-width: 180px;
  text-align: center;
  font-weight: 600;
  background: #fafafa;
}

.day-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.day-date {
  font-size: 12px;
  color: #666;
}

.room-name-cell {
  background: #fafafa;
  position: sticky;
  left: 0;
  z-index: 5;
  text-align: center;
}

.room-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.room-number {
  font-size: 12px;
  color: #999;
}

.day-cell {
  min-height: 100px;
  background: white;
}

.reservations-container {
  min-height: 100px;
  position: relative;
}

.reservation-block {
  margin-bottom: 6px;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid;
  position: relative;
}

.reservation-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1;
}

.reservation-time {
  font-weight: 600;
  font-size: 12px;
  color: #333;
  margin-bottom: 4px;
}

.reservation-title {
  font-size: 13px;
  color: #333;
  font-weight: 500;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reservation-user {
  font-size: 11px;
  color: #666;
  margin-bottom: 2px;
}

.reservation-status {
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}

.empty-hint {
  text-align: center;
  color: #ccc;
  font-size: 12px;
  padding: 20px 0;
}

.status-pending {
  background-color: #fff7e6;
  border-left-color: #faad14;
}

.status-approved {
  background-color: #f6ffed;
  border-left-color: #52c41a;
}

.status-rejected {
  background-color: #fff2f0;
  border-left-color: #ff4d4f;
}

.status-cancelled {
  background-color: #f5f5f5;
  border-left-color: #d9d9d9;
}

.status-completed {
  background-color: #e6f7ff;
  border-left-color: #1890ff;
}

.legend {
  margin-top: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
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
  background-color: #fff7e6;
  border-left-color: #faad14;
}

.legend-item.status-approved .legend-color {
  background-color: #f6ffed;
  border-left-color: #52c41a;
}

.legend-item.status-rejected .legend-color {
  background-color: #fff2f0;
  border-left-color: #ff4d4f;
}

.legend-item.status-cancelled .legend-color {
  background-color: #f5f5f5;
  border-left-color: #d9d9d9;
}

.legend-item.status-completed .legend-color {
  background-color: #e6f7ff;
  border-left-color: #1890ff;
}
</style>

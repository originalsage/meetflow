<template>
  <div class="schedule-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>当天占用情况</h2>
          <div class="header-controls">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="fetchDailyData"
              style="margin-right: 10px"
            />
            <el-button @click="prevDay">前一天</el-button>
            <el-button @click="nextDay">后一天</el-button>
            <el-button @click="today">今天</el-button>
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
                  v-for="hour in hours"
                  :key="hour"
                  class="hour-header"
                >
                  {{ hour }}:00
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
                  v-for="hour in hours"
                  :key="`${room.id}-${hour}`"
                  class="hour-cell"
                  :class="{ 'has-reservation': hasReservationAtHour(room.id, hour) }"
                >
                  <template v-if="getReservationAtHour(room.id, hour)">
                    <div
                      v-if="getReservationAtHour(room.id, hour).startTime === hour"
                      class="reservation-block"
                      :class="getReservationClass(getReservationAtHour(room.id, hour).status)"
                      :style="getReservationBlockStyle(getReservationAtHour(room.id, hour))"
                      :title="getReservationTooltip(getReservationAtHour(room.id, hour))"
                    >
                      <div class="reservation-time">
                        {{ getReservationAtHour(room.id, hour).startTime }}:00-{{ getReservationAtHour(room.id, hour).endTime }}:00
                      </div>
                      <div class="reservation-title">
                        {{ getReservationAtHour(room.id, hour).meetingTitle }}
                      </div>
                      <div class="reservation-user">
                        {{ getUserName(getReservationAtHour(room.id, hour)) }}
                      </div>
                      <div class="reservation-status">
                        {{ getStatusText(getReservationAtHour(room.id, hour).status) }}
                      </div>
                    </div>
                    <div
                      v-else
                      class="reservation-continuation"
                      :class="getReservationClass(getReservationAtHour(room.id, hour).status)"
                    ></div>
                  </template>
                  <div v-else class="empty-hint"></div>
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
import { getDailyReservations } from '@/api/reservation'
import { getMeetingRooms } from '@/api/meetingRoom'
import dayjs from 'dayjs'

const loading = ref(false)
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const meetingRooms = ref([])
const dailyReservations = ref([])

const hours = Array.from({ length: 24 }, (_, i) => i)

// 获取某个会议室在某个小时的预约（返回开始时间等于该小时的预约）
const getReservationAtHour = (roomId, hour) => {
  const reservationRoomId = (r) => r.meetingRoom?.id || r.meetingRoomId || r.roomId
  return dailyReservations.value.find(r => {
    return reservationRoomId(r) === roomId && 
           r.startTime === hour && 
           r.reservationDate === selectedDate.value
  })
}

// 检查某个会议室在某个小时是否有预约（包括跨小时的预约）
const hasReservationAtHour = (roomId, hour) => {
  const reservationRoomId = (r) => r.meetingRoom?.id || r.meetingRoomId || r.roomId
  return dailyReservations.value.some(r => {
    return reservationRoomId(r) === roomId && 
           r.startTime <= hour && 
           r.endTime > hour &&
           r.reservationDate === selectedDate.value
  })
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

const getReservationBlockStyle = (reservation) => {
  const duration = reservation.endTime - reservation.startTime
  return {
    width: `${duration * 100 - 2}%`,
    minWidth: `${duration * 80}px`
  }
}

const getReservationTooltip = (reservation) => {
  const userName = getUserName(reservation)
  const statusText = getStatusText(reservation.status)
  return `${reservation.meetingTitle}\n预约人：${userName}\n时间：${reservation.startTime}:00-${reservation.endTime}:00\n状态：${statusText}\n人数：${reservation.attendeeCount}人`
}

const getUserName = (reservation) => {
  if (!reservation) return '-'
  if (reservation.user?.name) return reservation.user.name
  if (reservation.user?.username) return reservation.user.username
  if (reservation.userName) return reservation.userName
  if (reservation.bookerName) return reservation.bookerName
  if (reservation.booker?.name) return reservation.booker.name
  if (reservation.booker?.username) return reservation.booker.username
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

const fetchDailyData = async () => {
  loading.value = true
  try {
    // 获取会议室列表
    const roomsRes = await getMeetingRooms()
    meetingRooms.value = roomsRes.data || []
    
    // 获取预约数据
    const res = await getDailyReservations(selectedDate.value)
    dailyReservations.value = res.data || []
    
    // 调试：打印数据
    if (dailyReservations.value.length > 0) {
      console.log('当天预约数据:', dailyReservations.value)
    }
  } catch (error) {
    console.error('获取当天占用情况失败:', error)
  } finally {
    loading.value = false
  }
}

const prevDay = () => {
  selectedDate.value = dayjs(selectedDate.value).subtract(1, 'day').format('YYYY-MM-DD')
  fetchDailyData()
}

const nextDay = () => {
  selectedDate.value = dayjs(selectedDate.value).add(1, 'day').format('YYYY-MM-DD')
  fetchDailyData()
}

const today = () => {
  selectedDate.value = dayjs().format('YYYY-MM-DD')
  fetchDailyData()
}

onMounted(() => {
  fetchDailyData()
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
  min-width: 1200px;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #e4e7ed;
  padding: 0;
  vertical-align: top;
  position: relative;
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
  padding: 12px 8px;
}

.hour-header {
  min-width: 100px;
  text-align: center;
  font-weight: 600;
  background: #fafafa;
  padding: 12px 8px;
  font-size: 13px;
}

.room-name-cell {
  background: #fafafa;
  position: sticky;
  left: 0;
  z-index: 5;
  text-align: center;
  padding: 12px 8px;
}

.room-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
}

.room-number {
  font-size: 12px;
  color: #999;
}

.hour-cell {
  min-width: 100px;
  height: 80px;
  padding: 2px;
  background: white;
  position: relative;
}

.hour-cell.has-reservation {
  background: #f9f9f9;
}

.empty-hint {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e0e0e0;
  font-size: 12px;
}

.reservation-block {
  position: absolute;
  left: 2px;
  top: 2px;
  bottom: 2px;
  padding: 6px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid;
  z-index: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.reservation-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 10;
}

.reservation-continuation {
  width: 100%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  border-left: 4px solid;
  opacity: 0.6;
}

.reservation-time {
  font-weight: 600;
  font-size: 11px;
  color: #333;
  margin-bottom: 3px;
  white-space: nowrap;
}

.reservation-title {
  font-size: 12px;
  color: #333;
  font-weight: 500;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
}

.reservation-user {
  font-size: 10px;
  color: #666;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reservation-status {
  font-size: 9px;
  color: #999;
  margin-top: 2px;
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

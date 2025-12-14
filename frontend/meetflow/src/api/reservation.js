import request from '@/utils/request'

// 创建预约
export const createReservation = (data) => {
  return request.post('/reservations', data)
}

// 查询我的预约记录
export const getMyReservations = (params) => {
  return request.get('/reservations/my', { params })
}

// 获取预约详情
export const getReservationDetail = (id) => {
  return request.get(`/reservations/${id}`)
}

// 取消预约
export const cancelReservation = (id) => {
  return request.put(`/reservations/${id}/cancel`)
}

// 查询所有预约记录（管理员）
export const getAllReservations = (params) => {
  return request.get('/reservations/all', { params })
}

// 审批预约（管理员）
export const approveReservation = (id, data = {}) => {
  return request.put(`/reservations/${id}/approve`, data)
}

// 获取一周预约情况（管理员）
export const getWeeklyReservations = (weekStartDate) => {
  return request.get('/reservations/weekly', {
    params: { weekStartDate }
  })
}

// 获取当天各时段占用情况（管理员）
export const getDailyReservations = (date) => {
  return request.get('/reservations/daily', {
    params: { date }
  })
}

// 获取用户当前会议室使用状态
export const getCurrentUsage = () => {
  return request.get('/reservations/current-usage')
}


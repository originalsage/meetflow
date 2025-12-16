import request from '@/utils/request'

// 创建预约
export const createReservation = (data) => {
  return request.post('/reservations', data)
}

// 查询我的预约记录
export const getMyReservations = (params) => {
  return request.get('/reservations/my', { params })
}

// 分页查询我的预约记录
export const getMyReservationsPage = (params) => {
  return request.get('/reservations/my/page', { params })
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

// 分页查询所有预约记录（管理员）
export const getAllReservationsPage = (params) => {
  return request.get('/reservations/all/page', { params })
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

// 完成预约（确认使用）
export const completeReservation = (id) => {
  return request.put(`/reservations/${id}/complete`)
}

// 通过token完成预约（确认使用）
export const completeReservationByToken = (id, token) => {
  return request.put(`/reservations/${id}/complete-by-token`, null, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
}

// 统计用户预约记录总数（用于检查用户是否有预约）
// 注意：这里需要管理员权限，或者使用其他方式检查
export const countByUserId = async (userId) => {
  // 由于前端无法直接查询其他用户的预约，我们通过尝试删除来检查
  // 或者可以添加一个管理员API来检查
  // 这里先返回一个简单的实现，实际应该调用后端API
  try {
    // 这里应该调用一个管理员API来检查用户预约数量
    // 暂时返回false，由后端返回错误信息
    return false
  } catch (error) {
    return false
  }
}


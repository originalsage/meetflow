import request from '@/utils/request'

// 获取所有会议室列表
export const getMeetingRooms = () => {
  return request.get('/meeting-rooms')
}

// 获取会议室详情
export const getMeetingRoomDetail = (id) => {
  return request.get(`/meeting-rooms/${id}`)
}

// 根据条件筛选可用会议室
export const getAvailableMeetingRooms = (params) => {
  return request.get('/meeting-rooms/available', { params })
}

// 添加会议室（管理员）
export const addMeetingRoom = (data) => {
  return request.post('/meeting-rooms', data)
}

// 修改会议室（管理员）
export const updateMeetingRoom = (id, data) => {
  return request.put(`/meeting-rooms/${id}`, data)
}

// 删除会议室（管理员）
export const deleteMeetingRoom = (id) => {
  return request.delete(`/meeting-rooms/${id}`)
}

// 设置会议室状态（管理员）
export const setMeetingRoomStatus = (id, status) => {
  return request.put(`/meeting-rooms/${id}/status`, null, {
    params: { status }
  })
}

// 上传会议室照片（管理员）
export const uploadMeetingRoomPhoto = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/meeting-rooms/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


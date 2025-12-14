import request from '@/utils/request'

// 获取所有用户列表（超级管理员）
export const getAllUsers = () => {
  return request.get('/users')
}

// 删除用户（超级管理员）
export const deleteUser = (id) => {
  return request.delete(`/users/${id}`)
}

// 提升用户权限（超级管理员）
export const promoteUser = (data) => {
  return request.put('/users/promote', data)
}

// 降级用户权限（超级管理员）
export const demoteUser = (data) => {
  return request.put('/users/demote', data)
}


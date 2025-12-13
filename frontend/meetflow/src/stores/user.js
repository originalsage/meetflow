import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 注册
  const register = async (registerData) => {
    try {
      const res = await request.post('/auth/register', registerData)
      return res
    } catch (error) {
      throw error
    }
  }

  // 登录
  const login = async (username, password, role) => {
    try {
      const res = await request.post('/auth/login', {
        username,
        password,
        role
      })
      token.value = res.data.token
      userInfo.value = res.data.userInfo
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
      return res
    } catch (error) {
      throw error
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const res = await request.get('/auth/info')
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      return res
    } catch (error) {
      throw error
    }
  }

  // 更新用户信息
  const updateUser = async (updateData) => {
    try {
      const res = await request.put('/auth/update', updateData)
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      return res
    } catch (error) {
      throw error
    }
  }

  // 退出登录
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 是否为管理员
  const isAdmin = () => {
    return userInfo.value?.role === 1
  }

  return {
    token,
    userInfo,
    register,
    login,
    getUserInfo,
    updateUser,
    logout,
    isAdmin
  }
})


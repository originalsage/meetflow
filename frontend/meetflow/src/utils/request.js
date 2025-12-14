import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建axios实例
// 优先使用环境变量，如果没有则使用相对路径（通过Vite代理）
// 如果在生产环境或需要直接指定后端地址，可以通过环境变量 VITE_API_BASE_URL 配置
const apiBaseURL = import.meta.env.VITE_API_BASE_URL || '/api'

const service = axios.create({
  baseURL: apiBaseURL,
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      // 401: 未登录或token过期
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        // 跳转到登录页
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    console.error('响应错误:', error)
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service


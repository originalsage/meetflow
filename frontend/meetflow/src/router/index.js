import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/Index.vue'),
    redirect: '/meeting-rooms',
    children: [
      {
        path: 'meeting-rooms',
        name: 'MeetingRooms',
        component: () => import('@/views/MeetingRooms.vue'),
        meta: { title: '会议室列表' }
      },
      {
        path: 'meeting-rooms/:id',
        name: 'MeetingRoomDetail',
        component: () => import('@/views/MeetingRoomDetail.vue'),
        meta: { title: '会议室详情' }
      },
      {
        path: 'reserve',
        name: 'Reserve',
        component: () => import('@/views/Reserve.vue'),
        meta: { title: '预约会议室' }
      },
      {
        path: 'my-reservations',
        name: 'MyReservations',
        component: () => import('@/views/MyReservations.vue'),
        meta: { title: '我的预约' }
      },
      // 管理员路由
      {
        path: 'admin/meeting-rooms',
        name: 'AdminMeetingRooms',
        component: () => import('@/views/admin/MeetingRoomManage.vue'),
        meta: { title: '会议室管理', requiresAdmin: true }
      },
      {
        path: 'admin/reservations',
        name: 'AdminReservations',
        component: () => import('@/views/admin/ReservationManage.vue'),
        meta: { title: '预约管理', requiresAdmin: true }
      },
      {
        path: 'admin/weekly-schedule',
        name: 'WeeklySchedule',
        component: () => import('@/views/admin/WeeklySchedule.vue'),
        meta: { title: '每周预约情况', requiresAdmin: true }
      },
      {
        path: 'admin/daily-schedule',
        name: 'DailySchedule',
        component: () => import('@/views/admin/DailySchedule.vue'),
        meta: { title: '当天占用情况', requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 如果访问登录页或注册页且已登录，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.token) {
    next('/')
    return
  }
  
  // 如果访问需要登录的页面且未登录，跳转到登录页
  if (to.path !== '/login' && to.path !== '/register' && !userStore.token) {
    next('/login')
    return
  }
  
  // 如果访问需要管理员权限的页面
  if (to.meta.requiresAdmin && !userStore.isAdmin()) {
    ElMessage.error('无权限访问')
    next('/')
    return
  }
  
  next()
})

export default router


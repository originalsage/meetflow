<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-left">
        <h2>会议室预约系统</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo?.name || userStore.userInfo?.username }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="aside">
        <el-menu
          :default-active="activeMenu"
          router
          class="menu"
        >
          <el-menu-item index="/meeting-rooms">
            <el-icon><OfficeBuilding /></el-icon>
            <span>会议室列表</span>
          </el-menu-item>
          <el-menu-item index="/reserve">
            <el-icon><Calendar /></el-icon>
            <span>预约会议室</span>
          </el-menu-item>
          <el-menu-item index="/my-reservations">
            <el-icon><Document /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
          <el-divider v-if="userStore.isAdmin()" />
          <template v-if="userStore.isAdmin()">
            <el-menu-item index="/admin/meeting-rooms">
              <el-icon><Setting /></el-icon>
              <span>会议室管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/reservations">
              <el-icon><List /></el-icon>
              <span>预约管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/weekly-schedule">
              <el-icon><Grid /></el-icon>
              <span>每周预约情况</span>
            </el-menu-item>
            <el-menu-item index="/admin/daily-schedule">
              <el-icon><Clock /></el-icon>
              <span>当天占用情况</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  User,
  ArrowDown,
  OfficeBuilding,
  Calendar,
  Document,
  Setting,
  List,
  Grid,
  Clock
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.aside {
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
}

.menu {
  border-right: none;
  height: 100%;
}

.main {
  background-color: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>


<template>
  <div class="manage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
        </div>
      </template>
      
      <el-table
        :data="users"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="name" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.role === 0"
              type="primary"
              size="small"
              @click="handlePromote(row)"
            >
              提升为管理员
            </el-button>
            <el-button
              v-if="row.role === 1"
              type="warning"
              size="small"
              @click="handleDemote(row)"
            >
              降为普通用户
            </el-button>
            <el-button
              v-if="row.role !== 2"
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <span v-if="row.role === 2">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllUsers, deleteUser, promoteUser, demoteUser } from '@/api/user'

const users = ref([])
const loading = ref(false)

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    users.value = res.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 删除用户
const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 提升用户权限
const handlePromote = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 "${user.username}" 提升为普通管理员吗？`,
      '确认提升权限',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await promoteUser({
      userId: user.id,
      role: 1
    })
    ElMessage.success('提升权限成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '提升权限失败')
    }
  }
}

// 降级用户权限
const handleDemote = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 "${user.username}" 降为普通用户吗？`,
      '确认降级权限',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await demoteUser({
      userId: user.id
    })
    ElMessage.success('降级权限成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      // 错误信息已经在响应拦截器中显示，这里不需要重复显示
      console.error('降级权限失败:', error)
    }
  }
}

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    0: '普通用户',
    1: '管理员',
    2: '超级管理员'
  }
  return roleMap[role] || '未知'
}

// 获取角色标签类型
const getRoleType = (role) => {
  const typeMap = {
    0: '',
    1: 'success',
    2: 'warning'
  }
  return typeMap[role] || ''
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.manage-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}
</style>


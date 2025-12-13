<template>
  <div class="reserve-container">
    <el-card>
      <template #header>
        <h2>预约会议室</h2>
      </template>
      
      <el-steps :active="currentStep" finish-status="success" style="margin-bottom: 40px">
        <el-step title="选择条件" />
        <el-step title="选择会议室" />
        <el-step title="填写信息" />
      </el-steps>
      
      <!-- 步骤1: 选择条件 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-form
          ref="conditionFormRef"
          :model="conditionForm"
          :rules="conditionRules"
          label-width="120px"
          style="max-width: 600px"
        >
          <el-form-item label="预约日期" prop="date">
            <el-date-picker
              v-model="conditionForm.date"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-select v-model="conditionForm.startTime" placeholder="选择开始时间" style="width: 100%">
              <el-option
                v-for="hour in hours"
                :key="hour"
                :label="`${hour}:00`"
                :value="hour"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-select v-model="conditionForm.endTime" placeholder="选择结束时间" style="width: 100%">
              <el-option
                v-for="hour in hours"
                :key="hour"
                :label="`${hour}:00`"
                :value="hour"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="参会人数" prop="attendeeCount">
            <el-input-number
              v-model="conditionForm.attendeeCount"
              :min="1"
              :max="1000"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchRooms">查找可用会议室</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 步骤2: 选择会议室 -->
      <div v-if="currentStep === 1" class="step-content">
        <div v-loading="searching">
          <el-row :gutter="20">
            <el-col
              v-for="room in availableRooms"
              :key="room.id"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <el-card
                class="room-select-card"
                :class="{ selected: selectedRoom?.id === room.id }"
                shadow="hover"
                @click="selectRoom(room)"
              >
                <h3>{{ room.name }}</h3>
                <p>房号：{{ room.roomNumber }}</p>
                <p>容量：{{ room.capacity }}人</p>
                <p>面积：{{ room.area }}㎡</p>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="!searching && availableRooms.length === 0" description="暂无可用会议室" />
          <div class="step-actions">
            <el-button @click="currentStep = 0">上一步</el-button>
            <el-button type="primary" :disabled="!selectedRoom" @click="currentStep = 2">
              下一步
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 步骤3: 填写信息 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form
          ref="reserveFormRef"
          :model="reserveForm"
          :rules="reserveRules"
          label-width="120px"
          style="max-width: 600px"
        >
          <el-form-item label="会议室">
            <el-input :value="selectedRoom?.name" disabled />
          </el-form-item>
          <el-form-item label="预约日期">
            <el-input :value="conditionForm.date" disabled />
          </el-form-item>
          <el-form-item label="时间段">
            <el-input :value="`${conditionForm.startTime}:00 - ${conditionForm.endTime}:00`" disabled />
          </el-form-item>
          <el-form-item label="参会人数">
            <el-input :value="conditionForm.attendeeCount" disabled />
          </el-form-item>
          <el-form-item label="会议主题" prop="meetingTitle">
            <el-input
              v-model="reserveForm.meetingTitle"
              placeholder="请输入会议主题"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button @click="currentStep = 1">上一步</el-button>
            <el-button type="primary" :loading="submitting" @click="submitReservation">
              提交预约
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAvailableMeetingRooms } from '@/api/meetingRoom'
import { createReservation } from '@/api/reservation'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const currentStep = ref(0)
const conditionFormRef = ref(null)
const reserveFormRef = ref(null)
const searching = ref(false)
const submitting = ref(false)
const availableRooms = ref([])
const selectedRoom = ref(null)

const hours = Array.from({ length: 24 }, (_, i) => i)

const conditionForm = reactive({
  date: '',
  startTime: null,
  endTime: null,
  attendeeCount: 1
})

const reserveForm = reactive({
  meetingTitle: ''
})

const conditionRules = {
  date: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value <= conditionForm.startTime) {
          callback(new Error('结束时间必须大于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  attendeeCount: [{ required: true, message: '请输入参会人数', trigger: 'blur' }]
}

const reserveRules = {
  meetingTitle: [
    { required: true, message: '请输入会议主题', trigger: 'blur' },
    { min: 2, message: '会议主题至少2个字符', trigger: 'blur' }
  ]
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const searchRooms = async () => {
  if (!conditionFormRef.value) return
  
  await conditionFormRef.value.validate(async (valid) => {
    if (valid) {
      searching.value = true
      try {
        const res = await getAvailableMeetingRooms({
          date: conditionForm.date,
          startTime: conditionForm.startTime,
          endTime: conditionForm.endTime,
          attendeeCount: conditionForm.attendeeCount
        })
        availableRooms.value = res.data || []
        if (availableRooms.value.length === 0) {
          ElMessage.warning('没有找到符合条件的可用会议室')
        } else {
          currentStep.value = 1
        }
      } catch (error) {
        console.error('查找会议室失败:', error)
      } finally {
        searching.value = false
      }
    }
  })
}

const selectRoom = (room) => {
  selectedRoom.value = room
}

const submitReservation = async () => {
  if (!reserveFormRef.value) return
  
  await reserveFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await createReservation({
          meetingRoomId: selectedRoom.value.id,
          meetingTitle: reserveForm.meetingTitle,
          reservationDate: conditionForm.date,
          startTime: conditionForm.startTime,
          endTime: conditionForm.endTime,
          attendeeCount: conditionForm.attendeeCount
        })
        ElMessage.success('预约提交成功，等待审批')
        router.push('/my-reservations')
      } catch (error) {
        console.error('提交预约失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  // 如果从详情页跳转过来，预填会议室ID
  if (route.query.roomId) {
    // 预选会议室，用户仍需填写条件进行筛选
    const roomId = parseInt(route.query.roomId)
    // 这里可以预加载会议室信息，但为了流程清晰，还是让用户先选择条件
  }
})
</script>

<style scoped>
.reserve-container {
  max-width: 1200px;
  margin: 0 auto;
}

.step-content {
  min-height: 400px;
  padding: 20px 0;
}

.room-select-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.room-select-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.room-select-card.selected {
  border: 2px solid #409eff;
  background-color: #ecf5ff;
}

.room-select-card h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.room-select-card p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.step-actions {
  margin-top: 30px;
  text-align: right;
}
</style>


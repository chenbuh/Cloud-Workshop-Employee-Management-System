<template>
  <div class="attendance-page employee-page">
    <div class="page-header">
      <h2>考勤签到管理</h2>
    </div>

    <div class="attendance-layout">
      <!-- 左侧：打卡卡片 -->
      <div class="glass-effect clock-card">
        <div class="current-time">
          <div class="time">{{ currentTime }}</div>
          <div class="date">{{ currentDate }}</div>
        </div>

        <div class="clock-status">
          <div class="status-item">
            <span class="label">上班打卡</span>
            <n-tag :type="todayRecord?.clockInTime ? 'success' : 'default'" round>
              {{ todayRecord?.clockInTime ? formatTime(todayRecord.clockInTime) : '未打卡' }}
            </n-tag>
          </div>
          <div class="status-item">
            <span class="label">下班打卡</span>
            <n-tag :type="todayRecord?.clockOutTime ? 'success' : 'default'" round>
              {{ todayRecord?.clockOutTime ? formatTime(todayRecord.clockOutTime) : '未打卡' }}
            </n-tag>
          </div>
        </div>

        <div class="clock-btn-wrapper">
          <n-button 
            v-if="!todayRecord?.clockInTime"
            type="primary" 
            circle
            class="big-clock-btn"
            :loading="loading"
            @click="handleClockIn"
          >
            上班打卡
          </n-button>
          <n-button 
            v-else
            type="info" 
            circle
            class="big-clock-btn"
            :loading="loading"
            :disabled="!!todayRecord?.clockOutTime"
            @click="handleClockOut"
          >
            {{ todayRecord?.clockOutTime ? '已签收' : '下班打卡' }}
          </n-button>
        </div>
      </div>

      <!-- 右侧：考勤历史 -->
      <div class="glass-effect history-card">
        <div class="card-header">
          <h3>考勤统计与记录</h3>
          <n-radio-group v-model:value="viewMode" size="small">
            <n-radio-button value="calendar">日历视图</n-radio-button>
            <n-radio-button value="list">列表视图</n-radio-button>
          </n-radio-group>
        </div>

        <div v-if="viewMode === 'list'" class="fade-in">
            <n-data-table
            :columns="columns"
            :data="historyData"
            :pagination="pagination"
            class="glass-table"
            />
        </div>
        
        <div v-else class="calendar-wrapper fade-in">
            <n-calendar #default="{ year, month, date }" class="custom-calendar">
                <div class="calendar-cell-content">
                    <template v-if="getAttendanceForDate(year, month, date)">
                        <div class="status-indicator" :class="getStatusClass(getAttendanceForDate(year, month, date).status)">
                            {{ getStatusText(getAttendanceForDate(year, month, date).status) }}
                        </div>
                        <div class="time-hint" v-if="getAttendanceForDate(year, month, date).clockInTime">
                            {{ formatShortTime(getAttendanceForDate(year, month, date).clockInTime) }}
                        </div>
                    </template>
                </div>
            </n-calendar>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, reactive, h } from 'vue'
import { NButton, NTag, NDataTable, NCalendar, NRadioGroup, NRadioButton, useMessage } from 'naive-ui'
import { clockIn, clockOut, getTodayAttendance, getAttendanceList } from '../api/attendance'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const currentTime = ref(moment().format('HH:mm:ss'))
const currentDate = ref(moment().format('LL dddd'))
const todayRecord = ref<any>(null)
const historyData = ref<any[]>([])
const viewMode = ref<'calendar' | 'list'>('calendar')

let timer: any = null

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page: number) => {
    pagination.page = page
    loadHistory()
  }
})

const columns = [
  { title: '日期', key: 'workDate', width: 220, render: (row: any) => moment(row.workDate).format('YYYY年MM月DD日 dddd') },
  { title: '上班时间', key: 'clockInTime', render: (row: any) => row.clockInTime ? moment(row.clockInTime).format('HH:mm:ss') : '-' },
  { title: '下班时间', key: 'clockOutTime', render: (row: any) => row.clockOutTime ? moment(row.clockOutTime).format('HH:mm:ss') : '-' },
  { title: '状态', key: 'status', width: 100, render: (row: any) => {
      const map: any = { 1: ['success', '正常'], 2: ['warning', '迟到'], 3: ['warning', '早退'], 4: ['error', '异常'] }
      const conf = map[row.status] || ['default', '未知']
      return h(NTag, { type: conf[0], size: 'small', round: true }, { default: () => conf[1] })
  }}
]

const formatTime = (date: any) => moment(date).format('HH:mm:ss')

const updateTime = () => {
    currentTime.value = moment().format('HH:mm:ss')
}

const loadToday = async () => {
  try {
    const res: any = await getTodayAttendance()
    todayRecord.value = res
  } catch (e) {}
}

const loadHistory = async () => {
  try {
    const res: any = await getAttendanceList({
      pageNum: pagination.page,
      pageSize: viewMode.value === 'list' ? pagination.pageSize : 50 // Get more for calendar
    })
    historyData.value = res.records
    pagination.itemCount = res.total
  } catch (e) {}
}

const getAttendanceForDate = (y: number, m: number, d: number) => {
    return historyData.value.find(item => {
        const date = moment(item.workDate)
        return date.year() === y && (date.month() + 1) === m && date.date() === d
    })
}

const getStatusClass = (status: number) => {
    const map: any = { 1: 'success', 2: 'warning', 3: 'warning', 4: 'error' }
    return map[status] || 'default'
}

const getStatusText = (status: number) => {
    const map: any = { 1: '正常', 2: '迟到', 3: '早退', 4: '异常' }
    return map[status] || '未知'
}

const formatShortTime = (time: any) => moment(time).format('HH:mm')

const handleClockIn = async () => {
  loading.value = true
  try {
    await clockIn()
    message.success('签到成功！愿你有美好的一天')
    loadToday()
    loadHistory()
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const handleClockOut = async () => {
  loading.value = true
  try {
    await clockOut()
    message.success('签退成功！辛苦了，陈总')
    loadToday()
    loadHistory()
  } catch (e) {
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadToday()
  loadHistory()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.attendance-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
}

.clock-card {
  padding: 40px 24px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  border-radius: 24px;
}

.current-time {
  line-height: 1;
}

.current-time .time {
  font-size: 48px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.current-time .date {
  color: #64748b;
  font-size: 16px;
}

.clock-status {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(241, 245, 249, 0.5);
  border-radius: 12px;
}

.status-item .label {
  color: #475569;
  font-weight: 600;
}

.big-clock-btn {
  width: 140px;
  height: 140px;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.3);
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.big-clock-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 20px 48px rgba(99, 102, 241, 0.4);
}

.history-card {
  padding: 24px;
  border-radius: 24px;
}

.history-card h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.calendar-wrapper {
    height: 600px;
}

.custom-calendar {
    --n-border-color: transparent !important;
}

:deep(.n-calendar .n-calendar-dates .n-calendar-date) {
    padding: 8px !important;
}

.calendar-cell-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
    height: 100%;
}

.status-indicator {
    font-size: 10px;
    padding: 2px 4px;
    border-radius: 4px;
    text-align: center;
    color: white;
}
.status-indicator.success { background: #10b981; }
.status-indicator.warning { background: #f59e0b; }
.status-indicator.error { background: #ef4444; }
.status-indicator.default { background: #94a3b8; }

.time-hint {
    font-size: 11px;
    color: #64748b;
    text-align: center;
}

.fade-in {
    animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(5px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>

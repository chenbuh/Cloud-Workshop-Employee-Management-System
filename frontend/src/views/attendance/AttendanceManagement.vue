<template>
  <div class="attendance-management-page">
    <div class="header-action glass-effect">
      <div class="title">考勤统计</div>
      <n-space>
        <n-button type="primary" secondary @click="exportCSV">
          <template #icon><n-icon :component="CloudDownloadOutline" /></template>
          导出报表
        </n-button>
        <n-date-picker 
          v-model:value="dateRange" 
          type="daterange" 
          clearable
          placeholder="选择日期范围"
          @update:value="loadData"
        />
        <n-select
          v-model:value="selectedUserId"
          :options="employeeOptions"
          placeholder="选择员工（全部）"
          clearable
          filterable
          style="width: 200px"
          @update:value="loadData"
        />
      </n-space>
    </div>
    <div class="charts-section glass-effect" v-show="attendanceData.length > 0">
      <div class="chart-header">
        <div class="chart-title">打卡趋势分析 (Abnormality Trend)</div>
        <div class="chart-tip">基于选定周期内的考勤波动情况</div>
      </div>
      <div ref="attendanceTrendRef" style="height: 300px; width: 100%"></div>
    </div>

    <!-- Edit Modal -->
    <n-modal v-model:show="showEditModal" preset="card" title="修正考勤记录" style="width: 500px">
      <n-form :model="editForm" label-placement="left" label-width="100px">
        <n-form-item label="员工姓名">
          <n-input :value="editForm.full_name" disabled />
        </n-form-item>
        <n-form-item label="考勤日期">
          <n-input :value="formatDate(editForm.work_date)" disabled />
        </n-form-item>
        <n-form-item label="上班打卡">
          <n-time-picker v-model:value="editForm.clock_in_time" format="HH:mm:ss" clearable />
        </n-form-item>
        <n-form-item label="下班打卡">
          <n-time-picker v-model:value="editForm.clock_out_time" format="HH:mm:ss" clearable />
        </n-form-item>
        <n-form-item label="考勤状态">
           <n-select v-model:value="editForm.status" :options="statusOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" @click="handleSave" :loading="saving">保存修正</n-button>
        </n-space>
      </template>
      </n-modal>
    
    <!-- 统计摘要 -->
    <div class="summary-cards">
      <div class="glass-effect summary-card">
        <div class="label">本次查询总记录</div>
        <div class="value">{{ attendanceData.length }} <span class="unit">条</span></div>
      </div>
      <div class="glass-effect summary-card">
        <div class="label">累计工作时长</div>
        <div class="value">{{ totalWorkHours.toFixed(1) }} <span class="unit">小时</span></div>
      </div>
      <div class="glass-effect summary-card" :class="{ 'has-abnormal': abnormalCount > 0 }">
        <div class="label">异常打卡统计</div>
        <div class="value">{{ abnormalCount }} <span class="unit">次</span></div>
      </div>
    </div>
    
    <div class="content-table glass-effect">
      <n-data-table 
        :columns="columns" 
        :data="attendanceData" 
        :loading="loading"
        :bordered="false"
        scroll-x="1000"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h, computed, reactive, nextTick } from 'vue'
import { 
  NButton, NDataTable, NDatePicker, NSelect, NSpace, NTag, useMessage, 
  NIcon, NModal, NForm, NFormItem, NInput, NTimePicker, useDialog 
} from 'naive-ui'
import { CloudDownloadOutline, PencilOutline, TrashOutline } from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getAllAttendance, updateAttendance, deleteAttendance } from '../../api/attendance'
import { getEmployeeList } from '../../api/employee'
import moment from 'moment'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)
const attendanceData = ref<any[]>([])
const dateRange = ref<[number, number] | null>(null)
const selectedUserId = ref<number | null>(null)
const employeeList = ref([])

const attendanceTrendRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null

const showEditModal = ref(false)
const editForm = reactive({
  id: null,
  userId: null,
  full_name: '',
  work_date: null,
  clock_in_time: null,
  clock_out_time: null,
  status: 1
})

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '迟到', value: 2 },
  { label: '早退', value: 3 },
  { label: '异常', value: 4 },
  { label: '缺勤', value: 0 }
]

const employeeOptions = computed(() => {
  if (!Array.isArray(employeeList.value)) {
    return []
  }
  return employeeList.value.map((emp: any) => ({
    label: `${emp.fullName} (${emp.empCode})`,
    value: emp.userId
  }))
})

// 计算统计数据
const totalWorkHours = computed(() => {
  return attendanceData.value.reduce((acc, row) => {
    if (row.clock_in_time && row.clock_out_time) {
      const hours = moment(row.clock_out_time).diff(moment(row.clock_in_time), 'hours', true)
      return acc + (hours > 0 ? hours : 0)
    }
    return acc
  }, 0)
})

const abnormalCount = computed(() => {
  return attendanceData.value.filter(row => row.status !== 1).length
})

// Helper functions for CSV Export
const formatDate = (d: any) => d ? moment(d).format('YYYY-MM-DD') : ''
const formatTime = (d: any) => d ? moment(d).format('HH:mm:ss') : ''

const columns = [
  { title: '工号', key: 'emp_code', width: 100 },
  { title: '姓名', key: 'full_name', width: 100 },
  { title: '部门', key: 'dept_name', width: 120 },
  { 
    title: '日期', 
    key: 'work_date',
    width: 140,
    render: (row: any) => moment(row.work_date).format('YYYY年MM月DD日 dddd')
  },
  { 
    title: '上班打卡', 
    key: 'clock_in_time',
    width: 100,
    render: (row: any) => row.clock_in_time ? moment(row.clock_in_time).format('HH:mm:ss') : '-'
  },
  { 
    title: '下班打卡', 
    key: 'clock_out_time',
    width: 100,
    render: (row: any) => row.clock_out_time ? moment(row.clock_out_time).format('HH:mm:ss') : '-'
  },
  {
    title: '时长',
    key: 'duration',
    width: 100,
    render: (row: any) => {
      if (!row.clock_in_time || !row.clock_out_time) return '-'
      const hours = moment(row.clock_out_time).diff(moment(row.clock_in_time), 'hours', true)
      return `${hours.toFixed(1)}小时`
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row: any) => {
      const statusMap: any = {
        0: { label: '缺勤', type: 'error' },
        1: { label: '正常', type: 'success' },
        2: { label: '迟到', type: 'warning' },
        3: { label: '早退', type: 'warning' },
        4: { label: '异常', type: 'error' }
      }
      const status = statusMap[row.status] || { label: '未知', type: 'default' }
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.label })
    }
  },
  {
    title: '打卡IP',
    key: 'ip_address',
    width: 130,
    render: (row: any) => row.ip_address || '-'
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render: (row: any) => {
      return h(NSpace, null, { default: () => [
        h(NButton, {
          size: 'small',
          circle: true,
          quaternary: true,
          onClick: () => handleEdit(row)
        }, { icon: () => h(NIcon, { component: PencilOutline }) }),
        h(NButton, {
          size: 'small',
          circle: true,
          quaternary: true,
          type: 'error',
          onClick: () => handleDelete(row)
        }, { icon: () => h(NIcon, { component: TrashOutline }) })
      ]})
    }
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {}
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = moment(dateRange.value[0]).format('YYYY-MM-DD')
      params.endDate = moment(dateRange.value[1]).format('YYYY-MM-DD')
    }
    
    if (selectedUserId.value) {
      params.userId = selectedUserId.value
    }
    
    attendanceData.value = await getAllAttendance(params) as any
    nextTick(initTrendChart)
  } catch (error: any) {
    message.error(error.message || '加载考勤数据失败')
  } finally {
    loading.value = false
  }
}

const initTrendChart = () => {
    if (!attendanceTrendRef.value || attendanceData.value.length === 0) return
    if (!trendChart) trendChart = echarts.init(attendanceTrendRef.value)
    
    // Process data: Group by date
    const dateMap: any = {}
    attendanceData.value.forEach(row => {
        const date = moment(row.work_date).format('MM-DD')
        if (!dateMap[date]) dateMap[date] = { normal: 0, abnormal: 0 }
        if (row.status === 1) dateMap[date].normal++
        else dateMap[date].abnormal++
    })
    
    const sortedDates = Object.keys(dateMap).sort()
    const normalData = sortedDates.map(d => dateMap[d].normal)
    const abnormalData = sortedDates.map(d => dateMap[d].abnormal)

    const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['正常打卡', '异常打卡'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
        xAxis: { type: 'category', data: sortedDates, axisLine: { lineStyle: { color: '#94a3b8' } } },
        yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
        series: [
            {
                name: '正常打卡',
                type: 'bar',
                stack: 'total',
                itemStyle: { color: '#10b981' },
                data: normalData
            },
            {
                name: '异常打卡',
                type: 'bar',
                stack: 'total',
                itemStyle: { color: '#ef4444' },
                data: abnormalData
            }
        ]
    }
    trendChart.setOption(option)
}

const loadEmployees = async () => {
  try {
    employeeList.value = await getEmployeeList() as any
  } catch (error: any) {
    message.error('加载员工列表失败')
  }
}

const handleEdit = (row: any) => {
  Object.assign(editForm, {
    id: row.id,
    userId: row.user_id,
    full_name: row.full_name,
    work_date: row.work_date,
    clock_in_time: row.clock_in_time ? moment(row.clock_in_time).valueOf() : null,
    clock_out_time: row.clock_out_time ? moment(row.clock_out_time).valueOf() : null,
    status: row.status
  })
  showEditModal.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateAttendance({
      id: editForm.id,
      userId: editForm.userId,
      status: editForm.status,
      clockInTime: editForm.clock_in_time,
      clockOutTime: editForm.clock_out_time
    })
    message.success('记录修正成功')
    showEditModal.value = false
    loadData()
  } catch(e: any) {
    message.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除这条考勤记录吗？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteAttendance(row.id)
        message.success('记录已删除')
        loadData()
      } catch(e: any) {
        message.error(e.message || '删除失败')
      }
    }
  })
}

const exportCSV = () => {
  if (attendanceData.value.length === 0) {
    message.warning('暂无数据可导出')
    return
  }
  
  // Define CSV headers
  const headers = ['工号', '姓名', '部门', '日期', '上班时间', '下班时间', '状态', 'IP地址']
  
  // Transform data
  const rows = attendanceData.value.map(row => {
    const statusLabel = statusOptions.find(o => o.value === row.status)?.label || '未知'
    return [
      row.emp_code,
      row.full_name,
      row.dept_name,
      formatDate(row.work_date),
      formatTime(row.clock_in_time),
      formatTime(row.clock_out_time),
      statusLabel,
      row.ip_address || ''
    ]
  })
  
  // Combine with BOM for UTF-8 Excel compatibility
  let csvContent = '\uFEFF' + headers.join(',') + '\n' + rows.map(e => e.join(',')).join('\n')
  
  // Download
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.setAttribute('href', url)
  link.setAttribute('download', `考勤报表_${moment().format('YYYYMMDDHHmmss')}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

onMounted(async () => {
  await loadEmployees()
  await loadData()
  window.addEventListener('resize', () => trendChart?.resize())
})
</script>

<style scoped>
.attendance-management-page {
  padding: 24px;
}

.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 24px;
  margin-bottom: 24px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.charts-section {
  padding: 24px;
  background: white;
  border-radius: 24px;
  margin-bottom: 24px;
}

.chart-header {
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.chart-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.summary-card {
  padding: 24px;
  border-radius: 20px;
  background: white;
  transition: transform 0.3s;
}

.summary-card:hover {
  transform: translateY(-4px);
}

.summary-card.has-abnormal {
  border-left: 4px solid #ef4444;
}

.summary-card .label {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 8px;
}

.summary-card .value {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
}

.summary-card .unit {
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
  margin-left: 4px;
}

.content-table {
  padding: 24px;
  background: white;
  border-radius: 24px;
}

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}
</style>

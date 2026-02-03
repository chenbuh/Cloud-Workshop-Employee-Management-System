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
    <div class="dashboard-grid">
      <div class="charts-section glass-effect main-chart">
        <div class="chart-header">
          <div class="chart-title">打卡趋势分析 (Abnormality Trend)</div>
          <div class="chart-tip">基于选定周期内的考勤波动情况</div>
        </div>
        <div ref="attendanceTrendRef" style="height: 300px; width: 100%"></div>
      </div>
      
      <div class="charts-section glass-effect side-chart">
        <div class="chart-header">
          <div class="chart-title">状态分布 (Distribution)</div>
        </div>
        <div ref="attendancePieRef" style="height: 300px; width: 100%"></div>
      </div>
    </div>

    <!-- 统计摘要 -->
    <div class="summary-cards">
      <div class="glass-effect summary-card">
        <div class="icon-box info"><n-icon :component="StatsChartOutline" /></div>
        <div class="card-content">
          <div class="label">本次查询总记录</div>
          <div class="value">{{ attendanceData.length }} <span class="unit">条</span></div>
        </div>
      </div>
      <div class="glass-effect summary-card">
        <div class="icon-box primary"><n-icon :component="TimeOutline" /></div>
        <div class="card-content">
          <div class="label">累计工作时长</div>
          <div class="value">{{ totalWorkHours.toFixed(1) }} <span class="unit">小时</span></div>
        </div>
      </div>
      <div class="glass-effect summary-card" :class="{ 'has-abnormal': abnormalCount > 0 }">
        <div class="icon-box error"><n-icon :component="AlertCircleOutline" /></div>
        <div class="card-content">
          <div class="label">异常打卡统计</div>
          <div class="value">{{ abnormalCount }} <span class="unit">次</span></div>
        </div>
      </div>
      <div class="glass-effect summary-card">
        <div class="icon-box success"><n-icon :component="CheckmarkCircleOutline" /></div>
        <div class="card-content">
          <div class="label">出勤率 (Normal Rate)</div>
          <div class="value">{{ attendanceRate }} <span class="unit">%</span></div>
        </div>
      </div>
    </div>
    
    <!-- Edit Modal -->
    <n-modal v-model:show="showEditModal" preset="card" title="修正考勤记录" style="width: 500px" class="glass-modal">
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

    <div class="content-table glass-effect">
      <div class="table-header">
         <h3>数据明细</h3>
         <n-button v-if="checkedRowKeys.length > 0" type="error" ghost size="small" @click="handleBatchDelete">
           批量删除 ({{ checkedRowKeys.length }})
         </n-button>
      </div>
      <n-data-table 
        v-model:checked-row-keys="checkedRowKeys"
        :columns="columns" 
        :data="attendanceData" 
        :loading="loading"
        :bordered="false"
        scroll-x="1000"
        :row-key="(row) => row.id"
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
import { 
  CloudDownloadOutline, PencilOutline, TrashOutline, 
  StatsChartOutline, TimeOutline, AlertCircleOutline, CheckmarkCircleOutline 
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getAllAttendance, updateAttendance, deleteAttendance } from '../../api/attendance'
import { getEmployeeListAll } from '../../api/employee'
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
const attendancePieRef = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
const checkedRowKeys = ref([])

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

const attendanceRate = computed(() => {
  if (attendanceData.value.length === 0) return 0
  const normal = attendanceData.value.filter(row => row.status === 1).length
  return Math.round((normal / attendanceData.value.length) * 100)
})

// Helper functions for CSV Export
const formatDate = (d: any) => d ? moment(d).format('YYYY-MM-DD') : ''
const formatTime = (d: any) => d ? moment(d).format('HH:mm:ss') : ''

const columns: any = [
  { type: 'selection', fixed: 'left' },
  { title: '工号', key: 'emp_code', width: 90, fixed: 'left' },
  { title: '姓名', key: 'full_name', width: 100, fixed: 'left' },
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
    render: (row: any) => {
        const ip = row.ip_address || '-'
        return ip === '0:0:0:0:0:0:0:1' ? '127.0.0.1' : ip
    }
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
    nextTick(() => {
        initTrendChart()
        initPieChart()
    })
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

const initPieChart = () => {
    if (!attendancePieRef.value || attendanceData.value.length === 0) return
    if (!pieChart) pieChart = echarts.init(attendancePieRef.value)
    
    const stats: any = { 1: 0, 2: 0, 3: 0, 4: 0, 0: 0 }
    attendanceData.value.forEach(row => stats[row.status]++)
    
    const data = statusOptions.map(opt => ({
        name: opt.label,
        value: stats[opt.value]
    })).filter(d => d.value > 0)

    const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [
            {
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 8 },
                label: { show: false, position: 'center' },
                emphasis: { label: { show: true, fontSize: '18', fontWeight: 'bold' } },
                data: data.map(d => {
                    const colorMap: any = { '正常': '#10b981', '迟到': '#f59e0b', '早退': '#f59e0b', '异常': '#ef4444', '缺勤': '#94a3b8' }
                    return { ...d, itemStyle: { color: colorMap[d.name] } }
                })
            }
        ]
    }
    pieChart.setOption(option)
}

const handleBatchDelete = () => {
    dialog.warning({
        title: '批量删除确认',
        content: `确定要删除所选的 ${checkedRowKeys.value.length} 条考勤记录吗？此操作不可撤销。`,
        positiveText: '确认删除',
        negativeText: '取消',
        onPositiveClick: async () => {
            try {
                // Since our current backend API only supports single delete, we might need to loop or update backend.
                // For now, let's try to do it via loop (not ideal but works for demonstration)
                for (const id of checkedRowKeys.value) {
                    await deleteAttendance(Number(id))
                }
                message.success('批量删除成功')
                checkedRowKeys.value = []
                loadData()
            } catch (e: any) {
                message.error('部分记录删除失败')
            }
        }
    })
}

const loadEmployees = async () => {
  try {
    employeeList.value = await getEmployeeListAll() as any
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
  window.addEventListener('resize', () => {
      trendChart?.resize()
      pieChart?.resize()
  })
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
  padding: 20px 24px;
  background: white;
  border-radius: 20px;
  margin-bottom: 24px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.charts-section {
  padding: 24px;
  background: white;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
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
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.summary-card {
  padding: 20px;
  border-radius: 20px;
  background: white;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.summary-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.15);
}

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.icon-box.info { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
.icon-box.primary { background: rgba(99, 102, 241, 0.1); color: #6366f1; }
.icon-box.error { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
.icon-box.success { background: rgba(16, 185, 129, 0.1); color: #10b981; }

.summary-card.has-abnormal {
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.summary-card .label {
  color: #64748b;
  font-size: 12px;
  margin-bottom: 4px;
}

.summary-card .value {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
}

.summary-card .unit {
  font-size: 12px;
  font-weight: 500;
  color: #94a3b8;
  margin-left: 2px;
}

.content-table {
  padding: 24px;
  background: white;
  border-radius: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.glass-modal :deep(.n-card) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
}
</style>

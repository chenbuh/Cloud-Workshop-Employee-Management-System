<template>
  <div class="payroll-page employee-page">
    <div class="page-header">
      <h2>财务结算中心</h2>
      <n-space>
        <n-date-picker 
          v-model:formatted-value="selectedMonth" 
          value-format="yyyy-MM"
          type="month" 
          placeholder="选择结算月份"
          @update:value="loadData"
        />
        <n-button type="primary" :loading="generating" @click="handleGenerate">
          <template #icon><n-icon :component="SyncOutline" /></template>
          生成本月工资单
        </n-button>
        <n-button strong secondary type="info" @click="handleExport">
          <template #icon><n-icon :component="DownloadOutline" /></template>
          导出报表
        </n-button>
      </n-space>
    </div>

    <!-- 统计卡片 -->
    <div class="summary-cards">
      <div class="glass-effect summary-card">
        <div class="label">{{ selectedMonth }} 月应发总额</div>
        <div class="value">¥ {{ totalAmount.toLocaleString() }}</div>
      </div>
      <div class="glass-effect summary-card">
        <div class="label">已结算人数</div>
        <div class="value">{{ payrollList.length }} 人</div>
      </div>
      <div class="glass-effect summary-card">
        <div class="label">发放状态</div>
        <div class="value">
          <n-tag :type="allIssued ? 'success' : 'warning'" round>
            {{ allIssued ? '全部已发放' : '部分待发放' }}
          </n-tag>
        </div>
      </div>
    </div>

    <div class="glass-effect table-container">
      <n-data-table
        :columns="columns"
        :data="payrollList"
        :loading="loading"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { NDataTable, NButton, NIcon, NTag, NSpace, NDatePicker, useMessage, useDialog } from 'naive-ui'
import { SyncOutline, CashOutline, DownloadOutline } from '@vicons/ionicons5'
import { getPayrollList, generatePayroll, issuePayroll, exportPayroll } from '../api/payroll'
import moment from 'moment'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const generating = ref(false)
const selectedMonth = ref(moment().format('YYYY-MM'))
const payrollList = ref<any[]>([])

const handleExport = async () => {
  try {
      const res: any = await exportPayroll(selectedMonth.value)
      const url = window.URL.createObjectURL(new Blob([res]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `薪资明细-${selectedMonth.value}.xlsx`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      message.success('导出成功')
  } catch (e) {
      message.error('导出失败')
  }
}

const totalAmount = computed(() => {
  return payrollList.value.reduce((acc, row) => acc + row.actualAmount, 0)
})

const allIssued = computed(() => {
  return payrollList.value.length > 0 && payrollList.value.every(row => row.status === 1)
})

const columns = [
  { title: '员工', key: 'full_name', render: (row: any) => h('div', [
      h('div', { style: 'font-weight: 600' }, row.full_name),
      h('div', { style: 'font-size: 11px; color: #64748b' }, row.emp_code)
  ])},
  { title: '部门', key: 'dept_name' },
  { title: '基本工资', key: 'baseSalary', render: (row: any) => `¥${row.baseSalary}` },
  { title: '绩效/奖金', key: 'bonus', render: (row: any) => `¥${row.bonus}` },
  { title: '五险一金', key: 'insurance', render: (row: any) => h('span', { style: 'color: #ef4444' }, `-¥${row.insurance}`) },
  { title: '考勤扣款', key: 'deduction', render: (row: any) => h('span', { style: 'color: #ef4444' }, `-¥${row.deduction || 0}`) },
  { title: '实发金额', key: 'actualAmount', render: (row: any) => h('span', { style: 'font-weight: 800; color: #10b981; font-size: 16px' }, `¥${row.actualAmount.toLocaleString()}`) },
  { title: '状态', key: 'status', render: (row: any) => {
      return h(NTag, {
        type: row.status === 1 ? 'success' : 'warning',
        bordered: false,
        size: 'small'
      }, { default: () => row.status === 1 ? '已发放' : '待发放' })
  }},
  { title: '操作', key: 'actions', render: (row: any) => {
      if (row.status === 1) return '-'
      return h(NButton, {
        size: 'small',
        type: 'primary',
        secondary: true,
        onClick: () => handleIssue(row)
      }, { 
        default: () => '发放工资',
        icon: () => h(NIcon, { component: CashOutline })
      })
  }}
]

const loadData = async () => {
  if (!selectedMonth.value) return
  loading.value = true
  try {
    const res: any = await getPayrollList(selectedMonth.value)
    payrollList.value = res
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    await generatePayroll(selectedMonth.value)
    message.success(`${selectedMonth.value} 月份工资单生成成功`)
    loadData()
  } finally {
    generating.value = false
  }
}

const handleIssue = (row: any) => {
  dialog.success({
    title: '工资发放确认',
    content: `确定要发放 ${row.full_name} 的工资单吗？系统将自动发送站内信通知。`,
    positiveText: '立即发放',
    negativeText: '取消',
    onPositiveClick: async () => {
      await issuePayroll(row.id)
      message.success('工资发放成功')
      loadData()
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
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
}

.summary-card .label {
  color: #64748b;
  font-size: 13px;
  margin-bottom: 8px;
}

.summary-card .value {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
}

.table-container {
  padding: 24px;
  border-radius: 24px;
}
</style>

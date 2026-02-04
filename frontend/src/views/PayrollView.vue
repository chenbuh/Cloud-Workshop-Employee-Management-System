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
        <n-button type="success" :disabled="checkedRowKeys.length === 0" :loading="sending" @click="handleBatchSend">
          <template #icon><n-icon :component="MailOutline" /></template>
          批量发送薪资条 ({{ checkedRowKeys.length }})
        </n-button>
      </n-space>
    </div>

    <!-- 统计卡片 -->
    <div class="summary-grid">
      <div class="glass-effect summary-card purple">
        <div class="label">{{ selectedMonth }} 月应发总额</div>
        <div class="value">¥ {{ totalAmount.toLocaleString() }}</div>
        <div class="chart-mini" ref="miniChartRef"></div>
      </div>
      <div class="glass-effect summary-item-card">
         <div class="sub-label">结算人数</div>
         <div class="sub-value">{{ payrollList.length }} 人</div>
      </div>
      <div class="glass-effect summary-item-card">
         <div class="sub-label">平均实发</div>
         <div class="sub-value">¥ {{ (totalAmount / (payrollList.length || 1)).toFixed(0) }}</div>
      </div>
      <div class="glass-effect summary-item-card">
         <div class="sub-label">发放状态</div>
         <div class="sub-value">
           <n-tag :type="allIssued ? 'success' : 'warning'" round secondary>
             {{ allIssued ? '全部已发放' : '部分待发放' }}
           </n-tag>
         </div>
      </div>
    </div>

    <n-grid :cols="24" :x-gap="24" :y-gap="24" style="margin-bottom: 24px">
      <n-gi :span="16">
        <div class="glass-effect table-container">
          <div class="card-title">薪资明细清单</div>
          <n-data-table
            :columns="columns"
            :data="payrollList"
            :loading="loading"
            :max-height="500"
            v-model:checked-row-keys="checkedRowKeys"
            :row-key="(row) => row.id"
          />
        </div>
      </n-gi>
      <n-gi :span="8">
        <div class="glass-effect chart-container">
            <div class="card-title">薪资结构分析</div>
            <div ref="compositionRef" style="height: 300px"></div>
            <n-divider />
            <div class="stat-hints">
                <div class="hint-item">
                    <span>总基本工资:</span>
                    <b>¥ {{ payrollList.reduce((a,b) => a + b.baseSalary, 0).toLocaleString() }}</b>
                </div>
                <div class="hint-item">
                    <span>总绩效奖金:</span>
                    <b>¥ {{ payrollList.reduce((a,b) => a + b.bonus, 0).toLocaleString() }}</b>
                </div>
                <div class="hint-item danger">
                    <span>考勤总扣款:</span>
                    <b>-¥ {{ payrollList.reduce((a,b) => a + (b.deduction || 0), 0).toLocaleString() }}</b>
                </div>
            </div>
        </div>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h, reactive } from 'vue'
import { NDataTable, NButton, NIcon, NTag, NSpace, NDatePicker, NGrid, NGi, NDivider, useMessage, useDialog } from 'naive-ui'
import { SyncOutline, CashOutline, DownloadOutline, MailOutline } from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getPayrollList, generatePayroll, issuePayroll, exportPayroll, batchSendPayroll } from '../api/payroll'
import { useAppStore } from '../store/app'
import moment from 'moment'

const appStore = useAppStore()

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const generating = ref(false)
const selectedMonth = ref(moment().format('YYYY-MM'))
const payrollList = ref<any[]>([])
const checkedRowKeys = ref<number[]>([])
const sending = ref(false)
const compositionRef = ref<HTMLElement | null>(null)
let compositionChart: echarts.ECharts | null = null

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

const columns: any[] = [
  {
    type: 'selection',
    disabled(row: any) {
      return row.status === 1
    }
  },
  { title: '员工', key: 'full_name', render: (row: any) => h('div', [
      h('div', { style: 'font-weight: 600' }, row.full_name),
      h('div', { style: 'font-size: 11px; color: var(--text-secondary)' }, row.emp_code)
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
    setTimeout(() => initCharts(), 100)
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
    if (!compositionRef.value || payrollList.value.length === 0) return
    if (!compositionChart) {
        compositionChart = echarts.init(compositionRef.value)
    }

    const data = [
        { name: '基本薪资', value: payrollList.value.reduce((a,b) => a + b.baseSalary, 0) },
        { name: '津贴补助', value: payrollList.value.reduce((a,b) => a + b.subsidy, 0) },
        { name: '绩效奖金', value: payrollList.value.reduce((a,b) => a + b.bonus, 0) },
        { name: '保险支出', value: payrollList.value.reduce((a,b) => a + b.insurance, 0) }
    ]

    const option = {
        tooltip: { trigger: 'item' },
        series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false },
            data: data
        }]
    }
    compositionChart.setOption(option)
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

const handleBatchSend = () => {
  if (checkedRowKeys.value.length === 0) return
  dialog.info({
    title: '批量发送确认',
    content: `确定要为选中的 ${checkedRowKeys.value.length} 名员工发送 PDF 薪资条邮件吗？`,
    positiveText: '开始发送',
    negativeText: '取消',
    onPositiveClick: async () => {
      sending.value = true
      try {
        await batchSendPayroll(checkedRowKeys.value)
        message.success('后台已开始批量发送，请稍后刷新查看状态')
        checkedRowKeys.value = []
        loadData()
      } catch (e) {
        message.error('发送失败')
      } finally {
        sending.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', () => compositionChart?.resize())
})
</script>

<style scoped>
.summary-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.summary-card {
  padding: 24px;
  border-radius: 24px;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.summary-card.purple { background: linear-gradient(135deg, var(--primary-color), #a855f7); }

.summary-item-card {
    padding: 24px;
    border-radius: 20px;
    background: var(--glass-bg);
    display: flex;
    flex-direction: column;
    justify-content: center;
    border: 1px solid var(--glass-border);
}

.summary-card .label { font-size: 14px; opacity: 0.9; margin-bottom: 8px; }
.summary-card .value { font-size: 32px; font-weight: 800; }

.sub-label { font-size: 13px; color: var(--text-secondary); margin-bottom: 4px; }
.sub-value { font-size: 20px; font-weight: 700; color: var(--text-primary); }

.table-container, .chart-container {
  padding: 24px;
  border-radius: 24px;
  background: var(--glass-bg);
}

.card-title { font-size: 16px; font-weight: 700; margin-bottom: 20px; color: var(--text-primary); }

.stat-hints {
    display: flex;
    flex-direction: column;
    gap: 12px;
}
.hint-item {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: var(--text-secondary);
}
.hint-item b { color: var(--text-primary); }
.hint-item.danger b { color: #ef4444; }
</style>

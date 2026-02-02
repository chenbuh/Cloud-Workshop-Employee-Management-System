<template>
  <div class="salary-page employee-page">
    <n-tabs type="segment" animated>
      <!-- Tab 1: 薪资方案配置 -->
      <n-tab-pane name="config" tab="薪资方案配置">
        <div class="page-header">
          <h2>薪资方案管理</h2>
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon :component="AddOutline" /></template>
            新增薪资方案
          </n-button>
        </div>

        <!-- 汇总卡片 -->
        <div class="summary-cards">
          <div class="glass-effect summary-card">
            <div class="label">月度总计薪资</div>
            <div class="value">¥ {{ totalMonthlyPay.toLocaleString() }}</div>
          </div>
          <div class="glass-effect summary-card">
            <div class="label">平均基本工资</div>
            <div class="value">¥ {{ avgBasePay.toLocaleString() }}</div>
          </div>
          <div class="glass-effect summary-card">
            <div class="label">配置进度</div>
            <div class="value">{{ configuredCount }} / {{ totalEmpCount }}</div>
          </div>
        </div>

        <div class="glass-effect table-container">
          <n-data-table
            :columns="configColumns"
            :data="salaryList"
            :loading="loading"
          />
        </div>
      </n-tab-pane>

      <!-- Tab 2: 月度工资单 -->
      <n-tab-pane name="payroll" tab="月度工资单">
        <div class="page-header">
           <div class="filter-area">
             <span class="label">选择月份: </span>
             <n-date-picker 
               v-model:value="selectedMonthTs" 
               type="month" 
               clearable 
               @update:value="handleMonthChange"
             />
           </div>
           <n-space>
             <n-button type="info" secondary @click="handleExportPayroll">
               <template #icon><n-icon :component="CloudDownloadOutline" /></template>
               导出明细
             </n-button>
             <n-popconfirm @positive-click="handleGeneratePayroll">
               <template #trigger>
                 <n-button type="primary">
                   <template #icon><n-icon :component="CalculatorOutline" /></template>
                   生成本月工资单
                 </n-button>
               </template>
               确定要生成 {{ selectedMonthStr }} 的工资单吗？如果已存在将不会重复生成。
             </n-popconfirm>
           </n-space>
        </div>

        <div class="glass-effect table-container">
           <n-data-table
             :columns="payrollColumns"
             :data="payrollList"
             :loading="payrollLoading"
           />
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 薪资配置弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="form.id ? '修改薪资配置' : '新增薪资方案'" style="width: 500px">
      <n-form :model="form" label-placement="left" label-width="100px">
        <n-form-item label="选择员工" v-if="!form.id">
          <n-select 
            v-model:value="form.userId" 
            :options="unconfiguredEmployees" 
            placeholder="请选择还未配置薪资的员工"
          />
        </n-form-item>
        <n-form-item label="姓名" v-else>
          <n-input :value="form.fullName" disabled />
        </n-form-item>
        
        <n-form-item label="基本工资">
          <n-input-number v-model:value="form.baseSalary" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="津贴合计">
          <n-input-number v-model:value="form.subsidy" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="绩效奖金">
          <n-input-number v-model:value="form.bonus" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="五险一金">
          <n-input-number v-model:value="form.insurance" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>

        <div class="gross-calculator">
          <div class="calc-label">预计实发工资 (税前扣除前):</div>
          <div class="calc-value">¥ {{ (form.baseSalary + form.subsidy + form.bonus - form.insurance).toFixed(2) }}</div>
        </div>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSave" :loading="saving">确认保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h, reactive } from 'vue'
import { 
  NDataTable, NButton, NIcon, NModal, NForm, NFormItem, 
  NInput, NInputNumber, NSelect, NSpace, useMessage, useDialog,
  NTabs, NTabPane, NDatePicker, NPopconfirm, NTag
} from 'naive-ui'
import { 
  AddOutline, PencilOutline, TrashOutline, CalculatorOutline, 
  CloudDownloadOutline, CardOutline 
} from '@vicons/ionicons5'
import { getSalaryList, getUnconfiguredEmployees, saveSalary, deleteSalary } from '../api/salary'
import { getPayrollList, generatePayroll, issuePayroll, exportPayroll } from '../api/payroll'
import moment from 'moment'

const message = useMessage()
const dialog = useDialog()

// === Config Tab State ===
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const salaryList = ref<any[]>([])
const unconfiguredEmployees = ref<any[]>([])
const form = reactive({
  id: null,
  userId: null,
  fullName: '',
  baseSalary: 0,
  subsidy: 0,
  bonus: 0,
  insurance: 0
})

// === Payroll Tab State ===
const payrollLoading = ref(false)
const payrollList = ref<any[]>([])
const selectedMonthTs = ref(Date.now())
const selectedMonthStr = computed(() => moment(selectedMonthTs.value).format('YYYY-MM'))

// === Config Logic ===

const totalMonthlyPay = computed(() => {
  return salaryList.value.reduce((acc, row) => acc + (row.baseSalary + row.subsidy + row.bonus - row.insurance), 0)
})

const avgBasePay = computed(() => {
  if (salaryList.value.length === 0) return 0
  const sum = salaryList.value.reduce((acc, row) => acc + row.baseSalary, 0)
  return Math.round(sum / salaryList.value.length)
})

const configuredCount = computed(() => salaryList.value.length)
const totalEmpCount = computed(() => salaryList.value.length + unconfiguredEmployees.value.length)

const configColumns = [
  { title: '工号/姓名', key: 'full_name', render: (row: any) => h('div', [
      h('div', { style: 'font-weight: 600' }, row.full_name),
      h('div', { style: 'font-size: 12px; color: #64748b' }, row.dept_name)
  ])},
  { title: '基本工资', key: 'baseSalary', render: (row: any) => `¥ ${row.baseSalary.toLocaleString()}` },
  { title: '津贴/绩效', key: 'subsidy', render: (row: any) => `¥ ${(row.subsidy + row.bonus).toLocaleString()}` },
  { title: '实发金额', key: 'actual', render: (row: any) => {
      const actual = row.baseSalary + row.subsidy + row.bonus - row.insurance
      return h('span', { style: 'color: #10b981; font-weight: 700' }, `¥ ${actual.toLocaleString()}`)
  }},
  { title: '操作', key: 'actions', render: (row: any) => {
    return h(NSpace, null, { default: () => [
      h(NButton, {
        size: 'small',
        quaternary: true,
        circle: true,
        onClick: () => handleEdit(row)
      }, { icon: () => h(NIcon, { component: PencilOutline }) }),
      h(NButton, {
        size: 'small',
        quaternary: true,
        circle: true,
        type: 'error',
        onClick: () => handleDelete(row)
      }, { icon: () => h(NIcon, { component: TrashOutline }) })
    ]})
  }}
]

const loadConfigData = async () => {
  loading.value = true
  try {
    const [salaries, unconfigured]: any = await Promise.all([
      getSalaryList(),
      getUnconfiguredEmployees()
    ])
    salaryList.value = salaries
    unconfiguredEmployees.value = unconfigured.map((e: any) => ({
      label: `${e.full_name} (${e.dept_name})`,
      value: e.userId
    }))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    id: null,
    userId: null,
    fullName: '',
    baseSalary: 5000,
    subsidy: 500,
    bonus: 0,
    insurance: 400
  })
  showModal.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    userId: row.userId,
    fullName: row.full_name,
    baseSalary: row.baseSalary,
    subsidy: row.subsidy,
    bonus: row.bonus,
    insurance: row.insurance
  })
  showModal.value = true
}

const handleSave = async () => {
  if (!form.userId) return message.warning('请选择员工')
  saving.value = true
  try {
    await saveSalary(form)
    message.success('薪资配置已保存')
    showModal.value = false
    loadConfigData()
  } finally {
    saving.value = false
  }
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要移除 ${row.full_name} 的薪资方案吗？`,
    positiveText: '确认移除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteSalary(row.id)
      message.success('已成功移除记录')
      loadConfigData()
    }
  })
}

// === Payroll Logic ===

const payrollColumns = [
  { title: '工号/姓名', key: 'full_name', render: (row: any) => h('div', [
      h('div', { style: 'font-weight: 600' }, row.fullName),
      h('div', { style: 'font-size: 12px; color: #64748b' }, row.deptName)
  ])},
  { title: '基本工资', key: 'baseSalary', render: (row: any) => `¥ ${Number(row.baseSalary).toLocaleString()}` },
  { title: '津贴+绩效', key: 'performanceBonus', render: (row: any) => `¥ ${Number(row.performanceBonus).toLocaleString()}` },
  { title: '考勤扣款', key: 'deduction', render: (row: any) => {
     return h('span', { style: 'color: #ef4444' }, `- ¥ ${Number(row.deduction).toLocaleString()}`)
  }},
  { title: '实发工资', key: 'actualAmount', render: (row: any) => {
     return h('span', { style: 'color: #10b981; font-weight: 800; font-size: 15px' }, `¥ ${Number(row.actualAmount).toLocaleString()}`)
  }},
  { title: '状态', key: 'status', render: (row: any) => {
      return h(NTag, { type: row.status === 1 ? 'success' : 'warning', size: 'small' }, { default: () => row.status === 1 ? '已发放' : '待发放' })
  }},
  { title: '操作', key: 'actions', render: (row: any) => {
      if (row.status === 1) return h('span', { style: 'color: #cbd5e1' }, '已完成')
      return h(NPopconfirm, {
          onPositiveClick: () => handleIssue(row)
      }, {
          trigger: () => h(NButton, { size: 'small', type: 'primary', secondary: true }, { default: () => '发放', icon: () => h(NIcon, { component: CardOutline }) }),
          default: () => `确认向 ${row.fullName} 发放工资吗？`
      })
  }}
]

const loadPayrollData = async () => {
  payrollLoading.value = true
  try {
    const res = await getPayrollList(selectedMonthStr.value) as any
    payrollList.value = res
  } catch (e) {
    message.error('加载工资单失败')
  } finally {
    payrollLoading.value = false
  }
}

const handleMonthChange = () => {
  loadPayrollData()
}

const handleGeneratePayroll = async () => {
  payrollLoading.value = true
  try {
    await generatePayroll(selectedMonthStr.value)
    message.success('工资单生成成功')
    loadPayrollData()
  } catch (e: any) {
     message.error(e.message || '生成失败')
  } finally {
    payrollLoading.value = false
  }
}

const handleIssue = async (row: any) => {
  try {
      await issuePayroll(row.id)
      message.success('发放成功，已发送通知')
      loadPayrollData()
  } catch (e: any) {
      message.error(e.message || '发放失败')
  }
}

const handleExportPayroll = async () => {
    try {
        const res: any = await exportPayroll(selectedMonthStr.value)
        const url = window.URL.createObjectURL(new Blob([res]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `薪资明细-${selectedMonthStr.value}.xlsx`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
    } catch (e) {
        message.error('导出失败')
    }
}

// === Lifecycle ===
onMounted(() => {
  loadConfigData()
  loadPayrollData()
})
</script>

<style scoped>
.salary-page {
    /* override padding only if necessary, naive-ui tabs handle layout well */
}
.summary-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  background: white;
}

.gross-calculator {
  margin-top: 24px;
  padding: 16px;
  background: rgba(16, 185, 129, 0.05);
  border-radius: 12px;
  border: 1px dashed #10b981;
}

.calc-label {
  font-size: 12px;
  color: #10b981;
  margin-bottom: 4px;
}

.calc-value {
  font-size: 20px;
  font-weight: 800;
  color: #10b981;
}

.filter-area {
    display: flex;
    align-items: center;
    gap: 12px;
}
.filter-area .label {
    font-weight: 600;
    color: #475569;
}
</style>

<template>
  <div class="contract-container">
    <div class="header-section">
      <div class="title-group">
        <h2>电子合同中心</h2>
        <p class="subtitle">数字化合同签署、存储与全生命周期状态追踪</p>
      </div>
      <n-button type="primary" @click="handleCreate">
        <template #icon><n-icon :component="Add" /></template>
        起草新合同
      </n-button>
    </div>

    <div class="glass-effect table-wrapper">
      <n-data-table
        :columns="columns"
        :data="dataList"
        :loading="loading"
        :bordered="false"
        :pagination="pagination"
      />
    </div>

    <!-- 编辑/新增 Modal -->
    <n-modal v-model:show="showModal" preset="card" :title="formValue.id ? '修改合同' : '起草合同'" style="width: 900px" class="glass-modal">
      <n-form :model="formValue" label-placement="left" label-width="80px">
        <n-grid :cols="2" :x-gap="24">
          <n-form-item-gi label="合同标题">
            <n-input v-model:value="formValue.title" placeholder="如：陈步-2024劳动合同" />
          </n-form-item-gi>
          <n-form-item-gi label="签署员工">
            <n-select v-model:value="formValue.employeeId" :options="employeeOptions" placeholder="请选择员工" />
          </n-form-item-gi>
          <n-form-item-gi label="合同类型">
             <n-select v-model:value="formValue.type" :options="typeOptions" />
          </n-form-item-gi>
           <n-form-item-gi label="生效期限">
             <n-date-picker v-model:value="dateRange" type="daterange" clearable />
          </n-form-item-gi>
        </n-grid>
        <n-form-item label="合同内容">
          <rich-editor v-model="formValue.content" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSave" :loading="saving">存入档案</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 预览 Modal -->
    <n-modal v-model:show="showPreview" preset="card" title="合同在线预览" style="width: 800px" class="preview-modal glass-modal">
        <div class="contract-preview-box" id="contract-view">
            <div class="contract-header">
                <h1>{{ previewData?.title }}</h1>
                <div class="contract-badge">{{ previewData?.type }}</div>
            </div>
            <div class="contract-seal" v-if="previewData?.status === 1">
                <div class="seal-inner">已签署</div>
            </div>
            <div class="contract-body" v-html="previewData?.content"></div>
            <div class="contract-footer">
                <div class="sign-block">
                    <p>甲方（盖章）：Cloud Workshop 科技有限公司</p>
                    <p>日期：{{ previewData?.signDate ? moment(previewData?.signDate).format('YYYY-MM-DD') : moment().format('YYYY-MM-DD') }}</p>
                </div>
                <div class="sign-block">
                    <p>乙方（签字）：{{ previewData?.employeeName }}</p>
                    <p>日期：{{ previewData?.signDate ? moment(previewData?.signDate).format('YYYY-MM-DD') : moment().format('YYYY-MM-DD') }}</p>
                </div>
            </div>
        </div>
        <template #footer>
            <n-space justify="end">
                <n-button @click="showPreview = false">关闭预览</n-button>
                <n-button type="primary" secondary @click="printContract">
                    <template #icon><n-icon :component="PrintOutline" /></template>
                    打印/导出 PDF
                </n-button>
            </n-space>
        </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, h, computed } from 'vue'
import { 
  NButton, NDataTable, NIcon, NTag, NModal, NForm, NFormItem, NInput, 
  NSelect, NDatePicker, NGrid, NFormItemGi, NSpace, useMessage, useDialog 
} from 'naive-ui'
import { Add, EyeOutline, CreateOutline, TrashOutline, PrintOutline } from '@vicons/ionicons5'
import { getContractList, saveContract, deleteContract } from '../api/contract'
import { getEmployeeList } from '../api/employee'
import RichEditor from '../components/RichEditor.vue'
import moment from 'moment'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const showPreview = ref(false)
const dataList = ref([])
const employees = ref<any[]>([])
const previewData = ref<any>(null)

const dateRange = ref<[number, number] | null>(null)

const formValue = ref({
  id: undefined,
  employeeId: null,
  title: '',
  type: '劳动合同',
  status: 1,
  content: '',
  startDate: null as any,
  endDate: null as any
})

const typeOptions = [
  { label: '劳动合同', value: '劳动合同' },
  { label: '保密协议', value: '保密协议' },
  { label: '竞业协议', value: '竞业协议' },
  { label: '补充协议', value: '补充协议' }
]

const employeeOptions = computed(() => employees.value.map(e => ({ label: `${e.full_name} (${e.emp_code})`, value: e.id })))

const columns = [
  { title: '合同标题', key: 'title', render: (row: any) => h('div', { style: 'font-weight: 700' }, row.title) },
  { title: '签署员工', key: 'employeeName' },
  { title: '类型', key: 'type', render: (row: any) => h(NTag, { size: 'small', ghost: true }, { default: () => row.type }) },
  { 
    title: '有效期', 
    key: 'dateRange',
    render: (row: any) => `${moment(row.startDate).format('YYYY-MM-DD')} ~ ${row.endDate ? moment(row.endDate).format('YYYY-MM-DD') : '长期'}`
  },
  {
    title: '状态',
    key: 'status',
    render: (row: any) => {
      const map: any = { 0: { l: '草稿', t: 'default' }, 1: { l: '正式生效', t: 'success' }, 2: { l: '已过期', t: 'error' } }
      const item = map[row.status] || { l: '未知', t: 'default' }
      return h(NTag, { type: item.t, size: 'small', round: true }, { default: () => item.l })
    }
  },
  {
    title: '操作',
    key: 'actions',
    render: (row: any) => h(NSpace, null, {
        default: () => [
            h(NButton, { size: 'tiny', secondary: true, onClick: () => handlePreview(row) }, { icon: () => h(NIcon, { component: EyeOutline }), default: () => '预览' }),
            h(NButton, { size: 'tiny', type: 'primary', onClick: () => handleEdit(row) }, { icon: () => h(NIcon, { component: CreateOutline }) }),
            h(NButton, { size: 'tiny', type: 'error', quaternary: true, onClick: () => handleDelete(row) }, { icon: () => h(NIcon, { component: TrashOutline }) })
        ]
    })
  }
]

const pagination = { pageSize: 12 }

const loadData = async () => {
  loading.value = true
  try {
    const [cRes, eRes]: any = await Promise.all([
        getContractList(),
        getEmployeeList({ pageNum: 1, pageSize: 1000 })
    ])
    dataList.value = cRes
    employees.value = eRes.records
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  formValue.value = { id: undefined, employeeId: null, title: '', type: '劳动合同', status: 1, content: '', startDate: null, endDate: null }
  dateRange.value = null
  showModal.value = true
}

const handleEdit = (row: any) => {
  formValue.value = { ...row }
  dateRange.value = row.startDate ? [new Date(row.startDate).getTime(), new Date(row.endDate).getTime()] : null
  showModal.value = true
}

const handlePreview = (row: any) => {
    previewData.value = row
    showPreview.value = true
}

const handleSave = async () => {
  if (dateRange.value) {
    formValue.value.startDate = moment(dateRange.value[0]).format('YYYY-MM-DD')
    formValue.value.endDate = moment(dateRange.value[1]).format('YYYY-MM-DD')
  }
  saving.value = true
  try {
    await saveContract(formValue.value)
    message.success('合同已存入档案')
    showModal.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认回滚',
    content: `删除合同 "${row.title}" 后将无法找回，确认操作？`,
    positiveText: '确认',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteContract(row.id)
      message.success('已删除记录')
      loadData()
    }
  })
}

const printContract = () => {
    const content = document.getElementById('contract-view')?.innerHTML
    if (!content) return
    const win = window.open('', '_blank')
    win?.document.write(`
        <html>
            <head>
                <title>${previewData.value.title}</title>
                <style>
                    body { font-family: SimSun, serif; padding: 40px; line-height: 1.8; }
                    h1 { text-align: center; }
                    .contract-footer { display: flex; justify-content: space-between; margin-top: 100px; }
                    img { max-width: 100%; }
                </style>
            </head>
            <body>${content}</body>
        </html>
    `)
    win?.document.close()
    win?.print()
}

onMounted(() => loadData())
</script>

<style scoped>
.contract-container { padding: 24px; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }
.title-group h2 { margin: 0; font-size: 24px; font-weight: 700; color: var(--text-primary); }
.subtitle { margin: 4px 0 0; color: var(--text-secondary); font-size: 14px; }

.table-wrapper { padding: 20px; border-radius: 24px; }

.contract-preview-box {
    padding: 60px 40px;
    background: white;
    color: #1e293b;
    border-radius: 4px;
    position: relative;
    min-height: 800px;
    font-family: serif;
    line-height: 1.8;
}

.contract-header { text-align: center; margin-bottom: 40px; border-bottom: 2px solid #334155; padding-bottom: 20px; }
.contract-header h1 { margin: 0; font-size: 28px; }
.contract-badge { display: inline-block; margin-top: 10px; background: #f1f5f9; padding: 4px 12px; border-radius: 20px; font-size: 14px; color: #64748b; }

.contract-body { font-size: 16px; margin-bottom: 100px; }

.contract-footer { display: flex; justify-content: space-between; }
.sign-block p { margin: 8px 0; }

.contract-seal {
    position: absolute;
    right: 80px;
    bottom: 150px;
    width: 120px;
    height: 120px;
    border: 4px solid #ef4444;
    border-radius: 50%;
    color: #ef4444;
    display: flex;
    align-items: center;
    justify-content: center;
    transform: rotate(-15deg);
    opacity: 0.8;
    z-index: 10;
}
.seal-inner { font-size: 24px; font-weight: 900; border: 2px solid #ef4444; padding: 5px; border-radius: 4px; }
</style>

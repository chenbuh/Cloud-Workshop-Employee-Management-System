<template>
  <div class="employee-page">
    <div class="page-header">
      <h2>员工花名册</h2>
      <n-space>
        <n-button secondary type="info" @click="handleExport">
          <template #icon><n-icon :component="DownloadOutline" /></template>
          导出报表
        </n-button>
        <n-button type="primary" size="medium" @click="showOnboardModal = true">
          <template #icon><n-icon :component="AddOutline" /></template>
          办理入职
        </n-button>
      </n-space>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-bar glass-effect">
      <n-input v-model:value="searchKeyword" placeholder="搜索姓名或工号..." clearable @keydown.enter="loadData">
        <template #prefix><n-icon :component="SearchOutline" /></template>
      </n-input>
      <n-button secondary type="primary" @click="loadData">查询</n-button>
    </div>

    <!-- 数据表格 -->
    <n-data-table
      remote
      :columns="columns"
      :data="dataList"
      :loading="loading"
      :pagination="pagination"
      @update:page="handlePageChange"
      :row-props="rowProps"
      class="glass-table"
    />

    <!-- 入职弹窗 -->
    <n-modal v-model:show="showOnboardModal" title="新员工入职向导" preset="card" style="width: 600px" class="glass-modal">
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="100px">
        <n-form-item label="姓名" path="fullName">
          <n-input v-model:value="formModel.fullName" placeholder="请输入真实姓名" />
        </n-form-item>
        <n-form-item label="工号(选填)" path="empCode">
          <n-input v-model:value="formModel.empCode" placeholder="留空则自动生成" />
        </n-form-item>
        <n-form-item label="手机号" path="emergencyPhone">
          <n-input v-model:value="formModel.emergencyPhone" placeholder="联系电话" />
        </n-form-item>
        <n-form-item label="部门" path="deptId">
          <n-select
            v-model:value="formModel.deptId"
            :options="deptOptions"
            placeholder="请选择部门"
          />
        </n-form-item>
        <n-form-item label="入职日期" path="entryDate">
          <n-date-picker v-model:value="formModel.entryDate" type="date" clearable style="width: 100%" />
        </n-form-item>
        <n-form-item label="岗位" path="postId">
          <n-select
            v-model:value="formModel.postId"
            :options="postOptions"
            placeholder="请选择岗位"
          />
        </n-form-item>
        <n-form-item label="职级" path="levelId">
          <n-select
            v-model:value="formModel.levelId"
            :options="levelOptions"
            placeholder="请选择职级"
          />
        </n-form-item>
        <n-form-item label="职位" path="jobTitle">
          <n-input v-model:value="formModel.jobTitle" placeholder="例如：高级前端开发工程师" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px">
          <n-button @click="showOnboardModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleOnboard">保存入职信息</n-button>
        </div>
      </template>
    </n-modal>

    <!-- 员工详情/编辑侧滑 -->
    <n-drawer v-model:show="showDetailDrawer" :width="500" class="glass-drawer">
      <n-drawer-content closable>
        <template #header>
          <div class="drawer-header">
            <span class="drawer-title">{{ isEdit ? '编辑员工信息' : '员工详细档案' }}</span>
            <n-tag :type="statusMap[detailForm.status]?.type" round size="small">
              {{ statusMap[detailForm.status]?.label }}
            </n-tag>
          </div>
        </template>
        
        <div class="drawer-body">
          <n-form label-placement="top" :model="detailForm" :disabled="!isEdit">
            <n-grid :cols="2" :x-gap="12">
              <n-form-item-gi label="姓名">
                <n-input v-model:value="detailForm.fullName" />
              </n-form-item-gi>
              <n-form-item-gi label="工号">
                <n-input v-model:value="detailForm.empCode" disabled />
              </n-form-item-gi>
              <n-form-item-gi label="部门">
                <n-select v-model:value="detailForm.deptId" :options="deptOptions" />
              </n-form-item-gi>
              <n-form-item-gi label="岗位">
                <n-select v-model:value="detailForm.postId" :options="postOptions" />
              </n-form-item-gi>
              <n-form-item-gi label="职级">
                <n-select v-model:value="detailForm.levelId" :options="levelOptions" />
              </n-form-item-gi>
              <n-form-item-gi label="职位">
                <n-input v-model:value="detailForm.jobTitle" />
              </n-form-item-gi>
              <n-form-item-gi label="手机号">
                <n-input v-model:value="detailForm.emergencyPhone" />
              </n-form-item-gi>
              <n-form-item-gi label="状态">
                <n-select v-model:value="detailForm.status" :options="statusOptions" />
              </n-form-item-gi>
              <n-form-item-gi label="入职日期" span="2">
                <n-date-picker v-model:value="detailForm.entryDate" type="date" style="width: 100%" />
              </n-form-item-gi>
            </n-grid>
          </n-form>
        </div>

        <template #footer>
          <n-space justify="end">
            <n-button type="info" secondary @click="viewFullProfile(detailForm.id)">
               <template #icon><n-icon :component="AnalyticsOutline" /></template>
               技能雷达与成长档案
            </n-button>
            <template v-if="!isEdit">
              <n-button type="primary" secondary @click="isEdit = true">
                进入编辑模式
              </n-button>
              <n-button type="error" ghost v-if="detailForm.status !== 3" @click="handleOpenResign">
                 办理离职
              </n-button>
            </template>
            <template v-else>
              <n-button @click="isEdit = false">取消</n-button>
              <n-button type="primary" :loading="submitting" @click="handleUpdate">保存修改</n-button>
            </template>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>

    <!-- 离职弹窗 -->
    <n-modal v-model:show="showResignModal" title="办理离职手续" preset="card" style="width: 450px">
      <n-form :model="resignForm" label-placement="left" label-width="80px">
        <n-form-item label="员工姓名">
           <n-input :value="detailForm.fullName" disabled />
        </n-form-item>
        <n-form-item label="离职日期">
           <n-date-picker v-model:value="resignForm.quitDate" type="date" style="width: 100%" />
        </n-form-item>
        <n-form-item label="离职原因">
           <n-input v-model:value="resignForm.quitReason" type="textarea" placeholder="请输入离职原因或去向..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
           <n-button @click="showResignModal = false">取消</n-button>
           <n-button type="error" @click="submitResign" :loading="submitting">确认离职</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
import { 
  NTag, NButton, NIcon, NSpace, NSelect, NDatePicker, NGrid, NFormItemGi, 
  NForm, NModal, NDrawer, NDrawerContent, NInput, NDataTable, useMessage, useDialog,
  FormRules
} from 'naive-ui'
import { AddOutline, SearchOutline, CreateOutline, TrashOutline, DownloadOutline, AnalyticsOutline } from '@vicons/ionicons5'
import { getEmployeeList, onboardEmployee, updateEmployee, removeEmployee, resignEmployee } from '../api/employee'
import { getDeptList } from '../api/dept'
import { allPost, allJobLevel } from '../api/job'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showOnboardModal = ref(false)
const showDetailDrawer = ref(false)
const isEdit = ref(false)

/* Resignation Logic */
const showResignModal = ref(false)
const resignForm = reactive({
  quitDate: Date.now(),
  quitReason: ''
})

const handleOpenResign = () => {
  resignForm.quitDate = Date.now()
  resignForm.quitReason = ''
  showResignModal.value = true
}

const submitResign = async () => {
    submitting.value = true
    try {
        await resignEmployee({
            id: detailForm.id,
            quitDate: new Date(resignForm.quitDate),
            quitReason: resignForm.quitReason
        })
        message.success(`员工 [${detailForm.fullName}] 离职手续办理完成`)
        showResignModal.value = false
        showDetailDrawer.value = false
        loadData()
    } catch(e: any) {
        message.error(e.message || '办理失败')
    } finally {
        submitting.value = false
    }
}

const viewFullProfile = (id: number) => {
    router.push(`/employee/profile/${id}`)
}

const handleExport = () => {
  const token = localStorage.getItem('token')
  const url = `/api/v1/employee/export?keyword=${searchKeyword.value}`
  
  message.loading('报表生成中...', { duration: 2000 })
  
  fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  .then(res => res.blob())
  .then(blob => {
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `员工工作花名册_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    message.success('导出成功')
  })
  .catch(() => {
    message.error('导出失败，请重试')
  })
}
const searchKeyword = ref('')
const dataList = ref([])
const deptOptions = ref([])
const postOptions = ref([])
const levelOptions = ref([])

const statusMap: any = {
  1: { type: 'success', label: '正式' },
  2: { type: 'info', label: '试用' },
  3: { type: 'error', label: '离职' }
}

const statusOptions = [
  { label: '正式', value: 1 },
  { label: '试用', value: 2 },
  { label: '离职', value: 3 }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

const formModel: any = reactive({
  fullName: '',
  empCode: '',
  emergencyPhone: '',
  entryDate: null,
  jobTitle: '',
  deptId: null,
  postId: null,
  levelId: null
})

const detailForm: any = reactive({
  id: null,
  fullName: '',
  empCode: '',
  emergencyPhone: '',
  entryDate: null,
  jobTitle: '',
  deptId: null,
  postId: null,
  levelId: null,
  status: 1
})

const rules: FormRules = {
  fullName: { required: true, message: '请输入姓名', trigger: 'blur' },
  deptId: { required: true, type: 'number', message: '请选择部门', trigger: ['blur', 'change'] },
  entryDate: { required: true, type: 'number', message: '请选择入职日期', trigger: ['blur', 'change'] },
  jobTitle: { required: true, message: '请输入职位', trigger: 'blur' }
}

const rowProps = (row: any) => {
  return {
    style: 'cursor: pointer;',
    onClick: () => {
      Object.assign(detailForm, row)
      isEdit.value = false
      showDetailDrawer.value = true
    }
  }
}

const handleEdit = (row: any, e: MouseEvent) => {
  e.stopPropagation()
  Object.assign(detailForm, row)
  isEdit.value = true
  showDetailDrawer.value = true
}

const handleDelete = (row: any, e: MouseEvent) => {
  e.stopPropagation()
  dialog.warning({
    title: '确认删除',
    content: `确定要删除员工 [${row.fullName}] 吗？此操作不可恢复。`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await removeEmployee(row.id)
        message.success('删除成功')
        loadData()
      } catch (err) {}
    }
  })
}

const columns = [
  { title: '工号', key: 'empCode', width: 140 },
  { title: '姓名', key: 'fullName', width: 120, render(row: any) {
      return h('b', { style: 'color: #3f51b5' }, row.fullName)
  }},
  { title: '部门', key: 'deptName', width: 140 },
  { title: '岗位', key: 'postName', width: 140 },
  { title: '职级', key: 'levelName', width: 100 },
  { title: '职位', key: 'jobTitle', width: 160 },
  { title: '入职日期', key: 'entryDate', width: 140, render(row: any) {
      return row.entryDate ? new Date(row.entryDate).toLocaleDateString() : '-'
  }},
  { title: '状态', key: 'status', width: 100, render(row: any) {
      const conf = statusMap[row.status] || { type: 'default', label: '未知' }
      return h(NTag, { type: conf.type, bordered: false, round: true, size: 'small' }, { default: () => conf.label })
  }},
  { title: '操作', key: 'actions', width: 120, render(row: any) {
      return h(NSpace, { justify: 'center' }, {
        default: () => [
          h(NButton, { 
            quaternary: true, 
            circle: true, 
            type: 'primary',
            size: 'small',
            onClick: (e: MouseEvent) => handleEdit(row, e)
          }, { icon: () => h(NIcon, { component: CreateOutline }) }),
          h(NButton, { 
            quaternary: true, 
            circle: true, 
            type: 'error',
            size: 'small',
            onClick: (e: MouseEvent) => handleDelete(row, e)
          }, { icon: () => h(NIcon, { component: TrashOutline }) })
        ]
      })
  }}
]

const loadDepts = async () => {
  try {
    const res: any = await getDeptList()
    deptOptions.value = res.map((d: any) => ({
      label: d.deptName,
      value: d.id
    }))
    
    // Load posts and levels too
    const posts: any = await allPost()
    postOptions.value = posts.map((p: any) => ({ label: p.postName, value: p.postId }))
    
    const levels: any = await allJobLevel()
    levelOptions.value = levels.map((l: any) => ({ label: l.levelName, value: l.levelId }))
  } catch (e) {}
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getEmployeeList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value
    })
    dataList.value = res.records
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

const handleOnboard = async () => {
  submitting.value = true
  try {
    await onboardEmployee(formModel)
    message.success('办理入职成功！')
    showOnboardModal.value = false
    loadData()
    Object.keys(formModel).forEach((k: any) => (formModel as any)[k] = null)
  } finally {
    submitting.value = false
  }
}

const handleUpdate = async () => {
  submitting.value = true
  try {
    await updateEmployee(detailForm)
    message.success('修改成功！')
    showDetailDrawer.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
  loadDepts()
})
</script>

<style scoped>
.employee-page {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

h2 {
  margin: 0;
  font-weight: 800;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 26px;
  letter-spacing: -0.5px;
}

.filter-bar {
  padding: 12px;
  border-radius: 12px;
  margin-bottom: 24px;
  display: flex;
  gap: 12px;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

:deep(.n-data-table) {
  background: transparent !important;
  border-radius: 12px;
  overflow: hidden;
}

:deep(.n-data-table-th) {
  background: rgba(248, 250, 252, 0.8) !important;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  font-size: 13px;
}

:deep(.n-data-table-td) {
  background: rgba(255, 255, 255, 0.6) !important;
  padding: 16px !important;
}

:deep(.n-data-table-tr:hover .n-data-table-td) {
  background: rgba(241, 245, 249, 0.8) !important;
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.drawer-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.glass-drawer {
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(20px);
}

.glass-modal {
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.9) !important;
}
</style>

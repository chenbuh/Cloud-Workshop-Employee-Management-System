<template>
  <div class="approval-page employee-page">
    <div class="page-header">
      <h2>审批管理中心</h2>
      <n-space>
        <n-radio-group v-model:value="filterStatus" @update:value="loadData">
          <n-radio-button :value="undefined">全部</n-radio-button>
          <n-radio-button :value="0">待处理</n-radio-button>
          <n-radio-button :value="1">已通过</n-radio-button>
          <n-radio-button :value="2">已驳回</n-radio-button>
        </n-radio-group>
        <n-button type="primary" @click="showApplyModal = true">提效申请</n-button>
      </n-space>
    </div>

    <!-- 申请弹窗 -->
    <n-modal v-model:show="showApplyModal" preset="card" title="提交请假/加班申请" style="width: 500px">
      <n-form :model="applyForm">
        <n-form-item label="类型">
          <n-select v-model:value="applyForm.leaveType" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="起止时间">
          <n-date-picker v-model:value="applyForm.range" type="datetimerange" />
        </n-form-item>
        <n-form-item label="理由">
          <n-input v-model:value="applyForm.reason" type="textarea" placeholder="请输入申请事由" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showApplyModal = false">取消</n-button>
          <n-button type="primary" @click="handleApply" :loading="submitting">确认提交</n-button>
        </n-space>
      </template>
    </n-modal>

    <div class="glass-effect table-container">
      <n-data-table
        :columns="columns"
        :data="dataList"
        :loading="loading"
        :pagination="pagination"
      />
    </div>

    <!-- 审批详情对话框 -->
    <n-modal v-model:show="showModal" preset="card" title="审批详情" style="width: 500px" class="glass-modal">
      <div v-if="currentRecord" class="record-detail">
        <n-descriptions label-placement="left" bordered :column="1">
          <n-descriptions-item label="申请人">{{ currentRecord.full_name }}</n-descriptions-item>
          <n-descriptions-item label="部门">{{ currentRecord.dept_name }}</n-descriptions-item>
          <n-descriptions-item label="申请类型">{{ currentRecord.leave_type }}</n-descriptions-item>
          <n-descriptions-item label="开始时间">{{ formatDate(currentRecord.start_time) }}</n-descriptions-item>
          <n-descriptions-item label="结束时间">{{ formatDate(currentRecord.end_time) }}</n-descriptions-item>
          <n-descriptions-item label="请假理由">{{ currentRecord.reason }}</n-descriptions-item>
          <n-descriptions-item label="当前状态">
            <n-tag :type="statusMap[currentRecord.status]?.type">
              {{ statusMap[currentRecord.status]?.label }}
            </n-tag>
          </n-descriptions-item>
        </n-descriptions>
      </div>
      <template #footer>
        <n-space justify="end" v-if="currentRecord && currentRecord.status === 0">
          <n-button type="error" ghost @click="handleApprove(2)">驳回</n-button>
          <n-button type="primary" @click="handleApprove(1)">批准</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { 
  NDataTable, NTag, NSpace, NButton, NModal, NDescriptions, 
  NDescriptionsItem, NRadioGroup, NRadioButton, useMessage,
  NForm, NFormItem, NSelect, NDatePicker, NInput
} from 'naive-ui'
import { getLeaveList, approveLeave, applyLeave } from '../api/leave'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const filterStatus = ref<number | undefined>(0)
const dataList = ref([])
const showModal = ref(false)
const showApplyModal = ref(false)
const currentRecord = ref<any>(null)

const applyForm = reactive({
  leaveType: '事假',
  range: null as [number, number] | null,
  reason: ''
})

const typeOptions = [
  { label: '事假', value: '事假' },
  { label: '病假', value: '病假' },
  { label: '年假', value: '年假' },
  { label: '加班', value: '加班' }
]

const statusMap: any = {
  0: { label: '待审批', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已驳回', type: 'error' }
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page: number) => {
    pagination.page = page
    loadData()
  }
})

const columns = [
  { title: '姓名', key: 'full_name', width: 100 },
  { title: '部门', key: 'dept_name', width: 120 },
  { title: '类型', key: 'leave_type', width: 100 },
  { title: '开始时间', key: 'start_time', render: (row: any) => formatDate(row.start_time) },
  { title: '时长', key: 'duration', render: (row: any) => calculateDuration(row.start_time, row.end_time) },
  { title: '状态', key: 'status', render: (row: any) => {
      const conf = statusMap[row.status]
      return h(NTag, { type: conf.type, bordered: false }, { default: () => conf.label })
  }},
  { title: '操作', key: 'actions', render: (row: any) => {
      return h(NButton, {
        size: 'small',
        type: 'primary',
        quaternary: true,
        onClick: () => {
          currentRecord.value = row
          showModal.value = true
        }
      }, { default: () => row.status === 0 ? '去审批' : '查看' })
  }}
]

const formatDate = (date: any) => moment(date).format('YYYY-MM-DD HH:mm')
const calculateDuration = (start: any, end: any) => {
    const diff = moment(end).diff(moment(start), 'hours')
    return diff + ' 小时'
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getLeaveList({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      status: filterStatus.value
    })
    dataList.value = res.records
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

const handleApply = async () => {
  if (!applyForm.range) {
    message.warning('请选择起止时间')
    return
  }
  submitting.value = true
  try {
    await applyLeave({
      leaveType: applyForm.leaveType,
      startTime: applyForm.range[0],
      endTime: applyForm.range[1],
      reason: applyForm.reason
    })
    message.success('申请已提交')
    showApplyModal.value = false
    loadData()
    applyForm.reason = ''
    applyForm.range = null
  } finally {
    submitting.value = false
  }
}

const handleApprove = async (status: number) => {
  try {
    await approveLeave({ id: currentRecord.value.id, status })
    message.success(status === 1 ? '已批准申请' : '已驳回申请')
    showModal.value = false
    loadData()
  } catch (e) {}
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.table-container {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.6);
}

.record-detail {
  padding: 12px 0;
}
</style>

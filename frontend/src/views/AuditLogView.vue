<template>
  <div class="audit-page">
    <div class="page-header">
      <h2>安全审计日志 (Security Audit)</h2>
      <n-space>
        <n-input v-model:value="queryParams.title" placeholder="操作模块..." clearable />
        <n-select
          v-model:value="queryParams.status"
          placeholder="状态"
          :options="statusOptions"
          style="width: 120px"
          clearable
        />
        <n-button type="primary" secondary @click="handleQuery">查询</n-button>
      </n-space>
    </div>

    <div class="glass-effect content-card">
      <n-data-table
        remote
        :columns="columns"
        :data="logList"
        :loading="loading"
        :pagination="pagination"
        @update:page="handlePageChange"
      />
    </div>

    <!-- 详情模态框 -->
    <n-modal v-model:show="showDetail" preset="card" title="日志详情" style="width: 700px">
      <n-descriptions label-placement="left" :column="2" bordered>
        <n-descriptions-item label="操作模块">{{ currentLog.title }}</n-descriptions-item>
        <n-descriptions-item label="业务类型">{{ getBusinessTypeText(currentLog.businessType) }}</n-descriptions-item>
        <n-descriptions-item label="操作人员">{{ currentLog.operatorName }}</n-descriptions-item>
        <n-descriptions-item label="操作地点">{{ currentLog.operIp }}</n-descriptions-item>
        <n-descriptions-item label="请求地址" :span="2">{{ currentLog.operUrl }}</n-descriptions-item>
        <n-descriptions-item label="操作时间" :span="2">{{ formatTime(currentLog.operTime) }}</n-descriptions-item>
        <n-descriptions-item label="请求参数" :span="2">
            <n-code :code="beautifyJson(currentLog.operParam)" language="json" word-wrap />
        </n-descriptions-item>
        <n-descriptions-item label="返回结果" :span="2" v-if="currentLog.jsonResult">
            <n-code :code="beautifyJson(currentLog.jsonResult)" language="json" word-wrap />
        </n-descriptions-item>
        <n-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMsg">
            <span style="color: #ef4444">{{ currentLog.errorMsg }}</span>
        </n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { 
  NButton, NDataTable, NSpace, NInput, NSelect, NModal, NDescriptions, 
  NDescriptionsItem, NTag, NCode, useMessage 
} from 'naive-ui'
import { listAuditLogs } from '../api/audit'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const logList = ref([])
const showDetail = ref(false)
const currentLog = ref<any>({})

const queryParams = reactive({
  title: '',
  status: null as number | null
})

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '异常', value: 0 }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

const columns = [
  { title: '模块', key: 'title' },
  { title: '操作人', key: 'operatorName' },
  { title: 'IP', key: 'operIp' },
  { title: '状态', key: 'status', render: (row: any) => h(NTag, { type: row.status === 1 ? 'success' : 'error' }, { default: () => row.status === 1 ? '正常' : '异常' }) },
  { title: '操作时间', key: 'operTime', render: (row: any) => moment(row.operTime).format('YYYY-MM-DD HH:mm:ss') },
  {
    title: '操作',
    key: 'actions',
    render: (row: any) => h(NButton, {
      size: 'small',
      quaternary: true,
      type: 'primary',
      onClick: () => viewDetail(row)
    }, { default: () => '详情' })
  }
]

const loadLogs = async () => {
  loading.value = true
  try {
    const res: any = await listAuditLogs({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      ...queryParams
    })
    logList.value = res.records
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  pagination.page = 1
  loadLogs()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadLogs()
}

const viewDetail = (log: any) => {
  currentLog.value = log
  showDetail.value = true
}

const getBusinessTypeText = (type: number) => {
    const map: any = { 0: '其它', 1: '新增', 2: '修改', 3: '删除' }
    return map[type] || '未知'
}

const beautifyJson = (jsonStr: string) => {
    if (!jsonStr) return ''
    try {
        const obj = JSON.parse(jsonStr)
        return JSON.stringify(obj, null, 2)
    } catch (e) {
        return jsonStr
    }
}

const formatTime = (time: any) => time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : '-'

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.audit-page {
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.content-card {
  padding: 24px;
  border-radius: 16px;
}
</style>

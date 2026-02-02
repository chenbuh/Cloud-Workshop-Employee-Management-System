<template>
  <div class="onboarding-page">
    <div class="page-header">
      <h2>入职漏斗 (Onboarding Funnel)</h2>
      <n-button type="primary" @click="showAddModal = true">
        <template #icon><n-icon :component="AddOutline" /></template>
        新增候选人
      </n-button>
    </div>

    <div class="filter-bar glass-effect">
      <n-space>
        <n-input v-model:value="searchName" placeholder="候选人姓名..." clearable @keyup.enter="handleQuery" />
        <n-button type="primary" secondary @click="handleQuery">查询</n-button>
      </n-space>
    </div>

    <div class="funnel-stats glass-effect">
      <div class="stat-item" v-for="stat in funnelStats" :key="stat.label">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
      </div>
    </div>

    <div class="glass-effect content-card">
      <n-tabs type="segment" animated @update:value="handleTabChange">
        <n-tab-pane :name="1" tab="初筛中" />
        <n-tab-pane :name="2" tab="面试中" />
        <n-tab-pane :name="3" tab="待入职" />
        <n-tab-pane :name="4" tab="已拒绝" />
        <n-tab-pane :name="5" tab="已入职" />
      </n-tabs>

      <div class="candidate-list">
        <n-data-table
          :columns="columns"
          :data="candidates"
          :loading="loading"
          :pagination="pagination"
          remote
          @update:page="handlePageChange"
        />
      </div>
    </div>

    <!-- 新增候选人弹窗 -->
    <n-modal v-model:show="showAddModal" preset="card" title="添加候选人" style="width: 500px">
      <n-form :model="addForm" label-placement="left" label-width="80px">
        <n-form-item label="姓名" path="fullName">
          <n-input v-model:value="addForm.fullName" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="addForm.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="岗位">
          <n-select v-model:value="addForm.postId" :options="postOptions" placeholder="请选择岗位" />
        </n-form-item>
        <n-form-item label="渠道">
          <n-input v-model:value="addForm.source" placeholder="例如：Boss直聘、内推" />
        </n-form-item>
        <n-form-item label="上传简历">
          <n-upload
            action="/api/v1/file/upload"
            :headers="uploadHeaders"
            :max="1"
            @finish="handleUploadFinish"
          >
            <n-button>
              <template #icon><n-icon :component="CloudUploadOutline" /></template>
              点击上传 PDF/Word
            </n-button>
          </n-upload>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" block @click="handleAddCandidate" :loading="submitting">保存</n-button>
      </template>
    </n-modal>

    <!-- 面试记录/状态变更弹窗 -->
    <n-modal v-model:show="showInterviewModal" preset="card" title="推进进度" style="width: 600px">
      <div class="candidate-info-brief">
          <h3>{{ currentCandidate?.fullName }}</h3>
          <p>{{ currentCandidate?.postName }} | {{ currentCandidate?.phone }}</p>
      </div>
      
      <n-tabs type="line">
        <n-tab-pane name="history" tab="面试记录">
          <n-timeline v-if="interviews.length > 0">
            <n-timeline-item
              v-for="item in interviews"
              :key="item.interviewId"
              :type="item.status === 2 ? 'success' : 'info'"
              :title="`第 ${item.round} 轮面试`"
              :content="item.comment"
              :time="formatTime(item.interviewTime)"
            />
          </n-timeline>
          <n-empty v-else description="暂无面试记录" />
        </n-tab-pane>
        <n-tab-pane name="action" tab="推进到下一阶段">
          <n-space vertical>
            <n-radio-group v-model:value="nextStatus">
                <n-radio :value="2">进入面试</n-radio>
                <n-radio :value="3">发放 Offer (待入职)</n-radio>
                <n-radio :value="4">不合适 (拒绝)</n-radio>
                <n-radio :value="5">办理入职</n-radio>
            </n-radio-group>
            
            <div v-if="nextStatus === 2" class="interview-form">
               <n-divider title-placement="left">面试安排</n-divider>
               <n-form-item label="面试反馈/备注">
                  <n-input v-model:value="interviewForm.comment" type="textarea" placeholder="记录面试评价或面试安排" />
               </n-form-item>
               <n-form-item label="轮次">
                  <n-input-number v-model:value="interviewForm.round" :min="1" />
               </n-form-item>
            </div>
            
            <div v-if="nextStatus === 5" class="onboard-form">
               <n-divider title-placement="left">录用安排</n-divider>
               <n-form-item label="分配部门">
                  <n-select v-model:value="onboardDeptId" :options="deptOptions" placeholder="请为新员工分配部门" />
               </n-form-item>
            </div>
            
            <n-button type="primary" block @click="handleConfirmStatusChange" :loading="submitting">确定变更</n-button>
          </n-space>
        </n-tab-pane>
      </n-tabs>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { 
  NButton, NIcon, NTabs, NTabPane, NDataTable, NModal, NForm, NFormItem, 
  NInput, NSelect, NTag, NSpace, NTimeline, NTimelineItem, NEmpty, NRadioGroup, 
  NRadio, NDivider, NInputNumber, NUpload, useMessage
} from 'naive-ui'
import { AddOutline, PencilOutline, CloudUploadOutline, DocumentAttachOutline } from '@vicons/ionicons5'
import { listCandidates, addCandidate, updateCandidateStatus, getInterviews, addInterview, convertToEmployee } from '../api/onboarding'
import { allPost } from '../api/job'
import { getDeptList } from '../api/dept'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const candidates = ref<any[]>([])
const activeStatus = ref(1)
const searchName = ref('')
const showAddModal = ref(false)
const showInterviewModal = ref(false)
const interviews = ref<any[]>([])
const currentCandidate = ref<any>(null)
const nextStatus = ref(2)
const postOptions = ref([])
const deptOptions = ref([])
const onboardDeptId = ref<number | null>(null)

const addForm = reactive({
  fullName: '',
  phone: '',
  postId: null,
  source: '招聘平台',
  resumeUrl: ''
})

const uploadHeaders = {
    Authorization: `Bearer ${localStorage.getItem('token')}`
}

const handleUploadFinish = ({ file, event }: any) => {
    const res = JSON.parse((event.target as any).response)
    if (res.code === 200) {
        addForm.resumeUrl = res.data.url
        message.success('简历上传成功')
    } else {
        message.error('上传失败: ' + res.message)
    }
}

const interviewForm = reactive({
    candidateId: null,
    round: 1,
    comment: '',
    status: 1
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

const funnelStats = ref([
  { label: '简历初筛', value: 0 },
  { label: '面试安排', value: 0 },
  { label: '待入职', value: 0 },
  { label: '已录取', value: 0 }
])

const columns = [
  { title: '姓名', key: 'fullName' },
  { title: '岗位', key: 'postName' },
  { title: '手机号', key: 'phone' },
  { title: '来源', key: 'source' },
  { title: '简历', key: 'resumeUrl', render: (row: any) => {
      if (!row.resumeUrl) return h('span', { style: 'color: #94a3b8' }, '无')
      return h(NButton, {
          size: 'tiny',
          quaternary: true,
          type: 'info',
          onClick: () => window.open(row.resumeUrl, '_blank')
      }, {
          icon: () => h(NIcon, { component: DocumentAttachOutline }),
          default: () => '查看'
      })
  }},
  { title: '应聘日期', key: 'createTime', render: (row: any) => moment(row.createTime).format('YYYY-MM-DD') },
  {
    title: '操作',
    key: 'actions',
    render: (row: any) => h(NButton, {
      size: 'small',
      quaternary: true,
      type: 'primary',
      onClick: () => openInterviewModal(row)
    }, { default: () => '推进/详情' })
  }
]

const loadCandidates = async () => {
  loading.value = true
  try {
    const res: any = await listCandidates({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      status: activeStatus.value,
      name: searchName.value
    })
    candidates.value = res.records
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
    pagination.page = 1
    loadCandidates()
}

const loadPosts = async () => {
    const posts: any = await allPost()
    postOptions.value = posts.map((p: any) => ({ label: p.postName, value: p.postId }))
}

const handleTabChange = (val: number) => {
  activeStatus.value = val
  pagination.page = 1
  loadCandidates()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadCandidates()
}

const handleAddCandidate = async () => {
  submitting.value = true
  try {
    await addCandidate(addForm)
    message.success('添加成功')
    showAddModal.value = false
    loadCandidates()
  } finally {
    submitting.value = false
  }
}

const openInterviewModal = async (candidate: any) => {
  currentCandidate.value = candidate
  const res: any = await getInterviews(candidate.candidateId)
  interviews.value = res
  showInterviewModal.value = true
  nextStatus.value = candidate.status === 5 ? 5 : (candidate.status + 1)
  if (nextStatus.value > 5) nextStatus.value = 5
}

const handleConfirmStatusChange = async () => {
    submitting.value = true
    try {
        if (nextStatus.value === 2 && interviewForm.comment) {
            await addInterview({
                candidateId: currentCandidate.value.candidateId,
                round: interviewForm.round,
                comment: interviewForm.comment,
                status: 1
            })
        }
        
        if (nextStatus.value === 5) {
            if (!onboardDeptId.value) {
                message.error('请选择分配部门')
                submitting.value = false
                return
            }
            await convertToEmployee(currentCandidate.value.candidateId, onboardDeptId.value)
            message.success('已成功转为正式员工')
        } else {
            await updateCandidateStatus(currentCandidate.value.candidateId, nextStatus.value)
            message.success('状态更新成功')
        }
        
        showInterviewModal.value = false
        loadCandidates()
    } finally {
        submitting.value = false
    }
}

const formatTime = (time: any) => time ? moment(time).format('YYYY-MM-DD HH:mm') : '-'

const loadDepts = async () => {
    const depts: any = await getDeptList()
    deptOptions.value = depts.map((d: any) => ({ label: d.deptName, value: d.id }))
}

onMounted(() => {
  loadCandidates()
  loadPosts()
  loadDepts()
})
</script>

<style scoped>
.onboarding-page {
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.filter-bar {
  padding: 12px 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.5);
}
.funnel-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
  padding: 24px;
  border-radius: 16px;
}
.stat-item {
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #6366f1;
}
.stat-label {
  color: #64748b;
  font-size: 14px;
  margin-top: 4px;
}
.content-card {
  padding: 24px;
  border-radius: 16px;
}
.candidate-list {
  margin-top: 24px;
}
.candidate-info-brief {
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f1f5f9;
}
.candidate-info-brief h3 { margin: 0; }
.candidate-info-brief p { margin: 4px 0 0; color: #64748b; }
.interview-form {
    margin: 16px 0;
    padding: 16px;
    background: #f8fafc;
    border-radius: 8px;
}
</style>

<template>
  <div class="recruitment-container">
    <div class="header-section">
      <div class="title-wrapper">
        <h2>招聘管理 (ATS)</h2>
        <p class="subtitle">人才引进与面试全流程跟踪</p>
      </div>
      <n-space>
        <n-radio-group v-model:value="viewMode" size="medium">
            <n-radio-button value="kanban">
                <n-icon :component="BriefcaseOutline" style="margin-right: 5px; position: relative; top: 2px;" />看板
            </n-radio-button>
            <n-radio-button value="calendar">
                <n-icon :component="CalendarOutline" style="margin-right: 5px; position: relative; top: 2px;" />日历
            </n-radio-button>
        </n-radio-group>
        <n-button type="primary" size="medium" @click="showJobModal = true">
          <template #icon><n-icon :component="AddOutline" /></template>
          发布职位
        </n-button>
        <!-- <n-input placeholder="搜索候选人..." style="width: 200px">
          <template #prefix><n-icon :component="SearchOutline" /></template>
        </n-input> -->
      </n-space>
    </div>

    <!-- 看板视图 -->
    <!-- 看板视图 -->
    <div v-if="viewMode === 'kanban'" class="kanban-board">
      <div 
        v-for="column in kanbanColumns" 
        :key="column.id" 
        class="kanban-column glass-effect"
        @dragover.prevent
        @drop="onDrop($event, column.status)"
      >
        <div class="column-header" :style="{ borderTopColor: column.color }">
          <span class="column-title">{{ column.title }}</span>
          <span class="column-count">{{ getCandidatesByStatus(column.status).length }}</span>
        </div>
        
        <div class="column-content">
          <div 
            v-for="candidate in getCandidatesByStatus(column.status)" 
            :key="candidate.id" 
            class="candidate-card"
            draggable="true"
            @dragstart="onDragStart($event, candidate)"
          >
            <div class="card-header">
              <span class="name">{{ candidate.name }}</span>
              <n-tag size="small" :bordered="false" type="info">{{ candidate.jobTitle }}</n-tag>
            </div>
            <div class="card-info">
              <div class="info-item"><n-icon :component="SchoolOutline" /> {{ candidate.education }}</div>
              <div class="info-item"><n-icon :component="BriefcaseOutline" /> {{ candidate.experienceYears }}年经验</div>
            </div>
            <div class="card-footer">
               <n-avatar size="small" round :src="undefined" :fallback-src="'https://api.dicebear.com/7.x/avataaars/svg?seed=' + candidate.name" />
               <n-button size="tiny" secondary circle @click="openCandidateDetail(candidate)">
                 <template #icon><n-icon :component="ChevronForwardOutline" /></template>
               </n-button>
            </div>
          </div>
        </div>
      </div>
    
    <!-- 日历视图 -->
    <div v-else class="glass-effect" style="flex: 1; padding: 24px; border-radius: 12px; background: white; overflow-y: auto;">
        <n-calendar
            v-model:value="calendarValue"
            #="{ year, month, date }"
        >
            <div v-for="interview in getInterviewsByDate(year, month, date)" :key="interview.id" class="calendar-event" @click.stop="handleCalendarEventClick(interview)">
                <n-tag size="small" :type="interview.result === 0 ? 'warning' : interview.result === 1 ? 'success' : 'error'" style="margin-bottom: 2px; width: 100%; justify-content: flex-start; cursor: pointer;">
                    {{ formatTime(interview.interviewTime) }} {{ interview.candidateName || '候选人' }}
                </n-tag>
            </div>
        </n-calendar>
    </div>
    </div>

    <!-- 职位发布弹窗 -->
    <n-modal v-model:show="showJobModal" preset="card" title="发布新职位" style="width: 600px">
        <n-form label-placement="left" label-width="100px">
            <n-form-item label="职位名称">
                <n-input v-model:value="jobForm.jobTitle" placeholder="例如：高级Java工程师" />
            </n-form-item>
            <n-form-item label="所属部门">
                <n-select v-model:value="jobForm.deptId" placeholder="选择部门" :options="deptOptions" />
            </n-form-item>
            <n-form-item label="招聘人数">
                <n-input-number v-model:value="jobForm.headCount" :min="1" />
            </n-form-item>
            <n-form-item label="职位描述">
                <n-input v-model:value="jobForm.jobDesc" type="textarea" placeholder="输入JD内容..." rows="5" />
            </n-form-item>
        </n-form>
        <template #footer>
            <n-button type="primary" block @click="handleCreateJob" :loading="submitting">确认发布</n-button>
        </template>
    </n-modal>

    <!-- 候选人详情弹窗 -->
    <n-modal v-model:show="showCandidateModal" preset="card" :title="currentCandidate?.name + ' - 候选人档案'" style="width: 800px">
        <div style="display: flex; gap: 24px; min-height: 400px;">
            <!-- 左侧：基本信息 -->
            <div style="flex: 1; border-right: 1px solid #eee; padding-right: 24px;">
                <n-space vertical size="large">
                    <div style="text-align: center;">
                        <n-avatar :size="80" round :src="undefined" :fallback-src="'https://api.dicebear.com/7.x/avataaars/svg?seed=' + currentCandidate?.name" />
                        <h3 style="margin: 10px 0 5px;">{{ currentCandidate?.name }}</h3>
                        <n-tag type="info">{{ currentCandidate?.jobTitle }}</n-tag>
                    </div>
                    
                    <n-descriptions column="1" label-placement="left" bordered>
                        <n-descriptions-item label="学历">{{ currentCandidate?.education }}</n-descriptions-item>
                        <n-descriptions-item label="经验">{{ currentCandidate?.experienceYears }} 年</n-descriptions-item>
                        <n-descriptions-item label="电话">{{ currentCandidate?.phone }}</n-descriptions-item>
                        <n-descriptions-item label="邮箱">{{ currentCandidate?.email }}</n-descriptions-item>
                    </n-descriptions>

                    <n-button block secondary type="primary">
                        <template #icon><n-icon :component="ChatbubbleEllipsesOutline" /></template>
                        查看简历原文
                    </n-button>
                </n-space>
            </div>

            <!-- 右侧：面试流程 -->
            <div style="flex: 2;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <span style="font-weight: 600; font-size: 16px;">面试记录</span>
                    <n-button size="small" type="primary" dashed @click="openAddInterview">
                        <template #icon><n-icon :component="AddOutline" /></template>
                        添加面试反馈
                    </n-button>
                </div>
                
                <n-scrollbar style="max-height: 400px;">
                    <n-timeline>
                        <n-timeline-item
                            v-for="interview in interviews"
                            :key="interview.id"
                            :type="interview.result === 1 ? 'success' : interview.result === 2 ? 'error' : 'info'"
                            :title="interview.type === 1 ? '初试' : interview.type === 2 ? '复试' : '终试'"
                            :time="new Date(interview.interviewTime).toLocaleString()"
                        >
                            <n-card size="small" embedded>
                                <div style="margin-bottom: 8px;">
                                    <span style="margin-right: 10px;">面试得分: </span>
                                    <n-rate :value="interview.score / 20" readonly allow-half size="small" />
                                    <span style="margin-left: 5px; font-weight: bold;">{{ interview.score }}</span>
                                </div>
                                <div style="color: #666; font-size: 13px;">
                                    {{ interview.comment }}
                                </div>
                            </n-card>
                        </n-timeline-item>
                         <n-timeline-item type="info" title="简历初筛" content="HR 初步筛选通过" />
                    </n-timeline>
                    <n-empty v-if="interviews.length === 0" description="暂无面试记录" />
                </n-scrollbar>
            </div>
        </div>
    </n-modal>

    <!-- 添加面试反馈弹窗 -->
    <n-modal v-model:show="interviewForm.show" preset="card" title="面试反馈录入" style="width: 500px">
        <n-form label-placement="left" label-width="80px">
            <n-form-item label="面试轮次">
                <n-select v-model:value="interviewForm.type" :options="[{label:'初试',value:1},{label:'复试',value:2},{label:'终试',value:3}]" />
            </n-form-item>
             <n-form-item label="面试得分">
                <n-input-number v-model:value="interviewForm.score" :min="0" :max="100" />
            </n-form-item>
            <n-form-item label="面试评价">
                <n-input v-model:value="interviewForm.comment" type="textarea" placeholder="请输入面试评价..." />
            </n-form-item>
            <n-form-item label="面试结论">
                <n-select v-model:value="interviewForm.result" :options="[{label:'通过',value:1},{label:'不通过',value:2},{label:'待定',value:0}]" />
            </n-form-item>
        </n-form>
        <template #footer>
            <n-button type="primary" block @click="handleSubmitInterview">提交反馈</n-button>
        </template>
    </n-modal>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { 
  NButton, NIcon, NSpace, NInput, NTag, NAvatar, NModal, NForm, NFormItem, 
  NSelect, NInputNumber, useMessage, NDescriptions, NDescriptionsItem, NScrollbar,
  NTimeline, NTimelineItem, NCard, NRate, NDivider, NCalendar, NRadioButton, NRadioGroup
} from 'naive-ui'
import { 
  AddOutline, SearchOutline, SchoolOutline, BriefcaseOutline, ChevronForwardOutline,
  CalendarOutline, ChatbubbleEllipsesOutline
} from '@vicons/ionicons5'
import { 
    getJobs, createJob, getCandidates, updateCandidateStatus,
    getInterviews, addInterview, updateInterview 
} from '../api/recruitment'
import { getDeptTree } from '../api/dept'

const message = useMessage()
const showJobModal = ref(false)
const submitting = ref(false)
const deptOptions = ref<any[]>([])

const viewMode = ref('kanban')
const calendarValue = ref(Date.now())
const allInterviews = ref<any[]>([])

const jobForm = reactive({
    jobTitle: '',
    deptId: null,
    headCount: 1,
    jobDesc: ''
})

const showCandidateModal = ref(false)
const currentCandidate = ref<any>(null)
const interviews = ref<any[]>([])
const interviewForm = reactive({
    show: false,
    candidateId: 0,
    interviewerId: 0, // Should be current user or selected
    interviewTime: Date.now(),
    type: 1,
    linkUrl: '',
    score: 80,
    comment: '',
    result: 0
})

// Define interface for Candidate
interface Candidate {
  id: number;
  name: string;
  jobTitle: string;
  education: string;
  experienceYears: number;
  status: number;
  phone?: string;
  email?: string;
  resumeUrl?: string;
}

const candidates = ref<Candidate[]>([])

const kanbanColumns = [
  { id: 1, title: '新简历', status: 1, color: '#3b82f6' },
  { id: 2, title: '初筛通过', status: 2, color: '#8b5cf6' },
  { id: 3, title: '面试中', status: 3, color: '#f59e0b' },
  { id: 4, title: '谈薪 Offer', status: 4, color: '#10b981' },
  { id: 5, title: '已入职', status: 5, color: '#64748b' },
  { id: 9, title: '已淘汰', status: 9, color: '#ef4444' }
]

const loadData = async () => {
    try {
        const res: any = await getCandidates()
        candidates.value = res
        
        // Load depts for job modal
        const depts: any = await getDeptTree()
        deptOptions.value = mapDepts(depts)
        
        // Load all interviews for calendar
        loadAllInterviews()
    } catch(e) {}
}

const loadAllInterviews = async () => {
    try {
        const res: any = await getInterviews()
        allInterviews.value = res
    } catch(e) {}
}

const mapDepts = (depts: any[]): any[] => {
    return depts.map((d: any) => ({
        label: d.deptName,
        value: d.id,
        children: d.children ? mapDepts(d.children) : undefined
    }))
}

const getCandidatesByStatus = (status: number) => {
  return candidates.value.filter((c) => c.status === status)
}

const onDragStart = (event: DragEvent, candidate: Candidate) => {
    if (event.dataTransfer) {
        event.dataTransfer.dropEffect = 'move'
        event.dataTransfer.effectAllowed = 'move'
        event.dataTransfer.setData('candidateId', candidate.id.toString())
    }
}

const onDrop = async (event: DragEvent, status: number) => {
    const candidateId = event.dataTransfer?.getData('candidateId')
    if (candidateId) {
        // Optimistic update
        const candidate = candidates.value.find((c) => c.id == Number(candidateId))
        if (candidate && candidate.status !== status) {
            const oldStatus = candidate.status
            candidate.status = status
            try {
                await updateCandidateStatus(Number(candidateId), status)
                message.success('状态更新成功')
                loadAllInterviews() // Refresh calendar potentially if status affects something
            } catch(e) {
                candidate.status = oldStatus // Revert on failure
                message.error('更新失败')
            }
        }
    }
}

const handleCreateJob = async () => {
    if (!jobForm.jobTitle || !jobForm.deptId) return message.warning('请填写职位名称和部门')
    submitting.value = true
    try {
        await createJob(jobForm)
        message.success('职位发布成功')
        showJobModal.value = false
    } catch(e) {} finally {
        submitting.value = false
    }
}

const openCandidateDetail = async (c: Candidate) => {
    currentCandidate.value = c
    showCandidateModal.value = true
    // Load interviews
    try {
        const res: any = await getInterviews(c.id)
        interviews.value = res
    } catch(e) {
        interviews.value = []
    }
}

const openAddInterview = () => {
    Object.assign(interviewForm, {
        show: true,
        candidateId: currentCandidate.value.id,
        interviewerId: 1, // Mock current user
        interviewTime: Date.now(),
        type: 1,
        linkUrl: '',
        score: 80,
        comment: '',
        result: 0
    })
}

const handleSubmitInterview = async () => {
    try {
        await addInterview({
            candidateId: interviewForm.candidateId,
            interviewerId: interviewForm.interviewerId,
            interviewTime: new Date(interviewForm.interviewTime),
            type: interviewForm.type,
            linkUrl: interviewForm.linkUrl,
            score: interviewForm.score,
            comment: interviewForm.comment,
            result: interviewForm.result
        })
        message.success('面试记录已添加')
        interviewForm.show = false
        openCandidateDetail(currentCandidate.value) // Refresh detail
        loadAllInterviews() // Refresh calendar
    } catch(e) {
        message.error('保存失败')
    }
}

const getInterviewsByDate = (year: number, month: number, date: number) => {
  return allInterviews.value.filter(i => {
    const d = new Date(i.interviewTime)
    return d.getFullYear() === year && (d.getMonth() + 1) === month && d.getDate() === date
  })
}

const formatTime = (ts: number) => {
  const d = new Date(ts)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

const handleCalendarEventClick = (interview: any) => {
    // Find candidate by interview.candidateId if possible, or we need to fetch candidate details
    const candidateArg = candidates.value.find(c => c.id === interview.candidateId)
    // Or we can just use the interview data to define a candidate object locally if we have enough info
    // But we need the full candidate object for openCandidateDetail to work fully (e.g. education)
    
    // Since we fetched all candidates in loadData, finding should work if they are in the list.
    // If we paginated we would have issues. But we are loading all candidates currently.
    
    if (candidateArg) {
        openCandidateDetail(candidateArg)
    } else {
        // Fallback or fetch specific candidate
        // For now just show available info or ignore
        message.info('无法加载详细候选人信息')
    }
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.recruitment-container {
  padding: 24px;
  height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
}
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.title-wrapper h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
}
.subtitle {
    margin: 4px 0 0;
    color: #64748b;
    font-size: 14px;
}
.kanban-board {
    flex: 1;
    display: flex;
    gap: 20px;
    overflow-x: auto;
    padding-bottom: 20px;
}
.kanban-column {
    flex: 0 0 280px;
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    height: 100%;
}
.column-header {
    padding: 16px;
    font-weight: 600;
    border-top: 4px solid #ccc;
    border-radius: 12px 12px 0 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(255, 255, 255, 0.4);
}
.column-count {
    background: rgba(0,0,0,0.05);
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 12px;
}
.column-content {
    flex: 1;
    padding: 12px;
    overflow-y: auto;
}
.candidate-card {
    background: white;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 12px;
    box-shadow: 0 2px 5px rgba(0,0,0,0.02);
    border: 1px solid rgba(0,0,0,0.05);
    cursor: grab;
    transition: transform 0.2s, box-shadow 0.2s;
}
.candidate-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
}
.name {
    font-weight: 600;
    color: #1e293b;
}
.card-info {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: #64748b;
    margin-bottom: 12px;
}
.info-item {
    display: flex;
    align-items: center;
    gap: 4px;
}
.card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.calendar-event {
    margin-bottom: 4px;
}
</style>

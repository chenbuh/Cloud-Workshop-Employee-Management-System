<template>
  <div class="candidate-pool-container">
    <div class="header-section">
      <div class="title-group">
        <h2>全球人才库</h2>
        <p class="subtitle">AI 辅助简历抽取与高端候选人数字化储备中心</p>
      </div>
      <n-space>
        <n-button type="primary" secondary @click="showParseModal = true">
          <template #icon><n-icon :component="ScanOutline" /></template>
          AI 简历解析录入
        </n-button>
        <n-button type="primary" @click="handleCreate">
          <template #icon><n-icon :component="PersonAddOutline" /></template>
          手动录入
        </n-button>
      </n-space>
    </div>

    <!-- 顶部洞察 -->
    <n-grid :cols="4" :x-gap="24" style="margin-bottom: 24px">
        <n-gi v-for="(stat, idx) in poolStats" :key="idx">
            <div class="stat-card glass-effect" :style="{ '--color': stat.color }">
                <div class="icon-box" :style="{ background: stat.color + '20' }">
                    <n-icon :component="stat.icon" :color="stat.color" size="24" />
                </div>
                <div class="stat-info">
                    <div class="label">{{ stat.label }}</div>
                    <div class="value">{{ stat.value }} <small>{{ stat.unit }}</small></div>
                </div>
            </div>
        </n-gi>
    </n-grid>

    <div class="glass-effect table-card">
      <div class="filter-bar">
        <n-input v-model:value="searchName" placeholder="搜索姓名/电话/邮箱..." clearable style="width: 300px">
            <template #prefix><n-icon :component="SearchOutline" /></template>
        </n-input>
        <n-radio-group v-model:value="filterStatus" size="small" @update:value="loadCandidates">
            <n-radio-button :value="0">全部</n-radio-button>
            <n-radio-button :value="1">新简历</n-radio-button>
            <n-radio-button :value="2">初筛通过</n-radio-button>
            <n-radio-button :value="3">面试中</n-radio-button>
        </n-radio-group>
      </div>
      <n-data-table
        :columns="columns"
        :data="filteredCandidates"
        :loading="loading"
        :pagination="pagination"
      />
    </div>

    <!-- AI 解析弹窗 -->
    <n-modal v-model:show="showParseModal" preset="card" title="AI 简历智能录入" style="width: 800px" class="glass-modal">
        <n-grid :cols="2" :x-gap="24">
            <n-gi>
                <div class="parse-left">
                    <p class="hint">请粘贴简历文本内容，AI 将自动提取核心字段</p>
                    <n-input
                        v-model:value="resumeRawText"
                        type="textarea"
                        placeholder="示例：姓名：张三，电话：13800138000，电子邮箱：zhangsan@example.com，本科，5年经验..."
                        :rows="15"
                    />
                    <n-button type="primary" block style="margin-top: 16px" @click="handleAiParse" :loading="parsing">
                        立即开始解析
                    </n-button>
                </div>
            </n-gi>
            <n-gi>
                <div class="parse-right glass-effect">
                    <h4 style="margin-top: 0">提取结果验证</h4>
                    <n-form :model="parsedForm" label-placement="left" label-width="70px" size="small">
                        <n-form-item label="姓名">
                            <n-input v-model:value="parsedForm.name" />
                        </n-form-item>
                        <n-form-item label="手机号">
                            <n-input v-model:value="parsedForm.phone" />
                        </n-form-item>
                        <n-form-item label="邮箱">
                            <n-input v-model:value="parsedForm.email" />
                        </n-form-item>
                        <n-form-item label="学历">
                            <n-select v-model:value="parsedForm.education" :options="eduOptions" />
                        </n-form-item>
                        <n-form-item label="经验(年)">
                            <n-input-number v-model:value="parsedForm.experienceYears" :min="0" style="width: 100%" />
                        </n-form-item>
                        <n-form-item label="投递职位">
                            <n-select v-model:value="parsedForm.applyJobId" :options="jobOptions" />
                        </n-form-item>
                    </n-form>
                </div>
            </n-gi>
        </n-grid>
        <template #footer>
            <n-space justify="end">
                <n-button @click="showParseModal = false">取消</n-button>
                <n-button type="primary" :disabled="!parsedForm.name" @click="saveCandidateAction">确认入库</n-button>
            </n-space>
        </template>
    </n-modal>

    <!-- PDF 预览弹窗 -->
    <n-modal v-model:show="showPreviewModal" preset="card" title="简历在线预览" style="width: 1000px; height: 85vh" class="glass-modal">
        <iframe :src="previewUrl" style="width: 100%; height: 100%; border: none; border-radius: 8px"></iframe>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, h, reactive } from 'vue'
import { 
  NGrid, NGi, NButton, NIcon, NSpace, NTag, NDataTable, NInput, NRadioGroup, 
  NRadioButton, NModal, NForm, NFormItem, NSelect, NInputNumber, NAvatar, useMessage, NUpload
} from 'naive-ui'
import { 
    PersonAddOutline, ScanOutline, SearchOutline, PeopleOutline,
    SchoolOutline, BriefcaseOutline, StarOutline, EyeOutline, CloudUploadOutline
} from '@vicons/ionicons5'
import { getCandidates, parseResume, saveCandidate, getJobs, updateCandidate } from '../api/recruitment'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const parsing = ref(false)
const candidates = ref<any[]>([])
const jobs = ref<any[]>([])
const searchName = ref('')
const filterStatus = ref(0)

const showParseModal = ref(false)
const showPreviewModal = ref(false)
const previewUrl = ref('')
const resumeRawText = ref('')
const parsedForm = reactive({
    name: '',
    phone: '',
    email: '',
    education: '',
    experienceYears: 1,
    applyJobId: null,
    resumeUrl: ''
})

const eduOptions = [
    { label: '大专', value: '大专' },
    { label: '本科', value: '本科' },
    { label: '硕士', value: '硕士' },
    { label: '博士', value: '博士' }
]

const jobOptions = computed(() => jobs.value.map(j => ({ label: j.title, value: j.id })))

const poolStats = computed(() => [
    { label: '储备人才', value: candidates.value.length, unit: '人', icon: PeopleOutline, color: '#6366f1' },
    { label: '高学历(硕博)', value: candidates.value.filter(c => ['硕士','博士'].includes(c.education)).length, unit: '人', icon: SchoolOutline, color: '#10b981' },
    { label: '资深人才(5+)', value: candidates.value.filter(c => c.experienceYears >= 5).length, unit: '人', icon: BriefcaseOutline, color: '#f59e0b' },
    { label: '本月新增', value: candidates.value.filter(c => moment(c.createTime).isSame(moment(), 'month')).length, unit: '人', icon: StarOutline, color: '#ec4899' }
])

const filteredCandidates = computed(() => {
    let res = candidates.value
    if (filterStatus.value !== 0) res = res.filter(c => c.status === filterStatus.value)
    if (searchName.value) {
        const s = searchName.value.toLowerCase()
        res = res.filter(c => 
            c.name?.toLowerCase().includes(s) || 
            c.phone?.includes(s) || 
            c.email?.toLowerCase().includes(s)
        )
    }
    return res
})

const columns = [
    {
        title: '候选人',
        key: 'name',
        render: (row: any) => h('div', { style: 'display: flex; align-items: center; gap: 12px' }, [
            h(NAvatar, { round: true, size: 'small', src: `https://api.dicebear.com/7.x/avataaars/svg?seed=${row.name}` }),
            h('div', [
                h('div', { style: 'font-weight: 700' }, row.name),
                h('div', { style: 'font-size: 11px; color: var(--text-secondary)' }, row.phone)
            ])
        ])
    },
    { title: '学历', key: 'education' },
    { title: '经验', key: 'experienceYears', render: (row: any) => `${row.experienceYears} 年` },
    { title: '求职意向', key: 'jobTitle', render: (row: any) => row.jobTitle || '自荐' },
    {
        title: '状态',
        key: 'status',
        render: (row: any) => {
            const map: any = { 1: { l: '新简历', t: 'info' }, 2: { l: '初筛通过', t: 'success' }, 3: { l: '面试中', t: 'warning' }, 9: { l: '已淘汰', t: 'error' } }
            const item = map[row.status] || { l: '未知', t: 'default' }
            return h(NTag, { type: item.t, size: 'small', round: true }, { default: () => item.l })
        }
    },
    { 
        title: '简历', 
        key: 'resume', 
        render: (row: any) => {
            if (row.resumeUrl) {
                return h(NButton, {
                    size: 'tiny',
                    secondary: true,
                    type: 'primary',
                    onClick: () => handlePreview(row.resumeUrl)
                }, { icon: () => h(NIcon, { component: EyeOutline }), default: () => '预览' })
            }
            return h('span', { style: 'color: #ccc; font-size: 12px' }, '未上传')
        }
    },
    { 
        title: '操作', 
        key: 'actions', 
        render: (row: any) => {
            return h(NUpload, {
                action: '/api/v1/file/upload',
                showFileList: false,
                onFinish: ({ event }: any) => {
                    handleUploadFinish(row, event)
                    return undefined
                }
            }, { 
                default: () => h(NButton, { size: 'tiny', quaternary: true }, { 
                    icon: () => h(NIcon, { component: CloudUploadOutline }),
                    default: () => '上传' 
                }) 
            })
        }
    }
]

const pagination = { pageSize: 12 }

const loadCandidates = async () => {
    loading.value = true
    try {
        const [cRes, jRes]: any = await Promise.all([
            getCandidates(),
            getJobs()
        ])
        candidates.value = cRes
        jobs.value = jRes
    } finally {
        loading.value = false
    }
}

const handleAiParse = async () => {
    if (!resumeRawText.value) return message.warning('请先粘贴简历内容')
    parsing.value = true
    try {
        const res: any = await parseResume(resumeRawText.value)
        Object.assign(parsedForm, res)
        message.success('解析完成，请校验结果')
    } finally {
        parsing.value = false
    }
}

const saveCandidateAction = async () => {
    try {
        await saveCandidate(parsedForm)
        message.success('人才库导入成功')
        showParseModal.value = false
        loadCandidates()
    } catch(e) {}
}

const handleCreate = () => {
    Object.assign(parsedForm, { name: '', phone: '', email: '', education: '', experienceYears: 1, applyJobId: null, resumeUrl: '' })
    showParseModal.value = true
}

const handlePreview = (url: string) => {
    previewUrl.value = url
    showPreviewModal.value = true
}

const handleUploadFinish = async (row: any, event: any) => {
    try {
        const res = JSON.parse(event.target.response)
        if (res.code === 200) {
            const newUrl = res.data.url
            await updateCandidate({ ...row, resumeUrl: newUrl })
            message.success('简历上传成功')
            row.resumeUrl = newUrl // Optimistic update
        } else {
            message.error(res.msg || '上传失败')
        }
    } catch (e) {
        message.error('上传处理异常')
    }
}

onMounted(() => loadCandidates())
</script>

<style scoped>
.candidate-pool-container { padding: 24px; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }
.title-group h2 { margin: 0; font-size: 24px; font-weight: 700; color: var(--text-primary); }
.subtitle { margin: 4px 0 0; color: var(--text-secondary); font-size: 14px; }

.stat-card {
    padding: 20px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
}
.icon-box {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}
.stat-info .label { font-size: 13px; color: var(--text-secondary); }
.stat-info .value { font-size: 20px; font-weight: 800; color: var(--text-primary); }

.table-card { padding: 24px; border-radius: 24px; }
.filter-bar { display: flex; justify-content: space-between; margin-bottom: 24px; }

.parse-left { display: flex; flex-direction: column; height: 100%; }
.parse-left .hint { font-size: 13px; color: var(--text-secondary); margin-bottom: 12px; }
.parse-right { padding: 20px; border-radius: 12px; height: 100%; }
</style>

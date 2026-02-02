<template>
  <div class="performance-container">
    <div class="header-section">
      <h2>绩效管理与人才盘点</h2>
      <n-space>
        <n-select
          v-model:value="selectedCycle"
          :options="cycleOptions"
          placeholder="选择考核周期"
          style="width: 200px"
          @update:value="loadAppraisals"
        />
        <n-button type="primary" @click="showAddCycle = true">
          <template #icon><n-icon :component="AddOutline" /></template>
          开启新周期
        </n-button>
        <n-button type="info" secondary @click="handleAddAppraisal" :disabled="!selectedCycle">
          <template #icon><n-icon :component="CreateOutline" /></template>
          新增评价
        </n-button>
      </n-space>
    </div>

    <n-grid :cols="24" :x-gap="24" :y-gap="24">
      <n-gi :span="14">
        <div class="glass-effect chart-card">
          <div class="card-title">人才九宫格 (9-Box Grid)</div>
          <div ref="nineBoxRef" style="height: 450px;"></div>
        </div>
      </n-gi>
      <n-gi :span="10">
        <div class="glass-effect list-card">
          <div class="card-title">绩效排行榜</div>
          <n-data-table
            :columns="rankingColumns"
            :data="appraisalData"
            :pagination="false"
            :max-height="400"
          />
        </div>
      </n-gi>
    </n-grid>

    <div class="glass-effect detail-card">
      <div class="card-title">评价明细</div>
      <n-data-table
        :columns="detailColumns"
        :data="appraisalData"
        :loading="loading"
      />
    </div>

    <!-- 新增周期模态框 -->
    <n-modal v-model:show="showAddCycle" preset="card" title="开启新绩效周期" style="width: 450px">
      <n-form :model="cycleForm" label-placement="left" label-width="80px">
        <n-form-item label="名称">
          <n-input v-model:value="cycleForm.cycleName" placeholder="如：2024年第一季度" />
        </n-form-item>
        <n-form-item label="起止日期">
           <n-date-picker v-model:value="cycleForm.range" type="daterange" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" block @click="handleAddCycle">启动</n-button>
      </template>
    </n-modal>

    <!-- 评价模态框 -->
    <n-modal v-model:show="showAppraisalModal" preset="card" :title="appraisalForm.id ? '编辑评价' : '新增评价'" style="width: 600px">
      <n-form :model="appraisalForm" label-placement="left" label-width="100px">
        <n-form-item label="员工">
           <n-select 
             v-model:value="appraisalForm.empId" 
             :options="employeeOptions" 
             filterable 
             placeholder="选择员工"
             :disabled="!!appraisalForm.id"
           />
        </n-form-item>
        <n-form-item label="绩效评分">
           <n-input-number v-model:value="appraisalForm.score" :min="0" :max="100" style="width: 100%" />
        </n-form-item>
        <n-form-item label="潜力维度">
           <n-rate v-model:value="appraisalForm.potentialScore" :count="5" />
           <span style="margin-left: 10px; color: #94a3b8; font-size: 12px">(1-5星:低潜->高潜)</span>
        </n-form-item>
        <n-form-item label="总评级">
           <n-select v-model:value="appraisalForm.rating" :options="ratingOptions" />
        </n-form-item>
        <n-form-item label="评价详情">
           <n-input v-model:value="appraisalForm.comment" type="textarea" placeholder="输入具体的绩效反馈..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showAppraisalModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmitAppraisal" :loading="submitting">提交评价</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, h, computed } from 'vue'
import { 
  NSelect, NButton, NIcon, NGrid, NGi, NDataTable, NSpace, NModal, 
  NForm, NFormItem, NInput, NDatePicker, NTag, useMessage,
  NInputNumber, NRate
} from 'naive-ui'
import { AddOutline, CreateOutline, PencilOutline } from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { listCycles, addCycle, listAppraisals, submitAppraisal } from '../api/performance'
import { getEmployeeList } from '../api/employee'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const selectedCycle = ref<number | null>(null)
const cycleOptions = ref([])
const appraisalData = ref([])
const employeeList = ref([])
const nineBoxRef = ref<HTMLElement | null>(null)
let nineBoxChart: echarts.ECharts | null = null

const showAddCycle = ref(false)
const cycleForm = reactive({
    cycleName: '',
    range: [Date.now(), Date.now()] as [number, number]
})

const showAppraisalModal = ref(false)
const appraisalForm = reactive({
    id: null,
    empId: null,
    score: 80,
    potentialScore: 3,
    rating: 'B',
    comment: ''
})

const employeeOptions = computed(() => {
    return employeeList.value.map((e: any) => ({
        label: `${e.fullName} (${e.deptName})`,
        value: e.userId // Assuming userId is linked to empId in this context, or use id from emp table
    }))
})

const ratingOptions = [
    { label: 'S (卓越)', value: 'S' },
    { label: 'A (优秀)', value: 'A' },
    { label: 'B (良好)', value: 'B' },
    { label: 'C (合格)', value: 'C' },
    { label: 'D (需改进)', value: 'D' }
]

const rankingColumns = [
    { title: '姓名', key: 'empName' },
    { title: '得分', key: 'score' },
    { title: '评级', key: 'rating', render: (row: any) => h(NTag, { type: row.rating === 'A' || row.rating === 'S' ? 'success' : 'info' }, { default: () => row.rating }) }
]

const detailColumns = [
    { title: '员工', key: 'empName' },
    { title: '最终得分', key: 'score' },
    { title: '绩效潜力', key: 'potentialScore', render: (row: any) => h(NRate, { value: row.potentialScore, readonly: true, size: 'small' }) },
    { title: '综合评级', key: 'rating', render: (row: any) => h(NTag, { type: row.rating === 'S' ? 'error' : row.rating === 'A' ? 'success' : 'warning', bordered: false }, { default: () => row.rating }) },
    { title: '评价内容', key: 'comment', ellipsis: { tooltip: true } },
    { title: '评价时间', key: 'createTime', render: (row: any) => new Date(row.createTime).toLocaleDateString() },
    { title: '操作', key: 'actions', render: (row: any) => {
        return h(NButton, {
            size: 'small',
            quaternary: true,
            circle: true,
            onClick: () => handleEditAppraisal(row)
        }, { icon: () => h(NIcon, { component: PencilOutline }) })
    }}
]

const loadCycles = async () => {
    const res: any = await listCycles()
    cycleOptions.value = res.map((c: any) => ({ label: c.cycleName, value: c.id }))
    if (res.length > 0 && !selectedCycle.value) {
        selectedCycle.value = res[0].id
        loadAppraisals()
    }
}

const loadEmployees = async () => {
    const res: any = await getEmployeeList()
    employeeList.value = res
}

const loadAppraisals = async () => {
    if (!selectedCycle.value) return
    loading.value = true
    try {
        const res: any = await listAppraisals(selectedCycle.value)
        appraisalData.value = res
        initNineBox()
    } finally {
        loading.value = false
    }
}

const handleAddCycle = async () => {
    try {
        await addCycle({
            cycleName: cycleForm.cycleName,
            startDate: new Date(cycleForm.range[0]),
            endDate: new Date(cycleForm.range[1]),
            status: 2
        })
        message.success('周期已启动')
        showAddCycle.value = false
        loadCycles()
    } catch(e) {}
}

const handleAddAppraisal = () => {
    if (!selectedCycle.value) return message.warning('请先选择考核周期')
    Object.assign(appraisalForm, {
        id: null,
        empId: null,
        score: 80,
        potentialScore: 3,
        rating: 'B',
        comment: ''
    })
    showAppraisalModal.value = true
}

const handleEditAppraisal = (row: any) => {
    Object.assign(appraisalForm, {
        id: row.id,
        empId: row.empId, // Ensure API returns empId
        score: row.score,
        potentialScore: row.potentialScore,
        rating: row.rating,
        comment: row.comment
    })
    showAppraisalModal.value = true
}

const handleSubmitAppraisal = async () => {
    if (!appraisalForm.empId) return message.warning('请选择员工')
    
    submitting.value = true
    try {
        await submitAppraisal({
            ...appraisalForm,
            cycleId: selectedCycle.value
        })
        message.success('评价已提交')
        showAppraisalModal.value = false
        loadAppraisals()
    } catch(e: any) {
        message.error(e.message || '提交失败')
    } finally {
        submitting.value = false
    }
}

const initNineBox = () => {
    if (!nineBoxRef.value) return
    if (!nineBoxChart) {
        nineBoxChart = echarts.init(nineBoxRef.value)
    }

    // Prepare data for 9-box: X = Score (Normalized to 1-3), Y = Potential (Normalized to 1-3)
    // Box 1 (1,1): Low Perform, Low Pot -> "Under Performer"
    // Box 9 (3,3): High Perform, High Pot -> "Star / Future Leader"
    const seriesData = appraisalData.value.map((item: any) => {
        // Map 0-100 score to 1-3
        let perfLevel = 1
        if (item.score >= 85) perfLevel = 3
        else if (item.score >= 70) perfLevel = 2
        
        // Map 1-5 potential to 1-3
        let potLevel = 1
        if (item.potentialScore >= 4) potLevel = 3
        else if (item.potentialScore >= 3) potLevel = 2

        return {
            name: item.empName,
            value: [perfLevel - 0.5 + (Math.random()-0.5)*0.3, potLevel - 0.5 + (Math.random()-0.5)*0.3], // Jitter for visibility
            item: item
        }
    })

    const option = {
        title: { text: '潜力 vs 绩效', left: 'center', bottom: 10 },
        xAxis: {
            name: '绩效 (Performance)',
            min: 0, max: 3,
            interval: 1,
            splitLine: { show: true, lineStyle: { color: '#e2e8f0', width: 2 } },
            axisLabel: { formatter: (v: number) => v === 0.5 ? '低' : v === 1.5 ? '中' : v === 2.5 ? '高' : '' }
        },
        yAxis: {
            name: '潜力 (Potential)',
            min: 0, max: 3,
            interval: 1,
            splitLine: { show: true, lineStyle: { color: '#e2e8f0', width: 2 } },
            axisLabel: { formatter: (v: number) => v === 0.5 ? '低' : v === 1.5 ? '中' : v === 2.5 ? '高' : '' }
        },
        tooltip: {
            formatter: (params: any) => `<b>${params.data.name}</b><br/>评分: ${params.data.item.score}<br/>潜力: ${params.data.item.potentialScore}<br/>评级: ${params.data.item.rating}`
        },
        series: [{
            type: 'scatter',
            symbolSize: 20,
            data: seriesData,
            label: { show: true, position: 'top', formatter: '{b}' },
            itemStyle: { 
                color: (params: any) => {
                    const r = params.data.item.rating
                    return r === 'S' || r === 'A' ? '#10b981' : r === 'B' ? '#6366f1' : '#f59e0b'
                } 
            }
        }],
        graphic: [
            // Example labels for boxes
            { type: 'text', left: '80%', top: '15%', style: { text: '明星人才', fill: '#10b981', font: 'bold 12px sans-serif' } },
            { type: 'text', left: '15%', top: '85%', style: { text: '待调整', fill: '#ef4444', font: 'bold 12px sans-serif' } },
            { type: 'text', left: '15%', top: '15%', style: { text: '潜力股', fill: '#f59e0b', font: 'bold 12px sans-serif' } },
            { type: 'text', left: '80%', top: '85%', style: { text: '熟练工', fill: '#6366f1', font: 'bold 12px sans-serif' } }
        ]
    }

    nineBoxChart.setOption(option)
}

onMounted(() => {
    loadCycles()
    loadEmployees()
    window.addEventListener('resize', () => nineBoxChart?.resize())
})
</script>

<style scoped>
.performance-container {
  padding: 24px;
}
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.chart-card, .list-card, .detail-card {
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  margin-bottom: 24px;
}
.card-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1e293b;
}
</style>

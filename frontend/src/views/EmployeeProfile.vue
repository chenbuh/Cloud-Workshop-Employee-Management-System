<template>
  <div class="profile-detail-page">
    <div class="header-section glass-effect">
      <div class="user-info">
        <n-avatar round :size="80" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${employee.fullName}`" />
        <div class="info-text">
          <div class="name-row">
            <h1>{{ employee.fullName }}</h1>
            <n-tag type="success" ghost round>{{ employee.jobTitle }}</n-tag>
            <n-tag v-if="employee.status === 3" type="error" round>已离职</n-tag>
          </div>
          <div class="meta-row">
             <span>工号: {{ employee.empCode }}</span> | <span>部门: {{ employee.deptName }}</span>
             <span v-if="employee.status === 3" style="margin-left: 10px; color: #ef4444">
               | 离职日期: {{ formatDate(employee.quitDate) }}
             </span>
          </div>
          <div v-if="employee.status === 3 && employee.quitReason" class="quit-reason">
             离职原因: {{ employee.quitReason }}
          </div>
        </div>
      </div>
      <n-space>
        <n-button v-if="employee.resumeUrl" type="info" secondary @click="handleViewResume">
          <template #icon><n-icon :component="DocumentAttachOutline" /></template>
          附件简历
        </n-button>
        <n-button secondary @click="router.back()">返回列表</n-button>
      </n-space>
    </div>

    <n-grid :cols="12" :x-gap="24" :y-gap="24">
      <!-- 技能雷达图 -->
      <n-gi :span="4">
        <div class="glass-effect chart-card">
          <div class="card-title">技能矩阵 (Skill Matrix)</div>
          <div ref="radarChartRef" style="height: 360px"></div>
          <div class="actions">
            <n-button size="small" type="primary" dashed @click="showSkillModal = true">添加技能</n-button>
          </div>
        </div>
      </n-gi>

      <!-- 成长时间轴 -->
      <n-gi :span="4">
        <div class="glass-effect timeline-card">
          <div class="card-title">成长档案 (Growth Timeline)</div>
          <n-timeline style="margin-top: 20px">
            <n-timeline-item
              v-for="item in growthRecords"
              :key="item.id"
              :type="getGrowthType(item.eventType)"
              :title="item.eventTitle"
              :content="item.eventContent"
              :time="formatDate(item.recordDate)"
            />
          </n-timeline>
          <div class="actions">
             <n-button size="small" type="info" dashed @click="showGrowthModal = true">记录里程碑</n-button>
          </div>
        </div>
      </n-gi>

      <!-- 绩效历史 -->
      <n-gi :span="4">
        <div class="glass-effect perf-card">
          <div class="card-title">绩效历程 (Performance)</div>
          <div class="perf-list" v-if="perfRecords.length > 0">
              <div class="perf-item" v-for="item in perfRecords" :key="item.id">
                  <div class="perf-cycle">{{ item.cycleName || '年度考核' }}</div>
                  <div class="perf-score" :class="item.rating">{{ item.rating }} ({{ item.score }})</div>
              </div>
          </div>
          <n-empty v-else description="暂无绩效记录" />
        </div>
      </n-gi>

      <!-- 薪资走势 -->
      <n-gi :span="12">
          <div class="glass-effect salary-card">
              <div class="card-title">薪资发放趋势 (Salary Trend)</div>
              <div ref="salaryChartRef" style="height: 300px"></div>
          </div>
      </n-gi>
    </n-grid>

    <!-- 添加技能弹窗 -->
    <n-modal v-model:show="showSkillModal" preset="card" title="添加新技能" style="width: 400px">
      <n-form :model="skillForm" label-placement="left" label-width="80px">
        <n-form-item label="技能名称">
          <n-input v-model:value="skillForm.skillName" />
        </n-form-item>
        <n-form-item label="分类">
          <n-select v-model:value="skillForm.category" :options="skillCategories" />
        </n-form-item>
        <n-form-item label="熟练度">
          <n-rate v-model:value="skillForm.skillLevel" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" block @click="handleAddSkill">保存</n-button>
      </template>
    </n-modal>

    <!-- 添加记录弹窗 -->
    <n-modal v-model:show="showGrowthModal" preset="card" title="记录里程碑" style="width: 500px">
      <n-form :model="growthForm" label-placement="left" label-width="80px">
        <n-form-item label="标题">
          <n-input v-model:value="growthForm.eventTitle" />
        </n-form-item>
        <n-form-item label="类型">
          <n-select v-model:value="growthForm.eventType" :options="growthTypes" />
        </n-form-item>
        <n-form-item label="日期">
          <n-date-picker v-model:value="growthForm.recordDate" type="date" />
        </n-form-item>
        <n-form-item label="详细说明">
          <n-input v-model:value="growthForm.eventContent" type="textarea" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" block @click="handleAddGrowth">保存</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  NGrid, NGi, NAvatar, NTag, NButton, NTimeline, NTimelineItem,
  NModal, NForm, NFormItem, NInput, NRate, NSelect, NDatePicker, useMessage, 
  NIcon, NSpace
} from 'naive-ui'
import * as echarts from 'echarts'
import { getFullProfile, addSkill, addGrowth } from '../api/employeeProfile'
import { getEmployeeList } from '../api/employee'
import { getEmpPerformance } from '../api/performance'
import { getPayrollHistory } from '../api/payroll'
import moment from 'moment'

import { AddOutline, DocumentAttachOutline } from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const userId = Number(route.params.id)

const employee = ref<any>({})
const skills = ref<any[]>([])
const growthRecords = ref<any[]>([])
const perfRecords = ref<any[]>([])
const payrollRecords = ref<any[]>([])
const radarChartRef = ref<HTMLElement | null>(null)
const salaryChartRef = ref<HTMLElement | null>(null)
let myChart: any = null
let salaryChart: any = null

const loading = ref(false)
const showSkillModal = ref(false)
const skillForm = reactive({
  userId,
  skillName: '',
  category: '技术',
  skillLevel: 3
})

const skillCategories = [
  { label: '技术', value: '技术' },
  { label: '管理', value: '管理' },
  { label: '软技能', value: '软技能' }
]

const showGrowthModal = ref(false)
const growthForm = reactive({
  userId,
  eventTitle: '',
  eventType: 'project',
  recordDate: Date.now(),
  eventContent: ''
})

const growthTypes = [
  { label: '入职', value: 'join' },
  { label: '晋升', value: 'promotion' },
  { label: '奖励', value: 'award' },
  { label: '项目', value: 'project' },
  { label: '培训', value: 'training' }
]

const getGrowthType = (type: string) => {
  const map: any = {
    join: 'info',
    promotion: 'success',
    award: 'warning',
    project: 'primary',
    training: 'info'
  }
  return map[type] || 'info'
}

const formatDate = (d: any) => moment(d).format('YYYY-MM-DD')

const initRadar = () => {
    if (!radarChartRef.value) return
    if (!myChart) myChart = echarts.init(radarChartRef.value)
    
    const indicator = skills.value.map(s => ({ name: s.skillName, max: 5 }))
    const dataValues = skills.value.map(s => s.skillLevel)

    const option = {
        radar: {
            indicator: indicator.length > 0 ? indicator : [
                { name: '技能1', max: 5 }, { name: '技能2', max: 5 }, { name: '技能3', max: 5 }
            ],
            splitArea: {
                areaStyle: {
                    color: ['rgba(99, 102, 241, 0.05)', 'rgba(99, 102, 241, 0.1)']
                }
            }
        },
        series: [{
            type: 'radar',
            data: [{
                value: dataValues,
                name: '能力值',
                areaStyle: {
                    color: 'rgba(99, 102, 241, 0.4)'
                },
                lineStyle: {
                    color: '#6366f1'
                },
                itemStyle: {
                    color: '#6366f1'
                }
            }]
        }]
    }
    myChart.setOption(option)
}

const initSalaryChart = () => {
    if (!salaryChartRef.value) return
    if (!salaryChart) salaryChart = echarts.init(salaryChartRef.value)

    const data = [...payrollRecords.value].reverse() // Show oldest to newest
    const months = data.map(p => p.payrollMonth)
    const amounts = data.map(p => p.actualAmount)

    const option = {
        tooltip: {
            trigger: 'axis',
            formatter: '{b}: ¥{c}'
        },
        grid: { left: '4%', right: '4%', top: '10%', bottom: '5%', containLabel: true },
        xAxis: {
            type: 'category',
            data: months,
            axisLine: { lineStyle: { color: '#94a3b8' } }
        },
        yAxis: {
            type: 'value',
            splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } }
        },
        series: [{
            data: amounts,
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            itemStyle: { color: '#10b981' },
            areaStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(16, 185, 129, 0.5)' },
                    { offset: 1, color: 'rgba(16, 185, 129, 0.0)' }
                ])
            }
        }]
    }
    salaryChart.setOption(option)
}

const loadData = async () => {
    loading.value = true
    try {
        const res: any = await getFullProfile(userId)
        skills.value = res.skills
        growthRecords.value = res.growth
        
        // 获取员工基本信息
        const listRes: any = await getEmployeeList({ pageNum: 1, pageSize: 100 })
        employee.value = listRes.records.find((e: any) => e.id === userId)

        // 获取绩效记录
        const perfRes: any = await getEmpPerformance(userId)
        perfRecords.value = perfRes
        
        // 获取薪资记录
        const payRes: any = await getPayrollHistory(userId)
        payrollRecords.value = payRes

        initRadar()
        setTimeout(initSalaryChart, 100) // Small delay to ensure DOM is ready
    } finally {
        loading.value = false
    }
}

const handleAddSkill = async () => {
    await addSkill(skillForm)
    message.success('技能添加成功')
    showSkillModal.value = false
    loadData()
}

const handleAddGrowth = async () => {
    growthForm.recordDate = moment(growthForm.recordDate).toDate() as any
    await addGrowth(growthForm)
    message.success('历程记录成功')
    showGrowthModal.value = false
    loadData()
}

const handleViewResume = () => {
    if (employee.value.resumeUrl) {
        window.open(employee.value.resumeUrl, '_blank')
    }
}

onMounted(() => {
    loadData()
    window.addEventListener('resize', () => {
        myChart?.resize()
        salaryChart?.resize()
    })
})
</script>

<style scoped>
.profile-detail-page {
  padding: 24px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30px;
  border-radius: 24px;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.7);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name-row h1 {
  margin: 0;
  font-size: 28px;
}

.meta-row {
  color: #64748b;
  margin-top: 8px;
}

.chart-card, .timeline-card, .perf-card, .salary-card {
  padding: 24px;
  border-radius: 24px;
  background: white;
  display: flex;
  flex-direction: column;
}

.chart-card, .timeline-card {
  min-height: 480px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #1e293b;
}

.actions {
  margin-top: auto;
  display: flex;
  justify-content: center;
  padding-top: 20px;
}

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}
.perf-list {
  margin-top: 20px;
}
.perf-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 12px;
  border-left: 4px solid #cbd5e1;
  transition: all 0.3s;
}
.perf-item:hover {
  transform: translateX(4px);
  background: #f1f5f9;
}
.perf-cycle {
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}
.perf-score {
  font-size: 14px;
  font-weight: 700;
}
.perf-score.A { color: #10b981; }
.perf-score.B { color: #6366f1; }
.perf-score.C { color: #f59e0b; }
.perf-score.D { color: #ef4444; }

</style>

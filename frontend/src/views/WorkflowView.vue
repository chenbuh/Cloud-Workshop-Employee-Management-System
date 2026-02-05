<template>
  <div class="workflow-container">
    <div class="header-section">
      <div class="title-group">
        <h2>员工全生命周期管理</h2>
        <p class="subtitle">引导式处理入职办理与离职交接自动化流程</p>
      </div>
      <n-space>
         <n-button secondary type="primary" @click="showTemplateModal = true">
            设计新模板
         </n-button>
         <n-button type="primary" @click="showStartModal = true">
           <template #icon><n-icon :component="FlashOutline" /></template>
           启动新流程
         </n-button>
      </n-space>
    </div>

    <!-- 流程看板 -->
    <div class="instance-grid">
      <div v-for="item in instances" :key="item.id" class="instance-card glass-effect" @click="viewDetail(item)">
        <div class="card-top">
          <n-tag :type="item.templateName.includes('入职') ? 'success' : 'warning'" size="small" round>
            {{ item.templateName }}
          </n-tag>
          <span class="date">{{ formatDate(item.startTime) }}</span>
        </div>
        <div class="employee-info">
          <n-avatar round size="large" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${item.employeeName}`" />
          <div class="meta">
            <div class="name">{{ item.employeeName }}</div>
            <div class="status" :class="{ completed: item.status === 1 }">
              {{ item.status === 1 ? '已办理完成' : '办理进行中' }}
            </div>
          </div>
        </div>
        <div class="progress-section">
          <div class="progress-text">
            <span>当前进度</span>
            <b>{{ item.progress }}%</b>
          </div>
          <n-progress
            type="line"
            :percentage="item.progress"
            :show-indicator="false"
            :color="getProgressColor(item.progress)"
            processing
          />
        </div>
      </div>
    </div>

    <!-- 启动流程弹窗 -->
    <n-modal v-model:show="showStartModal" preset="card" title="启动导向流程" style="width: 500px" class="glass-popover">
      <n-form :model="startForm" label-placement="left" label-width="80px">
        <n-form-item label="流程模板">
          <n-select v-model:value="startForm.templateId" :options="templateOptions" placeholder="选择业务类型" />
        </n-form-item>
        <n-form-item label="关联员工">
          <n-select v-model:value="startForm.employeeId" :options="employeeOptions" placeholder="搜索并选择员工" filterable />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showStartModal = false">取消</n-button>
          <n-button type="primary" @click="handleStart" :loading="starting">立即启动</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 模板设计弹窗 -->
    <n-modal v-model:show="showTemplateModal" preset="card" title="设计新流程模板" style="width: 700px" class="glass-popover">
        <n-form :model="templateForm" label-placement="left" label-width="80px">
            <n-form-item label="模板名称">
                <n-input v-model:value="templateForm.name" placeholder="如：技术部入职SOP" />
            </n-form-item>
             <n-form-item label="流程类型">
                <n-select v-model:value="templateForm.type" :options="[{label: '入职流程', value: 'ONBOARDING'}, {label: '离职交接', value: 'OFFBOARDING'}]" />
            </n-form-item>
            <n-form-item label="描述">
                <n-input type="textarea" v-model:value="templateForm.description" placeholder="流程说明..." />
            </n-form-item>
            
            <div class="steps-designer" style="background: #f8fafc; padding: 16px; border-radius: 8px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 12px; align-items: center">
                    <span style="font-weight: 700;">流程步骤</span> 
                    <n-button size="small" type="primary" dashed @click="addStep">+ 添加步骤</n-button>
                </div>
                <div v-for="(step, index) in templateForm.steps" :key="index" style="display: flex; gap: 8px; margin-bottom: 8px">
                    <n-tag type="info" size="small" style="width: 30px; justify-content: center">{{ index + 1 }}</n-tag>
                    <n-input v-model:value="step.title" placeholder="步骤名称，如：领取电脑" />
                    <n-select v-model:value="step.assigneeRole" :options="[{label: 'HR', value: 'HR'}, {label: 'IT管理员', value: 'IT'}, {label: '行政', value: 'ADMIN'}]" style="width: 140px" />
                    <n-button type="error" ghost circle size="small" @click="removeStep(index)">
                        <template #icon><n-icon :component="CheckmarkCircleOutline" style="transform: rotate(45deg)" /></template>
                    </n-button>
                </div>
                <n-empty v-if="templateForm.steps.length === 0" description="请添加至少一个步骤" />
            </div>
        </n-form>
        <template #footer>
            <n-space justify="end">
                <n-button @click="showTemplateModal = false">取消</n-button>
                <n-button type="primary" @click="handleCreateTemplate" :loading="creatingTemplate">保存模板</n-button>
            </n-space>
        </template>
    </n-modal>

    <!-- 流程详情抽屉 -->
    <n-drawer v-model:show="showDetail" :width="600" placement="right" class="workflow-drawer">
      <n-drawer-content :title="`流程追踪: ${selectedInstance?.employeeName}`" closable>
        <div class="drawer-body">
          <div class="status-banner" :class="selectedInstance?.templateName.includes('入职') ? 'on' : 'off'">
            <div class="info">
              <h3>{{ selectedInstance?.templateName }}</h3>
              <p>启动时间: {{ formatDate(selectedInstance?.startTime) }}</p>
            </div>
            <div class="percent">{{ selectedInstance?.progress }}%</div>
          </div>

          <div class="timeline-container">
            <n-timeline>
              <n-timeline-item
                v-for="task in tasks"
                :key="task.id"
                :type="task.status === 1 ? 'success' : 'warning'"
                :title="task.title"
                :content="task.assigneeRole + ' 负责'"
                :time="task.completeTime ? formatTime(task.completeTime) : '待办理'"
              >
                <template #footer v-if="task.status === 0">
                   <div class="task-action glass-effect">
                      <n-input v-model:value="task.tempRemark" placeholder="办理备注 (可选)..." size="small" style="margin-bottom: 8px" />
                      <n-button size="tiny" type="primary" @click="handleComplete(task)">完成此步骤</n-button>
                   </div>
                </template>
                <template #footer v-else>
                   <div class="task-done-info">
                      <n-icon :component="CheckmarkCircleOutline" color="#10b981" />
                      已由管理员处理完成 <span v-if="task.remark">备注: {{ task.remark }}</span>
                   </div>
                </template>
              </n-timeline-item>
            </n-timeline>
          </div>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { 
  NButton, NIcon, NTag, NAvatar, NProgress, NModal, NForm, NFormItem, 
  NSelect, NSpace, NDrawer, NDrawerContent, NTimeline, NTimelineItem,
  NInput, useMessage, NEmpty 
} from 'naive-ui'
import { FlashOutline, CheckmarkCircleOutline } from '@vicons/ionicons5'
import { getFlowTemplates, getFlowInstances, startFlow, getFlowTasks, completeFlowTask, addFlowTemplate } from '../api/flow'

import { getEmployeeList } from '../api/employee'
import moment from 'moment'

const message = useMessage()
const instances = ref<any[]>([])
const templates = ref<any[]>([])
const employees = ref<any[]>([])
const tasks = ref<any[]>([])

const showStartModal = ref(false)
const showDetail = ref(false)
const showTemplateModal = ref(false)
const starting = ref(false)
const creatingTemplate = ref(false)

const selectedInstance = ref<any>(null)

const startForm = reactive({
  templateId: null as number | null,
  employeeId: null as number | null
})

const templateForm = reactive({
    name: '',
    type: 'ONBOARDING',
    description: '',
    steps: [] as any[]
})

const addStep = () => {
    templateForm.steps.push({ title: '', assigneeRole: 'HR', sortOrder: templateForm.steps.length + 1 })
}

const removeStep = (index: number) => {
    templateForm.steps.splice(index, 1)
}

const handleCreateTemplate = async () => {
    if (!templateForm.name || templateForm.steps.length === 0) return message.warning('请补全模板信息')
    creatingTemplate.value = true
    try {
        await addFlowTemplate(templateForm)
        message.success('模板创建成功')
        showTemplateModal.value = false
        loadData()
    } catch(e: any) {
        message.error(e.message || '创建失败')
    } finally {
        creatingTemplate.value = false
    }
}

const templateOptions = computed(() => templates.value.map(t => ({ label: t.name, value: t.id })))
const employeeOptions = computed(() => employees.value.map(e => ({ label: `${e.full_name} (${e.dept_name})`, value: e.id })))

const loadData = async () => {
    const [tRes, iRes, eRes]: any = await Promise.all([
        getFlowTemplates(),
        getFlowInstances(),
        getEmployeeList({ pageNum: 1, pageSize: 1000 })
    ])
    templates.value = tRes
    instances.value = iRes
    employees.value = eRes.records
}

const formatDate = (d: any) => d ? moment(d).format('YYYY-MM-DD') : '-'
const formatTime = (d: any) => d ? moment(d).format('MM-DD HH:mm') : '-'

const getProgressColor = (p: number) => {
    if (p === 100) return '#10b981'
    if (p > 50) return '#6366f1'
    return '#f59e0b'
}

const handleStart = async () => {
    if (!startForm.templateId || !startForm.employeeId) return message.warning('请选择模板和员工')
    starting.value = true
    try {
        await startFlow(startForm as { templateId: number, employeeId: number })
        message.success('流程已启动')
        showStartModal.value = false
        loadData()
    } finally {
        starting.value = false
    }
}

const viewDetail = async (item: any) => {
    selectedInstance.value = item
    tasks.value = await getFlowTasks(item.id) as any
    showDetail.value = true
}

const handleComplete = async (task: any) => {
    try {
        await completeFlowTask({ taskId: task.id, remark: task.tempRemark || '' })
        message.success('步骤已确认')
        // Refresh detail
        tasks.value = await getFlowTasks(selectedInstance.value.id) as any
        // Refresh list
        const newList: any = await getFlowInstances()
        instances.value = newList
        // Update selected instance progress
        const me = newList.find((i: any) => i.id === selectedInstance.value.id)
        if (me) selectedInstance.value.progress = me.progress
    } catch(e) {}
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.workflow-container { padding: 24px; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; }
.title-group h2 { margin: 0; font-size: 24px; font-weight: 700; color: var(--text-primary); }
.subtitle { margin: 4px 0 0; color: var(--text-secondary); font-size: 14px; }

.instance-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
}

.instance-card {
    padding: 24px;
    border-radius: 24px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.instance-card:hover {
    transform: translateY(-5px);
    border-color: var(--primary-color);
}

.card-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.card-top .date { font-size: 12px; color: var(--text-secondary); }

.employee-info {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
}
.employee-info .name { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.employee-info .status { font-size: 12px; color: #f59e0b; }
.employee-info .status.completed { color: #10b981; }

.progress-section .progress-text {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 8px;
    color: var(--text-secondary);
}

.on { background: linear-gradient(to right, #6366f1, #818cf8); }
.off { background: linear-gradient(to right, #f43f5e, #fb7185); }

.status-banner {
    padding: 24px;
    border-radius: 16px;
    color: white;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
}
.status-banner h3 { margin: 0; font-size: 20px; }
.status-banner p { margin: 4px 0 0; opacity: 0.8; font-size: 12px; }
.status-banner .percent { font-size: 32px; font-weight: 800; }

.timeline-container { padding: 8px; }

.task-action {
    margin-top: 12px;
    padding: 12px;
    border-radius: 12px;
}

.task-done-info {
    font-size: 12px;
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
}
</style>

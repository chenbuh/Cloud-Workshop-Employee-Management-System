<template>
  <div class="dept-page employee-page">
    <div class="page-header">
      <h2>组织架构图谱</h2>
      <n-button type="primary" @click="handleAddRoot" v-if="treeData.length === 0">
        <template #icon><n-icon :component="AddOutline" /></template>
        初始化根节点
      </n-button>
    </div>

    <!-- 可视化部门树 (思维导图模式) -->
    <div class="glass-effect mind-map-container">
      <div class="mind-map-canvas" v-if="treeData.length > 0">
        <!-- 递归渲染入口 -->
        <DeptMindNode 
          v-for="root in treeData" 
          :key="root.id" 
          :data="root" 
          is-root
          @add="handleAddChild"
          @edit="handleEdit"
          @delete="handleDelete"
          @view-emp="handleViewEmp"
        />
      </div>
      
      <div v-else class="empty-state">
         <n-empty description="暂无组织架构数据">
           <template #extra>
             <n-button size="small" @click="handleAddRoot">创建根节点</n-button>
           </template>
         </n-empty>
      </div>
    </div>

    <!-- 部门编辑弹窗 (保持不变) -->
    <n-modal v-model:show="showModal" :title="modalType === 1 ? '新增部门' : '编辑部门'" preset="card" style="width: 500px">
      <n-form :model="formModel" label-placement="left" label-width="80px">
        <n-form-item label="上级部门">
           <n-input v-if="formModel.parentId === 0" value="顶级部门" disabled />
           <n-tree-select
             v-else
             v-model:value="formModel.parentId"
             :options="treeData"
             label-field="deptName"
             key-field="id"
             placeholder="请选择上级部门"
           />
        </n-form-item>
        <n-form-item label="部门名称" path="deptName">
          <n-input v-model:value="formModel.deptName" placeholder="例如：技术部" />
        </n-form-item>
        <n-form-item label="显示排序" path="orderNum">
          <n-input-number v-model:value="formModel.orderNum" />
        </n-form-item>
        <n-form-item label="负责人" path="leader">
          <n-input v-model:value="formModel.leader" placeholder="选填" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="dialog-footer">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确认提交</n-button>
        </div>
      </template>
    </n-modal>

    <!-- 部门员工侧滑列表 -->
    <n-drawer v-model:show="showEmpDrawer" :width="600" class="glass-drawer">
      <n-drawer-content closable>
        <template #header>
          <div class="drawer-header">
             <n-icon :component="PeopleOutline" size="24" color="#6366f1" style="margin-right: 8px" />
             <span class="drawer-title" v-if="currentDept">【{{ currentDept.deptName }}】 成员名单</span>
          </div>
        </template>
        
        <n-data-table
          :columns="empColumns"
          :data="empList"
          :loading="empLoading"
          class="glass-table"
        />
        
        <div v-if="empList.length === 0 && !empLoading" style="padding: 40px; text-align: center; color: #94a3b8">
          该部门暂无在职员工
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { 
  NButton, NIcon, useMessage, useDialog, NEmpty, NForm, NFormItem, 
  NInput, NInputNumber, NTreeSelect, NModal, NDrawer, NDrawerContent,
  NDataTable, NTag
} from 'naive-ui'
import { AddOutline, PeopleOutline } from '@vicons/ionicons5'
import { getDeptTree, addDept, updateDept, delDept } from '../api/dept'
import { getEmployeeList } from '../api/employee'
import DeptMindNode from '../components/DeptMindNode.vue'

const message = useMessage()
const dialog = useDialog()

const treeData = ref<any[]>([])
const showModal = ref(false)
const modalType = ref(1) // 1 Add, 2 Edit

// 员工列表相关状态
const showEmpDrawer = ref(false)
const empLoading = ref(false)
const empList = ref([])
const currentDept = ref<any>(null)

const empColumns = [
  { title: '工号', key: 'empCode', width: 120 },
  { title: '姓名', key: 'fullName', width: 100, render(row: any) {
      return h('b', { style: 'color: #3f51b5' }, row.fullName)
  }},
  { title: '职位', key: 'jobTitle', width: 150 },
  { title: '状态', key: 'status', width: 80, render(row: any) {
      const map: any = { 1: ['success', '正式'], 2: ['info', '试用'], 3: ['error', '离职'] }
      const conf = map[row.status] || ['default', '未知']
      return h(NTag, { type: conf[0], round: true, size: 'small' }, { default: () => conf[1] })
  }}
]

const formModel = reactive({
  id: undefined,
  parentId: 0,
  deptName: '',
  orderNum: 0,
  leader: ''
})

const loadTree = async () => {
    try {
      const res: any = await getDeptTree()
      treeData.value = res
    } catch (e) { }
}

const handleViewEmp = async (dept: any) => {
  currentDept.value = dept
  showEmpDrawer.value = true
  empLoading.value = true
  try {
    const res: any = await getEmployeeList({ deptId: dept.id, pageSize: 100 })
    empList.value = res.records
  } finally {
    empLoading.value = false
  }
}

const handleAddRoot = () => {
  modalType.value = 1
  resetForm()
  formModel.parentId = 0
  showModal.value = true
}

const handleAddChild = (row: any) => {
  modalType.value = 1
  resetForm()
  formModel.parentId = row.id
  showModal.value = true
}

const handleEdit = (row: any) => {
  modalType.value = 2
  Object.assign(formModel, row)
  showModal.value = true
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '警告',
    content: `确定要删除部门“${row.deptName}”吗？`,
    positiveText: '确定删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      await delDept(row.id)
      message.success('删除成功')
      loadTree()
    }
  })
}

const handleSubmit = async () => {
  if (!formModel.deptName) {
    message.warning('请输入部门名称')
    return
  }
  
  if (modalType.value === 1) {
    await addDept(formModel)
    message.success('新增成功')
  } else {
    await updateDept(formModel)
    message.success('更新成功')
  }
  showModal.value = false
  loadTree()
}

const resetForm = () => {
  formModel.id = undefined
  formModel.deptName = ''
  formModel.orderNum = 0
  formModel.leader = ''
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.mind-map-container {
  padding: 40px;
  border-radius: 20px;
  min-height: 600px;
  overflow: auto; /* 允许无限拖动查看 */
  
  /* 网格点阵背景 */
  background-color: #f8fafc;
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 20px 20px;
  
  position: relative;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.05);
}

.mind-map-canvas {
  display: inline-block; /* 允许内容撑开容器宽度 */
  min-width: 100%;
  padding: 20px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.drawer-header {
  display: flex;
  align-items: center;
}

.drawer-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.glass-drawer :deep(.n-drawer-content) {
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(20px);
}
</style>

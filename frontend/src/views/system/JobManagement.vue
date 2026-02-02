<template>
  <div class="job-container employee-page">
    <div class="page-header">
      <h2>岗位与职级体系</h2>
      <p class="subtitle">定义企业组织架构中的岗位职责与职级薪资标准</p>
    </div>

    <div class="glass-effect content-card">
      <n-tabs type="line" animated>
        <!-- 岗位管理 -->
        <n-tab-pane name="posts" tab="岗位管理">
          <div class="table-actions">
            <n-space>
              <n-button type="primary" @click="handleAddPost">
                <template #icon><n-icon><AddOutline /></n-icon></template>
                新增岗位
              </n-button>
              <n-input v-model:value="queryParams.postName" placeholder="岗位名称" style="width: 200px" clearable @keyup.enter="handleQuery" />
              <n-button @click="handleQuery">查询</n-button>
            </n-space>
          </div>
          <n-data-table
            :columns="postColumns"
            :data="postList"
            :loading="loading"
            :pagination="pagination"
            @update:page="handlePageChange"
          />
        </n-tab-pane>

        <!-- 职级管理 -->
        <n-tab-pane name="levels" tab="职级管理">
           <div class="table-actions">
            <n-space>
              <n-button type="primary" @click="handleAddLevel">
                <template #icon><n-icon><AddOutline /></n-icon></template>
                新增职级
              </n-button>
              <n-input v-model:value="levelQueryParams.levelName" placeholder="职级名称" style="width: 200px" clearable @keyup.enter="handleLevelQuery" />
              <n-button @click="handleLevelQuery">查询</n-button>
            </n-space>
          </div>
          <n-data-table
            :columns="levelColumns"
            :data="levelList"
            :loading="levelLoading"
            :pagination="levelPagination"
            @update:page="handleLevelPageChange"
          />
        </n-tab-pane>
      </n-tabs>
    </div>

    <!-- 岗位弹窗 -->
    <n-modal v-model:show="showPostModal" preset="card" :title="postTitle" style="width: 500px">
      <n-form :model="postForm" label-placement="left" label-width="80px">
        <n-form-item label="岗位名称" path="postName">
          <n-input v-model:value="postForm.postName" placeholder="请输入岗位名称" />
        </n-form-item>
        <n-form-item label="岗位编码" path="postCode">
          <n-input v-model:value="postForm.postCode" placeholder="请输入岗位编码" />
        </n-form-item>
        <n-form-item label="显示顺序" path="postSort">
          <n-input-number v-model:value="postForm.postSort" :min="0" />
        </n-form-item>
        <n-form-item label="状态">
          <n-radio-group v-model:value="postForm.status">
            <n-radio value="0">正常</n-radio>
            <n-radio value="1">停用</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="postForm.remark" type="textarea" placeholder="请输入备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPostModal = false">取消</n-button>
          <n-button type="primary" @click="submitPostForm">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 职级弹窗 -->
    <n-modal v-model:show="showLevelModal" preset="card" :title="levelTitle" style="width: 500px">
      <n-form :model="levelForm" label-placement="left" label-width="80px">
        <n-form-item label="职级名称" path="levelName">
          <n-input v-model:value="levelForm.levelName" placeholder="请输入职级名称" />
        </n-form-item>
        <n-form-item label="职级编码" path="levelCode">
          <n-input v-model:value="levelForm.levelCode" placeholder="请输入职级编码" />
        </n-form-item>
        <n-form-item label="薪资范围">
          <n-input-group>
            <n-input-number v-model:value="levelForm.minSalary" :min="0" placeholder="最低" style="width: 45%" />
            <n-input-group-label>~</n-input-group-label>
            <n-input-number v-model:value="levelForm.maxSalary" :min="0" placeholder="最高" style="width: 45%" />
          </n-input-group>
        </n-form-item>
        <n-form-item label="显示顺序" path="levelSort">
          <n-input-number v-model:value="levelForm.levelSort" :min="0" />
        </n-form-item>
        <n-form-item label="状态">
          <n-radio-group v-model:value="levelForm.status">
            <n-radio value="0">正常</n-radio>
            <n-radio value="1">停用</n-radio>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showLevelModal = false">取消</n-button>
          <n-button type="primary" @click="submitLevelForm">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { 
  NTabs, NTabPane, NDataTable, NButton, NSpace, NIcon, NInput, NModal, 
  NForm, NFormItem, NInputNumber, NRadioGroup, NRadio, NInputGroup, NInputGroupLabel,
  useMessage, useDialog
} from 'naive-ui'
import { AddOutline, PencilOutline, TrashOutline } from '@vicons/ionicons5'
import { 
  listPost, addPost, updatePost, delPost, getPost,
  listJobLevel, addJobLevel, updateJobLevel, delJobLevel, getJobLevel
} from '../../api/job'

const message = useMessage()
const dialog = useDialog()

// 岗位管理
const loading = ref(false)
const postList = ref([])
const showPostModal = ref(false)
const postTitle = ref('')
const queryParams = reactive({
  postName: '',
  pageNum: 1,
  pageSize: 10
})
const postForm = reactive({
  postId: null,
  postCode: '',
  postName: '',
  postSort: 0,
  status: '0',
  remark: ''
})

const handleQuery = () => {
  queryParams.pageNum = 1
  getPostList()
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  getPostList()
}

const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onChange: (page: number) => {
    pagination.page = page
    handlePageChange(page)
  }
})

const postColumns = [
  { title: '岗位名称', key: 'postName' },
  { title: '岗位编码', key: 'postCode' },
  { title: '排序', key: 'postSort' },
  { 
    title: '状态', 
    key: 'status',
    render(row: any) {
      return h(
        'span',
        { style: { color: row.status === '0' ? '#10b981' : '#ef4444' } },
        row.status === '0' ? '正常' : '停用'
      )
    }
  },
  { title: '创建时间', key: 'createTime' },
  {
    title: '操作',
    key: 'actions',
    render(row: any) {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => handleEditPost(row) }, { icon: () => h(NIcon, null, { default: () => h(PencilOutline) }) }),
          h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDeletePost(row) }, { icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) })
        ]
      })
    }
  }
]

const getPostList = async () => {
  loading.value = true
  try {
    const res: any = await listPost(queryParams)
    postList.value = res.records
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

const handleAddPost = () => {
  postTitle.value = '新增岗位'
  Object.assign(postForm, { postId: null, postCode: '', postName: '', postSort: 0, status: '0', remark: '' })
  showPostModal.value = true
}

const handleEditPost = (row: any) => {
  postTitle.value = '修改岗位'
  Object.assign(postForm, row)
  showPostModal.value = true
}

const handleDeletePost = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除岗位「${row.postName}」吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
        await delPost(row.postId)
        message.success('删除成功')
        getPostList()
    }
  })
}

const submitPostForm = async () => {
  if (postForm.postId) {
    await updatePost(postForm)
    message.success('更新成功')
  } else {
    await addPost(postForm)
    message.success('新增成功')
  }
  showPostModal.value = false
  getPostList()
}

// 职级管理
const levelLoading = ref(false)
const levelList = ref([])
const showLevelModal = ref(false)
const levelTitle = ref('')
const levelQueryParams = reactive({
  levelName: '',
  pageNum: 1,
  pageSize: 10
})
const levelForm = reactive({
  levelId: null,
  levelCode: '',
  levelName: '',
  levelSort: 0,
  status: '0',
  minSalary: 0,
  maxSalary: 0,
  remark: ''
})

const handleLevelQuery = () => {
    levelQueryParams.pageNum = 1
    getLevelList()
}

const handleLevelPageChange = (page: number) => {
    levelQueryParams.pageNum = page
    getLevelList()
}

const levelPagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  onChange: (page: number) => {
    levelPagination.page = page
    handleLevelPageChange(page)
  }
})

const levelColumns = [
  { title: '职级名称', key: 'levelName' },
  { title: '职级编码', key: 'levelCode' },
  { 
    title: '薪资范畴', 
    key: 'salary',
    render(row: any) {
      return `${row.minSalary} ~ ${row.maxSalary}`
    }
  },
  { title: '排序', key: 'levelSort' },
  { 
    title: '状态', 
    key: 'status',
    render(row: any) {
      return h(
        'span',
        { style: { color: row.status === '0' ? '#10b981' : '#ef4444' } },
        row.status === '0' ? '正常' : '停用'
      )
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row: any) {
        return h(NSpace, {}, {
        default: () => [
          h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => handleEditLevel(row) }, { icon: () => h(NIcon, null, { default: () => h(PencilOutline) }) }),
          h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDeleteLevel(row) }, { icon: () => h(NIcon, null, { default: () => h(TrashOutline) }) })
        ]
      })
    }
  }
]

const getLevelList = async () => {
  levelLoading.value = true
  try {
    const res: any = await listJobLevel(levelQueryParams)
    levelList.value = res.records
    levelPagination.itemCount = res.total
  } finally {
    levelLoading.value = false
  }
}

const handleAddLevel = () => {
    levelTitle.value = '新增职级'
    Object.assign(levelForm, { levelId: null, levelCode: '', levelName: '', levelSort: 0, status: '0', minSalary: 0, maxSalary: 0, remark: '' })
    showLevelModal.value = true
}

const handleEditLevel = (row: any) => {
    levelTitle.value = '修改职级'
    Object.assign(levelForm, row)
    showLevelModal.value = true
}

const handleDeleteLevel = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除职级「${row.levelName}」吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
        await delJobLevel(row.levelId)
        message.success('删除成功')
        getLevelList()
    }
  })
}

const submitLevelForm = async () => {
    if (levelForm.levelId) {
        await updateJobLevel(levelForm)
        message.success('更新成功')
    } else {
        await addJobLevel(levelForm)
        message.success('新增成功')
    }
    showLevelModal.value = false
    getLevelList()
}

onMounted(() => {
  getPostList()
  getLevelList()
})
</script>

<style scoped>
.job-container {
  padding: 24px;
}
.content-card {
  padding: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
}
.table-actions {
  margin-bottom: 20px;
}
.subtitle {
  color: #64748b;
  font-size: 14px;
  margin-top: -8px;
  margin-bottom: 24px;
}
</style>

<template>
  <div class="announcement-page">
    <div class="header-action glass-effect">
      <div class="title">企业文化动态管理</div>
      <n-button type="primary" @click="handleCreate">
         <template #icon><n-icon :component="Add" /></template>
         发布新动态
      </n-button>
    </div>

    <div class="content-table glass-effect">
      <n-data-table 
        :columns="columns" 
        :data="dataList" 
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        scroll-x="1000"
      />
    </div>

    <n-modal v-model:show="showModal" preset="card" :title="formValue.id ? '编辑动态' : '发布新动态'" style="width: 700px">
      <n-form :model="formValue" label-placement="left" label-width="80px" ref="formRef">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="formValue.title" placeholder="请输入标题" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="formValue.type" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="正文内容">
          <n-tabs type="segment" animated>
            <n-tab-pane name="edit" tab="编辑">
              <n-input 
                v-model:value="formValue.content" 
                type="textarea" 
                placeholder="支持 Markdown 语法" 
                :rows="12" 
              />
            </n-tab-pane>
            <n-tab-pane name="preview" tab="预览">
              <div class="markdown-body announcement-preview" v-html="renderMarkdown(formValue.content)"></div>
            </n-tab-pane>
          </n-tabs>
        </n-form-item>
        <n-form-item label="状态" path="isPublished">
          <n-switch v-model:value="formValue.isPublished" :checked-value="1" :unchecked-value="0">
            <template #checked>立即发布</template>
            <template #unchecked>存草稿</template>
          </n-switch>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h, reactive } from 'vue'
import { 
  NButton, NDataTable, NIcon, NModal, NForm, NFormItem, NInput, NSelect, 
  NTag, NSwitch, NSpace, useMessage, useDialog, NTabs, NTabPane
} from 'naive-ui'
import { Add, CreateOutline, TrashOutline, MegaphoneOutline } from '@vicons/ionicons5'
import { getAnnouncements, addAnnouncement, updateAnnouncement, deleteAnnouncement, publishAnnouncement } from '../../api/announcement'
import moment from 'moment'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()
const renderMarkdown = (content: string) => md.render(content || '')

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const showModal = ref(false)
const dataList = ref([])
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page: number) => {
    pagination.page = page
    loadData()
  }
})

const formValue = ref({
  id: undefined,
  title: '',
  content: '',
  type: 'news',
  isPublished: 1
})

const typeOptions = [
  { label: '通知公告', value: 'notice' },
  { label: '企业新闻', value: 'news' },
  { label: '活动预告', value: 'event' }
]

const columns = [
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  { 
    title: '类型', 
    key: 'type',
    width: 100,
    render(row: any) {
      const map: any = { notice: 'warning', news: 'success', event: 'info' }
      const labelMap: any = { notice: '公告', news: '新闻', event: '活动' }
      return h(NTag, { type: map[row.type], size: 'small' }, { default: () => labelMap[row.type] || row.type })
    }
  },
  {
    title: '状态',
    key: 'isPublished',
    width: 100,
    render(row: any) {
      return h(NTag, { 
        type: row.isPublished ? 'success' : 'default',
        bordered: false
      }, { default: () => row.isPublished ? '已发布' : '草稿' })
    }
  },
  { 
    title: '发布时间', 
    key: 'publishTime',
    width: 180,
    render(row: any) {
      return row.publishTime ? moment(row.publishTime).format('YYYY-MM-DD HH:mm') : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render(row: any) {
      return h(NSpace, null, {
        default: () => [
          h(NButton, {
            size: 'small',
            type: 'primary',
            ghost: true,
            onClick: () => handleEdit(row)
          }, { icon: () => h(NIcon, { component: CreateOutline }), default: () => '编辑' }),
          h(NButton, {
            size: 'small',
            type: row.isPublished ? 'warning' : 'success',
            ghost: true,
            onClick: () => handleTogglePublish(row)
          }, { default: () => row.isPublished ? '撤回' : '发布' }),
          h(NButton, {
            size: 'small',
            type: 'error',
            ghost: true,
            onClick: () => handleDelete(row)
          }, { icon: () => h(NIcon, { component: TrashOutline }) })
        ]
      })
    }
  }
]

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getAnnouncements({
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    })
    dataList.value = res.records
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  formValue.value = {
    id: undefined,
    title: '',
    content: '',
    type: 'news',
    isPublished: 1
  }
  showModal.value = true
}

const handleEdit = (row: any) => {
  formValue.value = { ...row }
  showModal.value = true
}

const handleSubmit = async () => {
  try {
    if (formValue.value.id) {
      await updateAnnouncement(formValue.value)
      message.success('更新成功')
    } else {
      await addAnnouncement(formValue.value)
      message.success('发布成功')
    }
    showModal.value = false
    loadData()
  } catch (e) {
    message.error('操作失败')
  }
}

const handleTogglePublish = async (row: any) => {
  const newStatus = row.isPublished ? 0 : 1
  try {
    await publishAnnouncement(row.id, newStatus)
    message.success(newStatus ? '发布成功' : '已撤回')
    loadData()
  } catch (e) {
    message.error('操作失败')
  }
}

const handleDelete = (row: any) => {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这条动态吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteAnnouncement(row.id)
      message.success('删除成功')
      loadData()
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.announcement-page {
  padding: 24px;
}

.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 24px;
  margin-bottom: 24px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.content-table {
  padding: 24px;
  background: white;
  border-radius: 24px;
}

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.announcement-preview {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
}

.markdown-body :deep(h1), .markdown-body :deep(h2) {
    border-bottom: 1px solid #e2e8f0;
    padding-bottom: 8px;
    margin-bottom: 16px;
}
</style>

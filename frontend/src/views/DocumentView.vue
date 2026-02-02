<template>
  <div class="document-center">
    <n-layout has-sider class="doc-layout">
      <!-- 侧边分类栏 -->
      <n-layout-sider
        bordered
        collapse-mode="width"
        :collapsed-width="0"
        :width="260"
        show-trigger="arrow-circle"
        class="category-sider glass-effect"
      >
        <div class="sider-header">
           <n-button type="primary" dashed block @click="showAddCategory = true">
             <template #icon><n-icon :component="AddOutline" /></template>
             添加分类
           </n-button>
        </div>
        <n-menu
          v-model:value="activeCategory"
          :options="categoryOptions"
          @update:value="handleCategoryChange"
          class="category-menu"
        />
      </n-layout-sider>

      <!-- 主文档区域 -->
      <n-layout-content class="doc-content">
        <div class="content-header">
          <div class="search-bar">
             <n-input v-model:value="keyword" placeholder="搜寻知识库..." clearable @keyup.enter="handleSearch">
               <template #prefix><n-icon :component="SearchOutline" /></template>
             </n-input>
          </div>
          <n-button type="primary" @click="handleCreateDoc">
            <template #icon><n-icon :component="CreateOutline" /></template>
             撰写文档
          </n-button>
          <n-upload
            action="/api/v1/file/upload"
            :headers="uploadHeaders"
            :show-file-list="false"
            @finish="handleQuickUpload"
            style="margin-left: 12px"
          >
            <n-button type="info" secondary>
              <template #icon><n-icon :component="CloudUploadOutline" /></template>
              上传文件
            </n-button>
          </n-upload>
        </div>

        <div v-if="loading" class="loading-state">
           <n-spin size="large" />
        </div>

        <div v-else-if="documents.length > 0" class="doc-grid">
           <div 
             v-for="doc in documents" 
             :key="doc.id" 
             class="doc-card glass-effect"
             @click="viewDocDetail(doc)"
           >
             <div class="doc-icon">
               <n-icon :component="doc.docType === 'file' ? DocumentAttachOutline : DocumentTextOutline" />
             </div>
             <div class="doc-info">
               <div class="doc-title">
                 <n-tag v-if="doc.isPinned" size="small" type="error" round class="pin-tag">置顶</n-tag>
                 {{ doc.title }}
               </div>
               <div class="doc-summary">{{ doc.summary || '暂无摘要' }}</div>
               <div class="doc-tags" v-if="doc.tags">
                 <n-tag v-for="tag in doc.tags.split(',')" :key="tag" size="tiny" quaternary round>{{ tag }}</n-tag>
               </div>
               <div class="doc-meta">
                 <span><n-icon :component="EyeOutline" /> {{ doc.viewCount }}</span>
                 <span>{{ formatDate(doc.createTime) }}</span>
               </div>
             </div>
           </div>
        </div>

        <n-empty v-else description="该分类下暂无文档" class="empty-state" />
        
        <div v-if="pagination.itemCount > pagination.pageSize" class="pagination-container">
           <n-pagination
             v-model:page="pagination.page"
             :item-count="pagination.itemCount"
             :page-size="pagination.pageSize"
             @update:page="handlePageChange"
           />
        </div>
      </n-layout-content>
    </n-layout>

    <!-- 文档阅读/编辑模态框 -->
    <n-modal v-model:show="showDocModal" preset="card" :title="isEditing ? '撰写文档' : '文档阅读'" style="width: 900px">
       <div v-if="isEditing" class="doc-edit-form">
          <n-form :model="docForm" label-placement="left" label-width="80px">
             <n-form-item label="标题">
                <n-input v-model:value="docForm.title" placeholder="输入文档标题" />
             </n-form-item>
             <n-form-item label="分类">
                <n-select v-model:value="docForm.categoryId" :options="categorySelectOptions" />
             </n-form-item>
             <n-form-item label="摘要">
                <n-input v-model:value="docForm.summary" type="textarea" placeholder="简要描述文档内容" />
             </n-form-item>
             <n-form-item label="标签">
                <n-input v-model:value="docForm.tags" placeholder="多个标签用逗号分隔" />
             </n-form-item>
             <n-form-item label="置顶">
                <n-switch v-model:value="docForm.isPinned" :checked-value="1" :unchecked-value="0" />
             </n-form-item>
             <n-form-item label="附件链接">
                <n-input-group>
                  <n-input v-model:value="docForm.fileUrl" placeholder="上传后自动填入或手动输入" />
                  <n-upload
                    action="/api/v1/file/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    @finish="handleUploadFinish"
                  >
                    <n-button>上传</n-button>
                  </n-upload>
                </n-input-group>
             </n-form-item>
             <n-form-item label="正文 (MD)" class="md-editor-item">
                <div class="md-toolbar">
                  <n-upload
                    action="/api/v1/file/upload"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    @finish="handleContentImageUpload"
                    style="display: inline-block"
                  >
                    <n-button size="small" secondary type="primary">
                      <template #icon><n-icon :component="ImageOutline" /></template>
                      插入图片
                    </n-button>
                  </n-upload>
                  <span class="toolbar-tip">支持 Markdown 语法</span>
                </div>
                <n-input 
                  ref="contentInputRef"
                  v-model:value="docForm.content" 
                  type="textarea" 
                  :rows="15" 
                  placeholder="在此输入文档内容..." 
                  class="md-textarea"
                />
             </n-form-item>
          </n-form>
          <n-divider title-placement="left">预览</n-divider>
          <div class="markdown-body preview-box" v-html="renderMarkdown(docForm.content)"></div>
          <div class="form-actions">
             <n-button type="primary" block @click="handleSaveDoc" :loading="submitting">发布文档</n-button>
          </div>
       </div>
       <div v-else class="doc-reader">
          <h1 class="reader-title">{{ currentDoc.title }}</h1>
          <div class="reader-meta">
             发布于: {{ formatDate(currentDoc.createTime) }} | 最后更新: {{ formatDate(currentDoc.updateTime) }} | 阅读量: {{ currentDoc.viewCount }}
             <n-tag v-if="currentDoc.isPinned" size="small" type="error" style="margin-left: 10px">已置顶</n-tag>
          </div>
          <div class="reader-tags" v-if="currentDoc.tags">
            <n-tag v-for="tag in currentDoc.tags.split(',')" :key="tag" size="small" ghost round style="margin-right: 6px">{{ tag }}</n-tag>
          </div>
          <n-divider />
          <div class="markdown-body reader-content" v-html="renderMarkdown(currentDoc.content)"></div>
          <div class="reader-footer">
             <n-space>
               <n-button v-if="currentDoc.fileUrl" type="info" secondary @click="handleOpenUrl(currentDoc.fileUrl)">查看附件</n-button>
               <n-button quaternary type="warning" size="small" @click="handleEditDoc">编辑文档</n-button>
             </n-space>
          </div>
       </div>
    </n-modal>

    <!-- 添加分类模态框 -->
    <n-modal v-model:show="showAddCategory" preset="card" title="新建文档分类" style="width: 400px">
       <n-form :model="categoryForm" label-placement="left" label-width="80px">
          <n-form-item label="分类名称">
             <n-input v-model:value="categoryForm.name" placeholder="例如：行政管理、技术规范" />
          </n-form-item>
          <n-form-item label="排序值">
             <n-input-number v-model:value="categoryForm.orderNum" :min="0" />
          </n-form-item>
       </n-form>
       <template #footer>
          <n-button type="primary" block @click="handleAddCategory">确定创建</n-button>
       </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, h } from 'vue'
import { useRoute } from 'vue-router'
import { 
  NLayout, NLayoutSider, NLayoutContent, NMenu, NButton, NIcon, NInput, 
  NEmpty, NPagination, NModal, NForm, NFormItem, NSelect, NDivider, 
  NInputNumber, NSpin, useMessage
} from 'naive-ui'

import { 
  AddOutline, SearchOutline, CreateOutline, DocumentTextOutline, 
  DocumentAttachOutline, EyeOutline, ListOutline, FolderOutline, ImageOutline, CloudUploadOutline
} from '@vicons/ionicons5'
import { 
  listCategories, addCategory, listDocuments, getDocument, saveDocument 
} from '../api/document'
import moment from 'moment'
import MarkdownIt from 'markdown-it'

const route = useRoute()
const md = new MarkdownIt()
const uploadHeaders = {
    Authorization: `Bearer ${localStorage.getItem('token')}`
}

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const activeCategory = ref<number>(0)
const categoryOptions = ref<any[]>([])
const categorySelectOptions = ref<any[]>([])
const documents = ref<any[]>([])
const keyword = ref('')
const contentInputRef = ref<any>(null)

const showAddCategory = ref(false)
const categoryForm = reactive({
    name: '',
    orderNum: 0
})

const showDocModal = ref(false)
const isEditing = ref(false)
const currentDoc = ref<any>({})
const docForm = reactive({
    id: null,
    title: '',
    categoryId: null as number | null,
    summary: '',
    content: '',
    docType: 'article',
    isPinned: 0,
    tags: '',
    fileUrl: ''
})

const pagination = reactive({
    page: 1,
    pageSize: 8,
    itemCount: 0
})

const loadCategoryList = async () => {
    const res: any = await listCategories()
    categorySelectOptions.value = res.map((c: any) => ({ label: c.name, value: c.id }))
    categoryOptions.value = [
        { label: '全部文档', key: 0, icon: () => h(NIcon, { component: ListOutline }) },
        ...res.map((c: any) => ({
            label: c.name,
            key: c.id,
            icon: () => h(NIcon, { component: FolderOutline })
        }))
    ]
}

const loadDocList = async () => {
    loading.value = true
    try {
        const res: any = await listDocuments({
            pageNum: pagination.page,
            pageSize: pagination.pageSize,
            categoryId: activeCategory.value,
            keyword: keyword.value
        })
        documents.value = res.records
        pagination.itemCount = res.total
    } finally {
        loading.value = false
    }
}

const handleCategoryChange = (val: number) => {
    activeCategory.value = val
    pagination.page = 1
    loadDocList()
}

const handleSearch = () => {
    pagination.page = 1
    loadDocList()
}

const handlePageChange = (page: number) => {
    pagination.page = page
    loadDocList()
}

const handleAddCategory = async () => {
    await addCategory(categoryForm)
    message.success('分类创建成功')
    showAddCategory.value = false
    loadCategoryList()
}

const handleCreateDoc = () => {
    Object.assign(docForm, {
        id: null,
        title: '',
        categoryId: activeCategory.value !== 0 ? activeCategory.value : null,
        summary: '',
        content: '',
        docType: 'article'
    })
    isEditing.value = true
    showDocModal.value = true
}

const viewDocDetail = async (doc: any) => {
    const res: any = await getDocument(doc.id)
    currentDoc.value = res
    isEditing.value = false
    showDocModal.value = true
    // Update local view count
    doc.viewCount++
}

const handleEditDoc = () => {
    Object.assign(docForm, currentDoc.value)
    isEditing.value = true
}

const handleSaveDoc = async () => {
    submitting.value = true
    try {
        await saveDocument(docForm)
        message.success('文档已发布')
        showDocModal.value = false
        loadDocList()
    } finally {
        submitting.value = false
    }
}

const renderMarkdown = (text: string) => {
    if (!text) return ''
    return md.render(text)
}

const handleUploadFinish = ({ event }: any) => {
    const res = JSON.parse((event.target as any).response)
    if (res.code === 200) {
        docForm.fileUrl = res.data.url
        message.success('附件上传成功')
    }
}

const handleContentImageUpload = ({ event }: any) => {
    const res = JSON.parse((event.target as any).response)
    if (res.code === 200) {
        const url = res.data.url
        const imageMarkdown = `\n![图片描述](${url})\n`
        
        // Simple append to end for now as getting cursor pos in NInput is tricky without direct DOM access in setup
        // But we can try validation.
        docForm.content += imageMarkdown
        
        message.success('图片插入成功')
    }
}

const handleQuickUpload = async ({ event, file }: any) => {
    const res = JSON.parse((event.target as any).response)
    if (res.code === 200) {
        const doc = {
            id: null,
            title: file.name,
            categoryId: activeCategory.value !== 0 ? activeCategory.value : null,
            summary: 'Uploaded file',
            content: '',
            docType: 'file',
            isPinned: 0,
            tags: '',
            fileUrl: res.data.url
        }
        
        try {
            await saveDocument(doc)
            message.success('文件上传成功')
            loadDocList()
        } catch (e: any) {
            message.error(e.message || '保存文档失败')
        }
    }
}

const handleOpenUrl = (url: string) => {
    window.open(url, '_blank')
}

const formatDate = (d: any) => d ? moment(d).format('YYYY-MM-DD HH:mm') : '-'

onMounted(async () => {
    await loadCategoryList()
    await loadDocList()

    // Check for document ID in query to open reader automatically
    if (route.query.id) {
        viewDocDetail({ id: Number(route.query.id) })
    }
})
</script>

<style scoped>
.document-center {
  height: calc(100vh - 100px);
  margin: -24px;
}
.doc-layout {
    height: 100%;
}
.category-sider {
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
}
.sider-header {
    padding: 20px;
}
.doc-content {
    background: #f8fafc;
    padding: 24px;
    display: flex;
    flex-direction: column;
}
.content-header {
    margin-bottom: 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.search-bar {
    width: 400px;
}
.doc-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    flex: 1;
}
.doc-card {
    padding: 20px;
    border-radius: 16px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    gap: 16px;
    background: white;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.doc-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
    border-color: #6366f1;
}
.doc-icon {
    font-size: 32px;
    color: #6366f1;
    display: flex;
    align-items: center;
}
.doc-info {
    flex: 1;
}
.doc-title {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 4px;
}
.doc-summary {
    font-size: 13px;
    color: #64748b;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.5;
}
.doc-meta {
    font-size: 12px;
    color: #94a3b8;
    display: flex;
    justify-content: space-between;
}
.loading-state, .empty-state {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
}
.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: center;
}

.reader-title {
    margin: 0;
    font-size: 28px;
    color: #1e293b;
}
.reader-meta {
    font-size: 14px;
    color: #64748b;
    margin-top: 8px;
}
.reader-content {
    color: #334155;
    min-height: 300px;
}
.reader-footer {
    margin-top: 40px;
    text-align: right;
}
.form-actions {
    margin-top: 20px;
}

/* Markdown Styling */
.markdown-body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
    font-size: 16px;
    line-height: 1.6;
    word-wrap: break-word;
}
.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) {
    margin-top: 24px;
    margin-bottom: 16px;
    font-weight: 600;
    line-height: 1.25;
}
.markdown-body :deep(code) {
    padding: 0.2em 0.4em;
    margin: 0;
    font-size: 85%;
    background-color: rgba(175, 184, 193, 0.2);
    border-radius: 6px;
}
.markdown-body :deep(pre) {
    padding: 16px;
    overflow: auto;
    font-size: 85%;
    line-height: 1.45;
    background-color: #f6f8fa;
    border-radius: 6px;
}
.markdown-body :deep(img) {
    max-width: 100%;
}
.preview-box {
    padding: 16px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #fdfdfd;
    max-height: 400px;
    overflow-y: auto;
}
.doc-tags {
    margin-bottom: 12px;
    display: flex;
    gap: 8px;
}
.pin-tag {
    margin-right: 8px;
}
.reader-tags {
    margin-top: 12px;
}
.md-editor-item :deep(.n-form-item-blank) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.md-toolbar {
  margin-bottom: 8px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.toolbar-tip {
  font-size: 12px;
  color: #94a3b8;
}
</style>

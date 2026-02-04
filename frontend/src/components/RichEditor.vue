<template>
  <div style="border: 1px solid #ccc; border-radius: 8px; overflow: hidden;" :class="{ 'dark-editor': appStore.theme === 'dark' }">
    <Toolbar
      style="border-bottom: 1px solid #ccc"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      :mode="mode"
    />
    <Editor
      style="height: 400px; overflow-y: hidden;"
      v-model="valueHtml"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="handleCreated"
    />
  </div>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { useAppStore } from '../store/app'

const appStore = useAppStore()
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const mode = 'default'
// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

// 内容 HTML
const valueHtml = ref(props.modelValue)

// 模拟 ajax 异步获取内容
watch(() => props.modelValue, (newVal) => {
  if (newVal !== valueHtml.value) {
    valueHtml.value = newVal
  }
})

watch(valueHtml, (newVal) => {
  emit('update:modelValue', newVal)
})

const toolbarConfig = {}
const editorConfig = { 
    placeholder: '请输入内容...',
    MENU_CONF: {
        uploadImage: {
            // 这里可以配置图片上传逻辑，目前先留空
        }
    }
}

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor: any) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}
</script>

<style scoped>
.dark-editor {
    border-color: #334155 !important;
}
.dark-editor :deep(.w-e-toolbar) {
    background-color: #1e293b !important;
    border-bottom-color: #334155 !important;
    color: #f1f5f9 !important;
}
.dark-editor :deep(.w-e-text-container) {
    background-color: #0f172a !important;
    color: #f1f5f9 !important;
}
.dark-editor :deep(.w-e-bar-item button) {
    color: #cbd5e1 !important;
}
.dark-editor :deep(.w-e-bar-item button:hover) {
    background-color: #334155 !important;
}
.dark-editor :deep(.w-e-select-list) {
    background-color: #1e293b !important;
    color: #f1f5f9 !important;
}
</style>

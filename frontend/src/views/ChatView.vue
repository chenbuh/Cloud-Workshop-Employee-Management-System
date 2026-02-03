<template>
  <div class="chat-page-container">
    <div class="chat-card glass-effect">
      <!-- Left Sidebar: Conversations & Search -->
      <div class="sidebar">
        <div class="sidebar-header">
           <h2 class="title">消息中心</h2>
           <n-button quaternary circle size="medium" @click="loadUsers">
             <template #icon><n-icon><RefreshOutline /></n-icon></template>
           </n-button>
        </div>
        
        <div class="search-section">
          <n-input
            v-model:value="userSearch"
            placeholder="搜索同事姓名或工号..."
            round
            clearable
          >
            <template #prefix><n-icon><SearchOutline /></n-icon></template>
          </n-input>
        </div>

        <n-scrollbar class="user-list-scrollbar">
          <div class="user-list">
            <!-- Public Channel -->
            <div 
              class="conv-item public" 
              :class="{ active: selectedUser === null }"
              @click="handleSelectUser(null)"
            >
              <div class="avatar-wrap">
                <n-avatar round size="large" class="public-avatar">#</n-avatar>
              </div>
              <div class="conv-info">
                <div class="header">
                  <span class="name">公共频道 (Broadcast)</span>
                  <span class="time">{{ getLastMsgTime(null) }}</span>
                </div>
                <div class="preview">{{ getLastMsgPreview(null) || '发布全员消息...' }}</div>
              </div>
            </div>

            <!-- User List -->
            <div 
              v-for="user in filteredUsers" 
              :key="user.id" 
              class="conv-item"
              :class="{ active: selectedUser?.id === user.id }"
              @click="handleSelectUser(user)"
            >
              <div class="avatar-wrap">
                <n-badge :value="chatStore.unreadMap.get(user.id) || 0" :max="99">
                  <n-avatar 
                    round 
                    size="large" 
                    :src="user.avatar" 
                    :fallback-src="'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (user.nickName || user.fullName)" 
                  />
                </n-badge>
                <div class="status-dot" :class="{ online: isOnline(user.id) }"></div>
              </div>
              <div class="conv-info">
                <div class="header">
                  <span class="name">{{ user.nickName || user.fullName }}</span>
                  <span class="time">{{ user.empCode }}</span>
                </div>
                <div class="preview">{{ user.jobTitle || '在线' }}</div>
              </div>
            </div>
          </div>
        </n-scrollbar>
      </div>

      <!-- Main Chat Area -->
      <div class="chat-main">
        <template v-if="selectedUser !== undefined">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="user-meta">
              <n-avatar 
                v-if="selectedUser"
                round 
                size="medium" 
                :src="selectedUser.avatar" 
                :fallback-src="'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (selectedUser.nickName || selectedUser.fullName)" 
              />
              <n-avatar v-else round size="medium" class="public-avatar">#</n-avatar>
              <div class="text">
                <h3>{{ selectedUser ? (selectedUser.nickName || selectedUser.fullName) : '公共频道' }}</h3>
                <p v-if="selectedUser">{{ selectedUser.jobTitle || '在线' }}</p>
                <p v-else>全员可见的消息广场</p>
              </div>
            </div>
            <div class="actions">
              <n-button quaternary circle><n-icon size="20"><CallOutline /></n-icon></n-button>
              <n-button quaternary circle><n-icon size="20"><VideocamOutline /></n-icon></n-button>
              <n-button quaternary circle><n-icon size="20"><EllipsisHorizontalOutline /></n-icon></n-button>
            </div>
          </div>

          <!-- Messages List -->
          <div class="message-area">
            <n-scrollbar ref="scrollbarRef" trigger="none">
              <div class="msg-list-padding">
                <div v-if="loading" class="chat-loading">
                  <n-spin size="small" />
                </div>
                <div v-else v-for="(msg, index) in currentHistory" :key="msg.id || index" class="msg-wrapper" :class="{ 'self': msg.fromUserId === currentUserId }">
                  <n-avatar 
                    v-if="msg.fromUserId !== currentUserId"
                    round 
                    size="small" 
                    :src="getAvatarByMsg(msg)"
                    :fallback-src="'https://api.dicebear.com/7.x/avataaars/svg?seed=' + getNickByMsg(msg)" 
                    class="msg-avatar"
                  />
                  <div class="msg-content-box">
                    <div v-if="shouldShowNick(msg, index)" class="msg-nick">{{ getNickByMsg(msg) }}</div>
                    <div class="bubble">
                      {{ msg.content }}
                    </div>
                    <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
                  </div>
                </div>
              </div>
            </n-scrollbar>
          </div>

          <!-- Input Area -->
          <div class="input-section">
            <div class="toolbar">
              <n-button quaternary circle size="small"><template #icon><n-icon><HappyOutline /></n-icon></template></n-button>
              <n-button quaternary circle size="small"><template #icon><n-icon><ImageOutline /></n-icon></template></n-button>
              <n-button quaternary circle size="small"><template #icon><n-icon><AttachOutline /></n-icon></template></n-button>
              <n-button quaternary circle size="small"><template #icon><n-icon><DocumentTextOutline /></n-icon></template></n-button>
            </div>
            <div class="input-wrap">
              <n-input
                v-model:value="inputValue"
                type="textarea"
                placeholder="键入消息... (Crtl/Cmd + Enter 发送)"
                :autosize="{ minRows: 2, maxRows: 6 }"
                @keypress.enter="handleKeyPress"
                class="main-input"
              />
              <div class="send-btn-wrap">
                <n-button type="primary" :disabled="!inputValue.trim()" @click="sendMessage">
                  发送
                  <template #icon><n-icon><SendOutline /></n-icon></template>
                </n-button>
              </div>
            </div>
          </div>
        </template>
        
        <div v-else class="welcome-screen">
          <div class="content">
             <div class="art-icon">
                <n-icon size="120" color="#6366f1"><ChatbubblesOutline /></n-icon>
             </div>
             <h2>云工坊桌面即时通讯</h2>
             <p>开启内部高效沟通，让协作无感发生</p>
             <n-button type="primary" secondary round @click="focusSearch" style="margin-top: 32px; padding: 0 32px">
                <template #prefix><n-icon><SearchOutline /></n-icon></template>
                查找正在共事的伙伴
             </n-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, onUnmounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { 
  SearchOutline, RefreshOutline, ChatbubblesOutline,
  SendOutline, HappyOutline, ImageOutline, AttachOutline,
  DocumentTextOutline, CallOutline, VideocamOutline,
  EllipsisHorizontalOutline 
} from '@vicons/ionicons5'
import { useMessage, NScrollbar } from 'naive-ui'
import { getChatHistory, getChatUsers, markAsRead } from '../api/chat'
import { useChatStore } from '../store/chat'
import { useUserStore } from '../store/user'
import moment from 'moment'

const message = useMessage()
const route = useRoute()
const chatStore = useChatStore()
const userStore = useUserStore()
const userSearch = ref('')
const users = ref<any[]>([])
const selectedUser = ref<any>(undefined)
const inputValue = ref('')
const loading = ref(false)
const currentHistory = ref<any[]>([])
const scrollbarRef = ref<any>(null)

// Auth
const currentUserId = computed(() => userStore.userInfo?.id)

const filteredUsers = computed(() => {
  const search = userSearch.value.toLowerCase()
  return users.value.filter(u => 
    u.id !== currentUserId.value && 
    (
      (u.nickName && u.nickName.toLowerCase().includes(search)) || 
      (u.fullName && u.fullName.toLowerCase().includes(search)) || 
      (u.empCode && u.empCode.toLowerCase().includes(search))
    )
  )
})

const loadUsers = async () => {
  try {
    const res: any = await getChatUsers()
    users.value = res
    
    if (route.query.userId) {
        const u = users.value.find(user => user.id === Number(route.query.userId))
        if (u) handleSelectUser(u)
    }
  } catch (e) {}
}

const handleSelectUser = async (user: any) => {
  selectedUser.value = user
  const toId = user ? user.id : null
  
  if (toId) chatStore.unreadMap.delete(toId)
  if (!user) chatStore.unreadMap.delete('public')
  
  if (toId) markAsRead(toId)
  
  loading.value = true
  try {
    const res: any = await getChatHistory(toId)
    currentHistory.value = res
    scrollToBottom()
  } catch (e) {} finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      scrollbarRef.value.scrollTo({ position: 'bottom' })
    }
  })
}

const handleKeyPress = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) {
    sendMessage()
  }
}

const sendMessage = () => {
  if (!inputValue.value.trim()) return
  
  const toId = selectedUser.value ? selectedUser.value.id : null
  chatStore.sendMessage(toId, inputValue.value)
  
  // Optimistic update
  currentHistory.value.push({
    fromUserId: currentUserId.value,
    toUserId: toId,
    content: inputValue.value,
    createTime: new Date(),
    type: 'text'
  })
  inputValue.value = ''
  scrollToBottom()
}

// Watch for incoming messages from store
watch(() => chatStore.messages, (newMsgs) => {
    if (newMsgs.length > 0) {
        const msg = newMsgs[newMsgs.length - 1]
        const isPublic = msg.toUserId === null
        const fromId = msg.fromUserId
        
        const isActive = (isPublic && selectedUser.value === null) || 
                       (!isPublic && selectedUser.value?.id === fromId)
        
        if (isActive && fromId !== currentUserId.value) {
            currentHistory.value.push(msg)
            scrollToBottom()
            if (!isPublic) markAsRead(fromId)
        }
    }
}, { deep: true })

const formatTime = (t: any) => t ? moment(t).format('HH:mm') : ''
const isOnline = (id: number) => chatStore.onlineUsers.has(id)

const getLastMsgTime = (id: number | null) => {
    const key = id === null ? 'public' : id
    const msg = chatStore.lastMessages.get(key)
    return msg ? formatTime(msg.createTime) : ''
}

const getLastMsgPreview = (id: number | null) => {
    const key = id === null ? 'public' : id
    const msg = chatStore.lastMessages.get(key)
    return msg ? msg.content : ''
}

const getNickByMsg = (msg: any) => msg.fromUserNick || '同事'
const getAvatarByMsg = (msg: any) => msg.fromUserAvatar
const shouldShowNick = (msg: any, index: number) => {
    if (msg.fromUserId === currentUserId.value) return false
    if (index === 0) return true
    return currentHistory.value[index - 1].fromUserId !== msg.fromUserId
}

const focusSearch = () => {
    // Logic to focus input if needed
}

onMounted(() => {
  loadUsers()
  // 只有在用户ID有效时才初始化 WebSocket
  if (currentUserId.value && typeof currentUserId.value === 'number') {
    chatStore.initWebSocket(currentUserId.value)
  } else {
    console.warn('Cannot initialize WebSocket: Invalid user ID')
  }
})

watch(() => route.query.userId, (newId) => {
    if (newId && users.value.length > 0) {
        const u = users.value.find(user => user.id === Number(newId))
        if (u) handleSelectUser(u)
    }
})

</script>

<style scoped>
.chat-page-container {
  height: calc(100vh - 120px);
  display: flex;
  overflow: hidden;
}

.chat-card {
  width: 100%;
  height: 100%;
  display: flex;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
  border: 1px solid #f1f5f9;
}

.sidebar {
  width: 300px;
  border-right: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  background: #fcfcfd;
}

.sidebar-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header .title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.search-section {
  padding: 0 16px 16px;
}

.user-list-scrollbar {
  flex: 1;
}

.user-list {
  padding: 8px;
}

.conv-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 2px;
}

.conv-item:hover {
  background: #f1f5f9;
}

.conv-item.active {
  background: #e2e8f0;
}

.avatar-wrap {
  position: relative;
}

.status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border: 2px solid white;
  border-radius: 50%;
  background: #cbd5e1;
}

.status-dot.online {
  background: #10b981;
}

.public-avatar {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%) !important;
  color: white;
  font-weight: 900;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-info .header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 2px;
}

.conv-info .name {
  font-weight: 600;
  color: #334155;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-info .time {
  font-size: 11px;
  color: #94a3b8;
}

.conv-info .preview {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-header {
  padding: 12px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-bottom: 1px solid #f1f5f9;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-meta .text h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
}

.user-meta .text p {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

.message-area {
  flex: 1;
  background: #f8fafc;
  overflow: hidden;
}

.msg-list-padding {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.msg-wrapper {
  display: flex;
  gap: 10px;
  max-width: 85%;
}

.msg-wrapper.self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-content-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.msg-nick {
  font-size: 11px;
  color: #64748b;
  margin-left: 4px;
}

.self .msg-nick {
  display: none;
}

.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  position: relative;
  word-break: break-all;
}

.msg-wrapper:not(.self) .bubble {
  background: white;
  color: #1e293b;
  border-top-left-radius: 2px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.msg-wrapper.self .bubble {
  background: #6366f1;
  color: white;
  border-top-right-radius: 2px;
}

.msg-time {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 2px;
}

.self .msg-time {
  text-align: right;
}

.input-section {
  background: white;
  padding: 12px 24px 20px;
  border-top: 1px solid #f1f5f9;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.toolbar :deep(.n-button) {
    color: #64748b;
}

.input-wrap {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.main-input :deep(.n-input-wrapper) {
    padding: 0;
}

.main-input :deep(.n-input__border), 
.main-input :deep(.n-input__state-border) {
    border: none !important;
}

.send-btn-wrap {
    display: flex;
    justify-content: flex-end;
}

.welcome-screen {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: white;
}

.welcome-screen .content {
  text-align: center;
  max-width: 400px;
}

.art-icon {
  margin-bottom: 24px;
  opacity: 0.8;
}

.welcome-screen h2 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1e293b;
}

.welcome-screen p {
  color: #64748b;
  line-height: 1.6;
}

.chat-loading {
    display: flex;
    justify-content: center;
    padding: 20px;
}
</style>

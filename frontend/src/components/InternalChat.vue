<template>
  <!-- Global Chat Background Handler (Invisible) -->
</template>

<script setup lang="ts">
import { onMounted, h } from 'vue'
import { useNotification, NAvatar } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useChatStore } from '../store/chat'

const notification = useNotification()
const router = useRouter()
const chatStore = useChatStore()

// Auth
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = userInfo.id

const handleGlobalNotification = (msg: any) => {
    // Only show if not on chat page
    if (router.currentRoute.value.path === '/chat') return
    
    const sender = msg.fromUserNick || '同事'
    const n = notification.info({
        title: `新消息: ${sender}`,
        content: () => h('div', { 
            style: 'cursor: pointer; padding: 4px 0;',
            onClick: () => {
                router.push({ path: '/chat', query: { userId: msg.fromUserId } })
                n.destroy()
            }
        }, [
            h('div', { style: 'font-size: 14px; margin-bottom: 8px;' }, msg.content),
            h('div', { style: 'font-size: 12px; color: #94a3b8;' }, '点击立即回复')
        ]),
        avatar: () => h(NAvatar, {
            size: 'small',
            round: true,
            src: msg.fromUserAvatar,
            fallbackSrc: `https://api.dicebear.com/7.x/avataaars/svg?seed=${sender}`
        }),
        duration: 5000,
        keepAliveOnHover: true
    })
}

onMounted(() => {
    // 确保 currentUserId 存在且为有效数字
    if (currentUserId && typeof currentUserId === 'number') {
        chatStore.initWebSocket(currentUserId)
        chatStore.setNotificationHandler(handleGlobalNotification)
    } else {
        console.warn('Cannot initialize chat: Invalid user ID')
    }
})
</script>

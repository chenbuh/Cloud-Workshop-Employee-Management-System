<template>
  <!-- Global Chat Background Handler (Invisible) -->
</template>

<script setup lang="ts">
import { onMounted, h, watch } from 'vue'
import { useNotification, NAvatar } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useChatStore } from '../store/chat'
import { useUserStore } from '../store/user'

const notification = useNotification()
const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()

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

// 监听用户信息变化，当 ID 可用时初始化 WebSocket
watch(() => userStore.userInfo?.id, (newId) => {
    if (newId && typeof newId === 'number') {
        chatStore.initWebSocket(newId)
        chatStore.setNotificationHandler(handleGlobalNotification)
    } else {
        // 如果用户登出，则关闭连接
        chatStore.closeWebSocket()
    }
}, { immediate: true })

onMounted(() => {
    // 组件挂载时如果已有 ID，且未连接，则初始化
    const currentId = userStore.userInfo?.id
    if (currentId && typeof currentId === 'number') {
        chatStore.initWebSocket(currentId)
        chatStore.setNotificationHandler(handleGlobalNotification)
    }
})
</script>

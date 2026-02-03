import { defineStore } from 'pinia'
import { ref, reactive, computed } from 'vue'
import { useNotification } from 'naive-ui'
import router from '../router'
import { markAsRead } from '../api/chat'

export const useChatStore = defineStore('chat', () => {
    const ws = ref<WebSocket | null>(null)
    const unreadMap = reactive(new Map<number | string, number>())
    const onlineUsers = ref(new Set<number>())
    const messages = ref<any[]>([])

    // Notification hook placeholder
    let notificationHandler: ((msg: any) => void) | null = null

    const setNotificationHandler = (handler: (msg: any) => void) => {
        notificationHandler = handler
    }

    const initWebSocket = (userId: number) => {
        // 检查 userId 是否有效
        if (!userId || typeof userId !== 'number') {
            console.warn('Invalid userId for WebSocket connection:', userId)
            return
        }

        if (ws.value) return

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        const host = window.location.hostname
        const port = 8080

        ws.value = new WebSocket(`${protocol}//${host}:${port}/ws/chat/${userId}`)

        ws.value.onmessage = (event) => {
            const msg = JSON.parse(event.data)
            handleIncomingMessage(msg)
        }

        ws.value.onclose = () => {
            ws.value = null
            // 只在 userId 有效时才重连
            if (userId && typeof userId === 'number') {
                setTimeout(() => initWebSocket(userId), 5000)
            }
        }
    }

    const lastMessages = reactive(new Map<number | string, any>())

    const handleIncomingMessage = (msg: any) => {
        // Handle control messages
        if (msg.type === 'status_change') {
            if (msg.status === 'online') {
                onlineUsers.value.add(msg.userId)
            } else {
                onlineUsers.value.delete(msg.userId)
            }
            return
        }

        if (msg.type === 'user_list') {
            onlineUsers.value = new Set(msg.onlineUsers || [])
            return
        }

        const isPublic = msg.toUserId === null
        const fromId = msg.fromUserId

        // Track last message for preview
        const convKey = isPublic ? 'public' : fromId
        lastMessages.set(convKey, msg)

        // Push to global messages
        messages.value.push(msg)

        // If not on chat page or active chat, show notification/update unread
        if (router.currentRoute.value.path !== '/chat') {
            const unreadKey = isPublic ? 'public' : fromId
            unreadMap.set(unreadKey, (unreadMap.get(unreadKey) || 0) + 1)

            if (notificationHandler) {
                notificationHandler(msg)
            }
        }
    }

    const sendMessage = (toUserId: number | null, content: string) => {
        if (ws.value && ws.value.readyState === WebSocket.OPEN) {
            ws.value.send(JSON.stringify({ toUserId, content, type: 'text' }))
        }
    }

    const totalUnreadCount = computed(() => {
        let sum = 0
        unreadMap.forEach(count => sum += count)
        return sum
    })

    return {
        ws,
        unreadMap,
        onlineUsers,
        messages,
        lastMessages,
        totalUnreadCount,
        initWebSocket,
        sendMessage,
        setNotificationHandler
    }
})

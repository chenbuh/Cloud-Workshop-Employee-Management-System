import request from '../utils/request'

export function getChatHistory(toUserId?: number) {
    return request({
        url: '/chat/history',
        method: 'get',
        params: { toUserId }
    })
}

export function getChatUsers() {
    return request({
        url: '/chat/users',
        method: 'get'
    })
}

export function markAsRead(fromUserId: number) {
    return request({
        url: '/chat/read',
        method: 'post',
        params: { fromUserId }
    })
}

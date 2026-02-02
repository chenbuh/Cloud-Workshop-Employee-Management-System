import request from '../utils/request'

export function getNotificationList() {
    return request({
        url: '/notification/list',
        method: 'get'
    })
}

export function getUnreadCount() {
    return request({
        url: '/notification/unread-count',
        method: 'get'
    })
}

export function readAllNotifications() {
    return request({
        url: '/notification/read-all',
        method: 'put'
    })
}

export function readNotification(id: number) {
    return request({
        url: `/notification/read/${id}`,
        method: 'put'
    })
}

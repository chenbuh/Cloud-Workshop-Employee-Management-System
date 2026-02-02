import request from '../utils/request'

export function getAllAttendance(params: { startDate?: string, endDate?: string, userId?: number } = {}) {
    return request({
        url: '/attendance/all',
        method: 'get',
        params
    })
}

export function clockIn() {
    return request({
        url: '/attendance/clock-in',
        method: 'post'
    })
}

export function clockOut() {
    return request({
        url: '/attendance/clock-out',
        method: 'post'
    })
}

export function getTodayAttendance() {
    return request({
        url: '/attendance/today',
        method: 'get'
    })
}

export function getAttendanceList(params: any) {
    return request({
        url: '/attendance/list',
        method: 'get',
        params
    })
}

export function updateAttendance(data: any) {
    return request({
        url: '/attendance/update',
        method: 'post',
        data
    })
}

export function deleteAttendance(id: number) {
    return request({
        url: '/attendance/delete',
        method: 'delete',
        params: { id }
    })
}

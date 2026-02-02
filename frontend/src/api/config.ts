import request from '../utils/request'

export function getAttendanceConfig() {
    return request({
        url: '/config/attendance',
        method: 'get'
    })
}

export function updateAttendanceConfig(data: any) {
    return request({
        url: '/config/attendance',
        method: 'put',
        data
    })
}

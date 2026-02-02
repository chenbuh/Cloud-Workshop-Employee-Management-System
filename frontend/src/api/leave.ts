import request from '../utils/request'

export function getLeaveList(params: any) {
    return request({
        url: '/leave/list',
        method: 'get',
        params
    })
}

export function applyLeave(data: any) {
    return request({
        url: '/leave/apply',
        method: 'post',
        data
    })
}

export function approveLeave(data: any) {
    return request({
        url: '/leave/approve',
        method: 'put',
        data
    })
}

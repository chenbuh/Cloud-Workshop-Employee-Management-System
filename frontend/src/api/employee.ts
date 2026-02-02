import request from '../utils/request'

export function getEmployeeList(params: any = {}) {
    return request({
        url: '/employee/list',
        method: 'get',
        params
    })
}

export function onboardEmployee(data: any) {
    return request({
        url: '/employee/onboard',
        method: 'post',
        data
    })
}

export function updateEmployee(data: any) {
    return request({
        url: '/employee',
        method: 'put',
        data
    })
}

export function removeEmployee(id: number) {
    return request({
        url: '/employee/' + id,
        method: 'delete'
    })
}

export function resignEmployee(data: { id: number, quitDate: any, quitReason: string }) {
    return request({
        url: '/employee/resign',
        method: 'post',
        data
    })
}

import request from '../utils/request'

export function getSalaryList() {
    return request({
        url: '/salary/list',
        method: 'get'
    })
}

export function getUnconfiguredEmployees() {
    return request({
        url: '/salary/unconfigured',
        method: 'get'
    })
}

export function saveSalary(data: any) {
    return request({
        url: '/salary/save',
        method: 'post',
        data
    })
}

export function deleteSalary(id: number) {
    return request({
        url: `/salary/${id}`,
        method: 'delete'
    })
}

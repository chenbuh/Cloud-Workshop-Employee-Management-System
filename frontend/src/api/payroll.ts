import request from '../utils/request'

export function getPayrollList(month: string) {
    return request({
        url: '/payroll/list',
        method: 'get',
        params: { month }
    })
}

export function generatePayroll(month: string) {
    return request({
        url: '/payroll/generate',
        method: 'post',
        params: { month }
    })
}

export function issuePayroll(id: number) {
    return request({
        url: `/payroll/issue/${id}`,
        method: 'put'
    })
}

export function batchSendPayroll(ids: number[]) {
    return request({
        url: '/payroll/batch-send',
        method: 'post',
        data: ids
    })
}

export function exportPayroll(month: string) {
    return request({
        url: '/payroll/export',
        method: 'get',
        params: { month },
        responseType: 'blob'
    })
}

export function getPayrollHistory(userId: number) {
    return request({
        url: '/payroll/history',
        method: 'get',
        params: { userId }
    })
}

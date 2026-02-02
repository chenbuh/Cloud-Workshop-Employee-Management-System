import request from '../utils/request'

export function listCycles() {
    return request({
        url: '/performance/cycles',
        method: 'get'
    })
}

export function addCycle(data: any) {
    return request({
        url: '/performance/cycles',
        method: 'post',
        data
    })
}

export function listAppraisals(cycleId: number) {
    return request({
        url: '/performance/appraisals',
        method: 'get',
        params: { cycleId }
    })
}

export function submitAppraisal(data: any) {
    return request({
        url: '/performance/appraisals',
        method: 'post',
        data
    })
}

export function getEmpPerformance(empId: number) {
    return request({
        url: `/performance/emp/${empId}`,
        method: 'get'
    })
}

import request from '../utils/request'

export function getFlowTemplates() {
    return request({
        url: '/flow/templates',
        method: 'get'
    })
}

export function getFlowInstances() {
    return request({
        url: '/flow/instances',
        method: 'get'
    })
}

export function startFlow(data: { templateId: number, employeeId: number }) {
    return request({
        url: '/flow/start',
        method: 'post',
        data
    })
}

export function getFlowTasks(instanceId: number) {
    return request({
        url: `/flow/tasks/${instanceId}`,
        method: 'get'
    })
}

export function completeFlowTask(data: { taskId: number, remark: string }) {
    return request({
        url: '/flow/task/complete',
        method: 'put',
        data
    })
}

import request from '../utils/request'

export function listAuditLogs(params: any) {
    return request({
        url: '/audit/list',
        method: 'get',
        params
    })
}

export function delAuditLog(id: number) {
    return request({
        url: `/audit/${id}`,
        method: 'delete'
    })
}

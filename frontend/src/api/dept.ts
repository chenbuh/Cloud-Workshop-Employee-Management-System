import request from '../utils/request'

export function getDeptTree() {
    return request({
        url: '/dept/tree',
        method: 'get'
    })
}

export function getDeptList() {
    return request({
        url: '/dept/list',
        method: 'get'
    })
}

export function addDept(data: any) {
    return request({
        url: '/dept',
        method: 'post',
        data
    })
}

export function updateDept(data: any) {
    return request({
        url: '/dept',
        method: 'put',
        data
    })
}

export function delDept(id: number) {
    return request({
        url: '/dept/' + id,
        method: 'delete'
    })
}

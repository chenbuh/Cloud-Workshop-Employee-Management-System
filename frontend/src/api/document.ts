import request from '../utils/request'

export function listCategories() {
    return request({
        url: '/doc/categories',
        method: 'get'
    })
}

export function addCategory(data: any) {
    return request({
        url: '/doc/category',
        method: 'post',
        data
    })
}

export function delCategory(id: number) {
    return request({
        url: `/doc/category/${id}`,
        method: 'delete'
    })
}

export function listDocuments(params: any) {
    return request({
        url: '/doc/list',
        method: 'get',
        params
    })
}

export function getDocument(id: number) {
    return request({
        url: `/doc/${id}`,
        method: 'get'
    })
}

export function saveDocument(data: any) {
    return request({
        url: '/doc/save',
        method: 'post',
        data
    })
}

export function delDocument(id: number) {
    return request({
        url: `/doc/${id}`,
        method: 'delete'
    })
}

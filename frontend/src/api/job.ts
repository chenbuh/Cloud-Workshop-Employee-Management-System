import request from '../utils/request'

// Post APIs
export function listPost(params: any) {
    return request({
        url: '/system/post/list',
        method: 'get',
        params
    })
}

export function allPost() {
    return request({
        url: '/system/post/all',
        method: 'get'
    })
}

export function getPost(postId: number | string) {
    return request({
        url: '/system/post/' + postId,
        method: 'get'
    })
}

export function addPost(data: any) {
    return request({
        url: '/system/post',
        method: 'post',
        data
    })
}

export function updatePost(data: any) {
    return request({
        url: '/system/post',
        method: 'put',
        data
    })
}

export function delPost(postIds: string | number) {
    return request({
        url: '/system/post/' + postIds,
        method: 'delete'
    })
}

// Job Level APIs
export function listJobLevel(params: any) {
    return request({
        url: '/system/job-level/list',
        method: 'get',
        params
    })
}

export function allJobLevel() {
    return request({
        url: '/system/job-level/all',
        method: 'get'
    })
}

export function getJobLevel(levelId: number | string) {
    return request({
        url: '/system/job-level/' + levelId,
        method: 'get'
    })
}

export function addJobLevel(data: any) {
    return request({
        url: '/system/job-level',
        method: 'post',
        data
    })
}

export function updateJobLevel(data: any) {
    return request({
        url: '/system/job-level',
        method: 'put',
        data
    })
}

export function delJobLevel(levelIds: string | number) {
    return request({
        url: '/system/job-level/' + levelIds,
        method: 'delete'
    })
}

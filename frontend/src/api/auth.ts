import request from '../utils/request'

export function login(data: any) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

export function logout() {
    return request({
        url: '/auth/logout',
        method: 'get'
    })
}

export function getInfo() {
    return request({
        url: '/auth/info',
        method: 'get'
    })
}

export function updatePassword(data: any) {
    return request({
        url: '/auth/update-password',
        method: 'post',
        data
    })
}

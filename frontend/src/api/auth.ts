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

export function getSecurityLogs() {
    return request({
        url: '/auth/security-logs',
        method: 'get'
    })
}

export function getSessions() {
    return request({
        url: '/auth/sessions',
        method: 'get'
    })
}

export function kickout(token: string) {
    return request({
        url: '/auth/kickout',
        method: 'post',
        params: { token }
    })
}

export function setupMfa() {
    return request({
        url: '/auth/mfa/setup',
        method: 'get'
    })
}

export function enableMfa(data: { secret: string, code: string }) {
    return request({
        url: '/auth/mfa/enable',
        method: 'post',
        data
    })
}

export function mfaLogin(data: { userId: number, code: string }) {
    return request({
        url: '/auth/mfa/login',
        method: 'post',
        data
    })
}

export function disableMfa(data: { code: string }) {
    return request({
        url: '/auth/mfa/disable',
        method: 'post',
        data
    })
}

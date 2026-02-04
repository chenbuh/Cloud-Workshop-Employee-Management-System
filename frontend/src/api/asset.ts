import request from '../utils/request'

export function getAssetList(params?: any) {
    return request({
        url: '/asset/list',
        method: 'get',
        params
    })
}

export function getClaimList() {
    return request({
        url: '/asset/claims',
        method: 'get'
    })
}

export function createClaim(data: any) {
    return request({
        url: '/asset/claim',
        method: 'post',
        data
    })
}

export function approveClaim(data: any) {
    return request({
        url: '/asset/claim/approve',
        method: 'put',
        data
    })
}

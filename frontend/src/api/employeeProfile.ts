import request from '../utils/request'

export function getFullProfile(userId: number) {
    return request({
        url: `/employee-profile/full/${userId}`,
        method: 'get'
    })
}

export function addSkill(data: any) {
    return request({
        url: '/employee-profile/skill',
        method: 'post',
        data
    })
}

export function addGrowth(data: any) {
    return request({
        url: '/employee-profile/growth',
        method: 'post',
        data
    })
}

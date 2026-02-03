import request from '../utils/request'

export function getJobs() {
    return request({
        url: '/recruitment/jobs',
        method: 'get'
    })
}

export function createJob(data: any) {
    return request({
        url: '/recruitment/jobs',
        method: 'post',
        data
    })
}

export function getCandidates() {
    return request({
        url: '/recruitment/candidates',
        method: 'get'
    })
}

export function updateCandidateStatus(id: number, status: number) {
    return request({
        url: '/recruitment/candidates/status',
        method: 'put',
        data: { id, status }
    })
}

export function getInterviews(candidateId?: number) {
    return request({
        url: '/recruitment/interviews',
        method: 'get',
        params: { candidateId }
    })
}

export function addInterview(data: any) {
    return request({
        url: '/recruitment/interviews',
        method: 'post',
        data
    })
}

export function updateInterview(data: any) {
    return request({
        url: '/recruitment/interviews',
        method: 'put',
        data
    })
}

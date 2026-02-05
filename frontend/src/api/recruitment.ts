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

export function deleteCandidate(id: number) {
    return request({
        url: `/recruitment/candidates/${id}`,
        method: 'delete'
    })
}

export function parseResume(text: string) {
    return request({
        url: '/recruitment/candidate/parse',
        method: 'post',
        data: { text }
    })
}

export function getInterviews(params?: any) {
    return request({
        url: '/recruitment/interviews',
        method: 'get',
        params
    })
}


export function saveCandidate(data: any) {
    return request({
        url: '/recruitment/candidates',
        method: 'post',
        data
    })
}

export function updateCandidateStatus(data: any) {
    return request({
        url: '/recruitment/candidates/status',
        method: 'put',
        data
    })
}

export function updateCandidate(data: any) {
    return request({
        url: '/recruitment/candidates',
        method: 'put',
        data
    })
}

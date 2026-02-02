import request from '../utils/request'

export function listCandidates(params: any) {
    return request({
        url: '/onboarding/candidates',
        method: 'get',
        params
    })
}

export function addCandidate(data: any) {
    return request({
        url: '/onboarding/candidates',
        method: 'post',
        data
    })
}

export function updateCandidateStatus(candidateId: number, status: number) {
    return request({
        url: '/onboarding/candidates/status',
        method: 'put',
        params: { candidateId, status }
    })
}

export function getInterviews(candidateId: number) {
    return request({
        url: '/onboarding/interviews',
        method: 'get',
        params: { candidateId }
    })
}

export function addInterview(data: any) {
    return request({
        url: '/onboarding/interviews',
        method: 'post',
        data
    })
}

export function convertToEmployee(candidateId: number, deptId: number) {
    return request({
        url: '/onboarding/candidates/convert',
        method: 'post',
        params: { candidateId, deptId }
    })
}

import request from '../utils/request'

export function getContractList() {
    return request({
        url: '/contract/list',
        method: 'get'
    })
}

export function getContract(id: number) {
    return request({
        url: `/contract/${id}`,
        method: 'get'
    })
}

export function saveContract(data: any) {
    return request({
        url: '/contract/save',
        method: 'post',
        data
    })
}

export function deleteContract(id: number) {
    return request({
        url: `/contract/${id}`,
        method: 'delete'
    })
}

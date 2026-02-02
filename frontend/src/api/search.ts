import request from '../utils/request'

export function globalSearch(keyword: string) {
    return request({
        url: '/search/global',
        method: 'get',
        params: { keyword }
    })
}

export function SuggestEmployee(keyword: string) {
    return request({
        url: '/search/suggest/emp',
        method: 'get',
        params: { keyword }
    })
}

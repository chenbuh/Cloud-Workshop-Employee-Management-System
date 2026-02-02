import request from '../utils/request'

export function getAnnouncements(params: any) {
    return request({
        url: '/announcement/list',
        method: 'get',
        params
    })
}

export function addAnnouncement(data: any) {
    return request({
        url: '/announcement/add',
        method: 'post',
        data
    })
}

export function updateAnnouncement(data: any) {
    return request({
        url: '/announcement/update',
        method: 'put',
        data
    })
}

export function deleteAnnouncement(id: number) {
    return request({
        url: `/announcement/delete/${id}`,
        method: 'delete'
    })
}

export function publishAnnouncement(id: number, status: number) {
    return request({
        url: `/announcement/publish/${id}/${status}`,
        method: 'put'
    })
}

export function getAnnouncement(id: number) {
    return request({
        url: `/announcement/${id}`,
        method: 'get'
    })
}

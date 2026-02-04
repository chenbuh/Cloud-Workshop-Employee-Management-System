import request from '../utils/request'

export function getResourceList() {
    return request({
        url: '/resource/list',
        method: 'get'
    })
}

export function getBookingList() {
    return request({
        url: '/resource/bookings',
        method: 'get'
    })
}

export function createBooking(data: any) {
    return request({
        url: '/resource/booking',
        method: 'post',
        data
    })
}

export function cancelBooking(id: number) {
    return request({
        url: `/resource/booking/${id}`,
        method: 'delete'
    })
}

export function getResourceAnalytics() {
    return request({
        url: '/resource/analytics',
        method: 'get'
    })
}

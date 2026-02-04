import request from '../utils/request'

export function getDashboardStats() {
    return request({
        url: '/dashboard/stats',
        method: 'get'
    })
}

export function getGrowthTrend(days: number = 180) {
    return request({
        url: '/dashboard/growth-trend',
        method: 'get',
        params: { days }
    })
}

export function getDeptDistribution() {
    return request({
        url: '/dashboard/dept-distribution',
        method: 'get'
    })
}

export function getNews() {
    return request({
        url: '/dashboard/news',
        method: 'get'
    })
}

export function getUpcomingBirthdays() {
    return request({
        url: '/dashboard/upcoming-birthdays',
        method: 'get'
    })
}

export function getPersonalDashboard() {
    return request({
        url: '/dashboard/personal',
        method: 'get'
    })
}


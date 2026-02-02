import request from '../utils/request'

// 用户管理
export function getUserList() {
    return request({
        url: '/user/list',
        method: 'get'
    })
}

export function createUser(data: any) {
    return request({
        url: '/user/create',
        method: 'post',
        data
    })
}

export function deleteUser(id: number) {
    return request({
        url: '/user/' + id,
        method: 'delete'
    })
}

// 角色管理
export function getRoleList() {
    return request({
        url: '/role/list',
        method: 'get'
    })
}

export function createRole(data: any) {
    return request({
        url: '/role',
        method: 'post',
        data
    })
}

export function deleteRole(id: number) {
    return request({
        url: '/role/' + id,
        method: 'delete'
    })
}

export function getMenuTree() {
    return request({
        url: '/menu/tree',
        method: 'get'
    })
}

export function getRoleMenuIds(roleId: number) {
    return request({
        url: `/role/menu-ids/${roleId}`,
        method: 'get'
    })
}

export function assignRoleMenus(data: { roleId: number, menuIds: number[] }) {
    return request({
        url: '/role/assign-menus',
        method: 'post',
        data
    })
}

export function getUserMenuIds(userId: number) {
    return request({
        url: `/user/menu-ids/${userId}`,
        method: 'get'
    })
}

export function assignUserMenus(data: { userId: number, menuIds: number[] }) {
    return request({
        url: '/user/assign-menus',
        method: 'post',
        data
    })
}


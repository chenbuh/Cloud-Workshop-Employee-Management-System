import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const permissions = ref<string[]>([])
    const userInfo = ref<any>(null)

    const setToken = (newToken: string) => {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    const setPermissions = (newPermissions: string[]) => {
        permissions.value = newPermissions
    }

    const hasPermission = (permission: string) => {
        return permissions.value.includes('*') || permissions.value.includes(permission)
    }

    const logout = () => {
        token.value = ''
        permissions.value = []
        userInfo.value = null
        localStorage.removeItem('token')
    }

    return {
        token,
        permissions,
        userInfo,
        setToken,
        setPermissions,
        hasPermission,
        logout
    }
})

import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
    // Theme: 'light' | 'dark'
    const theme = ref(localStorage.getItem('theme') || 'light')

    // Primary color variant: 'indigo' | 'rose' | 'emerald' | 'amber'
    const skin = ref(localStorage.getItem('skin') || 'indigo')

    const skinColors: any = {
        indigo: '#6366f1',
        rose: '#f43f5e',
        emerald: '#10b981',
        amber: '#f59e0b'
    }

    const updateCssVars = () => {
        const color = skinColors[skin.value] || skinColors.indigo
        document.documentElement.style.setProperty('--primary-color', color)
    }

    const setTheme = (val: string) => {
        theme.value = val
        localStorage.setItem('theme', val)
        if (val === 'dark') {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    }

    const setSkin = (val: string) => {
        skin.value = val
        localStorage.setItem('skin', val)
        updateCssVars()
    }

    // Initialize
    updateCssVars()
    if (theme.value === 'dark') {
        document.documentElement.classList.add('dark')
    }

    return {
        theme,
        skin,
        skinColors,
        setTheme,
        setSkin
    }
})

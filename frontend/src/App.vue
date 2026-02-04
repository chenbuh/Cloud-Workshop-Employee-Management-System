<template>
  <n-config-provider :theme="currentTheme" :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
          <router-view />
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { GlobalThemeOverrides, zhCN, dateZhCN, NNotificationProvider, darkTheme } from 'naive-ui'
import { useAppStore } from './store/app'

const appStore = useAppStore()

const currentTheme = computed(() => appStore.theme === 'dark' ? darkTheme : null)

const skinColors: any = {
  indigo: { primary: '#6366f1', hover: '#818cf8', pressed: '#4f46e5' },
  rose: { primary: '#f43f5e', hover: '#fb7185', pressed: '#e11d48' },
  emerald: { primary: '#10b981', hover: '#34d399', pressed: '#059669' },
  amber: { primary: '#f59e0b', hover: '#fbbf24', pressed: '#d97706' }
}

const themeOverrides = computed<GlobalThemeOverrides>(() => {
  const colors = skinColors[appStore.skin] || skinColors.indigo
  return {
    common: {
      primaryColor: colors.primary,
      primaryColorHover: colors.hover,
      primaryColorPressed: colors.pressed,
      borderRadius: '12px',
    },
    Card: {
      borderRadius: '16px',
    },
    Button: {
      borderRadiusMedium: '10px',
      fontWeight: '500'
    },
    Input: {
      borderRadius: '10px'
    }
  }
})
</script>

<style>
/* 可以在这里添加 App 级别的样式 */
</style>

<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
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
import { GlobalThemeOverrides, zhCN, dateZhCN, NNotificationProvider } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#6366f1',
    primaryColorHover: '#818cf8',
    primaryColorPressed: '#4f46e5',
    borderRadius: '12px',
  },
  Card: {
    borderRadius: '16px',
    boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.07)'
  },
  Button: {
    borderRadiusMedium: '10px',
    fontWeight: '500'
  },
  Input: {
    borderRadius: '10px'
  }
}
</script>

<style>
/* 可以在这里添加 App 级别的样式 */
</style>

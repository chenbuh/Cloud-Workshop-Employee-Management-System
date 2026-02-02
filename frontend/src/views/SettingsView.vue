<template>
  <div class="settings-page employee-page">
    <div class="page-header">
      <h2>系统设置</h2>
    </div>

    <div class="glass-effect settings-container">
      <n-tabs type="line" animated>
        <n-tab-pane name="basic" tab="基础设置">
          <n-form label-placement="left" label-width="120px" style="max-width: 600px">
            <n-form-item label="系统名称">
              <n-input value="Cloud Workshop 员工管理系统" />
            </n-form-item>
            <n-form-item label="备案号">
              <n-input value="京ICP备88888888号" />
            </n-form-item>
             <n-form-item label="系统Logo">
               <div class="logo-preview">
                 <img src="/logo.png" alt="Logo" style="width: 48px; height: 48px" />
               </div>
            </n-form-item>
            <n-form-item>
              <n-button type="primary">保存更改</n-button>
            </n-form-item>
          </n-form>
        </n-tab-pane>
        <n-tab-pane name="attendance" tab="考勤设置" v-if="userStore.hasPermission('system:config:list')">
          <n-form :model="attendanceForm" label-placement="left" label-width="120px" style="max-width: 600px">
            <n-form-item label="工作日设置">
              <n-checkbox-group v-model:value="attendanceForm.workDays">
                <n-space item-style="display: flex;">
                  <n-checkbox :value="1" label="周一" />
                  <n-checkbox :value="2" label="周二" />
                  <n-checkbox :value="3" label="周三" />
                  <n-checkbox :value="4" label="周四" />
                  <n-checkbox :value="5" label="周五" />
                  <n-checkbox :value="6" label="周六" />
                  <n-checkbox :value="7" label="周日" />
                </n-space>
              </n-checkbox-group>
            </n-form-item>
            <n-form-item label="上班打卡时间">
              <n-time-picker v-model:formatted-value="attendanceForm.startTime" value-format="HH:mm" format="HH:mm" />
            </n-form-item>
            <n-form-item label="下班打卡时间">
              <n-time-picker v-model:formatted-value="attendanceForm.endTime" value-format="HH:mm" format="HH:mm" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" @click="handleSaveAttendance" :loading="loading">保存配置</n-button>
            </n-form-item>
          </n-form>
        </n-tab-pane>
        <n-tab-pane name="security" tab="安全设置">
          <n-empty description="安全设置功能开发中..." />
        </n-tab-pane>
        <n-tab-pane name="notification" tab="通知配置">
           <n-empty description="通知配置功能开发中..." />
        </n-tab-pane>
      </n-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { 
  NTabs, NTabPane, NForm, NFormItem, NInput, NEmpty, NButton, 
  NCheckboxGroup, NCheckbox, NSpace, NTimePicker, useMessage 
} from 'naive-ui'
import { getAttendanceConfig, updateAttendanceConfig } from '../api/config'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const message = useMessage()
const loading = ref(false)
const attendanceForm = reactive({
    workDays: [],
    startTime: '09:00',
    endTime: '18:00'
})

const loadConfig = async () => {
    try {
        const res: any = await getAttendanceConfig()
        attendanceForm.workDays = res.workDays || []
        attendanceForm.startTime = res.startTime || '09:00'
        attendanceForm.endTime = res.endTime || '18:00'
        // Ensure format is HH:mm (backend might return HH:mm:ss)
        if (attendanceForm.startTime.length > 5) attendanceForm.startTime = attendanceForm.startTime.substring(0, 5)
        if (attendanceForm.endTime.length > 5) attendanceForm.endTime = attendanceForm.endTime.substring(0, 5)
    } catch (e) {
        message.error('加载配置失败')
    }
}

const handleSaveAttendance = async () => {
    loading.value = true
    try {
        await updateAttendanceConfig(attendanceForm)
        message.success('考勤配置已更新')
    } catch (e) {
        message.error('保存失败')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadConfig()
})
</script>

<style scoped>
.settings-container {
  padding: 32px;
  border-radius: 20px;
  min-height: 500px;
  background: rgba(255, 255, 255, 0.5);
}
</style>

<template>
  <n-layout class="main-layout" has-sider>
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
      class="glass-sider"
    >
      <div class="sider-wrapper">
        <div class="logo">
          <img src="/logo.png" alt="Logo" class="logo-sidebar" />
          <span v-if="!collapsed" class="logo-text">Cloud Workshop</span>
        </div>
        
        <n-scrollbar class="sider-scroll">
          <n-menu
            :collapsed="collapsed"
            :collapsed-width="64"
            :collapsed-icon-size="22"
            :options="menuOptions"
            :value="activeKey"
            @update:value="handleMenuUpdate"
            class="glass-menu"
          />
        </n-scrollbar>
  
        <div class="user-profile" v-if="!collapsed" @click="router.push('/profile')" style="cursor: pointer">
          <div class="user-avatar">
            <n-avatar round size="small" :src="userStore.userInfo?.avatar" :fallback-src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${userStore.userInfo?.nickName || 'User'}`" />
          </div>
          <div class="user-info">
            <div class="user-name">{{ userStore.userInfo?.nickName || '加载中...' }}</div>
            <div class="user-role">{{ userStore.userInfo?.userType === '00' ? '超级管理员' : '系统用户' }}</div>
          </div>
          <n-space :size="4">
            <n-button quaternary circle size="small" @click.stop="router.push('/profile')">
               <template #icon><n-icon :component="PersonCircleOutline" /></template>
            </n-button>
            <n-button quaternary circle size="small" @click.stop="handleLogout">
               <template #icon><n-icon :component="LogOutOutline" /></template>
            </n-button>
          </n-space>
        </div>
      </div>
    </n-layout-sider>
    
    <n-layout class="content-layout">
      <!-- ... (Header content unchanged) ... -->
      <n-layout-header class="glass-header" bordered>
        <div class="header-left"></div>
        <div class="header-right">
           <div class="search-box">
             <n-auto-complete 
               v-model:value="searchVal"
               :options="searchOptions"
               placeholder="搜索员工 (姓名/工号)..."
               clearable
               :loading="searchLoading"
               @update:value="handleSearch"
               @select="handleSelectUser"
               style="width: 240px"
             >
               <template #prefix>
                 <n-icon :component="SearchOutline" color="#94a3b8" />
               </template>
             </n-auto-complete>
           </div>
           
            <n-button quaternary circle @click="appStore.setTheme(appStore.theme === 'dark' ? 'light' : 'dark')">
               <template #icon>
                 <n-icon :component="appStore.theme === 'dark' ? SunnyOutline : MoonOutline" />
               </template>
            </n-button>

            <n-popover trigger="click" placement="bottom-end" class="glass-popover">
                <template #trigger>
                    <n-button quaternary circle>
                        <template #icon><n-icon :component="ColorPaletteOutline" /></template>
                    </n-button>
                </template>
                <div class="skin-panel">
                    <div class="panel-header">选择皮肤</div>
                    <div class="skin-grid">
                        <div 
                          v-for="(c, key) in skinOptions" 
                          :key="key" 
                          class="skin-item" 
                          :style="{ background: (c as any).color }" 
                          :class="{ active: appStore.skin === key }"
                          @click="appStore.setSkin(key as string)"
                        >
                            <n-icon v-if="appStore.skin === key" :component="CheckmarkCircleOutline" color="white" />
                        </div>
                    </div>
                </div>
            </n-popover>

           <n-badge :value="unreadCount" :show="unreadCount > 0" pill>
             <n-popover trigger="click" scrollable style="max-height: 400px; width: 320px; padding: 0" class="glass-popover">
               <template #trigger>
                 <n-button quaternary circle>
                   <template #icon><n-icon :component="NotificationsOutline" /></template>
                 </n-button>
               </template>
               <div class="notification-panel">
                 <div class="panel-header">
                   <span>通知中心</span>
                   <n-button quaternary size="tiny" type="primary" @click="handleReadAll">全部已读</n-button>
                 </div>
                 <div class="notification-list" v-if="notifications.length > 0">
                   <div 
                      v-for="item in notifications" 
                      :key="item.id" 
                      class="notification-item" 
                      :class="{ unread: item.isRead === 0 }"
                      @click="handleReadOne(item)"
                    >
                     <div class="item-icon">
                        <n-icon :component="getNotifyIcon(item.type)" :color="getNotifyColor(item.type)" />
                     </div>
                     <div class="item-content">
                       <div class="item-title">{{ item.title }}</div>
                       <div class="item-desc">{{ item.content }}</div>
                       <div class="item-time">{{ formatTime(item.createTime) }}</div>
                     </div>
                   </div>
                 </div>
                 <n-empty v-else description="暂无通知" style="padding: 40px 0" />
               </div>
             </n-popover>
           </n-badge>
        </div>
      </n-layout-header>

      <n-layout-content content-style="padding: 24px;">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>

  <!-- 修改密码弹窗 -->
  <n-modal v-model:show="showPasswordModal" preset="card" title="修改登录密码" style="width: 400px">
    <n-form :model="passwordForm" label-placement="left" label-width="80px">
      <n-form-item label="旧密码">
        <n-input type="password" v-model:value="passwordForm.oldPassword" show-password-on="click" placeholder="请输入当前密码"/>
      </n-form-item>
      <n-form-item label="新密码">
        <n-input type="password" v-model:value="passwordForm.newPassword" show-password-on="click" placeholder="建议包含字母和数字"/>
      </n-form-item>
      <n-form-item label="确认新密码">
        <n-input type="password" v-model:value="passwordForm.confirmPassword" show-password-on="click" placeholder="再次输入新密码"/>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
         <n-button @click="showPasswordModal = false">取消</n-button>
         <n-button type="primary" :loading="passwordLoading" @click="handleChangePassword">确认修改</n-button>
      </n-space>
    </template>
  </n-modal>
  <internal-chat />
</template>

<script setup lang="ts">
import { ref, h, onMounted, reactive, computed } from 'vue'
import type { Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  NIcon, useMessage, NBadge, NPopover, NEmpty, NAvatar,
  NLayout, NLayoutSider, NMenu, NButton, NLayoutHeader, NLayoutContent,
  NModal, NForm, NFormItem, NInput, NSpace, NAutoComplete
} from 'naive-ui'
import { 
  SpeedometerOutline, 
  PeopleOutline, 
  GitNetworkOutline, 
  SettingsOutline,
  PersonCircleOutline,
  LogOutOutline,
  NotificationsOutline,
  DocumentTextOutline,
  AlarmOutline,
  CardOutline,
  WalletOutline,
  InformationCircleOutline,
  CheckmarkCircleOutline,
  AlertCircleOutline,
  CloseCircleOutline,
  ShieldCheckmarkOutline,
  StatsChartOutline,
  SearchOutline,
  FlashOutline,
  BookOutline,
  KeyOutline,
  ChatbubbleEllipsesOutline,
  CubeOutline,
  MoonOutline,
  SunnyOutline,
  ColorPaletteOutline,
  BriefcaseOutline,
  StorefrontOutline,
  PieChartOutline,
  LayersOutline,
  DocumentLockOutline,
  EarthOutline
} from '@vicons/ionicons5'
import { getInfo, logout, updatePassword } from '../api/auth'
import { getNotificationList, getUnreadCount, readAllNotifications, readNotification } from '../api/notification'
import { globalSearch } from '../api/search'
import { useUserStore } from '../store/user'
import { useChatStore } from '../store/chat'
import { useAppStore } from '../store/app'
import InternalChat from '../components/InternalChat.vue'
import moment from 'moment'

const userStore = useUserStore()
const chatStore = useChatStore()
const appStore = useAppStore()
const router = useRouter()
const route = useRoute()
const message = useMessage()

const collapsed = ref(false)
const activeKey = ref<string | null>(null)
/* Search related refs */
const searchVal = ref('')
const searchOptions = ref<any[]>([])
const searchLoading = ref(false)
let searchTimer: any = null

/* Password Change Refs */
const showPasswordModal = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const skinOptions: any = {
    indigo: { color: '#6366f1', label: '靛蓝' },
    rose: { color: '#f43f5e', label: '蔷薇' },
    emerald: { color: '#10b981', label: '翡翠' },
    amber: { color: '#f59e0b', label: '琥珀' }
}

const handleSearch = (value: string) => {
  searchVal.value = value
  if (!value) {
    searchOptions.value = [] // Clear options if empty
    return
  }
  
  if (searchTimer) clearTimeout(searchTimer)
  searchLoading.value = true
  
  searchTimer = setTimeout(async () => {
    try {
      const res: any = await globalSearch(value)
      
      const options = []
      
      if (res.employees && res.employees.length > 0) {
        options.push({
          type: 'group',
          label: '员工',
          key: 'employees',
          children: res.employees.map((e: any) => ({
            label: e.title + ' (' + e.desc + ')',
            value: 'emp:' + e.id
          }))
        })
      }
      
      if (res.depts && res.depts.length > 0) {
        options.push({
          type: 'group',
          label: '部门',
          key: 'depts',
          children: res.depts.map((d: any) => ({
             label: d.title,
             value: 'dept:' + d.id
          }))
        })
      }

      if (res.docs && res.docs.length > 0) {
        options.push({
          type: 'group',
          label: '知识库文档',
          key: 'docs',
          children: res.docs.map((doc: any) => ({
            label: doc.title,
            value: 'doc:' + doc.id
          }))
        })
      }
      
      searchOptions.value = options
    } finally {
      searchLoading.value = false
    }
  }, 500)
}

const handleSelectUser = (value: string) => {
  if (value.startsWith('emp:')) {
    const empId = value.split(':')[1]
    message.success('已选择员工，正在跳转...') 
    router.push(`/employee/profile/${empId}`)
  } else if (value.startsWith('dept:')) {
    message.info('跳转至部门: ' + value.split(':')[1])
    router.push({ path: '/dept', query: { id: value.split(':')[1] } })
  } else if (value.startsWith('doc:')) {
    const docId = value.split(':')[1]
    message.success('已找到文档，正在打开...')
    router.push({ path: '/document', query: { id: docId } })
  }
}

const unreadCount = ref(0)
const notifications = ref<any[]>([])

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = computed(() => [
  {
    label: '仪表盘',
    key: 'dashboard',
    icon: renderIcon(SpeedometerOutline)
  },
  {
    label: '员工管理',
    key: 'employee',
    icon: renderIcon(PeopleOutline),
    children: [
      {
        label: '员工花名册',
        key: 'employee-list'
      },
      {
        label: '入职漏斗',
        key: 'onboarding'
      },
      {
        label: '招聘管理 (ATS)',
        key: 'recruitment'
      }
    ]
  },
  {
    label: '组织架构',
    key: 'dept',
    icon: renderIcon(GitNetworkOutline)
  },
  {
    label: '考勤签到',
    key: 'attendance',
    icon: renderIcon(AlarmOutline)
  },
  {
    label: '考勤统计',
    key: 'attendance-management',
    icon: renderIcon(StatsChartOutline)
  },
  {
    label: '薪资管理',
    key: 'salary',
    icon: renderIcon(CardOutline)
  },
  {
    label: '绩效管理',
    key: 'performance',
    icon: renderIcon(FlashOutline)
  },
  {
    label: '财务结算',
    key: 'payroll',
    icon: renderIcon(WalletOutline)
  },
  {
    label: '审批管理',
    key: 'approvals',
    icon: renderIcon(DocumentTextOutline)
  },
  {
    label: '资源预约',
    key: 'resource-booking',
    icon: renderIcon(CubeOutline)
  },
  {
    label: '物资申领',
    key: 'asset-management',
    icon: renderIcon(StorefrontOutline)
  },
  {
    label: '分析看板',
    key: 'resource-analytics',
    icon: renderIcon(PieChartOutline)
  },
  {
    label: '流程引导',
    key: 'workflow',
    icon: renderIcon(LayersOutline)
  },
  {
    label: '电子合同',
    key: 'contract',
    icon: renderIcon(DocumentLockOutline)
  },
  {
    label: '人才库',
    key: 'recruitment-pool',
    icon: renderIcon(EarthOutline)
  },
  {
    label: () => h('div', { style: 'display: flex; align-items: center; justify-content: space-between; width: 100%' }, [
        h('span', null, '内部沟通'),
        chatStore.totalUnreadCount > 0 ? h(NBadge, { value: chatStore.totalUnreadCount, dot: true, processing: true, style: 'margin-left: 8px' }) : null
    ]),
    key: 'chat',
    icon: renderIcon(ChatbubbleEllipsesOutline)
  },
  {
    label: '企业知识库',
    key: 'document',
    icon: renderIcon(BookOutline)
  },
  {
    label: '系统管理',
    key: 'system',
    icon: renderIcon(ShieldCheckmarkOutline),
    children: [
      {
        label: '账号管理',
        key: 'system/user'
      },
      {
        label: '角色权限',
        key: 'system/role'
      },
      {
        label: '岗位职级',
        key: 'system/job'
      },
      {
        label: '审计日志',
        key: 'audit'
      },
      {
        label: '系统公告',
        key: 'system/announcement'
      }
    ]
  },
  {
    label: '系统设置',
    key: 'settings',
    icon: renderIcon(SettingsOutline)
  }
])

const handleMenuUpdate = (key: string) => {
  activeKey.value = key
  router.push('/' + key)
}

const loadNotifications = async () => {
    try {
        const [list, count]: any = await Promise.all([
            getNotificationList(),
            getUnreadCount()
        ])
        notifications.value = list
        unreadCount.value = count
    } catch(e) {}
}

const handleReadAll = async () => {
    await readAllNotifications()
    loadNotifications()
}

const handleReadOne = async (item: any) => {
    if (item.isRead === 0) {
        await readNotification(item.id)
        loadNotifications()
    }
}

const getNotifyIcon = (type: string) => {
    if (type === 'success') return CheckmarkCircleOutline
    if (type === 'warning') return AlertCircleOutline
    if (type === 'error') return CloseCircleOutline
    return InformationCircleOutline
}

const getNotifyColor = (type: string) => {
    if (type === 'success') return '#10b981'
    if (type === 'warning') return '#f59e0b'
    if (type === 'error') return '#ef4444'
    return '#6366f1'
}

const formatTime = (time: any) => moment(time).fromNow()

const handleLogout = async () => {
    try {
        await logout()
    } finally {
        userStore.logout()
        message.success('已安全退出')
        router.push('/login')
    }
}

const handleChangePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入密码不一致')
    return
  }
  if (!passwordForm.oldPassword) {
    message.warning('请输入旧密码')
    return
  }
  
  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    message.success('密码修改成功，请重新登录')
    showPasswordModal.value = false
    handleLogout()
  } catch (e: any) {
    message.error(e.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(async () => {
    if (!userStore.token) {
        router.push('/login')
        return
    }

    try {
        const res: any = await getInfo()
        userStore.userInfo = res.user
        userStore.setPermissions(res.permissions)
    } catch (e) {
        userStore.logout()
        router.push('/login')
    }

    if (route.path.includes('dashboard')) activeKey.value = 'dashboard'
    else if (route.path.includes('employee')) activeKey.value = 'employee-list'
    
    loadNotifications()
    // 轮询通知
    setInterval(loadNotifications, 30000)
})

</script>

<style scoped>
.main-layout {
  height: 100vh;
  background: transparent;
}

.glass-sider {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
}


.sider-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.sider-scroll {
  flex: 1;
  overflow: hidden;
}

.logo {
  height: 64px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

.logo-sidebar {
  width: 32px;
  height: 32px;
  object-fit: contain;
  border-radius: 6px;
}

.logo-text {
  font-weight: 700;
  font-size: 16px;
  color: var(--text-primary);
}

.user-profile {
  flex-shrink: 0;
  margin: 12px;
  background: rgba(255, 255, 255, 0.5);
  padding: 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(255,255,255,0.3);
}

.user-avatar {
  font-size: 32px;
  color: var(--text-secondary);
  display: flex;
}

.user-info {
  flex: 1;
  overflow: hidden;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

.content-layout {
  background: transparent;
}

.glass-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: none !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 侧边栏菜单激活态精修 */
:deep(.n-menu .n-menu-item-content.n-menu-item-content--selected) {
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.1) 0%, rgba(99, 102, 241, 0) 100%);
  border-left: 3px solid var(--primary-color);
}

:deep(.n-menu .n-menu-item-content.n-menu-item-content--selected .n-menu-item-content-header) {
  color: var(--primary-color) !important;
  font-weight: 600;
}

:deep(.n-menu .n-menu-item-content .n-menu-item-content-header) {
  transition: all 0.3s;
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 通知面板样式 */
.notification-panel {
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 14px;
}

.notification-list {
  max-height: 340px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid rgba(0,0,0,0.02);
}

.notification-item:hover {
  background: rgba(99, 102, 241, 0.04);
}

.notification-item.unread {
  background: rgba(99, 102, 241, 0.02);
}

.notification-item.unread .item-title {
  font-weight: 700;
}

.item-icon {
  padding-top: 2px;
  font-size: 18px;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 13px;
  color: #1e293b;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
  margin-bottom: 6px;
}

.item-time {
  font-size: 11px;
  color: #94a3b8;
}

.glass-popover {
  backdrop-filter: blur(16px) !important;
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1) !important;
}

.skin-panel {
    padding: 12px;
}
.skin-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-top: 12px;
}
.skin-item {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.2s;
}
.skin-item:hover { transform: scale(1.1); }
.skin-item.active { box-shadow: 0 0 0 3px rgba(0,0,0,0.1); }
</style>

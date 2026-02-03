<template>
  <div class="profile-view-container">
    <div class="profile-header glass-effect">
      <div class="user-main">
        <div class="avatar-section" @click="triggerUpload">
          <n-avatar
            round
            :size="100"
            :src="userInfo?.avatar"
            :fallback-src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${userInfo?.nickName || 'User'}`"
          />
          <div class="avatar-overlay">
            <n-icon size="24"><CameraOutline /></n-icon>
            <span class="upload-text">点击上传</span>
          </div>
          <input type="file" ref="fileInputRef" style="display: none" accept="image/*" @change="handleFileChange" />
        </div>
        <div class="user-info">
          <div class="name-badge">
            <h1>{{ userInfo?.nickName || userInfo?.userName }}</h1>
            <n-tag type="primary" round ghost>{{ userInfo?.userType === '00' ? '超级管理员' : (fullProfile?.profile?.jobTitle || '正式员工') }}</n-tag>
          </div>
          <p class="email">{{ userInfo?.email || '未设置邮箱' }}</p>
          <p class="bio" v-if="userInfo?.remark">{{ userInfo?.remark }}</p>
          <p class="bio-placeholder" v-else>写点什么来介绍你自己吧...</p>
          <div class="quick-stats">
            <div class="stat-item">
              <span class="label">入职天数</span>
              <span class="value">{{ onboardDays }}</span>
            </div>
            <div class="divider"></div>
            <div class="stat-item">
              <span class="label">考勤率</span>
              <span class="value">98%</span>
            </div>
            <div class="divider"></div>
            <div class="stat-item">
              <span class="label">绩效等级</span>
              <span class="value">A</span>
            </div>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <n-space>
          <n-button type="primary" secondary round @click="openEditModal">
            <template #icon><n-icon><CreateOutline /></n-icon></template>
            编辑资料
          </n-button>
        </n-space>
      </div>
    </div>

    <n-grid :cols="12" :x-gap="24" :y-gap="24" class="content-grid" item-responsive responsive="screen">
      <!-- Left Column: Personal Info & Security -->
      <n-gi span="12 m:4">
        <div class="info-card glass-effect">
          <div class="card-header">
            <h3>个人详情</h3>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="label">账号名称</span>
              <span class="value">{{ userInfo?.userName }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系方式</span>
              <span class="value">{{ userInfo?.phonenumber || '未绑定手机号' }}</span>
            </div>
            <div class="info-item">
              <span class="label">性别</span>
              <span class="value">{{ userInfo?.sex === '0' ? '男' : (userInfo?.sex === '1' ? '女' : '未知') }}</span>
            </div>
            <div class="info-item">
              <span class="label">登录IP</span>
              <span class="value">{{ userInfo?.loginIp || '127.0.0.1' }}</span>
            </div>
            <div class="info-item">
              <span class="label">上次登录</span>
              <span class="value">{{ formatDate(userInfo?.loginDate) }}</span>
            </div>
          </div>

          <n-divider />

          <div class="card-header">
            <h3>账号安全</h3>
          </div>
          <div class="security-list">
            <div class="security-item">
              <div class="text">
                <span class="title">登录密码</span>
                <span class="desc">定期更换密码可以提高账号安全性</span>
              </div>
              <n-button text type="primary" @click="showPwdModal = true">修改</n-button>
            </div>
            <div class="security-item">
              <div class="text">
                <span class="title">两步验证 (MFA)</span>
                <span class="desc">{{ userInfo?.mfaEnabled === 1 ? '已开启，您的账户受到额外保护' : '未开启，启用后可大幅提升账户安全性' }}</span>
              </div>
              <div v-if="userInfo?.mfaEnabled === 1" style="display: flex; gap: 8px;">
                <n-tag type="success" size="small">已保护</n-tag>
                <n-button text type="error" size="small" @click="showDisableMfaModal = true">关闭</n-button>
              </div>
              <n-button v-else text type="primary" @click="handleSetupMfa">立即开启</n-button>
            </div>
            <div class="security-item">
              <div class="text">
                <span class="title">活动会话 ({{ activeSessions.length }})</span>
                <span class="desc">目前正在登录此账号的设备列表</span>
              </div>
              <n-button text type="primary" @click="showSessionModal = true">管理</n-button>
            </div>
          </div>
          
          <div class="security-score" style="margin-top: 24px; padding: 16px; background: rgba(16, 185, 129, 0.05); border-radius: 16px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                <span style="font-size: 13px; font-weight: 600; color: #10b981;">账户安全等级：{{ securityLevel }}</span>
                <span style="font-size: 18px; font-weight: 800; color: #10b981;">{{ securityScore }}</span>
            </div>
            <n-progress type="line" :percentage="securityScore" :show-indicator="false" color="#10b981" />
          </div>
          
          <n-divider />
          
          <n-button block ghost type="error" @click="handleLogout">
            <template #icon><n-icon><LogOutOutline /></n-icon></template>
            退出系统登录
          </n-button>
        </div>

        <div class="team-card glass-effect" style="margin-top: 24px; padding: 24px; border-radius: 24px;">
          <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3 style="margin: 0; font-size: 16px; font-weight: 700;">我的团队</h3>
            <n-tag size="small" round type="info">{{ colleagues.length }} 人</n-tag>
          </div>
          <div class="colleague-list" style="display: flex; flex-direction: column; gap: 12px;">
            <div 
              v-for="person in colleagues" 
              :key="person.id" 
              class="colleague-item"
              @click="router.push(`/employee/profile/${person.id}`)"
            >
              <n-avatar round size="small" :src="person.avatar" :fallback-src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${person.employeeName}`" />
              <div class="person-info">
                <span class="p-name">{{ person.employeeName }}</span>
                <span class="p-title">{{ person.jobTitle }}</span>
              </div>
              <n-icon class="arrow" color="#94a3b8"><ChevronForwardOutline /></n-icon>
            </div>
            <n-empty v-if="colleagues.length === 0" description="还没找到同部门的小伙伴" size="small" />
          </div>
        </div>
      </n-gi>

      <!-- Center & Right: Growth & Activity -->
      <n-gi span="12 m:8">
        <div class="tabs-card glass-effect">
          <n-tabs type="line" animated>
            <n-tab-pane name="growth" tab="成长历程">
              <div class="timeline-container">
                <div class="timeline-header" style="margin-bottom: 20px; display: flex; justify-content: flex-end;">
                  <n-button size="small" type="primary" dashed @click="showGrowthModal = true">
                    <template #icon><n-icon><AddOutline /></n-icon></template>
                    增加里程碑
                  </n-button>
                </div>
                <n-timeline>
                  <n-timeline-item
                    v-for="item in growthRecords"
                    :key="item.id"
                    :type="getGrowthType(item.eventType)"
                    :title="item.eventTitle"
                    :content="item.eventContent"
                    :time="formatDate(item.recordDate)"
                  />
                  <n-empty v-if="growthRecords.length === 0" description="暂无历程记录" />
                </n-timeline>
              </div>
            </n-tab-pane>
            <n-tab-pane name="skills" tab="技能矩阵">
              <div class="skills-container">
                <div class="pane-header">
                  <span>核心素质维度</span>
                  <n-button size="small" type="primary" dashed @click="showSkillModal = true">
                    <template #icon><n-icon><AddOutline /></n-icon></template>
                    添加技能
                  </n-button>
                </div>
                <div ref="radarChartRef" style="height: 400px; width: 100%"></div>
              </div>
            </n-tab-pane>
            <n-tab-pane name="activity" tab="近期活动">
              <div class="activity-list">
                <div class="activity-item">
                  <div class="dot success"></div>
                  <div class="activity-content">
                    <span class="time">今天 09:00</span>
                    <p>完成每日打卡，今日保持高效喔！</p>
                  </div>
                </div>
                <div class="activity-item">
                  <div class="dot info"></div>
                  <div class="activity-content">
                    <span class="time">昨天 14:20</span>
                    <p>您的考勤异常申请已通过审核。</p>
                  </div>
                </div>
                <div class="activity-item">
                  <div class="dot warning"></div>
                  <div class="activity-content">
                    <span class="time">2026-02-01</span>
                    <p>成功更新了个人技能矩阵，核心竞争力 +1</p>
                  </div>
                </div>
              </div>
            </n-tab-pane>
            <n-tab-pane name="security" tab="安全审计">
              <div class="security-logs-container">
                 <n-table :single-line="false" size="small">
                    <thead>
                        <tr>
                            <th>操作名称</th>
                            <th>请求地址</th>
                            <th>登录IP</th>
                            <th>时间</th>
                            <th>状态</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="log in securityLogs" :key="log.id">
                            <td>{{ log.title }}</td>
                            <td><n-text depth="3">{{ log.operUrl }}</n-text></td>
                            <td>{{ log.operIp }}</td>
                            <td>{{ formatDate(log.operTime) }}</td>
                            <td>
                                <n-tag :type="log.status === 1 ? 'success' : 'error'" size="small">
                                    {{ log.status === 1 ? '正常' : '异常' }}
                                </n-tag>
                            </td>
                        </tr>
                    </tbody>
                 </n-table>
                 <n-empty v-if="securityLogs.length === 0" description="暂无安全日志" />
              </div>
            </n-tab-pane>
          </n-tabs>
        </div>

        <div class="stats-grid">
            <div class="stat-card glass-effect purple">
              <div class="icon-wrap"><n-icon size="28"><TimeOutline /></n-icon></div>
              <div class="stat-data">
                <span class="label">本月考勤</span>
                <span class="count">{{ attendanceCount }} <small>天</small></span>
              </div>
           </div>
           <div class="stat-card glass-effect green" @click="router.push('/approvals')">
              <div class="icon-wrap"><n-icon size="28"><JournalOutline /></n-icon></div>
              <div class="stat-data">
                <span class="label">待处理审批</span>
                <span class="count">{{ pendingApprovals }} <small>个</small></span>
              </div>
           </div>
           <div class="stat-card glass-effect blue" @click="router.push('/document')">
              <div class="icon-wrap"><n-icon size="28"><DocumentTextOutline /></n-icon></div>
              <div class="stat-data">
                <span class="label">发布文档</span>
                <span class="count">{{ docCount }} <small>篇</small></span>
              </div>
           </div>
        </div>
      </n-gi>
    </n-grid>

    <!-- Edit Profile Modal -->
    <n-modal v-model:show="showEditModal" preset="card" title="编辑个人资料" style="width: 500px" class="glass-popover">
      <n-form :model="editForm" label-placement="left" label-width="80px">
        <n-form-item label="昵称">
          <n-input v-model:value="editForm.nickName" placeholder="请输入昵称" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="editForm.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="editForm.phonenumber" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="性别">
          <n-radio-group v-model:value="editForm.sex">
            <n-space>
              <n-radio value="0">男</n-radio>
              <n-radio value="1">女</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="个人简介">
          <n-input v-model:value="editForm.remark" type="textarea" placeholder="介绍一下你的职业方向或兴趣爱好..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEditModal = false">取消</n-button>
          <n-button type="primary" :loading="saveLoading" @click="handleSaveProfile">保存更改</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Change Password Modal -->
    <n-modal v-model:show="showPwdModal" preset="card" title="修改登录密码" style="width: 400px" class="glass-popover">
      <n-form :model="pwdForm" label-placement="left" label-width="100px">
        <n-form-item label="旧密码">
          <n-input type="password" v-model:value="pwdForm.oldPassword" show-password-on="click" />
        </n-form-item>
        <n-form-item label="新密码">
          <n-input type="password" v-model:value="pwdForm.newPassword" show-password-on="click" />
        </n-form-item>
        <n-form-item label="确认新密码">
          <n-input type="password" v-model:value="pwdForm.confirmPassword" show-password-on="click" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPwdModal = false">取消</n-button>
          <n-button type="primary" :loading="pwdLoading" @click="handleUpdatePwd">确认修改</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Add Skill Modal -->
    <n-modal v-model:show="showSkillModal" preset="card" title="添加专业技能" style="width: 400px" class="glass-popover">
      <n-form :model="skillForm" label-placement="left" label-width="80px">
        <n-form-item label="技能名称">
          <n-input v-model:value="skillForm.skillName" placeholder="如：Java, Vue, 团队协作" />
        </n-form-item>
        <n-form-item label="熟练度">
          <n-slider v-model:value="skillForm.skillLevel" :min="1" :max="5" :step="1" style="flex: 1" />
          <span style="margin-left: 12px; font-weight: bold; color: #6366f1">{{ skillForm.skillLevel }}</span>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showSkillModal = false">取消</n-button>
          <n-button type="primary" :loading="skillLoading" @click="handleAddSkill">确认添加</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Add Growth Modal -->
    <n-modal v-model:show="showGrowthModal" preset="card" title="记录成长时刻" style="width: 500px" class="glass-popover">
      <n-form :model="growthForm" label-placement="left" label-width="80px">
        <n-form-item label="事件标题">
          <n-input v-model:value="growthForm.eventTitle" placeholder="如：顺利入职, 晋升高级架构师" />
        </n-form-item>
        <n-form-item label="事件内容">
          <n-input v-model:value="growthForm.eventContent" type="textarea" placeholder="简述一下这个重要时刻..." />
        </n-form-item>
        <n-form-item label="事件类型">
          <n-select v-model:value="growthForm.eventType" :options="growthTypeOptions" />
        </n-form-item>
        <n-form-item label="发生日期">
          <n-date-picker v-model:value="growthForm.recordDate" type="date" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showGrowthModal = false">取消</n-button>
          <n-button type="primary" :loading="growthLoading" @click="handleAddGrowth">记录这一刻</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Sessions Modal -->
    <n-modal v-model:show="showSessionModal" preset="card" title="活动会话管理" style="width: 500px" class="glass-popover">
        <div v-if="activeSessions.length > 0" style="display: flex; flex-direction: column; gap: 16px;">
            <div v-for="sess in activeSessions" :key="sess.token" class="session-item" style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background: rgba(99,102,241,0.05); border-radius: 12px;">
                <div style="display: flex; align-items: center; gap: 12px;">
                    <n-icon size="24" color="#6366f1"><GlobeOutline /></n-icon>
                    <div>
                        <div style="font-weight: 600; font-size: 14px;">
                            {{ sess.isCurrent ? '当前设备' : '其他设备' }}
                            <n-tag v-if="sess.isCurrent" size="tiny" type="success" round style="margin-left: 8px;">主会话</n-tag>
                        </div>
                        <n-text depth="3" style="font-size: 12px;">Token: {{ sess.token.substring(0, 16) }}...</n-text>
                    </div>
                </div>
                <n-button v-if="!sess.isCurrent" size="small" type="error" ghost @click="handleKickout(sess.token)">强退</n-button>
            </div>
        </div>
        <n-empty v-else description="暂无其他会话" />
    </n-modal>

    <!-- MFA Setup Modal -->
    <n-modal v-model:show="showMfaSetupModal" preset="card" title="设置两步验证" style="width: 450px" class="glass-popover">
        <div style="text-align: center; padding: 10px;">
            <div style="margin-bottom: 16px;">
                <n-text depth="3">请使用 Google Authenticator 或其他身份验证器扫描下方二维码：</n-text>
            </div>
            <div v-if="mfaQrCodeDataUrl" style="background: white; padding: 20px; border-radius: 12px; display: inline-block; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                <img :src="mfaQrCodeDataUrl" alt="MFA QR Code" style="width: 200px; height: 200px;" />
            </div>
            <div style="margin-top: 16px; font-family: monospace; font-size: 16px; font-weight: bold; color: #6366f1;">
                密钥: {{ mfaSetupData.secret }}
            </div>
            <n-divider />
            <div style="text-align: left;">
                <n-form-item label="请输入 6 位验证码以确认绑定">
                    <n-input v-model:value="mfaSetupCode" placeholder="000000" maxlength="6" />
                </n-form-item>
            </div>
        </div>
        <template #footer>
            <n-button type="primary" block :loading="mfaSetupLoading" @click="handleEnableMfa">
                确认开启
            </n-button>
        </template>
    </n-modal>

    <!-- MFA Disable Modal -->
    <n-modal v-model:show="showDisableMfaModal" preset="dialog" title="关闭两步验证" positive-text="确认关闭" negative-text="取消" @positive-click="handleDisableMfa">
        <div style="padding: 16px 0;">
            <n-alert type="warning" title="安全提示" style="margin-bottom: 16px;">
                关闭两步验证将降低您的账户安全性，请确认是否继续。
            </n-alert>
            <n-form-item label="请输入当前的 6 位验证码以确认">
                <n-input v-model:value="disableMfaCode" placeholder="000000" maxlength="6" />
            </n-form-item>
        </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  NAvatar, NTag, NIcon, NButton, NGrid, NGi, NDivider, NTabs, NTabPane, 
  NTimeline, NTimelineItem, NEmpty, NModal, NForm, NFormItem, NInput,
  NRadioGroup, NRadio, NSpace, useMessage, NSlider, NSelect, NDatePicker,
  NTable, NText, NProgress
} from 'naive-ui'
import { 
  CameraOutline, CreateOutline, TimeOutline, JournalOutline, 
  DocumentTextOutline, ShieldCheckmarkOutline, LogOutOutline, AddOutline,
  ChevronForwardOutline, GlobeOutline
} from '@vicons/ionicons5'
import { useUserStore } from '../store/user'
import { getFullProfile, addSkill, addGrowth } from '../api/employeeProfile'
import { updateProfile, uploadFile } from '../api/user'
import { updatePassword, logout, getSecurityLogs, getSessions, kickout, setupMfa, enableMfa, disableMfa } from '../api/auth'
import { getAttendanceList } from '../api/attendance'
import { getLeaveList } from '../api/leave'
import { getEmployeeList } from '../api/employee'
import * as echarts from 'echarts'
import moment from 'moment'
import QRCode from 'qrcode'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const userInfo = computed(() => userStore.userInfo)

const onboardDays = computed(() => {
    const date = fullProfile.value?.profile?.entryDate || userInfo.value?.createTime
    if (!date) return 0
    return moment().diff(moment(date), 'days') + 1
})

const growthRecords = ref<any[]>([])
const skills = ref<any[]>([])
const colleagues = ref<any[]>([])
const fullProfile = ref<any>(null)
const radarChartRef = ref<HTMLElement | null>(null)
let myChart: any = null

/* --- Stats Data --- */
const attendanceCount = ref(0)
const pendingApprovals = ref(0)
const docCount = ref(0)
/* ------------------ */

/* --- Security Data --- */
const securityLogs = ref<any[]>([])
const activeSessions = ref<any[]>([])
const showSessionModal = ref(false)
const showMfaSetupModal = ref(false)
const mfaSetupLoading = ref(false)
const mfaSetupCode = ref('')
const mfaSetupData = reactive({
    secret: '',
    qrcodeUrl: ''
})
const mfaQrCodeDataUrl = ref('') // 二维码 Base64 数据 URL
const showDisableMfaModal = ref(false)
const disableMfaCode = ref('')
const securityScore = computed(() => {
    let score = 60
    if (userInfo.value?.phonenumber) score += 10
    if (userInfo.value?.email) score += 10
    if (userInfo.value?.mfaEnabled === 1) score += 10
    if (activeSessions.value.length === 1) score += 10
    return Math.min(score, 100)
})
const securityLevel = computed(() => {
    if (securityScore.value >= 90) return '极高'
    if (securityScore.value >= 70) return '良好'
    return '中等'
})
/* ------------------ */

const showEditModal = ref(false)
const saveLoading = ref(false)
const editForm = reactive({
    nickName: '',
    email: '',
    phonenumber: '',
    sex: '0',
    remark: ''
})

const openEditModal = () => {
    editForm.nickName = userInfo.value?.nickName || ''
    editForm.email = userInfo.value?.email || ''
    editForm.phonenumber = userInfo.value?.phonenumber || ''
    editForm.sex = userInfo.value?.sex || '0'
    editForm.remark = userInfo.value?.remark || ''
    showEditModal.value = true
}

// Skill Management
const showSkillModal = ref(false)
const skillLoading = ref(false)
const skillForm = reactive({
    skillName: '',
    skillLevel: 3
})

const handleAddSkill = async () => {
    if (!skillForm.skillName) return
    skillLoading.value = true
    try {
        await addSkill({
            userId: userInfo.value.id,
            skillName: skillForm.skillName,
            skillLevel: skillForm.skillLevel
        })
        message.success('技能添加成功')
        showSkillModal.value = false
        skillForm.skillName = ''
        loadData()
    } catch (e: any) {
        message.error(e.message || '添加失败')
    } finally {
        skillLoading.value = false
    }
}

// Growth Management
const showGrowthModal = ref(false)
const growthLoading = ref(false)
const growthForm = reactive({
    eventTitle: '',
    eventContent: '',
    eventType: 'project',
    recordDate: Date.now()
})

const growthTypeOptions = [
    { label: '入职/阶段', value: 'join' },
    { label: '职级晋升', value: 'promotion' },
    { label: '荣誉奖项', value: 'award' },
    { label: '重大项目', value: 'project' },
    { label: '技能培训', value: 'training' }
]

const handleAddGrowth = async () => {
    if (!growthForm.eventTitle) return
    growthLoading.value = true
    try {
        await addGrowth({
            userId: userInfo.value.id,
            ...growthForm,
            recordDate: moment(growthForm.recordDate).format('YYYY-MM-DD')
        })
        message.success('里程碑记录成功')
        showGrowthModal.value = false
        loadData()
    } catch (e: any) {
        message.error(e.message || '记录失败')
    } finally {
        growthLoading.value = false
    }
}

const fileInputRef = ref<HTMLInputElement | null>(null)
const triggerUpload = () => fileInputRef.value?.click()

const handleFileChange = async (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return
    
    if (file.size > 2 * 1024 * 1024) {
        message.warning('头像大小不能超过 2MB')
        return
    }

    const formData = new FormData()
    formData.append('file', file)
    
    try {
        const res: any = await uploadFile(formData)
        const avatarUrl = res.url
        
        // Immediately update via API
        await updateProfile({ avatar: avatarUrl })
        
        // Update local state
        userStore.userInfo.avatar = avatarUrl
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
        message.success('头像更新成功')
    } catch (e: any) {
        message.error(e.message || '头像上传失败')
    }
}

const handleSaveProfile = async () => {
    saveLoading.value = true
    try {
        await updateProfile(editForm)
        
        // Update local state
        Object.assign(userStore.userInfo, editForm)
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
        
        message.success('个人资料更新成功')
        showEditModal.value = false
    } catch (e: any) {
        message.error(e.message || '更新失败')
    } finally {
        saveLoading.value = false
    }
}

const showPwdModal = ref(false)
const pwdLoading = ref(false)
const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

const handleUpdatePwd = async () => {
    if (!pwdForm.oldPassword || !pwdForm.newPassword) {
        message.warning('请输入密码')
        return
    }
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
        message.warning('两次输入的新密码不一致')
        return
    }
    
    pwdLoading.value = true
    try {
        await updatePassword({
            oldPassword: pwdForm.oldPassword,
            newPassword: pwdForm.newPassword
        })
        message.success('密码修改成功，请重新登录')
        showPwdModal.value = false
        handleLogout()
    } catch (e: any) {
        message.error(e.message || '修改失败')
    } finally {
        pwdLoading.value = false
    }
}

const handleLogout = async () => {
    try {
        await logout()
    } finally {
        userStore.logout()
        message.success('已安全退出')
        router.push('/login')
    }
}

const formatDate = (d: any) => d ? moment(d).format('YYYY-MM-DD HH:mm') : '暂无记录'

const getGrowthType = (type: string) => {
  const map: any = {
    join: 'info',
    promotion: 'success',
    award: 'warning',
    project: 'primary',
    training: 'info'
  }
  return map[type] || 'info'
}

const loadData = async () => {
    if (!userInfo.value?.id) return
    try {
        const res: any = await getFullProfile(userInfo.value.id)
        fullProfile.value = res
        growthRecords.value = res.growth || []
        skills.value = res.skills || []
        
        // Fetch Stats
        const [attRes, leaveRes]: any = await Promise.all([
            getAttendanceList({ userId: userInfo.value.id, pageNum: 1, pageSize: 100 }),
            getLeaveList({ userId: userInfo.value.id, status: 0, pageNum: 1, pageSize: 1 })
        ])
        
        attendanceCount.value = attRes.total || 0
        pendingApprovals.value = leaveRes.total || 0
        docCount.value = 12 // Placeholder
        
        // Fetch Colleagues
        if (userInfo.value.deptId) {
            const collRes: any = await getEmployeeList({ deptId: userInfo.value.deptId, pageSize: 5 })
            colleagues.value = collRes.records?.filter((p: any) => p.userId !== userInfo.value.id) || []
        }

        // Fetch Security Data
        const [logsRes, sessRes]: any = await Promise.all([
            getSecurityLogs(),
            getSessions()
        ])
        securityLogs.value = logsRes || []
        activeSessions.value = sessRes || []
        
        nextTick(() => {
            initRadar()
        })
    } catch (e) {}
}

const handleKickout = async (token: string) => {
    try {
        await kickout(token)
        message.success('已强制断开该会话')
        const sessRes: any = await getSessions()
        activeSessions.value = sessRes || []
    } catch (e: any) {
        message.error(e.message || '操作失败')
    }
}

const handleSetupMfa = async () => {
    try {
        const res: any = await setupMfa()
        mfaSetupData.secret = res.secret
        mfaSetupData.qrcodeUrl = res.qrcodeUrl
        
        // 使用 qrcode 库生成二维码
        mfaQrCodeDataUrl.value = await QRCode.toDataURL(res.qrcodeUrl, {
            width: 200,
            margin: 2
        })
        
        showMfaSetupModal.value = true
    } catch (e: any) {
        message.error('无法获取 MFA 配置')
    }
}

const handleEnableMfa = async () => {
    if (!mfaSetupCode.value || mfaSetupCode.value.length !== 6) {
        message.warning('请输入 6 位验证码')
        return
    }
    mfaSetupLoading.value = true
    try {
        await enableMfa({
            secret: mfaSetupData.secret,
            code: mfaSetupCode.value
        })
        message.success('两步验证已成功开启')
        showMfaSetupModal.value = false
        // Update user store state
        userStore.setUserInfo({ ...userInfo.value, mfaEnabled: 1 })
    } catch (e: any) {
        message.error(e.message || '开启失败')
    } finally {
        mfaSetupLoading.value = false
    }
}

const handleDisableMfa = async () => {
    if (!disableMfaCode.value || disableMfaCode.value.length !== 6) {
        message.warning('请输入 6 位验证码')
        return false // 阻止对话框关闭
    }
    
    try {
        await disableMfa({ code: disableMfaCode.value })
        message.success('两步验证已关闭')
        showDisableMfaModal.value = false
        disableMfaCode.value = ''
        
        // 更新用户状态
        userStore.setUserInfo({ ...userInfo.value, mfaEnabled: 0 })
        return true
    } catch (e: any) {
        message.error(e.message || '关闭失败')
        return false // 阻止对话框关闭
    }
}

const initRadar = () => {
    if (!radarChartRef.value) return
    if (!myChart) myChart = echarts.init(radarChartRef.value)
    
    const indicator = skills.value.length > 0 
        ? skills.value.map(s => ({ name: s.skillName, max: 5 }))
        : [
            { name: '技术深度', max: 5 },
            { name: '团队协作', max: 5 },
            { name: '解决问题', max: 5 },
            { name: '学习能力', max: 5 },
            { name: '沟通水平', max: 5 }
        ]
    
    const dataValues = skills.value.length > 0
        ? skills.value.map(s => s.skillLevel)
        : [4, 4, 5, 4, 3]

    const option = {
        radar: {
            indicator: indicator,
            radius: '65%',
            splitArea: {
                areaStyle: {
                    color: ['rgba(99, 102, 241, 0.02)', 'rgba(99, 102, 241, 0.05)']
                }
            },
            axisName: {
                color: '#64748b'
            }
        },
        series: [{
            type: 'radar',
            data: [{
                value: dataValues,
                name: '个人能力',
                areaStyle: {
                    color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                        { offset: 0, color: 'rgba(99, 102, 241, 0.6)' },
                        { offset: 1, color: 'rgba(99, 102, 241, 0.1)' }
                    ])
                },
                lineStyle: {
                    color: '#6366f1',
                    width: 2
                },
                itemStyle: {
                    color: '#6366f1'
                }
            }]
        }]
    }
    myChart.setOption(option)
}

onMounted(() => {
    loadData()
    window.addEventListener('resize', () => myChart?.resize())
})
</script>

<style scoped>
.profile-view-container {
  padding: 0 20px 40px;
}

.glass-effect {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.profile-header {
  padding: 40px;
  border-radius: 24px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.user-main {
  display: flex;
  gap: 32px;
  align-items: center;
}

.avatar-section {
  position: relative;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-section:hover .avatar-overlay {
  opacity: 1;
}

.upload-text {
  position: absolute;
  bottom: 12px;
  font-size: 12px;
  font-weight: 600;
}

.glass-popover {
  backdrop-filter: blur(20px) !important;
  background: rgba(255, 255, 255, 0.8) !important;
  border-radius: 20px !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
}

.name-badge {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.name-badge h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e293b 0%, #6366f1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.email {
  color: #64748b;
  margin-bottom: 8px;
}

.bio {
    font-size: 13px;
    color: #475569;
    margin-bottom: 20px;
    line-height: 1.5;
    max-width: 500px;
    background: rgba(99, 102, 241, 0.05);
    padding: 8px 16px;
    border-radius: 12px;
    border-left: 4px solid #6366f1;
}

.bio-placeholder {
    font-size: 13px;
    color: #94a3b8;
    font-style: italic;
    margin-bottom: 20px;
}

.quick-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-item .label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.stat-item .value {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

.divider {
  width: 1px;
  height: 24px;
  background: #e2e8f0;
}

.content-grid {
  margin-top: 0;
}

.info-card, .tabs-card {
  padding: 30px;
  border-radius: 24px;
  height: 100%;
}

.card-header h3 {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
}

.info-item .label {
  color: #94a3b8;
}

.info-item .value {
  color: #1e293b;
  font-weight: 500;
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.security-item .title {
  display: block;
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}

.security-item .desc {
  font-size: 12px;
  color: #94a3b8;
}

.timeline-container {
  padding: 20px 10px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px;
}

.activity-item {
  display: flex;
  gap: 16px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-top: 6px;
}

.dot.success { background: #10b981; }
.dot.info { background: #6366f1; }
.dot.warning { background: #f59e0b; }

.activity-content .time {
  font-size: 12px;
  color: #94a3b8;
}

.activity-content p {
  margin: 4px 0 0;
  color: #475569;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.stat-card {
  padding: 24px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.stat-card.purple .icon-wrap { background: rgba(168, 85, 247, 0.1); color: #a855f7; }
.stat-card.green .icon-wrap { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.stat-card.blue .icon-wrap { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }

.stat-data {
  display: flex;
  flex-direction: column;
}

.stat-data .label {
  font-size: 12px;
  color: #94a3b8;
}

.stat-data .count {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
}

.stat-data .count small {
  font-size: 12px;
  font-weight: 400;
  color: #94a3b8;
}

/* Tab Overrides */
:deep(.n-tabs-tab-wrapper) {
  padding-bottom: 8px;
}
:deep(.n-tabs-tab) {
  font-weight: 600;
}

.security-logs-container {
    padding: 12px;
}

.session-item {
    transition: transform 0.2s;
}

.session-item:hover {
    transform: scale(1.01);
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pane-header span {
  font-weight: 600;
  color: #64748b;
}

.colleague-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.colleague-item:hover {
    background: rgba(99, 102, 241, 0.05);
}

.colleague-item .person-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.colleague-item .p-name {
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
}

.colleague-item .p-title {
    font-size: 11px;
    color: #94a3b8;
}

.colleague-item .arrow {
    opacity: 0;
    transition: opacity 0.2s;
}

.colleague-item:hover .arrow {
    opacity: 1;
}
</style>

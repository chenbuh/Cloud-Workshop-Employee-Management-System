<template>
  <div class="dashboard-container">
    <div class="welcome-banner">
      <h2>{{ greeting }}，{{ userStore.userInfo?.nickName || '用户' }} 👋</h2>
      <p>今天是 {{ currentDate }}，祝您拥有高效的一天。</p>
    </div>

    <!-- 核心指标卡片 -->
    <div class="stats-grid">
      <div v-for="(item, index) in stats" :key="index" class="stat-card glass-effect" :style="{ '--accent-color': item.color }">
        <div class="stat-icon-wrapper">
          <n-icon :component="item.icon" size="24" :color="item.color" />
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <n-number-animation :from="0" :to="item.value" v-if="typeof item.value === 'number'" />
            <span v-else>{{ item.value }}</span>
          </div>
          <div class="stat-label">{{ item.title }}</div>
        </div>
        <div class="stat-trend" :class="{ up: item.trend > 0, down: item.trend < 0 }">
          <n-icon :component="item.trend > 0 ? TrendingUpOutline : TrendingDownOutline" />
          {{ Math.abs(item.trend) }}%
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card glass-effect wide">
        <div class="card-header">
          <h3>员工增长趋势</h3>
          <n-select v-model:value="trendDays" :options="trendOptions" size="small" style="width: 100px" @update:value="handleTrendChange" />
        </div>
        <div ref="lineChartRef" class="chart-canvas"></div>
      </div>
      <div class="chart-card glass-effect narrow">
        <div class="card-header">
          <h3>部门人数分布</h3>
        </div>
        <div ref="pieChartRef" class="chart-canvas"></div>
      </div>
    </div>

    <!-- 快捷操作与动态 -->
    <div class="bottom-section">
      <div class="quick-actions glass-effect">
         <div class="card-header"><h3>快捷操作</h3></div>
         <div class="action-grid">
           <n-button strong secondary type="primary" @click="$router.push('/employee-list')">
             <template #icon><n-icon :component="PersonAddOutline" /></template>
             员工入职
           </n-button>
           <n-button strong secondary type="info" @click="$router.push('/dept')">
             <template #icon><n-icon :component="GitNetworkOutline" /></template>
             架构调整
           </n-button>
           <n-button strong secondary type="warning" @click="$router.push('/approvals')">
             <template #icon><n-icon :component="DocumentTextOutline" /></template>
             审批处理
           </n-button>
           <n-button strong secondary @click="$router.push('/settings')">
             <template #icon><n-icon :component="SettingsOutline" /></template>
             系统设置
           </n-button>
         </div>
      </div>

      <div class="culture-news glass-effect">
        <div class="card-header">
          <h3>企业文化动态</h3>
          <n-button text type="primary" size="small" @click="$router.push('/system/announcement')">管理</n-button>
        </div>
        <div class="news-list">
          <div v-for="item in newsList" :key="item.id" class="news-item" @click="handleViewNews(item)">
            <n-tag :type="getNewsType(item.type)" size="small" round>{{ item.type }}</n-tag>
            <span class="news-title">{{ item.title }}</span>
            <span class="news-time">{{ formatDate(item.publishTime) }}</span>
          </div>
        </div>
      </div>

      <div class="birthday-card glass-effect">
         <div class="card-header"><h3>近期入职纪念</h3></div>
         <div class="user-list">
           <div v-for="user in birthdayList" :key="user.id" class="user-item">
             <n-avatar round size="small" :src="`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.fullName}`" />
             <div class="user-meta">
               <div class="user-name">{{ user.fullName }}</div>
               <div class="user-dept">{{ user.deptName }}</div>
             </div>
             <n-tag size="tiny" type="info">{{ formatDate(user.entryDate) }}</n-tag>
           </div>
         </div>
      </div>
    </div>

    <n-modal v-model:show="showNewsModal" preset="card" :title="selectedNews?.title" style="width: 700px" class="glass-modal">
        <div class="markdown-body news-reader" v-html="renderMarkdown(selectedNews?.content || '')"></div>
        <template #footer>
            <div style="display: flex; justify-content: space-between; color: #94a3b8; font-size: 12px;">
                <span>发布类型: {{ selectedNews?.type }}</span>
                <span>发布时间: {{ selectedNews?.publishTime ? moment(selectedNews.publishTime).format('YYYY-MM-DD HH:mm') : '-' }}</span>
            </div>
        </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw } from 'vue'
import { NButton, NIcon, NNumberAnimation, NTag, NAvatar, NSelect } from 'naive-ui'
import { 
  PeopleOutline, 
  PersonAddOutline, 
  StatsChartOutline, 
  TrendingUpOutline, 
  TrendingDownOutline,
  GitNetworkOutline,
  DocumentTextOutline,
  SettingsOutline,
  BriefcaseOutline,
  CashOutline
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getDashboardStats, getGrowthTrend, getDeptDistribution, getNews, getUpcomingBirthdays } from '../api/dashboard'
import { useUserStore } from '../store/user'
import moment from 'moment'
import MarkdownIt from 'markdown-it'

const md = new MarkdownIt()
const renderMarkdown = (content: string) => md.render(content)

const userStore = useUserStore()
const currentDate = moment().format('LL dddd')

const getGreeting = () => {
    const hour = moment().hour()
    if (hour < 6) return '夜深了'
    if (hour < 9) return '早安'
    if (hour < 12) return '上午好'
    if (hour < 14) return '中午好'
    if (hour < 18) return '下午好'
    return '晚上好'
}
const greeting = getGreeting()

const stats = ref([
  { title: '总员工数', value: 0, trend: 12.5, icon: markRaw(PeopleOutline), color: '#6366f1' },
  { title: '本月入职', value: 0, trend: 5.2, icon: markRaw(PersonAddOutline), color: '#10b981' },
  { title: '待办审批', value: 0, trend: -2.4, icon: markRaw(BriefcaseOutline), color: '#f59e0b' },
  { title: '月度预支出', value: 0, trend: 8.1, icon: markRaw(CashOutline), color: '#ec4899' }
])

const lineChartRef = ref(null)
const pieChartRef = ref(null)
const newsList = ref<any[]>([])
const birthdayList = ref<any[]>([])

const showNewsModal = ref(false)
const selectedNews = ref<any>(null)

const loadStats = async () => {
    const res: any = await getDashboardStats()
    stats.value[0].value = res.totalEmployees
    stats.value[1].value = res.newHires
    stats.value[2].value = res.pendingApprovals
    stats.value[3].value = res.totalPayroll
}

const loadNews = async () => {
    newsList.value = await getNews() as any
}

const loadBirthdays = async () => {
    birthdayList.value = await getUpcomingBirthdays() as any
}

const getNewsType = (type: string) => {
    const map: any = { notice: 'info', event: 'warning', news: 'primary' }
    return map[type] || 'info'
}

const formatDate = (v: any) => moment(v).format('M月D日')

const handleViewNews = (item: any) => {
    selectedNews.value = item
    showNewsModal.value = true
}

const trendDays = ref(localStorage.getItem('dashboard_trend_days') ? Number(localStorage.getItem('dashboard_trend_days')) : 180)

const trendOptions = [
  { label: '近7天', value: 7 },
  { label: '近30天', value: 30 },
  { label: '近半年', value: 180 },
  { label: '近一年', value: 365 }
]
let lineChartInstance: any = null

const handleTrendChange = async (val: number) => {
    trendDays.value = val
    localStorage.setItem('dashboard_trend_days', String(val))
    
    if (!lineChartInstance) return
    
    lineChartInstance.showLoading({
        text: '加载中...',
        color: '#6366f1'
    })
    
    try {
        const res: any = await getGrowthTrend(val)
        lineChartInstance.setOption({
            xAxis: { data: res.dates },
            series: [{ data: res.counts }]
        })
    } finally {
        lineChartInstance.hideLoading()
    }
}

const initLineChart = async () => {
  lineChartInstance = echarts.init(lineChartRef.value)
  const res: any = await getGrowthTrend(trendDays.value)
  
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { 
        type: 'category', 
        data: res.dates,
        axisLine: { lineStyle: { color: '#94a3b8' } }
    },
    yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { type: 'dashed', color: '#e2e8f0' } },
        minInterval: 1 // Ensure integer ticks for counts
    },
    series: [{
      data: res.counts,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#6366f1' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(99, 102, 241, 0.5)' },
          { offset: 1, color: 'rgba(99, 102, 241, 0.0)' }
        ])
      }
    }]
  }
  lineChartInstance.setOption(option)
  window.addEventListener('resize', () => lineChartInstance.resize())
}

const initPieChart = async () => {
  const chart = echarts.init(pieChartRef.value)
  const res: any = await getDeptDistribution()
  
  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', left: 'center' },
    series: [
      {
        name: '部门分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: '18', fontWeight: 'bold' }
        },
        data: res
      }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

onMounted(() => {
  loadStats()
  loadNews()
  loadBirthdays()
  initLineChart()
  initPieChart()
})
</script>

<style scoped>
.dashboard-container {
  padding-bottom: 40px;
}

.welcome-banner {
  margin-bottom: 32px;
}

.welcome-banner h2 {
  font-size: 28px;
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 700;
}

.welcome-banner p {
  color: #64748b;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  padding: 24px;
  border-radius: 20px;
  background: white;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #f1f5f9; 
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 8px;
  border-radius: 12px;
  background: #f1f5f9;
  font-weight: 600;
}

.stat-trend.up { color: #10b981; background: rgba(16, 185, 129, 0.1); }
.stat-trend.down { color: #f59e0b; background: rgba(245, 158, 11, 0.1); }

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.chart-card {
  padding: 24px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.6);
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1e293b;
}

.chart-canvas {
  flex: 1;
  width: 100%;
}

/* Bottom Section */
.bottom-section {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 24px;
}

.quick-actions, .culture-news, .birthday-card {
    padding: 24px;
    border-radius: 24px;
    background: white;
    min-height: 280px;
    display: flex;
    flex-direction: column;
}

.action-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
}

.news-list, .user-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.news-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 0;
    border-bottom: 1px dashed #e2e8f0;
    cursor: pointer;
    transition: all 0.2s;
}

.news-item:hover {
    background: rgba(99, 102, 241, 0.05);
    padding-left: 8px;
    padding-right: 8px;
    border-radius: 8px;
}

.news-title {
    flex: 1;
    font-size: 14px;
    color: #334155;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.news-time {
    font-size: 12px;
    color: #94a3b8;
}

.user-item {
    display: flex;
    align-items: center;
    gap: 12px;
}

.user-meta {
    flex: 1;
}

.user-name {
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
}

.user-dept {
    font-size: 12px;
    color: #94a3b8;
}

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

/* Markdown Styling for News */
.news-reader {
    font-size: 15px;
    line-height: 1.8;
    color: #334155;
}

.markdown-body :deep(h1), .markdown-body :deep(h2), .markdown-body :deep(h3) {
    margin-top: 20px;
    margin-bottom: 12px;
    font-weight: 700;
}

.markdown-body :deep(img) {
    max-width: 100%;
}

.markdown-body :deep(code) {
    padding: 2px 4px;
    background-color: #f1f5f9;
    border-radius: 4px;
    font-family: monospace;
}
</style>

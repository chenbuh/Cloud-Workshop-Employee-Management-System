<template>
  <div class="analytics-container">
    <div class="header-section">
      <div class="title-group">
        <h2>行政资源分析看板</h2>
        <p class="subtitle">实时监控资产利用率与办公物资消耗趋势</p>
      </div>
      <n-button type="primary" secondary @click="loadData">
        <template #icon><n-icon :component="RefreshOutline" /></template>
        刷新数据
      </n-button>
    </div>

    <!-- 顶部概览 -->
    <n-grid :cols="4" :x-gap="24" style="margin-bottom: 24px">
        <n-gi v-for="(stat, idx) in summaryStats" :key="idx">
            <div class="stat-card glass-effect" :style="{ '--color': stat.color }">
                <div class="icon-box" :style="{ background: stat.color + '20' }">
                    <n-icon :component="stat.icon" :color="stat.color" size="24" />
                </div>
                <div class="stat-info">
                    <div class="label">{{ stat.label }}</div>
                    <div class="value">{{ stat.value }} <small>{{ stat.unit }}</small></div>
                </div>
            </div>
        </n-gi>
    </n-grid>

    <div class="charts-grid">
        <!-- 预约趋势图 -->
        <div class="chart-card glass-effect wide">
            <div class="card-header">
                <h3>会议室预约活跃度 (近7日)</h3>
                <n-tag type="info" size="small" round>趋势分析</n-tag>
            </div>
            <div ref="bookingTrendRef" class="chart-canvas"></div>
        </div>

        <!-- 资源热度排行 -->
        <div class="chart-card glass-effect narrow">
            <div class="card-header">
                <h3>热门资源预约排行</h3>
            </div>
            <div ref="resourcePopRef" class="chart-canvas"></div>
        </div>

        <!-- 资产申领状态分布 -->
        <div class="chart-card glass-effect narrow">
            <div class="card-header">
                <h3>物资申领处理效率</h3>
            </div>
            <div ref="assetClaimRef" class="chart-canvas"></div>
        </div>

         <!-- 库存周转率模拟数据 -->
         <div class="chart-card glass-effect wide">
            <div class="card-header">
                <h3>物资库存水位波动</h3>
            </div>
            <div ref="stockWaterRef" class="chart-canvas"></div>
        </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { NGrid, NGi, NButton, NIcon, NTag } from 'naive-ui'
import { 
    RefreshOutline, CalendarOutline, CartOutline, CubeOutline,
    PeopleOutline 
} from '@vicons/ionicons5'
import * as echarts from 'echarts'
import { getResourceAnalytics } from '../api/resource'
import { useAppStore } from '../store/app'

const appStore = useAppStore()
const loading = ref(false)
const rawData = ref<any>(null)

const bookingTrendRef = ref<HTMLElement | null>(null)
const resourcePopRef = ref<HTMLElement | null>(null)
const assetClaimRef = ref<HTMLElement | null>(null)
const stockWaterRef = ref<HTMLElement | null>(null)

let charts: any[] = []

const summaryStats = computed(() => [
    { label: '累计成功预约', value: rawData.value?.bookingTrend?.reduce((a:any, b:any) => a + b.count, 0) || 0, unit: '场', icon: CalendarOutline, color: '#6366f1' },
    { label: '活跃资源数', value: rawData.value?.resourcePopularity?.length || 0, unit: '个', icon: CubeOutline, color: '#10b981' },
    { label: '物资申领总量', value: rawData.value?.assetClaimStatus?.reduce((a:any, b:any) => a + b.count, 0) || 0, unit: '笔', icon: CartOutline, color: '#f59e0b' },
    { label: '平均审批时长', value: '2.4', unit: '小时', icon: PeopleOutline, color: '#ec4899' }
])

const loadData = async () => {
    loading.value = true
    try {
        const res: any = await getResourceAnalytics()
        rawData.value = res
        initCharts()
    } finally {
        loading.value = false
    }
}

const initCharts = () => {
    charts.forEach(c => c.dispose())
    charts = []

    // 1. Booking Trend
    if (bookingTrendRef.value) {
        const chart = echarts.init(bookingTrendRef.value)
        const trendData = rawData.value?.bookingTrend || []
        chart.setOption({
            tooltip: { trigger: 'axis' },
            grid: { top: '15%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { 
                type: 'category', 
                data: trendData.map((d: any) => d.date),
                axisLine: { lineStyle: { color: 'var(--text-secondary)' } }
            },
            yAxis: { 
                type: 'value',
                splitLine: { lineStyle: { type: 'dashed', color: 'var(--glass-border)' } }
            },
            series: [{
                data: trendData.map((d: any) => d.count),
                type: 'line',
                smooth: true,
                areaStyle: { 
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: appStore.skinColors[appStore.skin] + '80' },
                        { offset: 1, color: appStore.skinColors[appStore.skin] + '00' }
                    ])
                },
                itemStyle: { color: appStore.skinColors[appStore.skin] }
            }]
        })
        charts.push(chart)
    }

    // 2. Resource Popularity
    if (resourcePopRef.value) {
        const chart = echarts.init(resourcePopRef.value)
        const popData = rawData.value?.resourcePopularity || []
        chart.setOption({
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            grid: { top: '10%', left: '3%', right: '10%', bottom: '3%', containLabel: true },
            xAxis: { type: 'value', show: false },
            yAxis: { 
                type: 'category', 
                data: popData.map((d: any) => d.name).reverse(),
                axisLine: { show: false }
            },
            series: [{
                data: popData.map((d: any) => d.count).reverse(),
                type: 'bar',
                barWidth: '50%',
                itemStyle: { 
                    borderRadius: [0, 10, 10, 0],
                    color: appStore.skinColors[appStore.skin]
                },
                label: { show: true, position: 'right' }
            }]
        })
        charts.push(chart)
    }

    // 3. Asset Claim Status
    if (assetClaimRef.value) {
        const chart = echarts.init(assetClaimRef.value)
        const statusMap: any = { 0: '待处理', 1: '已发放', 2: '已驳回' }
        const colors = ['#f59e0b', '#10b981', '#ef4444']
        const claimData = (rawData.value?.assetClaimStatus || []).map((d: any, idx: number) => ({
            name: statusMap[d.status] || '未知',
            value: d.count,
            itemStyle: { color: colors[idx % 3] }
        }))
        chart.setOption({
            tooltip: { trigger: 'item' },
            legend: { bottom: '5%', left: 'center', textStyle: { color: 'var(--text-secondary)' } },
            series: [{
                type: 'pie',
                radius: ['40%', '70%'],
                center: ['50%', '45%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
                label: { show: false },
                data: claimData
            }]
        })
        charts.push(chart)
    }

    // 4. Stock Water (Mocked for better viz)
    if (stockWaterRef.value) {
        const chart = echarts.init(stockWaterRef.value)
        chart.setOption({
            tooltip: { trigger: 'axis' },
            grid: { top: '15%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: { 
                type: 'category', 
                data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                axisLine: { lineStyle: { color: 'var(--text-secondary)' } }
            },
            yAxis: { type: 'value' },
            series: [{
                name: '中性笔',
                type: 'bar',
                stack: 'total',
                data: [120, 132, 101, 134, 90, 230, 210],
                itemStyle: { color: '#6366f1' }
            }, {
                name: '复印纸',
                type: 'bar',
                stack: 'total',
                data: [220, 182, 191, 234, 290, 330, 310],
                itemStyle: { color: '#10b981' }
            }]
        })
        charts.push(chart)
    }
}

watch(() => appStore.skin, () => {
    setTimeout(initCharts, 100)
})

onMounted(() => {
    loadData()
    window.addEventListener('resize', () => charts.forEach(c => c.resize()))
})
</script>

<style scoped>
.analytics-container {
    padding: 24px;
}
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32px;
}
.title-group h2 { margin: 0; font-size: 24px; font-weight: 700; color: var(--text-primary); }
.subtitle { margin: 4px 0 0; color: var(--text-secondary); font-size: 14px; }

.stat-card {
    padding: 20px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
}
.icon-box {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}
.stat-info .label { font-size: 13px; color: var(--text-secondary); }
.stat-info .value { font-size: 20px; font-weight: 800; color: var(--text-primary); }
.stat-info small { font-size: 12px; font-weight: normal; margin-left: 4px; }

.charts-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
}
.chart-card {
    padding: 24px;
    border-radius: 24px;
    min-height: 350px;
    display: flex;
    flex-direction: column;
}
.chart-card.wide { grid-column: span 1; }
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.card-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: var(--text-primary); }
.chart-canvas {
    flex: 1;
    width: 100%;
}
</style>

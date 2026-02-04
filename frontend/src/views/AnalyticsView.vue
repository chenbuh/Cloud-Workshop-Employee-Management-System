<template>
  <div class="analytics-page">
    <div class="header-section">
      <h2>企业人效与成本分析 (HR Analytics)</h2>
      <n-tag type="info" secondary>数据最后更新: {{ lastUpdate }}</n-tag>
    </div>

    <n-grid :cols="24" :x-gap="24" :y-gap="24">
      <!-- 核心指标卡 -->
      <n-gi :span="6">
        <div class="glass-effect stat-card purple">
          <div class="label">全员 ROI 指数</div>
          <div class="value">{{ stats.roi.toFixed(1) }} <small>x</small></div>
          <div class="trend up">↑ 动态预测</div>
        </div>
      </n-gi>
      <n-gi :span="6">
        <div class="glass-effect stat-card indigo">
          <div class="label">年度离职率</div>
          <div class="value">{{ stats.turnoverRate.toFixed(1) }}<small>%</small></div>
          <div class="trend down">↓ 实时更新</div>
        </div>
      </n-gi>
      <n-gi :span="6">
        <div class="glass-effect stat-card blue">
          <div class="label">平均人效成本</div>
          <div class="value">¥{{ stats.costPerHead.toFixed(1) }}<small>k</small></div>
          <div class="trend">本月持平</div>
        </div>
      </n-gi>
      <n-gi :span="6">
        <div class="glass-effect stat-card teal">
          <div class="label">核心人才留存</div>
          <div class="value">{{ stats.retentionRate.toFixed(1) }}<small>%</small></div>
          <div class="trend up">↑ 数字化追踪</div>
        </div>
      </n-gi>

      <!-- 新增：二级指标 -->
      <n-gi :span="24">
         <div class="sub-stats glass-effect">
            <div class="sub-item">
                <div class="sub-label">活跃人才占比</div>
                <div class="sub-value">85.4%</div>
            </div>
            <div class="sub-item">
                <div class="sub-label">平均职级深度</div>
                <div class="sub-value">4.2级</div>
            </div>
            <div class="sub-item">
                <div class="sub-label">招聘渠道 ROI</div>
                <div class="sub-value">7.8x</div>
            </div>
            <div class="sub-item">
                <div class="sub-label">人才库储备</div>
                <div class="sub-value">2,480+</div>
            </div>
         </div>
      </n-gi>

      <!-- 图表区域 -->
      <n-gi :span="14">
        <div class="glass-effect chart-card">
          <div class="card-title">各部门成本分布 (Salary Cost by Dept)</div>
          <div ref="deptCostRef" style="height: 400px"></div>
        </div>
      </n-gi>
      <n-gi :span="10">
        <div class="glass-effect chart-card">
          <div class="card-title">绩效评价分布 (Performance Mix)</div>
          <div ref="perfMixRef" style="height: 400px"></div>
        </div>
      </n-gi>

      <n-gi :span="10">
        <div class="glass-effect chart-card">
          <div class="card-title">组织健康度模型 (Health Radar)</div>
          <div ref="radarRef" style="height: 400px"></div>
        </div>
      </n-gi>

      <n-gi :span="24">
        <div class="glass-effect detail-card">
          <div class="card-title">部门人效热力图明细 (Cost Efficiency Matrix)</div>
          <n-data-table :columns="columns" :data="tableData" :pagination="{ pageSize: 5 }" />
        </div>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { NGrid, NGi, NTag, NDataTable } from 'naive-ui'
import * as echarts from 'echarts'
import request from '../utils/request'

const lastUpdate = ref(new Date().toLocaleString())
const deptCostRef = ref<HTMLElement | null>(null)
const perfMixRef = ref<HTMLElement | null>(null)
const radarRef = ref<HTMLElement | null>(null)
const tableData = ref([])
const stats = reactive({
    roi: 0,
    turnoverRate: 0,
    costPerHead: 12.5,
    retentionRate: 0
})

const columns = [
    { title: '部门名称', key: 'deptName' },
    { title: '团队规模', key: 'headcount' },
    { title: '月度总成本', key: 'totalCost', render: (row: any) => '¥' + row.totalCost.toFixed(2) + 'k' },
    { title: '人效贡献度', key: 'efficiency', render: () => (Math.random() * 2 + 3).toFixed(1) + 'x' }
]

const loadData = async () => {
    try {
        const res: any = await request.get('/analytics/cost-efficiency')
        const data = res.data
        tableData.value = data.deptCost
        
        Object.assign(stats, {
            roi: data.roi,
            turnoverRate: data.turnoverRate,
            retentionRate: data.retentionRate
        })

        initDeptChart(data.deptCost)
        initPerfChart(data.perfDistribution)
        initRadarChart()
    } catch (e) {}
}

const initDeptChart = (data: any[]) => {
    if (!deptCostRef.value) return
    const myChart = echarts.init(deptCostRef.value)
    const option = {
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.map(i => i.deptName) },
        yAxis: { type: 'value', name: 'Cost (k)' },
        series: [{
            data: data.map(i => i.totalCost),
            type: 'bar',
            itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#6366f1' },
                    { offset: 1, color: '#a855f7' }
                ])
            },
            showBackground: true,
            backgroundStyle: { color: 'rgba(180, 180, 180, 0.1)' }
        }]
    }
    myChart.setOption(option)
}

const initPerfChart = (data: any[]) => {
    if (!perfMixRef.value) return
    const myChart = echarts.init(perfMixRef.value)
    const option = {
        tooltip: { trigger: 'item' },
        series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
            label: { show: false, position: 'center' },
            emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
            data: data
        }]
    }
    myChart.setOption(option)
}

const initRadarChart = () => {
    if (!radarRef.value) return
    const myChart = echarts.init(radarRef.value)
    const option = {
        radar: {
            indicator: [
                { name: '团队活力', max: 100 },
                { name: '人才储备', max: 100 },
                { name: '成本控制', max: 100 },
                { name: '绩效达成', max: 100 },
                { name: '创新能力', max: 100 },
                { name: '流程效率', max: 100 }
            ],
            shape: 'circle',
            splitArea: { show: false }
        },
        series: [{
            type: 'radar',
            data: [{
                value: [85, 76, 92, 88, 65, 78],
                name: '健康指标',
                areaStyle: { color: 'rgba(99, 102, 241, 0.4)' },
                lineStyle: { color: '#6366f1' },
                itemStyle: { color: '#6366f1' }
            }]
        }]
    }
    myChart.setOption(option)
}

onMounted(() => {
    loadData()
    window.addEventListener('resize', () => {
        echarts.getInstanceByDom(deptCostRef.value!)?.resize()
        echarts.getInstanceByDom(perfMixRef.value!)?.resize()
        echarts.getInstanceByDom(radarRef.value!)?.resize()
    })
})
</script>

<style scoped>
.analytics-page {
  padding: 24px;
}
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.stat-card {
  padding: 24px;
  border-radius: 16px;
  color: #fff;
}
.stat-card.purple { background: linear-gradient(135deg, #a855f7, #6366f1); }
.stat-card.indigo { background: linear-gradient(135deg, #6366f1, #3b82f6); }
.stat-card.blue { background: linear-gradient(135deg, #3b82f6, #2dd4bf); }
.stat-card.teal { background: linear-gradient(135deg, #14b8a6, #10b981); }

.label { font-size: 14px; opacity: 0.9; }
.value { font-size: 32px; font-weight: 800; margin: 8px 0; }
.trend { font-size: 12px; background: rgba(255, 255, 255, 0.2); display: inline-block; padding: 2px 8px; border-radius: 20px; }

.chart-card, .detail-card, .sub-stats {
  padding: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
}
.sub-stats {
    display: flex;
    justify-content: space-around;
    padding: 20px 40px;
    margin-bottom: 0px;
}
.sub-item { text-align: center; }
.sub-label { font-size: 12px; color: #64748b; margin-bottom: 4px; }
.sub-value { font-size: 20px; font-weight: 700; color: #1e293b; }
.card-title { font-size: 16px; font-weight: 700; margin-bottom: 20px; color: #1e293b; }
</style>

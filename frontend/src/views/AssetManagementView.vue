<template>
  <div class="asset-management-container">
    <div class="header-section">
      <div class="title-group">
        <h2>物资领用中心</h2>
        <p class="subtitle">办公设备申领与消耗品库存追踪</p>
      </div>
      <n-space>
        <n-button type="primary" secondary @click="loadData">
          <template #icon><n-icon :component="RefreshOutline" /></template>
          刷新
        </n-button>
        <n-button type="primary" @click="showClaimModal = true">
          <template #icon><n-icon :component="CartOutline" /></template>
          发起申领
        </n-button>
      </n-space>
    </div>

    <!-- 顶部统计 -->
    <n-grid :cols="4" :x-gap="24" style="margin-bottom: 24px">
        <n-gi v-for="(stat, idx) in stats" :key="idx">
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

    <n-tabs type="line" animated>
      <n-tab-pane name="inventory" tab="物资档案">
        <div class="glass-effect table-card">
          <div class="filter-bar">
            <n-radio-group v-model:value="filterCategory" size="small" @update:value="loadAssets">
              <n-radio-button value="">全部</n-radio-button>
              <n-radio-button value="固定资产">固定资产</n-radio-button>
              <n-radio-button value="办公耗材">办公耗材</n-radio-button>
              <n-radio-button value="办公家具">办公家具</n-radio-button>
            </n-radio-group>
            <n-input v-model:value="searchName" placeholder="搜索物资名称..." clearable style="width: 250px" />
          </div>
          <n-data-table
            :columns="assetColumns"
            :data="filteredAssets"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
          />
        </div>
      </n-tab-pane>

      <n-tab-pane name="claims" tab="申领进度">
        <div class="glass-effect table-card">
          <n-data-table
            :columns="claimColumns"
            :data="claims"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
          />
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 申领弹窗 -->
    <n-modal v-model:show="showClaimModal" preset="card" title="物资申领申请" style="width: 500px" class="glass-popover">
        <n-form :model="claimForm" label-placement="left" label-width="80px">
            <n-form-item label="申领物资">
                <n-select v-model:value="claimForm.assetId" :options="assetOptions" placeholder="请选择需要的物资" />
            </n-form-item>
            <n-form-item label="申报数量">
                <n-input-number v-model:value="claimForm.count" :min="1" style="width: 100%" />
            </n-form-item>
            <n-form-item label="用途说明">
                <n-input v-model:value="claimForm.reason" type="textarea" placeholder="请说明使用部门及具体用途..." />
            </n-form-item>
        </n-form>
        <template #footer>
            <n-space justify="end">
                <n-button @click="showClaimModal = false">取消</n-button>
                <n-button type="primary" @click="handleClaim" :loading="submitting">提交申请</n-button>
            </n-space>
        </template>
    </n-modal>

    <!-- 审批弹窗 -->
    <n-modal v-model:show="showApproveModal" preset="dialog" title="申领审批" positive-text="通过发放" negative-text="驳回申请" @positive-click="handleApprove(1)" @negative-click="handleApprove(2)">
        <div style="padding: 10px 0">
            <p>正在处理 <b>{{ currentClaim?.userName }}</b> 申领的 <b>{{ currentClaim?.assetName }} x {{ currentClaim?.count }}</b></p>
            <n-input v-model:value="approveRemark" type="textarea" placeholder="请输入审批备注 (可选)..." />
        </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, h } from 'vue'
import { 
  NGrid, NGi, NButton, NIcon, NSpace, NTag, NDataTable, NTabs, NTabPane, 
  NRadioButton, NRadioGroup, NModal, NForm, NFormItem, NInput, NSelect, 
  NInputNumber, useMessage
} from 'naive-ui'
import { 
    RefreshOutline, CartOutline, CubeOutline, TimeOutline,
    CheckmarkCircleOutline, CloseCircleOutline, HardwareChipOutline,
    PrintOutline
} from '@vicons/ionicons5'
import { getAssetList, getClaimList, createClaim, approveClaim } from '../api/asset'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const assets = ref<any[]>([])
const claims = ref<any[]>([])
const filterCategory = ref('')
const searchName = ref('')

const showClaimModal = ref(false)
const showApproveModal = ref(false)
const currentClaim = ref<any>(null)
const approveRemark = ref('')

const claimForm = reactive({
    assetId: null,
    count: 1,
    reason: ''
})

const stats = computed(() => [
    { label: '在库物资', value: assets.value.length, unit: '种', icon: CubeOutline, color: '#6366f1' },
    { label: '待办申领', value: claims.value.filter(c => c.status === 0).length, unit: '笔', icon: TimeOutline, color: '#f59e0b' },
    { label: '库存预警', value: assets.value.filter(a => a.stock < 5).length, unit: '项', icon: HardwareChipOutline, color: '#ef4444' },
    { label: '本月发放', value: claims.value.filter(c => c.status === 1).length, unit: '次', icon: PrintOutline, color: '#10b981' }
])

const assetOptions = computed(() => {
    return assets.value.map(a => ({ label: `${a.name} (库存: ${a.stock}${a.unit})`, value: a.id, disabled: a.stock <= 0 }))
})

const filteredAssets = computed(() => {
    let res = assets.value
    if (filterCategory.value) res = res.filter(a => a.category === filterCategory.value)
    if (searchName.value) res = res.filter(a => a.name.includes(searchName.value))
    return res
})

const assetColumns = [
    { title: '物资名称', key: 'name', render: (row: any) => h('div', { style: 'font-weight: 700' }, row.name) },
    { title: '分类', key: 'category', render: (row: any) => h(NTag, { size: 'small', ghost: true }, { default: () => row.category }) },
    { title: '规格', key: 'spec' },
    { title: '库存量', key: 'stock', render: (row: any) => h('span', { style: row.stock < 5 ? 'color: #ef4444; font-weight: bold' : '' }, `${row.stock} ${row.unit}`) },
    { title: '存放地点', key: 'location' }
]

const claimColumns = [
    { title: '申领人', key: 'userName' },
    { title: '物资名称', key: 'assetName' },
    { title: '申请数量', key: 'count', render: (row: any) => `${row.count} ${row.unit}` },
    { title: '申请原因', key: 'reason', ellipsis: true },
    { title: '申请时间', key: 'claimTime', render: (row: any) => moment(row.claimTime).format('MM-DD HH:mm') },
    { title: '状态', key: 'status', render: (row: any) => {
        const maps: any = {
            0: { label: '待审核', type: 'warning' },
            1: { label: '已发放', type: 'success' },
            2: { label: '已驳回', type: 'error' }
        }
        const item = maps[row.status]
        return h(NTag, { type: item.type, size: 'small' }, { default: () => item.label })
    }},
    { title: '操作', key: 'actions', render: (row: any) => {
        if (row.status !== 0) return '-'
        return h(NButton, { size: 'tiny', type: 'primary', onClick: () => openApprove(row) }, { default: () => '审批' })
    }}
]

const loadAssets = async () => {
    loading.value = true
    try {
        assets.value = await getAssetList({ category: filterCategory.value }) as any
    } finally {
        loading.value = false
    }
}

const loadData = async () => {
    loading.value = true
    try {
        const [aRes, cRes]: any = await Promise.all([
            getAssetList(),
            getClaimList()
        ])
        assets.value = aRes
        claims.value = cRes
    } finally {
        loading.value = false
    }
}

const handleClaim = async () => {
    if (!claimForm.assetId || !claimForm.reason) return message.warning('请填写完整申领信息')
    submitting.value = true
    try {
        await createClaim(claimForm)
        message.success('申请已提交')
        showClaimModal.value = false
        loadData()
    } catch(e: any) {
        message.error(e.message || '申请失败')
    } finally {
        submitting.value = false
    }
}

const openApprove = (row: any) => {
    currentClaim.value = row
    approveRemark.value = ''
    showApproveModal.value = true
}

const handleApprove = async (status: number) => {
    try {
        await approveClaim({
            id: currentClaim.value.id,
            status,
            remark: approveRemark.value
        })
        message.success(status === 1 ? '已同意发放' : '已驳回申请')
        loadData()
    } catch(e) {}
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.asset-management-container {
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
    background: var(--glass-bg);
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

.table-card {
    padding: 24px;
    border-radius: 24px;
    min-height: 500px;
}
.filter-bar {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}
</style>

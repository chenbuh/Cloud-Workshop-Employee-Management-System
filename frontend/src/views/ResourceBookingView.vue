<template>
  <div class="resource-booking-container">
    <div class="header-section">
      <div class="title-group">
        <h2>行政资源管理</h2>
        <p class="subtitle">会议室预约与设备资源调度</p>
      </div>
      <n-space>
        <n-button type="primary" secondary @click="loadData">
          <template #icon><n-icon :component="RefreshOutline" /></template>
          刷新
        </n-button>
        <n-button type="primary" @click="showBookingModal = true">
          <template #icon><n-icon :component="AddOutline" /></template>
          立即预约
        </n-button>
      </n-space>
    </div>

    <n-grid :cols="24" :x-gap="24" :y-gap="24">
      <!-- 左侧：会议室概览 -->
      <n-gi :span="6">
        <div class="glass-effect resource-sidebar">
          <h3 class="card-title">可选资源</h3>
          <div class="resource-list">
            <div 
              v-for="res in resources" 
              :key="res.id" 
              class="resource-card"
              :class="{ active: selectedResourceId === res.id }"
              @click="selectedResourceId = res.id"
            >
              <div class="res-info">
                <span class="res-name">{{ res.name }}</span>
                <n-tag size="tiny" :type="res.status === 1 ? 'success' : 'error'" round>
                   {{ res.status === 1 ? '可用' : '维护中' }}
                </n-tag>
              </div>
              <div class="res-meta">
                <span><n-icon :component="PeopleOutline" /> {{ res.capacity }}人</span>
                <span><n-icon :component="LocationOutline" /> {{ res.location }}</span>
              </div>
            </div>
          </div>
        </div>
      </n-gi>

      <!-- 中间：日历视图 -->
      <n-gi :span="18">
        <div class="glass-effect main-calendar-card">
          <div class="calendar-header">
            <h3>预约看板</h3>
            <n-radio-group v-model:value="displayMode" size="small">
              <n-radio-button value="calendar">日历视图</n-radio-button>
              <n-radio-button value="list">列表视图</n-radio-button>
            </n-radio-group>
          </div>

          <div v-if="displayMode === 'calendar'" class="calendar-wrapper">
             <n-calendar
                v-model:value="calendarValue"
                #="{ year, month, date }"
             >
                <div 
                    v-for="booking in getBookingsByDate(year, month, date)" 
                    :key="booking.id" 
                    class="booking-tag"
                    :class="{ cancelled: booking.status === 0 }"
                    @click="viewBookingDetail(booking)"
                >
                    <span class="time">{{ formatTimeShort(booking.startTime) }}</span>
                    <span class="title">{{ booking.title }}</span>
                </div>
             </n-calendar>
          </div>

          <div v-else class="list-wrapper">
             <n-data-table
                :columns="columns"
                :data="filteredBookings"
                :loading="loading"
                :pagination="{ pageSize: 8 }"
             />
          </div>
        </div>
      </n-gi>
    </n-grid>

    <!-- 预约弹窗 -->
    <n-modal v-model:show="showBookingModal" preset="card" title="资源预约申请" style="width: 500px" class="glass-popover">
        <n-form :model="bookingForm" label-placement="left" label-width="80px">
            <n-form-item label="资源">
                <n-select v-model:value="bookingForm.resourceId" :options="resourceOptions" placeholder="请选择会议室/设备" />
            </n-form-item>
            <n-form-item label="用途">
                <n-input v-model:value="bookingForm.title" placeholder="如：XX项目周会, 客户接待" />
            </n-form-item>
            <n-form-item label="时间">
                <n-date-picker v-model:value="bookingForm.range" type="datetimerange" clearable style="width: 100%" />
            </n-form-item>
            <n-form-item label="备注">
                <n-input v-model:value="bookingForm.remark" type="textarea" placeholder="备注信息..." />
            </n-form-item>
        </n-form>
        <template #footer>
            <n-space justify="end">
                <n-button @click="showBookingModal = false">取消</n-button>
                <n-button type="primary" @click="handleBooking" :loading="submitting">确认预约</n-button>
            </n-space>
        </template>
    </n-modal>

    <!-- 详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="预约详情" style="width: 400px">
        <div v-if="currentBooking" class="booking-detail">
            <n-descriptions bordered :column="1" label-placement="left">
                <n-descriptions-item label="会议主题">{{ currentBooking.title }}</n-descriptions-item>
                <n-descriptions-item label="预约人">{{ currentBooking.userName }}</n-descriptions-item>
                <n-descriptions-item label="资源">{{ currentBooking.resourceName }}</n-descriptions-item>
                <n-descriptions-item label="时间">
                    {{ formatDateTime(currentBooking.startTime) }} <br/>至<br/> {{ formatDateTime(currentBooking.endTime) }}
                </n-descriptions-item>
                <n-descriptions-item label="备注" v-if="currentBooking.remark">{{ currentBooking.remark }}</n-descriptions-item>
            </n-descriptions>
            <div style="margin-top: 20px; text-align: center;" v-if="currentBooking.status === 1">
                <n-button type="error" ghost @click="handleCancel(currentBooking.id)">取消预约</n-button>
            </div>
        </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, h } from 'vue'
import { 
  NGrid, NGi, NButton, NIcon, NSpace, NTag, NCalendar, NDataTable, NRadioButton, 
  NRadioGroup, NModal, NForm, NFormItem, NInput, NSelect, NDatePicker, useMessage,
  NDescriptions, NDescriptionsItem
} from 'naive-ui'
import { 
    RefreshOutline, AddOutline, PeopleOutline, LocationOutline,
    CloseCircleOutline, CheckmarkCircleOutline
} from '@vicons/ionicons5'
import { getResourceList, getBookingList, createBooking, cancelBooking } from '../api/resource'
import moment from 'moment'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const displayMode = ref('calendar')
const calendarValue = ref(Date.now())

const resources = ref<any[]>([])
const bookings = ref<any[]>([])
const selectedResourceId = ref<number | null>(null)

const showBookingModal = ref(false)
const showDetailModal = ref(false)
const currentBooking = ref<any>(null)

const bookingForm = reactive({
    resourceId: null,
    title: '',
    range: null as [number, number] | null,
    remark: ''
})

const resourceOptions = computed(() => {
    return resources.value.map(r => ({ label: r.name + ' (' + r.location + ')', value: r.id }))
})

const filteredBookings = computed(() => {
    if (!selectedResourceId.value) return bookings.value
    return bookings.value.filter(b => b.resourceId === selectedResourceId.value)
})

const columns = [
    { title: '主题', key: 'title' },
    { title: '资源', key: 'resourceName' },
    { title: '预约人', key: 'userName' },
    { title: '开始时间', key: 'startTime', render: (row: any) => formatDateTime(row.startTime) },
    { title: '时长', key: 'duration', render: (row: any) => calculateDuration(row.startTime, row.endTime) },
    { title: '状态', key: 'status', render: (row: any) => h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '预约中' : '已取消' }) }
]

const loadData = async () => {
    loading.value = true
    try {
        const [resRes, bookRes]: any = await Promise.all([
            getResourceList(),
            getBookingList()
        ])
        resources.value = resRes
        bookings.value = bookRes
    } catch(e) {} finally {
        loading.value = false
    }
}

const handleBooking = async () => {
    if (!bookingForm.resourceId || !bookingForm.title || !bookingForm.range) {
        return message.warning('请填写完整预约信息')
    }
    submitting.value = true
    try {
        await createBooking({
            resourceId: bookingForm.resourceId,
            title: bookingForm.title,
            startTime: bookingForm.range[0],
            endTime: bookingForm.range[1],
            remark: bookingForm.remark
        })
        message.success('预约成功')
        showBookingModal.value = false
        loadData()
    } catch(e: any) {
        message.error(e.message || '预约失败')
    } finally {
        submitting.value = false
    }
}

const handleCancel = async (id: number) => {
    try {
        await cancelBooking(id)
        message.success('已取消预约')
        showDetailModal.value = false
        loadData()
    } catch(e) {}
}

const getBookingsByDate = (year: number, month: number, date: number) => {
    return filteredBookings.value.filter(b => {
        const d = new Date(b.startTime)
        return d.getFullYear() === year && (d.getMonth() + 1) === month && d.getDate() === date
    })
}

const viewBookingDetail = (b: any) => {
    currentBooking.value = b
    showDetailModal.value = true
}

const formatDateTime = (ts: any) => moment(ts).format('MM-DD HH:mm')
const formatTimeShort = (ts: any) => moment(ts).format('HH:mm')
const calculateDuration = (start: any, end: any) => {
    const min = moment(end).diff(moment(start), 'minutes')
    if (min < 60) return min + ' 分'
    return (min / 60).toFixed(1) + ' 时'
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.resource-booking-container {
    padding: 24px;
    height: 100%;
}
.header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.title-group h2 { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.subtitle { margin: 4px 0 0; color: #64748b; font-size: 14px; }

.resource-sidebar {
    padding: 20px;
    height: 100%;
    border-radius: 20px;
}
.card-title { font-size: 16px; font-weight: 700; margin-bottom: 20px; color: #1e293b; }

.resource-list { display: flex; flex-direction: column; gap: 12px; }
.resource-card {
    padding: 16px;
    border-radius: 12px;
    background: white;
    box-shadow: 0 4px 10px rgba(0,0,0,0.02);
    border: 2px solid transparent;
    cursor: pointer;
    transition: all 0.2s;
}
.resource-card:hover { transform: translateY(-2px); box-shadow: 0 8px 15px rgba(0,0,0,0.05); }
.resource-card.active { border-color: #6366f1; background: rgba(99, 102, 241, 0.02); }

.res-info { display: flex; justify-content: space-between; margin-bottom: 8px; }
.res-name { font-weight: 700; font-size: 15px; color: #1e293b; }
.res-meta { display: flex; gap: 12px; font-size: 12px; color: #64748b; }
.res-meta span { display: flex; align-items: center; gap: 4px; }

.main-calendar-card {
    padding: 24px;
    border-radius: 24px;
    height: 100%;
}
.calendar-header { display: flex; justify-content: space-between; items-center; margin-bottom: 20px; }
.calendar-header h3 { margin: 0; font-size: 18px; }

.booking-tag {
    background: #6366f1;
    color: white;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 11px;
    margin-bottom: 2px;
    cursor: pointer;
    display: flex;
    gap: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.booking-tag.cancelled {
    background: #f1f5f9;
    color: #94a3b8;
    text-decoration: line-through;
}
.booking-tag .time { font-weight: bold; opacity: 0.9; }

:deep(.n-calendar .n-calendar-cell) {
    min-height: 100px;
}
</style>

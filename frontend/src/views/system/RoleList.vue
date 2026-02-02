<template>
  <div class="role-management-page">
    <div class="header-action glass-effect">
      <div class="title">角色与权限</div>
      <n-button type="info" @click="showAddModal = true">
         <template #icon><n-icon :component="ShieldCheckmarkOutline" /></template>
         新增角色
      </n-button>
    </div>

    <div class="content-table glass-effect">
      <n-data-table 
        :columns="columns" 
        :data="roles" 
        :loading="loading"
        :bordered="false"
      />
    </div>

    <!-- 新增角色弹窗 -->
    <n-modal v-model:show="showAddModal" preset="card" title="新增角色" style="width: 500px">
      <n-form :model="roleForm" label-placement="left" label-width="80px">
        <n-form-item label="角色名称">
          <n-input v-model:value="roleForm.roleName" placeholder="如：财务专员" />
        </n-form-item>
        <n-form-item label="权限标识">
          <n-input v-model:value="roleForm.roleKey" placeholder="如：finance" />
        </n-form-item>
        <n-form-item label="排序">
          <n-input-number v-model:value="roleForm.roleSort" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="info" block @click="handleCreate">确认保存</n-button>
      </template>
    </n-modal>

    <!-- 权限分配弹窗 -->
    <n-modal v-model:show="showPermissionModal" preset="card" title="分配权限" style="width: 600px">
      <div v-if="menuLoading" style="padding: 40px; text-align: center">
        <n-spin size="large" />
      </div>
      <div v-else class="permission-tree-container">
        <div class="role-info-banner">正在配置：{{ activeRole?.roleName }}</div>
        <n-tree
          block-line
          cascade
          checkable
          check-strategy="all"
          :data="menuTree"
          :default-checked-keys="checkedMenuIds"
          key-field="menuId"
          label-field="menuName"
          children-field="children"
          @update:checked-keys="handleCheckedUpdate"
          style="max-height: 400px; overflow-y: auto"
        />
      </div>
      <template #footer>
        <n-space justify="end">
            <n-button @click="showPermissionModal = false">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="submitPermissions">保存设置</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue'
import { 
  NButton, NDataTable, NIcon, NModal, NForm, NFormItem, NInput, NInputNumber, 
  NTag, NTree, NSpin, NSpace, useMessage, useDialog
} from 'naive-ui'
import { ShieldCheckmarkOutline, TrashOutline, SettingsOutline } from '@vicons/ionicons5'
import { getRoleList, createRole, deleteRole, getMenuTree, getRoleMenuIds, assignRoleMenus } from '../../api/system'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const menuLoading = ref(false)
const submitting = ref(false)
const roles = ref([])
const showAddModal = ref(false)
const showPermissionModal = ref(false)

const menuTree = ref([])
const checkedMenuIds = ref<number[]>([])
const activeRole = ref<any>(null)

const roleForm = ref({
  roleName: '',
  roleKey: '',
  roleSort: 10
})

const columns = [
  { title: '角色ID', key: 'roleId' },
  { title: '角色名称', key: 'roleName' },
  { 
    title: '权限标识', 
    key: 'roleKey',
    render(row: any) {
        return h(NTag, { type: 'info', ghost: true }, { default: () => row.roleKey })
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row: any) {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              quaternary: true,
              type: 'info',
              onClick: () => handleOpenPermission(row)
            },
            { icon: () => h(NIcon, { component: SettingsOutline }), default: () => '权限' }
          ),
          h(
            NButton,
            {
              size: 'small',
              quaternary: true,
              type: 'error',
              disabled: row.roleKey === 'admin',
              onClick: () => handleDelete(row.roleId)
            },
            { icon: () => h(NIcon, { component: TrashOutline }) }
          )
        ]
      })
    }
  }
]

const loadRoles = async () => {
    loading.value = true
    try {
        roles.value = await getRoleList() as any
    } finally {
        loading.value = false
    }
}

const handleCreate = async () => {
    await createRole(roleForm.value)
    message.success('角色创建成功')
    showAddModal.value = false
    loadRoles()
}

const handleOpenPermission = async (role: any) => {
    activeRole.value = role
    showPermissionModal.value = true
    menuLoading.value = true
    try {
        const [tree, ids]: any = await Promise.all([
            getMenuTree(),
            getRoleMenuIds(role.roleId)
        ])
        menuTree.value = tree
        checkedMenuIds.value = ids
    } finally {
        menuLoading.value = false
    }
}

const handleCheckedUpdate = (keys: number[]) => {
    checkedMenuIds.value = keys
}

const submitPermissions = async () => {
    submitting.value = true
    try {
        await assignRoleMenus({
            roleId: activeRole.value.roleId,
            menuIds: checkedMenuIds.value
        })
        message.success('权限保存成功')
        showPermissionModal.value = false
    } finally {
        submitting.value = false
    }
}

const handleDelete = (id: number) => {
    dialog.warning({
        title: '确认删除',
        content: '确定要删除该角色吗？',
        positiveText: '确定',
        negativeText: '取消',
        onPositiveClick: async () => {
            await deleteRole(id)
            message.success('已删除')
            loadRoles()
        }
    })
}

onMounted(loadRoles)
</script>

<style scoped>
.role-management-page {
  padding: 24px;
}

.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 24px;
  margin-bottom: 24px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.content-table {
  padding: 24px;
  background: white;
  border-radius: 24px;
}

.permission-tree-container {
    padding: 10px;
}

.role-info-banner {
    padding: 12px;
    background: rgba(99, 102, 241, 0.05);
    border-radius: 12px;
    margin-bottom: 16px;
    color: #6366f1;
    font-weight: 600;
}

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}
</style>

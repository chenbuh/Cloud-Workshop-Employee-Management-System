<template>
  <div class="user-management-page">
    <div class="header-action glass-effect">
      <div class="title">账号管理</div>
      <n-button type="primary" @click="showAddModal = true">
         <template #icon><n-icon :component="PersonAddOutline" /></template>
         新建账号
      </n-button>
    </div>

    <div class="content-table glass-effect">
      <n-data-table 
        :columns="columns" 
        :data="users" 
        :loading="loading"
        :bordered="false"
      />
    </div>

    <n-modal v-model:show="showAddModal" preset="card" title="新建系统账号" style="width: 500px">
      <n-form :model="userForm" label-placement="left" label-width="80px">
        <n-form-item label="用户名">
          <n-input v-model:value="userForm.userName" placeholder="用于登录" />
        </n-form-item>
        <n-form-item label="昵称">
          <n-input v-model:value="userForm.nickName" placeholder="显示名称（支持中文）" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="userForm.password" type="password" show-password-on="click" />
        </n-form-item>
        <n-form-item label="类型">
          <n-select v-model:value="userForm.userType" :options="userTypeOptions" />
        </n-form-item>
        <n-form-item label="分配角色">
           <n-select 
             v-model:value="userForm.roleIds" 
             multiple 
             :options="roleOptions" 
             label-field="roleName"
             value-field="roleId"
             placeholder="请选择该账号的角色权限"
           />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" block @click="handleCreate">确认创建</n-button>
      </template>
    </n-modal>

    <!-- 自定义权限弹窗 -->
    <n-modal v-model:show="showPermissionModal" preset="card" title="自定义权限" style="width: 600px">
      <div v-if="menuLoading" style="padding: 40px; text-align: center">
        <n-spin size="large" />
      </div>
      <div v-else class="permission-tree-container">
        <div class="role-info-banner">正在配置：{{ activeUser?.nickName || activeUser?.userName }}</div>
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
  NButton, NDataTable, NIcon, NModal, NForm, NFormItem, NInput, NSelect, 
  NTag, NTree, NSpin, NSpace, useMessage, useDialog
} from 'naive-ui'
import { PersonAddOutline, TrashOutline, SettingsOutline } from '@vicons/ionicons5'
import { getRoleList, getUserList, createUser, deleteUser, getMenuTree, getUserMenuIds, assignUserMenus } from '../../api/system'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const menuLoading = ref(false)
const submitting = ref(false)
const users = ref([])
const showAddModal = ref(false)
const showPermissionModal = ref(false)
const roleOptions = ref([])
const menuTree = ref([])
const checkedMenuIds = ref<number[]>([])
const activeUser = ref<any>(null)

const userForm = ref({
  userName: '',
  nickName: '',
  password: '',
  userType: '00',
  roleIds: []
})

const userTypeOptions = [
  { label: '系统管理员', value: '00' },
  { label: '普通账号', value: '11' }
]

const columns = [
  { title: 'ID', key: 'id' },
  { title: '用户名', key: 'userName' },
  { title: '昵称', key: 'nickName' },
  { 
    title: '类型', 
    key: 'userType',
    render(row: any) {
        return h(NTag, { type: row.userType === '00' ? 'error' : 'info', ghost: true }, { default: () => row.userType === '00' ? '管理员' : '普通用户' })
    }
  },
  {
    title: '状态',
    key: 'status',
    render(row: any) {
        return h(NTag, { type: row.status === '0' ? 'success' : 'warning' }, { default: () => row.status === '0' ? '正常' : '禁用' })
    }
  },
  {
    title: '操作',
    key: 'actions',
    render(row: any) {
      return h('div', { style: 'display: flex; gap: 8px;' }, [
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
            disabled: row.id === 1,
            onClick: () => handleDelete(row.id)
          },
          { icon: () => h(NIcon, { component: TrashOutline }) }
        )
      ])
    }
  }
]

const loadUsers = async () => {
    loading.value = true
    try {
        const [userList, roleList]: any = await Promise.all([
            getUserList(),
            getRoleList()
        ])
        users.value = userList
        roleOptions.value = roleList
    } finally {
        loading.value = false
    }
}

const handleCreate = async () => {
    await createUser(userForm.value)
    message.success('创作成功')
    showAddModal.value = false
    loadUsers()
}

const handleOpenPermission = async (user: any) => {
    activeUser.value = user
    showPermissionModal.value = true
    menuLoading.value = true
    try {
        const [tree, ids]: any = await Promise.all([
            getMenuTree(),
            getUserMenuIds(user.id)
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
        await assignUserMenus({
            userId: activeUser.value.id,
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
        content: '确定要删除该系统账号吗？',
        positiveText: '确定',
        negativeText: '取消',
        onPositiveClick: async () => {
            await deleteUser(id)
            message.success('已删除')
            loadUsers()
        }
    })
}

onMounted(loadUsers)
</script>

<style scoped>
.user-management-page {
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

.glass-effect {
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
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
</style>

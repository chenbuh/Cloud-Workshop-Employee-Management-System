<template>
  <div class="login-container">
    <div class="login-card glass-effect">
      <div class="login-header">
        <div class="logo-box">
          <img src="/logo.png" alt="Logo" class="logo-img" />
        </div>
        <h1>Cloud Workshop</h1>
        <p>未来感员工管理平台</p>
      </div>

      <n-form ref="formRef" :model="loginModel" :rules="rules">
        <n-form-item path="username" label="账号">
          <n-input v-model:value="loginModel.username" placeholder="请输入账号" size="large">
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
            v-model:value="loginModel.password"
            type="password"
            show-password-on="click"
            placeholder="请输入密码"
            size="large"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>

        <div class="login-options">
          <n-checkbox>记住我</n-checkbox>
          <n-button text type="primary">忘记密码？</n-button>
        </div>

        <n-button
          type="primary"
          block
          size="large"
          :loading="loading"
          @click="handleLogin"
          class="login-btn"
        >
          立即登录
        </n-button>
      </n-form>

      <!-- MFA Modal -->
      <n-modal v-model:show="showMfaModal" preset="card" title="二次验证 (MFA)" style="width: 400px" class="glass-popover">
        <div style="text-align: center; margin-bottom: 20px;">
            <n-text depth="3">您的账号已开启两步验证，请输入身份验证器中的 6 位动态验证码。</n-text>
        </div>
        <n-form-item label="身份验证码">
            <n-input
                v-model:value="mfaCode"
                placeholder="000000"
                size="large"
                autofocus
                @keyup.enter="handleMfaSubmit"
                style="text-align: center; letter-spacing: 4px; font-weight: 800; font-size: 20px;"
            />
        </n-form-item>
        <template #footer>
            <n-button type="primary" block size="large" :loading="mfaLoading" @click="handleMfaSubmit">
                验证并登录
            </n-button>
        </template>
      </n-modal>

      <div class="login-footer">
        © 2026 Cloud Workshop · Next Gen EMS
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { login, mfaLogin } from '../api/auth'
import { useUserStore } from '../store/user'
import { NModal, NText } from 'naive-ui'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const userStore = useUserStore()

// MFA state
const showMfaModal = ref(false)
const mfaCode = ref('')
const mfaLoading = ref(false)
const currentUserId = ref<number | null>(null)

const loginModel = reactive({
  username: '',
  password: ''
})

const rules = {
  username: { required: true, message: '请输入账号', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res: any = await login(loginModel)
    
    if (res.mfaRequired) {
        currentUserId.value = res.userId
        showMfaModal.value = true
        return
    }

    // Save token to store and storage
    userStore.setToken(res.token)
    message.success('登录成功，欢迎回来')
    router.push('/dashboard')
  } catch (error) {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleMfaSubmit = async () => {
    if (!mfaCode.value || mfaCode.value.length !== 6) {
        message.warning('请输入 6 位动态验证码')
        return
    }
    mfaLoading.value = true
    try {
        const res: any = await mfaLogin({
            userId: currentUserId.value!,
            code: mfaCode.value
        })
        userStore.setToken(res.token)
        message.success('两步验证通过，欢迎回来')
        showMfaModal.value = false
        router.push('/dashboard')
    } catch (e: any) {
        // Error handled by interceptor
    } finally {
        mfaLoading.value = false
    }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top right, #eef2ff 0%, #ffffff 50%, #f5f7ff 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.05) 0%, transparent 70%);
  top: -200px;
  left: -200px;
}

.login-card {
  width: 420px;
  padding: 48px;
  border-radius: 24px;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-box {
  width: 64px;
  height: 64px;
  background: white;
  border-radius: 16px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15);
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 12px;
}

h1 {
  font-size: 28px;
  margin: 0;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

p {
  color: var(--text-secondary);
  margin-top: 8px;
  font-size: 14px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-btn {
  height: 48px;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
  opacity: 0.6;
}
</style>

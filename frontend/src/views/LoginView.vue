<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="logo">ResumePilot</h1>
      <p class="subtitle">AI 求职助手</p>

      <!-- 登录 / 注册切换 -->
      <div class="tabs">
        <div :class="['tab', mode === 'login' && 'active']" @click="mode = 'login'">登录</div>
        <div :class="['tab', mode === 'register' && 'active']" @click="mode = 'register'">注册</div>
      </div>

      <!-- 登录表单 -->
      <div v-if="mode === 'login'" class="form">
        <el-input v-model="email" placeholder="邮箱" size="large" />
        <el-input v-model="password" placeholder="密码" type="password" size="large" show-password />
        <el-button type="primary" size="large" @click="handleLogin" :loading="loading" style="width: 100%">
          登录
        </el-button>
      </div>

      <!-- 注册表单 -->
      <div v-else class="form">
        <el-input v-model="email" placeholder="邮箱" size="large" />
        <el-input v-model="password" placeholder="密码（6-20位）" type="password" size="large" show-password />
        <div class="code-row">
          <el-input v-model="code" placeholder="验证码" size="large" />
          <el-button @click="handleSendCode" :disabled="countdown > 0" size="large">
            {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
          </el-button>
        </div>
        <el-button type="primary" size="large" @click="handleRegister" :loading="loading" style="width: 100%">
          注册
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { sendCode, register, login } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const email = ref('')
const password = ref('')
const code = ref('')
const loading = ref(false)
const countdown = ref(0)

async function handleSendCode() {
  if (!email.value) { ElMessage.warning('请输入邮箱'); return }
  try {
    await sendCode(email.value)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '发送失败')
  }
}

async function handleRegister() {
  if (!email.value || !password.value || !code.value) {
    ElMessage.warning('请填写完整信息'); return
  }
  loading.value = true
  try {
    await register(email.value, password.value, code.value)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

async function handleLogin() {
  if (!email.value || !password.value) {
    ElMessage.warning('请输入邮箱和密码'); return
  }
  loading.value = true
  try {
    const res = await login(email.value, password.value)
    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('email', res.data.data.email)
      ElMessage.success('登录成功')
      router.push('/chat')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
.login-card {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  text-align: center;
}
.logo {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #10a37f, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 4px;
}
.subtitle { color: #999; margin: 0 0 24px; font-size: 14px; }
.tabs { display: flex; gap: 0; margin-bottom: 24px; border-bottom: 1px solid #eee; }
.tab {
  flex: 1; padding: 10px; cursor: pointer; color: #999; font-size: 15px;
  border-bottom: 2px solid transparent; transition: all 0.2s;
}
.tab.active { color: #10a37f; border-bottom-color: #10a37f; }
.form { display: flex; flex-direction: column; gap: 12px; }
.code-row { display: flex; gap: 8px; }
.code-row .el-input { flex: 1; }
</style>

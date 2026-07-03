<template>
  <div class="optimize-view">
    <div class="page-header">
      <h2>✨ 简历优化</h2>
      <p>选择简历和优化方向，AI 帮你改写，支持多轮修改</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar" v-if="!started">
      <el-select v-model="resumeId" placeholder="选择简历" style="width: 240px" :loading="resumesLoading">
        <el-option v-for="r in resumes" :key="r.id" :label="r.fileName" :value="r.id" />
      </el-select>
      <el-select v-model="direction" placeholder="优化方向" style="width: 160px">
        <el-option label="项目经历" value="项目经历" />
        <el-option label="技能描述" value="技能描述" />
        <el-option label="自我评价" value="自我评价" />
        <el-option label="全文优化" value="全文优化" />
      </el-select>
      <el-button type="primary" @click="handleStart" :loading="loading" :disabled="!resumeId">
        开始优化
      </el-button>
    </div>

    <div v-if="!resumes.length && !resumesLoading && !started" class="empty-tip">
      暂无简历，请先<router-link to="/resume">上传简历</router-link>
    </div>

    <!-- 对话区 -->
    <div class="messages" ref="messagesRef">
      <ChatMessage v-for="(msg, i) in messages" :key="i" :message="msg" />
    </div>

    <!-- 多轮修改输入框 -->
    <ChatInput
      v-if="started"
      placeholder="输入修改指令，例如：再短一点、偏大厂风格、增加量化数据..."
      :disabled="loading"
      @send="handleRefine"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import type { Message } from '../stores/chat'
import { getMyResumes } from '../api'
import { ElMessage } from 'element-plus'

const resumeId = ref<number | null>(null)
const resumes = ref<any[]>([])
const resumesLoading = ref(false)
const direction = ref('项目经历')
const loading = ref(false)
const started = ref(false)
const messages = ref<Message[]>([])
const messagesRef = ref<HTMLElement>()

onMounted(async () => {
  resumesLoading.value = true
  try {
    const res = await getMyResumes()
    if (res.data.code === 200) {
      resumes.value = res.data.data
      if (resumes.value.length > 0) resumeId.value = resumes.value[0].id
    }
  } finally { resumesLoading.value = false }
})

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

async function streamRequest(url: string, userText: string) {
  messages.value.push({ role: 'user', content: userText, timestamp: Date.now() })
  const aiIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '', timestamp: Date.now() })
  loading.value = true
  scrollToBottom()

  try {
    const jwtToken = localStorage.getItem('token')
    const response = await fetch(url, {
      method: 'POST',
      headers: jwtToken ? { 'Authorization': `Bearer ${jwtToken}` } : {}
    })
    const reader = response.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      // SSE 格式：每条消息以 \n\n 分隔，内容是 data: xxx
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''  // 最后一行可能不完整，留到下次处理

      for (const line of lines) {
        const trimmed = line.trim()
        if (trimmed === '') continue
        if (trimmed.startsWith('data:')) {
          // 提取 data: 后面的内容
          const content = trimmed.substring(5)
          if (content) {
            messages.value[aiIndex].content += content
            scrollToBottom()
          }
        }
      }
    }
  } catch (e) {
    messages.value[aiIndex].content += '\n\n[连接失败]'
  } finally {
    loading.value = false
  }
}

async function handleStart() {
  if (!resumeId.value) return
  started.value = true
  const url = `/api/resume/optimize?resumeId=${resumeId.value}&direction=${encodeURIComponent(direction.value)}`
  await streamRequest(url, `请优化简历的「${direction.value}」部分`)
}

async function handleRefine(text: string) {
  if (!resumeId.value) return
  const url = `/api/resume/refine?resumeId=${resumeId.value}&message=${encodeURIComponent(text)}`
  await streamRequest(url, text)
}
</script>

<style scoped>
.optimize-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
}
.page-header { padding: 20px 24px 0; }
.page-header h2 { margin: 0 0 4px; font-size: 20px; }
.page-header p { margin: 0; color: #888; font-size: 13px; }
.action-bar { display: flex; gap: 12px; padding: 12px 24px; }
.empty-tip { color: #999; font-size: 14px; padding: 0 24px; }
.empty-tip a { color: #10a37f; }
.messages { flex: 1; overflow-y: auto; padding: 16px 0; }
</style>

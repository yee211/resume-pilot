<template>
  <div class="chat-view">
    <!-- 消息列表 -->
    <div class="messages" ref="messagesRef">
      <div v-if="store.messages.length === 0" class="welcome">
        <h1>ResumePilot</h1>
        <p>AI 求职助手，帮你优化简历、分析岗位匹配度</p>
      </div>
      <ChatMessage v-for="(msg, i) in store.messages" :key="i" :message="msg" />
    </div>
    <!-- 输入框 -->
    <ChatInput placeholder="输入消息..." :disabled="store.loading" @send="handleSend" />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import { useChatStore } from '../stores/chat'

const store = useChatStore()
const messagesRef = ref<HTMLElement>()
const memoryId = 'chat-' + Date.now()

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function handleSend(text: string) {
  store.addUserMessage(text)
  store.loading = true
  const aiIndex = store.addAssistantMessage()
  scrollToBottom()

  try {
    const url = `/api/chat/stream?memoryId=${memoryId}&message=${encodeURIComponent(text)}`
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

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (trimmed === '') continue
        if (trimmed.startsWith('data:')) {
          const content = trimmed.substring(5)
          if (content) {
            store.appendToken(aiIndex, content)
            scrollToBottom()
          }
        }
      }
    }
  } catch (e) {
    store.appendToken(aiIndex, '\n\n[连接失败，请检查后端是否启动]')
  } finally {
    store.loading = false
  }
}
</script>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff;
}
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
}
.welcome {
  text-align: center;
  padding: 120px 0 0;
  color: #999;
}
.welcome h1 {
  font-size: 32px;
  color: #333;
  margin-bottom: 8px;
}
</style>

<template>
  <div class="message" :class="message.role">
    <div class="avatar">
      <span v-if="message.role === 'user'">你</span>
      <span v-else>AI</span>
    </div>
    <div class="bubble">
      <div class="content" v-html="formatContent(message.content)"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Message } from '../stores/chat'

defineProps<{
  message: Message
}>()

function formatContent(content: string): string {
  if (!content) return ''
  // 简单的换行转换
  return content.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.message {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}
.message.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}
.message.assistant .avatar {
  background: #10a37f;
  color: #fff;
}
.message.user .avatar {
  background: #6366f1;
  color: #fff;
}
.bubble {
  max-width: 85%;
  line-height: 1.7;
  font-size: 15px;
}
.message.user .bubble {
  background: #f0f0f0;
  padding: 12px 16px;
  border-radius: 12px 12px 4px 12px;
}
.message.assistant .bubble {
  padding: 4px 0;
}
</style>

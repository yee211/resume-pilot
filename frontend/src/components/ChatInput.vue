<template>
  <div class="chat-input">
    <div class="input-wrapper">
      <textarea
        v-model="inputText"
        :placeholder="placeholder"
        @keydown.enter.exact.prevent="handleSend"
        rows="1"
        ref="textareaRef"
      />
      <button class="send-btn" @click="handleSend" :disabled="disabled || !inputText.trim()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'

const props = defineProps<{
  placeholder?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  send: [message: string]
}>()

const inputText = ref('')
const textareaRef = ref<HTMLTextAreaElement>()

function handleSend() {
  const text = inputText.value.trim()
  if (!text || props.disabled) return
  emit('send', text)
  inputText.value = ''
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
    }
  })
}

// 自动调整高度
watch(inputText, () => {
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.style.height = textareaRef.value.scrollHeight + 'px'
    }
  })
})
</script>

<style scoped>
.chat-input {
  padding: 16px 24px;
  border-top: 1px solid #e5e5e5;
  background: #fff;
}
.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  border: 1px solid #ddd;
  border-radius: 12px;
  padding: 12px 16px;
  background: #fafafa;
  transition: border-color 0.2s;
}
.input-wrapper:focus-within {
  border-color: #10a37f;
}
textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  font-size: 15px;
  line-height: 1.5;
  background: transparent;
  font-family: inherit;
  max-height: 120px;
}
.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: #10a37f;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s;
}
.send-btn:hover { background: #0d8c6d; }
.send-btn:disabled { background: #ccc; cursor: not-allowed; }
</style>

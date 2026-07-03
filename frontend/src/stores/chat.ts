import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 消息类型
 */
export interface Message {
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

/**
 * 聊天状态管理
 *
 * 为什么用 Pinia？
 *   - 多个组件共享聊天状态（消息列表、加载状态）
 *   - 流式输出时需要实时更新消息内容
 *   - 组件卸载后状态不丢失
 */
export const useChatStore = defineStore('chat', () => {
  const messages = ref<Message[]>([])
  const loading = ref(false)

  /**
   * 添加用户消息
   */
  function addUserMessage(content: string) {
    messages.value.push({
      role: 'user',
      content,
      timestamp: Date.now()
    })
  }

  /**
   * 添加 AI 消息（占位，等流式填充）
   */
  function addAssistantMessage(): number {
    messages.value.push({
      role: 'assistant',
      content: '',
      timestamp: Date.now()
    })
    return messages.value.length - 1
  }

  /**
   * 追加 Token 到最后一条 AI 消息
   */
  function appendToken(index: number, token: string) {
    if (messages.value[index]) {
      messages.value[index].content += token
    }
  }

  /**
   * 清空消息
   */
  function clearMessages() {
    messages.value = []
  }

  return { messages, loading, addUserMessage, addAssistantMessage, appendToken, clearMessages }
})

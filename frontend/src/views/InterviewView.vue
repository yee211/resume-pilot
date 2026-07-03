<template>
  <div class="interview-container">
    <!-- 左侧：面试配置 -->
    <div class="interview-sidebar">
      <div class="sidebar-header">
        <h3>模拟面试</h3>
      </div>

      <!-- 开始面试表单 -->
      <div v-if="!currentSession" class="start-form">
        <div class="form-group">
          <label>选择简历</label>
          <select v-model="form.resumeId" class="form-select">
            <option value="" disabled>请选择简历</option>
            <option v-for="resume in resumes" :key="resume.id" :value="resume.id">
              {{ resume.title }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>目标职位</label>
          <input v-model="form.jobTitle" type="text" class="form-input" placeholder="如：Java 开发工程师" />
        </div>

        <div class="form-group">
          <label>目标公司</label>
          <input v-model="form.company" type="text" class="form-input" placeholder="如：阿里巴巴" />
        </div>

        <button class="btn-start" @click="startInterview" :disabled="!form.resumeId || loading">
          {{ loading ? '准备中...' : '开始面试' }}
        </button>
      </div>

      <!-- 面试中 -->
      <div v-else class="session-info">
        <div class="info-item">
          <span class="label">职位</span>
          <span class="value">{{ currentSession.jobTitle }}</span>
        </div>
        <div class="info-item">
          <span class="label">公司</span>
          <span class="value">{{ currentSession.company }}</span>
        </div>
        <div class="info-item">
          <span class="label">进度</span>
          <span class="value">第 {{ currentSession.questionCount }} 题</span>
        </div>
        <div class="info-item">
          <span class="label">状态</span>
          <span class="value" :class="currentSession.status">
            {{ currentSession.status === 'in_progress' ? '进行中' : '已结束' }}
          </span>
        </div>

        <button class="btn-new" @click="resetInterview">新面试</button>
      </div>

      <!-- 历史记录 -->
      <div class="history-section">
        <h4>面试历史</h4>
        <div class="history-list">
          <div v-for="item in history" :key="item.id" class="history-item" @click="loadInterview(item.id)">
            <div class="history-title">{{ item.jobTitle }}</div>
            <div class="history-meta">
              <span>{{ item.company }}</span>
              <span>{{ item.questionCount }} 题</span>
            </div>
          </div>
          <div v-if="history.length === 0" class="empty-tip">暂无记录</div>
        </div>
      </div>
    </div>

    <!-- 右侧：面试对话 -->
    <div class="interview-main">
      <!-- 未开始 -->
      <div v-if="!currentSession" class="empty-state">
        <div class="empty-icon">🎯</div>
        <h2>模拟面试</h2>
        <p>选择简历和目标职位，开始一场 AI 模拟面试</p>
      </div>

      <!-- 面试对话 -->
      <div v-else class="chat-area">
        <div class="messages" ref="messagesRef">
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="message-avatar">
              {{ msg.role === 'assistant' ? '👔' : '👤' }}
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="message assistant">
            <div class="message-avatar">👔</div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div v-if="currentSession.status === 'in_progress'" class="input-area">
          <textarea
            v-model="userInput"
            @keydown.enter.prevent="submitAnswer"
            placeholder="输入你的回答..."
            rows="3"
          ></textarea>
          <button class="btn-send" @click="submitAnswer" :disabled="!userInput.trim() || loading">
            发送
          </button>
        </div>

        <!-- 面试结束 -->
        <div v-else class="interview-ended">
          <p>面试已结束</p>
          <button class="btn-restart" @click="resetInterview">开始新面试</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'

interface Resume {
  id: number
  title: string
}

interface Question {
  id: number
  questionOrder: number
  question: string
  answer: string | null
  score: number | null
  feedback: string | null
}

interface InterviewSession {
  id: number
  resumeId: number
  jobTitle: string
  company: string
  status: string
  questionCount: number
  score: number | null
  feedback: string | null
  createdAt: string
  questions: Question[]
}

interface Message {
  role: 'assistant' | 'user'
  content: string
  time: string
}

const resumes = ref<Resume[]>([])
const history = ref<InterviewSession[]>([])
const currentSession = ref<InterviewSession | null>(null)
const messages = ref<Message[]>([])
const userInput = ref('')
const loading = ref(false)
const messagesRef = ref<HTMLElement | null>(null)

const form = ref({
  resumeId: '',
  jobTitle: '',
  company: ''
})

onMounted(() => {
  loadResumes()
  loadHistory()
})

async function loadResumes() {
  try {
    const res = await axios.get('/resume/my')
    resumes.value = res.data.data
  } catch (e) {
    console.error('加载简历失败', e)
  }
}

async function loadHistory() {
  try {
    const res = await axios.get('/interview/my')
    history.value = res.data.data
  } catch (e) {
    console.error('加载历史失败', e)
  }
}

async function startInterview() {
  if (!form.value.resumeId) return

  loading.value = true
  try {
    const res = await axios.post('/interview/start', {
      resumeId: Number(form.value.resumeId),
      jobTitle: form.value.jobTitle || undefined,
      company: form.value.company || undefined
    })

    currentSession.value = res.data.data
    messages.value = []

    // 添加第一题
    if (currentSession.value?.questions?.length > 0) {
      addMessage('assistant', currentSession.value.questions[0].question)
    }

    loadHistory()
  } catch (e: any) {
    alert(e.response?.data?.message || '开始面试失败')
  } finally {
    loading.value = false
  }
}

async function submitAnswer() {
  if (!userInput.value.trim() || !currentSession.value || loading.value) return

  const answer = userInput.value.trim()
  userInput.value = ''

  // 添加用户消息
  addMessage('user', answer)
  loading.value = true

  try {
    const res = await axios.post(`/interview/${currentSession.value.id}/answer`, null, {
      params: { answer }
    })

    currentSession.value = res.data.data

    // 获取最新的题目（AI 的回复）
    const questions = currentSession.value.questions
    if (questions.length > 0) {
      const lastQuestion = questions[questions.length - 1]
      addMessage('assistant', lastQuestion.question)
    }

    // 如果面试结束，显示总结
    if (currentSession.value.status === 'completed' && currentSession.value.feedback) {
      addMessage('assistant', currentSession.value.feedback)
    }
  } catch (e: any) {
    alert(e.response?.data?.message || '提交失败')
  } finally {
    loading.value = false
  }
}

async function loadInterview(id: number) {
  try {
    const res = await axios.get(`/interview/${id}`)
    currentSession.value = res.data.data
    messages.value = []

    // 重建对话历史
    if (currentSession.value?.questions) {
      for (const q of currentSession.value.questions) {
        addMessage('assistant', q.question)
        if (q.answer) {
          addMessage('user', q.answer)
        }
      }
    }

    if (currentSession.value.status === 'completed' && currentSession.value.feedback) {
      addMessage('assistant', currentSession.value.feedback)
    }
  } catch (e: any) {
    alert(e.response?.data?.message || '加载失败')
  }
}

function resetInterview() {
  currentSession.value = null
  messages.value = []
}

function addMessage(role: 'assistant' | 'user', content: string) {
  messages.value.push({
    role,
    content,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })

  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function formatMessage(text: string): string {
  return text.replace(/\n/g, '<br>')
}
</script>

<style scoped>
.interview-container {
  display: flex;
  height: calc(100vh - 60px);
  background: #f5f5f5;
}

.interview-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.start-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.form-input, .form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.btn-start {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}

.btn-start:hover:not(:disabled) {
  background: #5a6fd6;
}

.btn-start:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.session-info {
  padding: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  color: #666;
  font-size: 14px;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.info-item .value.in_progress {
  color: #667eea;
}

.info-item .value.completed {
  color: #52c41a;
}

.btn-new {
  width: 100%;
  padding: 10px;
  margin-top: 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
}

.btn-new:hover {
  border-color: #667eea;
  color: #667eea;
}

.history-section {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.history-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
}

.history-item {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.history-item:hover {
  background: #f0f0f0;
}

.history-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.history-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.empty-tip {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 20px;
}

/* 右侧主区域 */
.interview-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  margin: 0 12px;
}

.message.assistant .message-content {
  background: white;
  border-radius: 12px 12px 12px 0;
  padding: 14px 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.message.user .message-content {
  background: #667eea;
  color: white;
  border-radius: 12px 12px 0 12px;
  padding: 14px 18px;
}

.message-text {
  font-size: 15px;
  line-height: 1.6;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}

.message.user .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #999;
  border-radius: 50%;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-8px); }
}

.input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 12px;
}

.input-area textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  resize: none;
  font-family: inherit;
}

.input-area textarea:focus {
  outline: none;
  border-color: #667eea;
}

.btn-send {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
}

.btn-send:hover:not(:disabled) {
  background: #5a6fd6;
}

.btn-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.interview-ended {
  padding: 20px;
  text-align: center;
  background: white;
  border-top: 1px solid #e0e0e0;
}

.interview-ended p {
  margin: 0 0 12px 0;
  color: #666;
}

.btn-restart {
  padding: 10px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>

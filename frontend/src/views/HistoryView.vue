<template>
  <div class="history-container">
    <div class="history-header">
      <h2>历史记录</h2>
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          {{ tab.icon }} {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="history-content">
      <!-- 加载中 -->
      <div v-if="loading" class="loading">加载中...</div>

      <!-- 空状态 -->
      <div v-else-if="currentList.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p>暂无{{ currentTabLabel }}记录</p>
      </div>

      <!-- ATS 记录列表 -->
      <div v-else-if="activeTab === 'ats'" class="record-list">
        <div v-for="item in currentList" :key="item.id" class="record-card">
          <div class="record-header">
            <span class="record-title">ATS 评分 #{{ item.id }}</span>
            <span class="record-time">{{ formatTime(item.analyzedAt) }}</span>
          </div>
          <div class="record-body">
            <div class="score-display">
              <span class="score" :class="getScoreClass(item.result?.overallScore)">
                {{ item.result?.overallScore || '-' }}
              </span>
              <span class="score-label">综合评分</span>
            </div>
            <div class="score-details" v-if="item.result">
              <div class="detail-item">
                <span class="label">关键词</span>
                <span class="value">{{ item.result.keywordScore }}</span>
              </div>
              <div class="detail-item">
                <span class="label">技能</span>
                <span class="value">{{ item.result.skillScore }}</span>
              </div>
              <div class="detail-item">
                <span class="label">项目</span>
                <span class="value">{{ item.result.projectScore }}</span>
              </div>
              <div class="detail-item">
                <span class="label">STAR</span>
                <span class="value">{{ item.result.starScore }}</span>
              </div>
            </div>
          </div>
          <div class="record-status" :class="getStatusClass(item.status)">
            {{ getStatusText(item.status) }}
          </div>
        </div>
      </div>

      <!-- JD 匹配记录列表 -->
      <div v-else-if="activeTab === 'jd'" class="record-list">
        <div v-for="item in currentList" :key="item.id" class="record-card">
          <div class="record-header">
            <span class="record-title">JD 匹配 #{{ item.id }}</span>
            <span class="record-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <div class="record-body">
            <div class="score-display">
              <span class="score" :class="getScoreClass(item.result?.matchScore)">
                {{ item.result?.matchScore || '-' }}
              </span>
              <span class="score-label">匹配度</span>
            </div>
            <div class="keywords" v-if="item.result">
              <div class="keyword-section" v-if="item.result.matchedSkills">
                <span class="label">✅ 匹配：</span>
                <span class="keywords-list">{{ item.result.matchedSkills.join(', ') }}</span>
              </div>
              <div class="keyword-section" v-if="item.result.missingSkills">
                <span class="label">❌ 缺失：</span>
                <span class="keywords-list">{{ item.result.missingSkills.join(', ') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 面试记录列表 -->
      <div v-else-if="activeTab === 'interview'" class="record-list">
        <div v-for="item in currentList" :key="item.id" class="record-card" @click="viewInterview(item.id)">
          <div class="record-header">
            <span class="record-title">{{ item.jobTitle || '模拟面试' }}</span>
            <span class="record-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <div class="record-body">
            <div class="interview-info">
              <div class="info-item">
                <span class="label">公司</span>
                <span class="value">{{ item.company || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="label">题数</span>
                <span class="value">{{ item.questionCount }} 题</span>
              </div>
              <div class="info-item">
                <span class="label">状态</span>
                <span class="value" :class="item.status">
                  {{ item.status === 'completed' ? '已完成' : '进行中' }}
                </span>
              </div>
              <div class="info-item" v-if="item.score">
                <span class="label">评分</span>
                <span class="value score-value">{{ item.score }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()

interface AtsRecord {
  id: number
  status: number
  result: any
  analyzedAt: string
}

interface JdRecord {
  id: number
  jdText: string
  status: number
  result: {
    matchScore: number
    matchedSkills: string[]
    missingSkills: string[]
    suggestions: string[]
    summary: string
  } | null
  createdAt: string
}

interface InterviewRecord {
  id: number
  jobTitle: string
  company: string
  questionCount: number
  score: number
  status: string
  createdAt: string
}

const tabs = [
  { key: 'ats', label: 'ATS 评分', icon: '📊' },
  { key: 'jd', label: 'JD 匹配', icon: '🎯' },
  { key: 'interview', label: '模拟面试', icon: '🎤' }
]

const activeTab = ref('ats')
const loading = ref(false)
const atsRecords = ref<AtsRecord[]>([])
const jdRecords = ref<JdRecord[]>([])
const interviewRecords = ref<InterviewRecord[]>([])

const currentTabLabel = computed(() => {
  return tabs.find(t => t.key === activeTab.value)?.label || ''
})

const currentList = computed(() => {
  switch (activeTab.value) {
    case 'ats': return atsRecords.value
    case 'jd': return jdRecords.value
    case 'interview': return interviewRecords.value
    default: return []
  }
})

onMounted(() => {
  loadHistory()
})

async function loadHistory() {
  loading.value = true
  try {
    const res = await api.get('/history/all')
    const data = res.data.data
    atsRecords.value = data.ats || []
    jdRecords.value = data.jd || []
    interviewRecords.value = data.interview || []
  } catch (e) {
    console.error('加载历史记录失败', e)
  } finally {
    loading.value = false
  }
}

function formatTime(time: string): string {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function getScoreClass(score: number): string {
  if (!score) return ''
  if (score >= 80) return 'high'
  if (score >= 60) return 'medium'
  return 'low'
}

function getStatusClass(status: number): string {
  if (status === 1) return 'success'
  if (status === 2) return 'error'
  return 'pending'
}

function getStatusText(status: number): string {
  if (status === 1) return '已完成'
  if (status === 2) return '失败'
  return '分析中'
}

function viewInterview(id: number) {
  router.push(`/interview?id=${id}`)
}
</script>

<style scoped>
.history-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.history-header {
  margin-bottom: 24px;
}

.history-header h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
  color: #333;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.tab-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.2s;
}

.record-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.record-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.record-time {
  font-size: 13px;
  color: #999;
}

.record-body {
  display: flex;
  gap: 24px;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 80px;
}

.score {
  font-size: 36px;
  font-weight: 700;
}

.score.high { color: #52c41a; }
.score.medium { color: #faad14; }
.score.low { color: #ff4d4f; }

.score-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.score-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  flex: 1;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.detail-item .label {
  color: #666;
  font-size: 13px;
}

.detail-item .value {
  font-weight: 600;
  color: #333;
}

.keywords {
  flex: 1;
}

.keyword-section {
  margin-bottom: 8px;
}

.keyword-section .label {
  font-size: 13px;
  color: #666;
}

.keywords-list {
  font-size: 13px;
  color: #333;
}

.interview-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  flex: 1;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
}

.info-item .label {
  color: #666;
  font-size: 13px;
}

.info-item .value {
  font-weight: 500;
  color: #333;
}

.info-item .value.completed {
  color: #52c41a;
}

.info-item .value.in_progress {
  color: #667eea;
}

.info-item .value.score-value {
  color: #667eea;
  font-weight: 700;
}

.record-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  margin-top: 12px;
}

.record-status.success {
  background: #f6ffed;
  color: #52c41a;
}

.record-status.error {
  background: #fff2f0;
  color: #ff4d4f;
}

.record-status.pending {
  background: #e6f7ff;
  color: #1890ff;
}
</style>

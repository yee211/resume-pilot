<template>
  <div class="page-view">
    <div class="page-header">
      <h2>📊 ATS 评分</h2>
      <p>模拟简历筛选系统，给出多维度评分和优化建议</p>
    </div>

    <div class="input-row">
      <el-select v-model="resumeId" placeholder="选择简历" style="width: 240px" :loading="resumesLoading">
        <el-option v-for="r in resumes" :key="r.id" :label="r.fileName" :value="r.id" />
      </el-select>
      <el-button type="primary" @click="handleAnalyze" :loading="submitting" :disabled="!resumeId">
        开始评分
      </el-button>
    </div>

    <div v-if="!resumes.length && !resumesLoading" class="empty-tip">
      暂无简历，请先<router-link to="/resume">上传简历</router-link>
    </div>

    <!-- 分析中 -->
    <div v-if="analyzing" class="parsing-card">
      <div class="parsing-icon">⏳</div>
      <h3>AI 正在评分...</h3>
      <p>正在分析简历的关键词覆盖、技能匹配度、STAR 结构</p>
      <el-progress :percentage="progress" :stroke-width="8" style="margin-top: 16px" />
    </div>

    <!-- 结果 -->
    <div v-if="result && !analyzing" class="ats-result">
      <div class="score-cards">
        <div class="score-card main">
          <div class="score-num">{{ result.overallScore }}</div>
          <div class="score-label">综合评分</div>
        </div>
        <div class="score-card">
          <div class="score-num">{{ result.keywordScore }}</div>
          <div class="score-label">关键词覆盖</div>
        </div>
        <div class="score-card">
          <div class="score-num">{{ result.skillScore }}</div>
          <div class="score-label">技能评分</div>
        </div>
        <div class="score-card">
          <div class="score-num">{{ result.projectScore }}</div>
          <div class="score-label">项目评分</div>
        </div>
        <div class="score-card">
          <div class="score-num">{{ result.starScore }}</div>
          <div class="score-label">STAR 评分</div>
        </div>
      </div>
      <div v-if="result.summary" class="summary">{{ result.summary }}</div>
      <div v-if="result.missingKeywords?.length" class="section">
        <h4>缺失关键词</h4>
        <div class="tags">
          <el-tag v-for="k in result.missingKeywords" :key="k" type="warning" size="small">{{ k }}</el-tag>
        </div>
      </div>
      <div v-if="result.suggestions?.length" class="section">
        <h4>优化建议</h4>
        <ul class="suggestions"><li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li></ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { submitAts, getAtsResult, getMyResumes } from '../api'
import { ElMessage } from 'element-plus'

const resumeId = ref<number | null>(null)
const resumes = ref<any[]>([])
const resumesLoading = ref(false)
const submitting = ref(false)
const analyzing = ref(false)
const progress = ref(10)
const result = ref<any>(null)
let pollTimer: number | null = null

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

async function handleAnalyze() {
  if (!resumeId.value) return
  submitting.value = true
  result.value = null
  try {
    const res = await submitAts(resumeId.value)
    if (res.data.code === 200) {
      analyzing.value = true
      progress.value = 10
      startPolling(res.data.data.id)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  } finally { submitting.value = false }
}

function startPolling(recordId: number) {
  pollTimer = window.setInterval(async () => {
    try {
      const res = await getAtsResult(recordId)
      if (res.data.code === 200) {
        const data = res.data.data
        progress.value = Math.min(progress.value + 15, 90)
        if (data.status === 1 || data.status === 2) {
          stopPolling()
          analyzing.value = false
          progress.value = 100
          if (data.status === 1) {
            result.value = data.result
            ElMessage.success('评分完成')
          } else {
            ElMessage.error('评分失败，请重试')
          }
        }
      }
    } catch (e) {}
  }, 2000)
}

function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

onUnmounted(() => stopPolling())
</script>

<style scoped>
.page-view { max-width: 800px; margin: 0 auto; padding: 32px 24px; height: 100vh; overflow-y: auto; }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0 0 4px; font-size: 22px; }
.page-header p { margin: 0; color: #888; font-size: 14px; }
.input-row { display: flex; gap: 12px; margin-bottom: 24px; }
.empty-tip { color: #999; font-size: 14px; margin-bottom: 24px; }
.empty-tip a { color: #10a37f; }
.parsing-card { text-align: center; padding: 48px 24px; border: 1px solid #e5e5e5; border-radius: 12px; }
.parsing-icon { font-size: 48px; margin-bottom: 16px; }
.parsing-card h3 { margin: 0 0 8px; font-size: 18px; }
.parsing-card p { color: #888; margin: 0; font-size: 14px; }
.ats-result { border: 1px solid #e5e5e5; border-radius: 12px; padding: 24px; }
.score-cards { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.score-card { flex: 1; min-width: 100px; text-align: center; padding: 16px; border-radius: 10px; background: #f8f8f8; }
.score-card.main { background: #10a37f; color: #fff; }
.score-num { font-size: 28px; font-weight: 700; }
.score-label { font-size: 12px; color: #888; margin-top: 4px; }
.score-card.main .score-label { color: rgba(255,255,255,0.8); }
.summary { padding: 12px 16px; background: #f0fdf4; border-radius: 8px; margin-bottom: 16px; font-size: 14px; line-height: 1.6; }
.section { margin-bottom: 16px; }
.section h4 { font-size: 14px; color: #666; margin-bottom: 8px; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; }
.suggestions { padding-left: 20px; margin: 0; }
.suggestions li { font-size: 14px; line-height: 1.8; color: #444; }
</style>

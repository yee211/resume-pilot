<template>
  <div class="page-view">
    <div class="page-header">
      <h2>🎯 JD 匹配</h2>
      <p>粘贴岗位描述，AI 对比简历输出匹配度和缺失技能</p>
    </div>

    <div class="form-area">
      <el-select v-model="resumeId" placeholder="选择简历" style="width: 240px" :loading="resumesLoading">
        <el-option v-for="r in resumes" :key="r.id" :label="r.fileName" :value="r.id" />
      </el-select>
      <el-input v-model="jdText" type="textarea" :rows="6" placeholder="粘贴岗位描述..." />
      <el-button type="primary" @click="handleMatch" :loading="submitting" :disabled="!resumeId">
        开始匹配
      </el-button>
    </div>

    <div v-if="!resumes.length && !resumesLoading" class="empty-tip">
      暂无简历，请先<router-link to="/resume">上传简历</router-link>
    </div>

    <!-- 分析中 -->
    <div v-if="analyzing" class="parsing-card">
      <div class="parsing-icon">⏳</div>
      <h3>AI 正在匹配...</h3>
      <p>正在对比简历与岗位描述的技能匹配度</p>
      <el-progress :percentage="progress" :stroke-width="8" style="margin-top: 16px" />
    </div>

    <!-- 结果 -->
    <div v-if="result && !analyzing" class="jd-result">
      <div class="match-score">
        <div class="score-circle">{{ result.matchScore }}%</div>
        <div>匹配度</div>
      </div>
      <div v-if="result.summary" class="summary">{{ result.summary }}</div>
      <div class="two-col">
        <div v-if="result.matchedSkills?.length" class="section">
          <h4>✅ 已匹配技能</h4>
          <div class="tags">
            <el-tag v-for="s in result.matchedSkills" :key="s" type="success" size="small">{{ s }}</el-tag>
          </div>
        </div>
        <div v-if="result.missingSkills?.length" class="section">
          <h4>❌ 缺失技能</h4>
          <div class="tags">
            <el-tag v-for="s in result.missingSkills" :key="s" type="danger" size="small">{{ s }}</el-tag>
          </div>
        </div>
      </div>
      <div v-if="result.suggestions?.length" class="section">
        <h4>优化建议</h4>
        <ul><li v-for="(s, i) in result.suggestions" :key="i">{{ s }}</li></ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { submitJd, getJdResult, getMyResumes } from '../api'
import { ElMessage } from 'element-plus'

const resumeId = ref<number | null>(null)
const resumes = ref<any[]>([])
const resumesLoading = ref(false)
const jdText = ref('')
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

async function handleMatch() {
  if (!jdText.value.trim()) { ElMessage.warning('请输入岗位描述'); return }
  if (!resumeId.value) return
  submitting.value = true
  result.value = null
  try {
    const res = await submitJd(resumeId.value, jdText.value)
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
      const res = await getJdResult(recordId)
      if (res.data.code === 200) {
        const data = res.data.data
        progress.value = Math.min(progress.value + 15, 90)
        if (data.status === 1 || data.status === 2) {
          stopPolling()
          analyzing.value = false
          progress.value = 100
          if (data.status === 1) {
            result.value = data.result
            ElMessage.success('匹配完成')
          } else {
            ElMessage.error('匹配失败，请重试')
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
.form-area { display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px; }
.empty-tip { color: #999; font-size: 14px; margin-bottom: 24px; }
.empty-tip a { color: #10a37f; }
.parsing-card { text-align: center; padding: 48px 24px; border: 1px solid #e5e5e5; border-radius: 12px; }
.parsing-icon { font-size: 48px; margin-bottom: 16px; }
.parsing-card h3 { margin: 0 0 8px; font-size: 18px; }
.parsing-card p { color: #888; margin: 0; font-size: 14px; }
.jd-result { border: 1px solid #e5e5e5; border-radius: 12px; padding: 24px; }
.match-score { text-align: center; margin-bottom: 16px; }
.score-circle {
  width: 80px; height: 80px; border-radius: 50%; display: inline-flex;
  align-items: center; justify-content: center; font-size: 22px; font-weight: 700;
  background: #10a37f; color: #fff; margin-bottom: 4px;
}
.summary { padding: 12px 16px; background: #f0fdf4; border-radius: 8px; margin-bottom: 16px; font-size: 14px; line-height: 1.6; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.section h4 { font-size: 14px; color: #666; margin-bottom: 8px; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; }
ul { padding-left: 20px; margin: 0; }
li { font-size: 14px; line-height: 1.8; color: #444; }
</style>

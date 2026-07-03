<template>
  <div class="career-container">
    <div class="career-header">
      <h2>💼 求职话术</h2>
      <p>根据简历自动生成 Boss/智联/牛客/邮件不同风格的求职话术</p>
    </div>

    <div class="career-content">
      <!-- 配置表单 -->
      <div class="config-section">
        <div class="form-group">
          <label>选择简历</label>
          <select v-model="form.resumeId" class="form-select">
            <option value="" disabled>请选择简历</option>
            <option v-for="resume in resumes" :key="resume.id" :value="resume.id">
              {{ resume.fileName }}
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

        <button class="btn-generate" @click="generate" :disabled="!form.resumeId || loading">
          {{ loading ? '生成中...' : '✨ 生成话术' }}
        </button>
      </div>

      <!-- 生成结果 -->
      <div v-if="result" class="result-section">
        <div class="result-card" v-for="item in resultCards" :key="item.key">
          <div class="card-header">
            <span class="card-icon">{{ item.icon }}</span>
            <span class="card-title">{{ item.title }}</span>
            <button class="btn-copy" @click="copyText(result[item.key])">📋 复制</button>
          </div>
          <div class="card-body">
            <p>{{ result[item.key] }}</p>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">💬</div>
        <p>选择简历后点击"生成话术"</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

interface Resume {
  id: number
  fileName: string
}

interface CareerResult {
  boss: string
  zhilian: string
  niuke: string
  email: string
}

const resumes = ref<Resume[]>([])
const loading = ref(false)
const result = ref<CareerResult | null>(null)

const form = ref({
  resumeId: '',
  jobTitle: '',
  company: ''
})

const resultCards = [
  { key: 'boss', title: 'Boss 直聘', icon: '📱' },
  { key: 'zhilian', title: '智联招聘', icon: '💼' },
  { key: 'niuke', title: '牛客网', icon: '👨‍💻' },
  { key: 'email', title: '邮件投递', icon: '📧' }
]

onMounted(() => {
  loadResumes()
})

async function loadResumes() {
  try {
    const res = await axios.get('/resume/my')
    resumes.value = res.data.data
  } catch (e) {
    console.error('加载简历失败', e)
  }
}

async function generate() {
  if (!form.value.resumeId) return

  loading.value = true
  result.value = null

  try {
    const res = await axios.post('/career/generate', {
      resumeId: Number(form.value.resumeId),
      jobTitle: form.value.jobTitle || undefined,
      company: form.value.company || undefined
    })
    result.value = res.data.data
  } catch (e: any) {
    alert(e.response?.data?.message || '生成失败')
  } finally {
    loading.value = false
  }
}

function copyText(text: string) {
  navigator.clipboard.writeText(text).then(() => {
    alert('已复制到剪贴板')
  }).catch(() => {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    alert('已复制到剪贴板')
  })
}
</script>

<style scoped>
.career-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.career-header {
  margin-bottom: 24px;
}

.career-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.career-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.career-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.config-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: #667eea;
}

.btn-generate {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-generate:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-generate:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.result-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.result-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: #f9f9f9;
  border-bottom: 1px solid #e0e0e0;
}

.card-icon {
  font-size: 20px;
}

.card-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.btn-copy {
  padding: 6px 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-copy:hover {
  border-color: #667eea;
  color: #667eea;
}

.card-body {
  padding: 20px;
}

.card-body p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
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

@media (max-width: 768px) {
  .result-section {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <div class="page-view">
    <div class="page-header">
      <h2>📄 简历上传</h2>
      <p>上传 PDF 或 DOCX 文件，AI 自动解析为结构化数据</p>
    </div>

    <!-- 上传区域 -->
    <div class="upload-area" v-if="!resume">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        accept=".pdf,.docx"
        :limit="1"
      >
        <div class="upload-content">
          <div class="upload-icon">📄</div>
          <p>拖拽文件到此处，或 <em>点击上传</em></p>
          <p class="upload-tip">支持 PDF、DOCX 格式，最大 10MB</p>
        </div>
      </el-upload>
      <el-button type="primary" @click="handleUpload" :loading="loading" :disabled="!file" size="large">
        上传并解析
      </el-button>
    </div>

    <!-- 解析中状态 -->
    <div v-if="resume && resume.status === 0" class="parsing-card">
      <div class="parsing-icon">⏳</div>
      <h3>AI 正在解析简历...</h3>
      <p>文件「{{ resume.fileName }}」已上传，AI 正在提取结构化信息</p>
      <el-progress :percentage="progress" :stroke-width="8" style="margin-top: 16px" />
    </div>

    <!-- 解析失败 -->
    <div v-if="resume && resume.status === 2" class="error-card">
      <div class="parsing-icon">❌</div>
      <h3>解析失败</h3>
      <p>AI 解析简历时出错，请重试</p>
      <el-button @click="resetUpload" style="margin-top: 12px">重新上传</el-button>
    </div>

    <!-- 解析结果 -->
    <div v-if="resume && resume.status === 1" class="result-card">
      <div class="result-header">
        <span>解析结果</span>
        <el-button text @click="resetUpload">重新上传</el-button>
      </div>
      <div class="info-grid">
        <div class="info-item"><label>姓名</label><span>{{ resume.parsedData?.name || '-' }}</span></div>
        <div class="info-item"><label>手机</label><span>{{ resume.parsedData?.phone || '-' }}</span></div>
        <div class="info-item"><label>邮箱</label><span>{{ resume.parsedData?.email || '-' }}</span></div>
        <div class="info-item"><label>求职意向</label><span>{{ resume.parsedData?.jobTarget || '-' }}</span></div>
      </div>
      <div v-if="resume.parsedData?.skills?.length" class="section">
        <h4>技能</h4>
        <div class="tags">
          <el-tag v-for="s in resume.parsedData.skills" :key="s" size="small">{{ s }}</el-tag>
        </div>
      </div>
      <div v-if="resume.parsedData?.projects?.length" class="section">
        <h4>项目经历</h4>
        <div v-for="p in resume.parsedData.projects" :key="p.name" class="project-item">
          <strong>{{ p.name }}</strong>
          <p>{{ p.description }}</p>
          <div class="tags" v-if="p.techStack?.length">
            <el-tag v-for="t in p.techStack" :key="t" type="info" size="small">{{ t }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { uploadResume, getResume } from '../api'
import { ElMessage } from 'element-plus'
import type { UploadFile } from 'element-plus'

const file = ref<File | null>(null)
const loading = ref(false)
const resume = ref<any>(null)
const progress = ref(10)
let pollTimer: number | null = null

function handleFileChange(uploadFile: UploadFile) {
  file.value = uploadFile.raw!
}

async function handleUpload() {
  if (!file.value) return
  loading.value = true
  try {
    const res = await uploadResume(file.value)
    if (res.data.code === 200) {
      resume.value = res.data.data
      // 上传成功后，开始轮询状态
      if (resume.value.status === 0) {
        startPolling(resume.value.id)
      }
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e: any) {
    ElMessage.error('上传失败：' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 轮询查询简历状态
 * 每 2 秒查一次，直到 status 变成 1（成功）或 2（失败）
 */
function startPolling(resumeId: number) {
  progress.value = 10
  pollTimer = window.setInterval(async () => {
    try {
      const res = await getResume(resumeId)
      if (res.data.code === 200) {
        resume.value = res.data.data
        progress.value = Math.min(progress.value + 15, 90)
        // 解析完成或失败，停止轮询
        if (resume.value.status === 1 || resume.value.status === 2) {
          stopPolling()
          progress.value = 100
          if (resume.value.status === 1) {
            ElMessage.success('解析完成')
          }
        }
      }
    } catch (e) {
      // 轮询出错不停止，等下一次
    }
  }, 2000)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function resetUpload() {
  stopPolling()
  file.value = null
  resume.value = null
  progress.value = 10
}

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.page-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px;
  height: 100vh;
  overflow-y: auto;
}
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0 0 4px; font-size: 22px; }
.page-header p { margin: 0; color: #888; font-size: 14px; }
.upload-area { display: flex; flex-direction: column; gap: 16px; align-items: center; }
.upload-content { padding: 24px; text-align: center; }
.upload-icon { font-size: 48px; margin-bottom: 12px; }
.upload-tip { color: #999; font-size: 12px; }

.parsing-card, .error-card {
  text-align: center;
  padding: 48px 24px;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
}
.parsing-icon { font-size: 48px; margin-bottom: 16px; }
.parsing-card h3 { margin: 0 0 8px; font-size: 18px; }
.parsing-card p, .error-card p { color: #888; margin: 0; font-size: 14px; }

.result-card {
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 24px;
}
.result-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px; font-size: 16px; font-weight: 600;
}
.info-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 12px;
  margin-bottom: 20px;
}
.info-item label { display: block; font-size: 12px; color: #999; margin-bottom: 2px; }
.info-item span { font-size: 15px; }
.section { margin-bottom: 16px; }
.section h4 { font-size: 14px; color: #666; margin-bottom: 8px; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; }
.project-item { margin-bottom: 12px; }
.project-item strong { font-size: 15px; }
.project-item p { color: #666; font-size: 14px; margin: 4px 0 8px; }
</style>

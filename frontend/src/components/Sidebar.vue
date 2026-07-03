<template>
  <div class="sidebar">
    <div class="logo">
      <span class="logo-text">ResumePilot</span>
    </div>
    <nav class="nav">
      <router-link to="/chat" class="nav-item" :class="{ active: $route.path === '/chat' }">
        <span class="icon">💬</span> 通用对话
      </router-link>
      <router-link to="/resume" class="nav-item" :class="{ active: $route.path === '/resume' }">
        <span class="icon">📄</span> 简历上传
      </router-link>
      <router-link to="/ats" class="nav-item" :class="{ active: $route.path === '/ats' }">
        <span class="icon">📊</span> ATS 评分
      </router-link>
      <router-link to="/jd" class="nav-item" :class="{ active: $route.path === '/jd' }">
        <span class="icon">🎯</span> JD 匹配
      </router-link>
      <router-link to="/optimize" class="nav-item" :class="{ active: $route.path === '/optimize' }">
        <span class="icon">✨</span> 简历优化
      </router-link>
      <router-link to="/interview" class="nav-item" :class="{ active: $route.path === '/interview' }">
        <span class="icon">🎯</span> 模拟面试
      </router-link>
      <router-link to="/history" class="nav-item" :class="{ active: $route.path === '/history' }">
        <span class="icon">📋</span> 历史记录
      </router-link>
      <router-link to="/career" class="nav-item" :class="{ active: $route.path === '/career' }">
        <span class="icon">💼</span> 求职话术
      </router-link>
    </nav>
    <!-- 用户信息 + 退出 -->
    <div class="user-area">
      <span class="user-email">{{ email }}</span>
      <button class="logout-btn" @click="handleLogout">退出</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const email = localStorage.getItem('email') || ''

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('email')
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  background: #171717;
  color: #fff;
  display: flex;
  flex-direction: column;
  height: 100vh;
  flex-shrink: 0;
}
.logo {
  padding: 20px 16px;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid #2a2a2a;
}
.logo-text {
  background: linear-gradient(135deg, #10a37f, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.nav {
  padding: 12px 8px;
  flex: 1;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  color: #a0a0a0;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  margin-bottom: 4px;
}
.nav-item:hover { background: #2a2a2a; color: #fff; }
.nav-item.active { background: #2a2a2a; color: #fff; }
.icon { font-size: 16px; }

.user-area {
  padding: 12px 16px;
  border-top: 1px solid #2a2a2a;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.user-email {
  font-size: 12px;
  color: #888;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}
.logout-btn {
  background: none;
  border: 1px solid #555;
  color: #aaa;
  padding: 4px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  flex-shrink: 0;
}
.logout-btn:hover { border-color: #e74c3c; color: #e74c3c; }
</style>

<template>
  <div class="app-layout">
    <!-- 登录页不显示侧边栏 -->
    <Sidebar v-if="$route.path !== '/login'" />
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}
.main-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>

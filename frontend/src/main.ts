import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './style.css'

/**
 * 入口文件
 * 注册全局插件：Pinia（状态管理）、Element Plus（UI 组件）、Router（路由）
 */
const app = createApp(App)
app.use(createPinia())
app.use(ElementPlus)
app.use(router)
app.mount('#app')

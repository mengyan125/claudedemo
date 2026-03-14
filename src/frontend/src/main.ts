import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/global.css'
import App from './App.vue'
import router from './router'

/* 开发环境启用 Mock（通过 VITE_ENABLE_MOCK 控制） */
if (import.meta.env.DEV && import.meta.env.VITE_ENABLE_MOCK !== 'false') {
  import('./mock').then(({ setupMock }) => setupMock())
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: undefined }) // 后续配置中文语言包
app.mount('#app')

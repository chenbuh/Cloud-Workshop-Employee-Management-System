import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import moment from 'moment'
import 'moment/dist/locale/zh-cn'

// Configure moment locale globally
moment.locale('zh-cn')

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.use(naive)

app.mount('#app')

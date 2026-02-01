import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { setupI18n } from './i18n'
import router from './router'

// Initialize i18n and mount app
setupI18n().then((i18n) => {
  const app = createApp(App)
  app.use(i18n)
  app.use(router)
  app.mount('#app')
})

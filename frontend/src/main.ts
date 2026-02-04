import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import { setupI18n } from './i18n'
import router from './router'
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faHashtag, faVolumeHigh, faBullhorn, faComments } from '@fortawesome/free-solid-svg-icons'

// Add icons to library
library.add(faHashtag, faVolumeHigh, faBullhorn, faComments)

// Create Pinia instance
const pinia = createPinia()

// Initialize i18n and mount app
setupI18n().then((i18n) => {
  const app = createApp(App)
  app.component('font-awesome-icon', FontAwesomeIcon)
  app.use(pinia)
  app.use(i18n)
  app.use(router)
  app.mount('#app')
})

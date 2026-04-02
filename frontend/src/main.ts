/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import {createApp} from 'vue'
import {createPinia} from 'pinia'
import './style.css'
import App from './App.vue'
import {setupI18n} from './i18n'
import router from './router'
import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'
import {
    faBullhorn,
    faCheck,
    faComments,
    faFileContract,
    faGlobe,
    faHashtag,
    faLifeRing,
    faQuestionCircle,
    faChevronDown,
    faChevronUp,
    faChevronRight,
    faServer,
    faFolder,
    faArrowTurnUp,
    faVolumeHigh,
    faXmark,
    faTriangleExclamation,
    faCircleExclamation,
    faCheckCircle,
    faExclamationTriangle,
    faExclamationCircle,
    faSpinner,
    faPlus,
    faMinus,
    faTrash,
    faPen,
    faCoins,
    faRightFromBracket,
    faUserGear,
    faCheckToSlot,
    faCalendarAlt,
    faSyncAlt,
    faBell,
    faBellSlash,
    faShoppingCart,
    faTimesCircle,
    faMugHot,
    faInfoCircle,
    faShieldHalved,
    faChartLine,
    faRobot,
    faTrophy,
    faListOl,
    faLayerGroup,
    faPalette,
    faScroll,
    faChartBar,
    faStar,
    faFaceSmile,
    faIdCard,
    faBookOpen,
    faClockRotateLeft,
    faSliders,
    faArrowUp,
    faUserShield,
    faMagnifyingGlass,
    faGauge,
    faLock,
    faClipboardList
} from '@fortawesome/free-solid-svg-icons'
import {faGithub, faDiscord} from '@fortawesome/free-brands-svg-icons'

// Add icons to library
library.add(
    faHashtag,
    faVolumeHigh,
    faBullhorn,
    faComments,
    faCheck,
    faXmark,
    faGlobe,
    faQuestionCircle,
    faLifeRing,
    faFileContract,
    faGithub,
    faDiscord,
    faChevronDown,
    faChevronUp,
    faChevronRight,
    faServer,
    faFolder,
    faArrowTurnUp,
    faTriangleExclamation,
    faCircleExclamation,
    faCheckCircle,
    faExclamationTriangle,
    faExclamationCircle,
    faSpinner,
    faPlus,
    faMinus,
    faTrash,
    faPen,
    faCoins,
    faRightFromBracket,
    faUserGear,
    faCheckToSlot,
    faCalendarAlt,
    faSyncAlt,
    faBell,
    faBellSlash,
    faShoppingCart,
    faTimesCircle,
    faMugHot,
    faInfoCircle,
    faShieldHalved,
    faChartLine,
    faRobot,
    faTrophy,
    faListOl,
    faLayerGroup,
    faPalette,
    faScroll,
    faChartBar,
    faStar,
    faFaceSmile,
    faIdCard,
    faBookOpen,
    faClockRotateLeft,
    faSliders,
    faArrowUp,
    faUserShield,
    faMagnifyingGlass,
    faGauge,
    faLock,
    faClipboardList
)


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

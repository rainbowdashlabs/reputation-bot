/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import SettingsHeader from './components/SettingsHeader.vue'
import AppFooter from './components/AppFooter.vue'
import HelpIcon from './components/HelpIcon.vue'
import ErrorNotification from './components/ErrorNotification.vue'
import ExpiredSessionWarning from './components/ExpiredSessionWarning.vue'
import {api} from './api'
import {useSession} from './composables/useSession'
import {useDarkMode} from './composables/useDarkMode'

const router = useRouter()
const route = useRoute()
const {setSession, clearSession, setExpired} = useSession()
useDarkMode()

const isSettingsPage = computed(() => route.path.startsWith('/settings/edit'))
const showSettingsHeader = computed(() => route.path.startsWith('/settings'))

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (token) {
    api.setToken(token);
    // Remove token from URL
    urlParams.delete('token');
    const newRelativePathQuery = window.location.pathname + (urlParams.toString() ? '?' + urlParams.toString() : '');
    window.history.replaceState(null, '', newRelativePathQuery);
  }

  // Check if token exists in localStorage
  let storedToken = localStorage.getItem('token');

  // If we don't have a token, but we have saved sessions, use the first one as default
  if (!storedToken) {
    const sessionsJson = localStorage.getItem('reputation_bot_sessions');
    if (sessionsJson) {
      const sessions = JSON.parse(sessionsJson);
      if (sessions.length > 0) {
        storedToken = sessions[0].token;
        if (storedToken) {
          api.setToken(storedToken);
        }
      }
    }
  }

  // Don't redirect if already on error page
  if (!storedToken && router.currentRoute.value.path !== '/error/no-token') {
    return;
  }

  // If token exists, try to load session data
  if (storedToken) {
    try {
      const sessionData = await api.getSession();
      setSession(sessionData);
    } catch (error: any) {
      // If session loading fails, clear session and set expired state if it's a 401
      console.error('Failed to load session:', error);
      clearSession();
      if (error.response?.status === 401) {
        setExpired(true);
      }
    }
  }
})
</script>

<template>
  <AppHeader/>
  <div class="h-[73px]"></div>

  <SettingsHeader v-if="showSettingsHeader"/>

  <div :class="{'pt-8': showSettingsHeader}">
    <router-view/>
  </div>

  <AppFooter/>

  <HelpIcon v-if="isSettingsPage"/>

  <ErrorNotification/>

  <ExpiredSessionWarning/>
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}

.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}

.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import { api } from './api'
import { useSession } from './composables/useSession'
import { useDarkMode } from './composables/useDarkMode'

const router = useRouter()
const { setSession, clearSession } = useSession()
useDarkMode()

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
  const storedToken = localStorage.getItem('token');
  
  // Don't redirect if already on error page
  if (!storedToken && router.currentRoute.value.path !== '/error/no-token') {
    router.push('/error/no-token');
    return;
  }
  
  // If token exists, try to load session data
  if (storedToken) {
    try {
      const sessionData = await api.getSession();
      setSession(sessionData);
    } catch (error) {
      // If session loading fails, clear session and redirect to error page
      console.error('Failed to load session:', error);
      clearSession();
      if (router.currentRoute.value.path !== '/error/no-token') {
        router.push('/error/no-token');
      }
    }
  }
})
</script>

<template>
  <AppHeader />
  
  <div class="pt-20">
    <router-view />
  </div>
  
  <AppFooter />
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

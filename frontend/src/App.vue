/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import SettingsHeader from './components/SettingsHeader.vue'
import LoginPanel from './views/settings/components/LoginPanel.vue'
import AppFooter from './components/AppFooter.vue'
import HelpIcon from './components/HelpIcon.vue'
import ErrorNotification from './components/ErrorNotification.vue'
import ExpiredSessionWarning from './components/ExpiredSessionWarning.vue'
import {api} from './api'
import {useSession} from './composables/useSession'
import {useDarkMode} from './composables/useDarkMode'

const router = useRouter()
const route = useRoute()
const {userSession, setSession, setUserSession, setGuildMeta, clearSession, loadSettings, loadPremiumFeatures} = useSession()
useDarkMode()

const isSettingsPage = computed(() => route.path.startsWith('/settings/edit'))
const showSettingsHeader = computed(() => route.path.startsWith('/settings'))
const isSetupPage = computed(() => route.path.startsWith('/setup'))
const ready = ref(false)

async function loadSession() {
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get('token');

  if (token) {
    api.setToken(token);
    // Remove token from URL
    urlParams.delete('token');
    const newRelativePathQuery = window.location.pathname + (urlParams.toString() ? '?' + urlParams.toString() : '');
    window.history.replaceState(null, '', newRelativePathQuery);

    // Redirect to originally requested page if available
    const redirectPath = localStorage.getItem('reputation_bot_oauth_redirect');
    if (redirectPath) {
      localStorage.removeItem('reputation_bot_oauth_redirect');
      if (redirectPath !== window.location.pathname + window.location.search) {
        router.push(redirectPath);
      }
    }
  }

  // Check if token exists in localStorage
  let storedToken = localStorage.getItem('reputation_bot_token');

  // If we don't have a token, redirect to login unless on public page
  if (!storedToken && router.currentRoute.value.path !== '/error/no-token') {
    ready.value = true;
    return;
  }

  // If token exists, try to load session data
  if (storedToken) {
    try {
      const userSessionData = await api.getSession();
      setUserSession(userSessionData);
      const isBotOwner = userSessionData.isBotOwner;

      // Select guild:
      // 1. Query param 'guild'
      // 2. Fallback to localStorage
      // 3. First guild with admin role
      // 4. First guild in list
      let guildId: string | null = urlParams.get('guild') || (route.query.guild as string | null);

      if (!guildId) {
        // Fallback to localStorage if no query param
        guildId = localStorage.getItem('reputation_bot_guild_id');
      }

      if (!guildId || (!userSessionData.guilds[guildId] && !isBotOwner)) {
        // Find first admin guild
        const adminGuild = Object.entries(userSessionData.guilds)
            .find(([_, data]) => data.accessLevel === 'GUILD_ADMIN');

        if (adminGuild) {
          guildId = adminGuild[0];
        } else {
          // Use first available guild
          const guildIds = Object.keys(userSessionData.guilds);
          guildId = guildIds.length > 0 ? (guildIds[0] as string) : null;
        }
      }

      if (guildId) {
        localStorage.setItem('reputation_bot_guild_id', guildId);
        const [sessionData, metaData] = await Promise.all([
          api.getGuildSession(),
          api.getGuildMeta()
        ]);
        setSession(sessionData);
        setGuildMeta(metaData);

        if (isSettingsPage.value) {
          await Promise.all([
            loadSettings(),
            loadPremiumFeatures()
          ]);
        }
      }
      ready.value = true
    } catch (error: any) {
      // If session loading fails, clear session and set expired state if it's a 401
      console.error('Failed to load session:', error);
      if (error.response?.status === 401) {
        clearSession();
      }
      ready.value = true
    }
  } else {
    ready.value = true
  }
}

onMounted(loadSession)

watch(() => route.query.guild, (newGuildId) => {
  if (newGuildId) {
    loadSession()
  }
})

watch(isSettingsPage, async (isSettings) => {
  if (isSettings) {
    await loadSettings()
  }
})
</script>

<template>
  <AppHeader/>
  <div class="h-[73px]"></div>

  <template v-if="ready">
    <SettingsHeader v-if="showSettingsHeader && userSession"/>

    <div :class="{'pt-8': showSettingsHeader}">
      <div v-if="(showSettingsHeader || isSetupPage) && !userSession" class="mx-auto px-4" style="max-width: 1200px;">
        <LoginPanel class="mt-8"/>
      </div>
      <router-view v-else/>
    </div>

    <AppFooter/>

    <HelpIcon v-if="isSettingsPage"/>
  </template>

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

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useRoute} from 'vue-router'
import {computed} from 'vue'
import UserDisplay from '@/components/UserDisplay.vue'

const {t} = useI18n()
const route = useRoute()
const isLoggedIn = computed(() => !!localStorage.getItem('reputation_bot_token'))
</script>

<template>
  <header
      class="fixed top-0 left-0 z-50 w-screen border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
    <div class="mx-auto flex justify-between items-center p-4" style="max-width: 1600px;">
      <div class="flex items-center gap-4">
        <a>
          <img alt="Reputation Bot Logo" class="logo-small" src="/favicon.ico"/>
        </a>
      </div>

      <nav v-if="!route.path.startsWith('/setup')" class="flex items-center gap-8">
        <router-link
            active-class="text-indigo-600 dark:text-indigo-400"
            class="text-gray-700 dark:text-gray-300 hover:text-indigo-600 dark:hover:text-indigo-400 font-medium transition-colors"
            to="/settings/edit"
        >
          {{ t('navigation.settings') }}
        </router-link>
      </nav>

      <div class="flex items-center gap-4">
        <UserDisplay v-if="isLoggedIn" />
      </div>
    </div>
  </header>
</template>

<style scoped>
.logo-small {
  height: 2.5em;
  will-change: filter;
  transition: filter 300ms;
}

.logo-small:hover {
  filter: drop-shadow(0 0 1em #646cffaa);
}

.logo-small.vue:hover {
  filter: drop-shadow(0 0 1em #42b883aa);
}
</style>

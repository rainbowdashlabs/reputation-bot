/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import LocaleSwitcher from '@/components/appheader/LocaleSwitcher.vue'
import LightModeSwitch from "@/components/appheader/LightModeSwitch.vue";

const { t } = useI18n()
</script>

<template>
  <header class="fixed top-0 left-0 z-50 flex justify-between items-center p-4 border-b border-gray-200 dark:border-gray-700 w-screen bg-white dark:bg-gray-800">
    <div class="flex items-center gap-4">
      <a>
        <img src="/favicon.ico" class="logo-small" alt="Reputation Bot Logo" />
      </a>
    </div>
    
    <nav class="flex items-center gap-8">
      <router-link 
        to="/settings" 
        class="text-gray-700 dark:text-gray-300 hover:text-indigo-600 dark:hover:text-indigo-400 font-medium transition-colors"
        active-class="text-indigo-600 dark:text-indigo-400"
      >
        {{ t('navigation.settings') }}
      </router-link>
      <router-link 
        to="/presets" 
        class="text-gray-700 dark:text-gray-300 hover:text-indigo-600 dark:hover:text-indigo-400 font-medium transition-colors"
        active-class="text-indigo-600 dark:text-indigo-400"
      >
        {{ t('navigation.presets') }}
      </router-link>
      <router-link 
        to="/setup" 
        class="text-gray-700 dark:text-gray-300 hover:text-indigo-600 dark:hover:text-indigo-400 font-medium transition-colors"
        active-class="text-indigo-600 dark:text-indigo-400"
      >
        {{ t('navigation.setup') }}
      </router-link>
    </nav>
    
    <div class="flex items-center gap-4">
      <LightModeSwitch/>
      <LocaleSwitcher />
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

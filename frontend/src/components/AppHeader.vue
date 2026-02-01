<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import LocaleSwitcher from './LocaleSwitcher.vue'

const { t } = useI18n()
</script>

<template>
  <header class="absolute top-0 left-0 z-50 flex justify-between items-center p-4 border-b border-gray-200 w-screen bg-white">
    <div class="flex items-center gap-4">
      <a>
        <img src="/favicon.ico" class="logo-small" alt="Reputation Bot Logo" />
      </a>
    </div>
    
    <nav class="flex items-center gap-8">
      <router-link 
        to="/settings" 
        class="text-gray-700 hover:text-indigo-600 font-medium transition-colors"
        active-class="text-indigo-600"
      >
        {{ t('navigation.settings') }}
      </router-link>
      <router-link 
        to="/presets" 
        class="text-gray-700 hover:text-indigo-600 font-medium transition-colors"
        active-class="text-indigo-600"
      >
        {{ t('navigation.presets') }}
      </router-link>
      <router-link 
        to="/setup" 
        class="text-gray-700 hover:text-indigo-600 font-medium transition-colors"
        active-class="text-indigo-600"
      >
        {{ t('navigation.setup') }}
      </router-link>
    </nav>
    
    <LocaleSwitcher />
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

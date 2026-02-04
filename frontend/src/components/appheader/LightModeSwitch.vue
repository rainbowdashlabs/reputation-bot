/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useDarkMode} from '@/composables/useDarkMode'
import BaseButton from '@/components/BaseButton.vue'

const {isDark, toggleDarkMode} = useDarkMode()

</script>

<template>
  <BaseButton
      :class="isDark ? 'bg-gray-700 text-gray-300 hover:bg-gray-600' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'"
      :title="isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'"
      class="p-2"
      color="secondary"
      style="background-color: transparent; color: inherit; border: none; box-shadow: none;"
      @click.stop="toggleDarkMode"
  >
    <svg v-if="isDark" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364-6.364l-.707.707M6.343 17.657l-.707.707m12.728 0l-.707-.707M6.343 6.343l-.707-.707M12 8a4 4 0 100 8 4 4 0 000-8z" stroke-linecap="round" stroke-linejoin="round"
            stroke-width="2"/>
    </svg>
    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" stroke-linecap="round" stroke-linejoin="round"
            stroke-width="2"/>
    </svg>
  </BaseButton>

</template>

<style scoped>

</style>

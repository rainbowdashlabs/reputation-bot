/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
const isOpen = ref(false)

const toggleHelp = () => {
  isOpen.value = !isOpen.value
}

const closeHelp = () => {
  isOpen.value = false
}
</script>

<template>
  <div class="fixed top-32 right-4 z-40">
    <!-- Help Button -->
    <button
        class="flex items-center justify-center w-10 h-10 rounded-full bg-indigo-600 text-white shadow-lg hover:bg-indigo-700 transition-colors focus:outline-none"
        @click="toggleHelp"
        :aria-label="t('help.title')"
    >
      <span class="text-xl font-bold">?</span>
    </button>

    <!-- Help Popup -->
    <div
        v-if="isOpen"
        class="absolute right-0 mt-2 w-72 p-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-xl z-50"
    >
      <div class="flex justify-between items-start mb-2">
        <h3 class="font-bold text-gray-900 dark:text-white">{{ t('help.title') }}</h3>
        <button
            @click="closeHelp"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      <p class="text-sm text-gray-600 dark:text-gray-300">
        {{ t('help.message') }}
      </p>
    </div>

    <!-- Backdrop to close when clicking outside -->
    <Teleport to="body">
      <div
          v-if="isOpen"
          class="fixed inset-0 z-30"
          @click="closeHelp"
      ></div>
    </Teleport>
  </div>
</template>

<style scoped>
</style>

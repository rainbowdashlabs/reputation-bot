/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'

const props = defineProps<{
  title: string
  description: string
  settings: string
}>()

const emit = defineEmits<{
  apply: []
}>()

const isApplying = ref(false)
const isApplied = ref(false)

const handleApply = async () => {
  isApplying.value = true
  try {
    await emit('apply')
    isApplied.value = true
  } finally {
    isApplying.value = false
  }
}
</script>

<template>
  <div class="flex bg-white justify-between dark:bg-gray-800 shadow rounded-lg p-6 transition-colors">
    <div>
      <h3 class="text-xl font-semibold text-gray-900 dark:text-white">{{ title }}</h3>
      <p class="text-gray-700 dark:text-gray-300 mb-3 italic">{{ description }}</p>
      <p class="text-sm text-gray-600 dark:text-gray-400">{{ settings }}</p>
    </div>
    <div class="flex justify-between items-start mb-4">
      <button
          :disabled="isApplying"
          :class="[
            'px-4 py-2 text-white rounded-md transition-colors',
            isApplied
              ? 'bg-green-600 hover:bg-green-700'
              : 'bg-blue-600 hover:bg-blue-700',
            'disabled:bg-gray-400 disabled:cursor-not-allowed'
          ]"
          @click="handleApply"
      >
        <span v-if="isApplied">{{ $t('presets.applied') }}</span>
        <span v-else-if="!isApplying">{{ $t('presets.apply') }}</span>
        <span v-else>{{ $t('presets.applying') }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>

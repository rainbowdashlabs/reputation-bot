/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'

const emit = defineEmits<{
  (e: 'canProceed', value: boolean): void;
  (e: 'scanStarted', value: boolean): void;
}>()

const {t} = useI18n()
const isStarting = ref(false)
const scanStarted = ref(false)
const error = ref<string | null>(null)

onMounted(() => {
  emit('canProceed', true)
})

const startScan = async () => {
  isStarting.value = true
  error.value = null
  try {
    await api.startScan()
    scanStarted.value = true
    emit('scanStarted', true)
  } catch (err: any) {
    if (err.response?.status === 409) {
      scanStarted.value = true
      emit('scanStarted', true)
    } else {
      console.error('Failed to start scan:', err)
      error.value = t('setup.steps.scan.error')
    }
  } finally {
    isStarting.value = false
  }
}
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.scan.description') }}
    </p>

    <div v-if="error" class="p-4 bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 rounded-lg">
      {{ error }}
    </div>

    <div class="flex flex-col items-center justify-center p-8 bg-gray-50 dark:bg-gray-900/50 rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700">
      <div v-if="scanStarted" class="text-center">
        <div class="flex items-center justify-center w-12 h-12 rounded-full bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 mb-4 mx-auto">
          <font-awesome-icon icon="check" class="text-xl" />
        </div>
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ t('setup.steps.scan.started.title') }}
        </h3>
        <p class="text-sm text-gray-500 dark:text-gray-400">
          {{ t('setup.steps.scan.started.description') }}
        </p>
      </div>
      <div v-else class="text-center">
        <div class="flex items-center justify-center w-12 h-12 rounded-full bg-indigo-100 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 mb-4 mx-auto">
          <font-awesome-icon icon="search" class="text-xl" />
        </div>
        <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
          {{ t('setup.steps.scan.prompt.title') }}
        </h3>
        <p class="text-sm text-gray-500 dark:text-gray-400 mb-6">
          {{ t('setup.steps.scan.prompt.description') }}
        </p>
        <button
            :disabled="isStarting"
            class="px-6 py-2 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition-colors disabled:opacity-50"
            @click="startScan"
        >
          <span v-if="isStarting">{{ t('setup.steps.scan.starting') }}</span>
          <span v-else>{{ t('setup.steps.scan.start') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

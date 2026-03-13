/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onMounted, onUnmounted, ref} from 'vue'
import type {ScanResult} from '@/api/types'
import SettingsContainer from './components/SettingsContainer.vue'
import ScanProgressItem from './components/ScanProgressItem.vue'
import {useI18n} from 'vue-i18n'

import {api} from '@/api'
const {t} = useI18n()
const scanResult = ref<ScanResult | null>(null)
const isLoading = ref(true)
const isStarting = ref(false)
const error = ref<string | null>(null)

let pollInterval: number | null = null

const fetchStatus = async () => {
  try {
    const result = await api.getScanStatus()
    scanResult.value = result
    error.value = null

    if (result.end === null && !pollInterval) {
      startPolling()
    } else if (result.end !== null && pollInterval) {
      stopPolling()
    }
  } catch (err: any) {
    if (err.response?.status === 404) {
      scanResult.value = null
      error.value = null
    } else {
      console.error('Failed to fetch scan status:', err)
      error.value = t('scan.error.fetchStatus')
    }
  } finally {
    isLoading.value = false
  }
}

const startScan = async () => {
  isStarting.value = true
  error.value = null
  try {
    await api.startScan()
    await fetchStatus()
  } catch (err: any) {
    if (err.response?.status === 409) {
      error.value = t('scan.error.alreadyRunning')
    } else {
      console.error('Failed to start scan:', err)
      error.value = t('scan.error.startFailed')
    }
  } finally {
    isStarting.value = false
  }
}

const startPolling = () => {
  if (pollInterval) return
  pollInterval = window.setInterval(fetchStatus, 5000)
}

const stopPolling = () => {
  if (pollInterval) {
    clearInterval(pollInterval)
    pollInterval = null
  }
}

onMounted(() => {
  fetchStatus()
})

onUnmounted(() => {
  stopPolling()
})

const getPercentage = (scanned: number, max: number) => {
  if (max === 0) return 100
  return Math.min(Math.round((scanned / max) * 100), 100)
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}
</script>

<template>
  <SettingsContainer :description="t('scan.description')" :title="t('settings.scan')">
    <template #header-actions>
      <button
          :disabled="isStarting || !!(scanResult && scanResult.end === null)"
          class="px-4 py-2 rounded-md font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed bg-indigo-600 text-white hover:bg-indigo-700"
          @click="startScan"
      >
        <span v-if="isStarting">{{ t('scan.starting') }}</span>
        <span v-else>{{ t('scan.start') }}</span>
      </button>
    </template>

    <div v-if="isLoading" class="flex justify-center p-8">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
    </div>

    <div v-else-if="error" class="p-4 bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 rounded-lg mb-6">
      {{ error }}
    </div>

    <div v-if="scanResult" class="space-y-6">
      <div class="flex flex-col gap-4">
        <div class="flex justify-between items-end">
          <div>
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">
              <span v-if="scanResult.end === null">{{ t('scan.status.running') }}</span>
              <span v-else>{{ t('scan.status.finished') }}</span>
            </h3>
            <p class="text-sm text-gray-500 dark:text-gray-400">
              {{ t('scan.startedAt') }}: {{ formatDate(scanResult.start) }}
              <span v-if="scanResult.end">| {{ t('scan.finishedAt') }}: {{ formatDate(scanResult.end) }}</span>
            </p>
          </div>
          <div class="text-right">
            <span class="text-2xl font-bold text-indigo-600 dark:text-indigo-400">
              {{ getPercentage(scanResult.progress.scanned, scanResult.progress.maxMessages) }}%
            </span>
          </div>
        </div>

        <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-4 overflow-hidden">
          <div
              :style="{ width: getPercentage(scanResult.progress.scanned, scanResult.progress.maxMessages) + '%' }"
              class="bg-indigo-600 h-full transition-all duration-500 ease-out"
          ></div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="bg-gray-50 dark:bg-gray-900/50 p-4 rounded-lg border border-gray-200 dark:border-gray-700">
            <p class="text-sm text-gray-500 dark:text-gray-400 uppercase font-semibold tracking-wider">
              {{ t('scan.messagesScanned') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ scanResult.progress.scanned }} / {{ scanResult.progress.maxMessages }}
            </p>
          </div>
          <div class="bg-gray-50 dark:bg-gray-900/50 p-4 rounded-lg border border-gray-200 dark:border-gray-700">
            <p class="text-sm text-gray-500 dark:text-gray-400 uppercase font-semibold tracking-wider">
              {{ t('scan.reputationFound') }}
            </p>
            <p class="text-2xl font-bold text-gray-900 dark:text-white">
              {{ scanResult.progress.hits }}
            </p>
          </div>
        </div>

        <div class="mt-8">
          <h4 class="text-md font-semibold text-gray-900 dark:text-white mb-4 uppercase tracking-wider">
            {{ t('scan.progressDetails') }}
          </h4>
          <div class="bg-gray-50 dark:bg-gray-900/30 rounded-lg border border-gray-200 dark:border-gray-700 p-2">
            <template v-if="scanResult.progress.childs && scanResult.progress.childs.length > 0">
              <ScanProgressItem
                  v-for="child in scanResult.progress.childs"
                  :key="child.id"
                  :depth="0"
                  :progress="child"
              />
            </template>
            <ScanProgressItem v-else :depth="0" :progress="scanResult.progress"/>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!isLoading" class="p-8 text-center text-gray-500 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700 italic">
      {{ t('scan.noScanYet') }}
    </div>
  </SettingsContainer>
</template>

<style scoped>
</style>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import AddRankForm from './AddRankForm.vue'
import RankList from './RankList.vue'

const {t} = useI18n()

const isRefreshing = ref(false)
const refreshMessage = ref('')

const refreshRanks = async () => {
  if (isRefreshing.value) return

  isRefreshing.value = true
  refreshMessage.value = ''

  try {
    const result = await api.refreshRanks()
    if (result.alreadyRunning) {
      refreshMessage.value = t('general.ranks.refresh.alreadyRunning')
    } else {
      refreshMessage.value = t('general.ranks.refresh.success')
    }
  } catch (error) {
    console.error('Failed to refresh ranks:', error)
    refreshMessage.value = t('general.ranks.refresh.failed')
  } finally {
    isRefreshing.value = false
    // Clear message after 5 seconds
    setTimeout(() => {
      refreshMessage.value = ''
    }, 5000)
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="items-center justify-between">
      <div class="flex-1">
        <AddRankForm/>
      </div>
    </div>
    <RankList/>
    <div class="mt-4 flex flex-col items-end">
      <button
          :disabled="isRefreshing"
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          @click="refreshRanks"
      >
        {{ isRefreshing ? t('general.ranks.refresh.refreshing') : t('general.ranks.refresh.button') }}
      </button>
      <p v-if="refreshMessage" :class="refreshMessage.includes(t('general.ranks.refresh.failed')) ? 'text-red-600' : 'text-green-600'"
         class="mt-2 text-sm">
        {{ refreshMessage }}
      </p>
    </div>

  </div>
</template>

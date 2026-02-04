/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import type {RankEntry} from '@/api/types'
import AddRankForm from './AddRankForm.vue'
import RankList from './RankList.vue'

const {t} = useI18n()
const {session} = useSession()

const ranks = ref<RankEntry[]>([])
const isRefreshing = ref(false)
const refreshMessage = ref('')

watch(session, (newSession) => {
  if (newSession?.settings?.ranks) {
    ranks.value = JSON.parse(JSON.stringify(newSession.settings.ranks.ranks))
  }
}, {immediate: true})

const saveRanks = async () => {
  try {
    await api.updateRanks({ranks: ranks.value})
  } catch (error) {
    console.error('Failed to update ranks:', error)
  }
}

const onAddRank = async (newRank: RankEntry) => {
  ranks.value.push(newRank)
  await saveRanks()
}

const onUpdateRanks = async (updatedRanks: RankEntry[]) => {
  ranks.value = updatedRanks
  await saveRanks()
}

const onDeleteRank = async (roleId: string) => {
  ranks.value = ranks.value.filter(r => r.roleId !== roleId)
  await saveRanks()
}

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
        <AddRankForm :existing-ranks="ranks" @add="onAddRank"/>
      </div>
    </div>
    <RankList :ranks="ranks" @delete="onDeleteRank" @update="onUpdateRanks"/>
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

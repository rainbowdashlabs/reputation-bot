/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import type { BotlistVotePOJO, VoteLog } from '@/api/types.ts'
import ViewContainer from '@/components/ViewContainer.vue'
import SettingsContainer from '../settings/components/SettingsContainer.vue'
import LoginPanel from '../settings/components/LoginPanel.vue'
import TokenStats from './votingview/TokenStats.vue'
import BotlistTable from './votingview/BotlistTable.vue'
import VoteLogTable from './votingview/VoteLogTable.vue'
import TransferToGuild from './votingview/TransferToGuild.vue'
import BaseButton from '@/components/BaseButton.vue'

const { t } = useI18n()
const { userSession } = useSession()

const tokens = ref<number | null>(null)
const botlists = ref<BotlistVotePOJO[]>([])
const voteLogs = ref<VoteLog[]>([])
const loading = ref(true)
const logLoading = ref(false)
const page = ref(0)
const maxPages = ref(0)

const fetchVoteLog = async (newPage: number) => {
  logLoading.value = true
  try {
    const response = await api.getVoteLog(newPage, 25)
    voteLogs.value = response.content
    page.value = response.page
    maxPages.value = response.maxPages
  } catch (error) {
    console.error('Failed to fetch vote log:', error)
  } finally {
    logLoading.value = false
  }
}

onMounted(async () => {
  try {
    const [tokensResponse, botlistsResponse] = await Promise.all([
      api.getUserTokens(),
      api.getVoteLists()
    ])
    tokens.value = tokensResponse.tokens
    botlists.value = botlistsResponse
    await fetchVoteLog(0)
  } catch (error) {
    console.error('Failed to fetch voting data:', error)
  } finally {
    loading.value = false
  }
})

watch(page, (newPage) => {
  fetchVoteLog(newPage)
})
</script>

<template>
  <ViewContainer class="pt-8">
    <div v-if="!userSession" class="max-w-4xl mx-auto px-4">
      <LoginPanel />
    </div>
    <SettingsContainer v-else :description="t('voting.description')" :title="t('voting.title')">
      <div class="mb-4 px-4 py-3 rounded-md bg-indigo-50 dark:bg-indigo-900/20 text-indigo-700 dark:text-indigo-300 text-sm">
        {{ t('voting.noteDefaultGuild') }}
        <router-link to="/user/settings" class="underline font-medium">{{ t('voting.noteDefaultGuildLink') }}</router-link>.
      </div>
      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>

      <div v-else class="space-y-8">
        <TokenStats :tokens="tokens" />
        <TransferToGuild :max-tokens="tokens" @transferred="async () => { const t = await api.getUserTokens(); tokens = t.tokens; await fetchVoteLog(0); }" />
        <BotlistTable :botlists="botlists" />
        <div class="space-y-4">
          <VoteLogTable :vote-logs="voteLogs" :loading="logLoading" />
          <div v-if="maxPages > 1" class="flex justify-center gap-4 mt-4">
            <BaseButton
                :disabled="page === 0 || logLoading"
                color="secondary"
                class="px-4 py-2"
                @click="page--"
            >
              {{ t('common.previous') || 'Previous' }}
            </BaseButton>
            <span class="flex items-center text-sm text-gray-500 dark:text-gray-400">
              {{ t('common.pageOf', { current: page + 1, total: maxPages }) || `Page ${page + 1} of ${maxPages}` }}
            </span>
            <BaseButton
                :disabled="page >= maxPages - 1 || logLoading"
                color="secondary"
                class="px-4 py-2"
                @click="page++"
            >
              {{ t('common.next') || 'Next' }}
            </BaseButton>
          </div>
        </div>
      </div>
    </SettingsContainer>
  </ViewContainer>
</template>

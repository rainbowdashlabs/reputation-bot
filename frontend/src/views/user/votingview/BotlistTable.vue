/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import type { BotlistVotePOJO } from '@/api/types.ts'
import BaseButton from '@/components/BaseButton.vue'

defineProps<{
  botlists: BotlistVotePOJO[]
}>()

const { t } = useI18n()

const isNever = (dateStr: string) => {
  return dateStr.startsWith('1970-01-01')
}

const formattedDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}
</script>

<template>
  <section>
    <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">{{ t('voting.botlists.title') }}</h3>
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">{{ t('voting.botlists.description') }}</p>

    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('voting.botlists.name') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('voting.botlists.streak') }}</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">{{ t('voting.botlists.lastVote') }}</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider"></th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="botlist in botlists" :key="botlist.name">
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">{{ botlist.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300">
                {{ botlist.streak }} {{ t('voting.botlists.days') }}
              </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
              {{ isNever(botlist.lastVote) ? t('voting.botlists.never') : formattedDate(botlist.lastVote) }}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
              <a :href="botlist.voteUrl" target="_blank" class="inline-block">
                <BaseButton class="px-4 py-1.5" color="indigo">
                  {{ t('voting.botlists.vote') }}
                </BaseButton>
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

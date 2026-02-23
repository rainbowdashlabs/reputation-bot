/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import type { VoteLog } from '@/api/types.ts'
import VoteLogRow from './VoteLogRow.vue'

interface Props {
  voteLogs: VoteLog[]
  loading?: boolean
}

defineProps<Props>()

const { t } = useI18n()
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h3 class="text-lg font-medium text-gray-900 dark:text-white">{{ t('voting.log.title') }}</h3>
    </div>
    <p class="text-sm text-gray-500 dark:text-gray-400">{{ t('voting.log.description') }}</p>

    <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 rounded-lg">
      <table class="min-w-full divide-y divide-gray-300 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-900/50">
          <tr>
            <th scope="col" class="py-2 px-3 text-left text-xs font-semibold text-gray-900 dark:text-white uppercase tracking-wider">
              {{ t('voting.log.date') }}
            </th>
            <th scope="col" class="py-2 px-3 text-left text-xs font-semibold text-gray-900 dark:text-white uppercase tracking-wider">
              {{ t('voting.log.guild') }}
            </th>
            <th scope="col" class="py-2 px-3 text-left text-xs font-semibold text-gray-900 dark:text-white uppercase tracking-wider">
              {{ t('voting.log.reason.label') }}
            </th>
            <th scope="col" class="py-2 px-3 text-right text-xs font-semibold text-gray-900 dark:text-white uppercase tracking-wider">
              {{ t('voting.log.tokens') }}
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200 dark:divide-gray-800 bg-white dark:bg-gray-800">
          <tr v-if="loading" v-for="i in 3" :key="i" class="animate-pulse">
            <td v-for="j in 4" :key="j" class="py-2 px-3">
              <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-full"></div>
            </td>
          </tr>
          <tr v-else-if="voteLogs.length === 0">
            <td colspan="4" class="py-8 text-center text-sm text-gray-500 dark:text-gray-400">
              {{ t('common.noData') || 'No entries found' }}
            </td>
          </tr>
          <VoteLogRow v-for="log in voteLogs" :key="log.created" :log="log" />
        </tbody>
      </table>
    </div>
  </div>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import type {RankingEntryPOJO} from '@/api/types'
import MemberDisplay from '@/components/display/MemberDisplay.vue'

defineProps<{
  title: string
  entries: RankingEntryPOJO[]
}>()

const emit = defineEmits<{
  (e: 'click-member', memberId: string): void
}>()

const {t} = useI18n()
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
      <h2 class="text-xl font-semibold text-gray-900 dark:text-white">{{ title }}</h2>
    </div>
    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead class="bg-gray-50 dark:bg-gray-900/50">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-20">
            {{ t('dashboard.rank') }}
          </th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-32">
            {{ t('dashboard.reputation') }}
          </th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
            {{ t('dashboard.user') }}
          </th>
        </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
        <tr v-for="entry in entries" :key="entry.member.id"
            class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors cursor-pointer"
            @click="emit('click-member', entry.member.id)">
          <td class="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900 dark:text-white">
            #{{ entry.rank }}
          </td>
          <td class="px-6 py-4 whitespace-nowrap text-left text-sm font-bold text-indigo-600 dark:text-indigo-400">
            {{ entry.value }}
          </td>
          <td class="px-6 py-4 whitespace-nowrap">
            <MemberDisplay :member="entry.member"/>
          </td>
        </tr>
        <tr v-if="entries.length === 0">
          <td colspan="3" class="px-6 py-10 text-center text-gray-500 dark:text-gray-400">
            {{ t('common.noData') }}
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
</style>

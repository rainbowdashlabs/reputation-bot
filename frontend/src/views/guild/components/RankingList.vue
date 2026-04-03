/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import type {RankingEntryStatPOJO} from '@/api/types'
import MemberDisplay from '@/components/display/MemberDisplay.vue'

const props = defineProps<{
  title: string
  entries: RankingEntryStatPOJO[]
  accentColor?: 'indigo' | 'purple'
  page?: number
  totalPages?: number
}>()

const emit = defineEmits<{
  (e: 'prev'): void
  (e: 'next'): void
  (e: 'click-member', memberId: string): void
}>()

const {t} = useI18n()

const hasPagination = () => (props.totalPages ?? 0) > 1
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ title }}</h2>
    </div>
    <ul class="divide-y divide-gray-200 dark:divide-gray-700">
      <li
          v-for="entry in entries"
          :key="entry.member.id"
          class="px-6 py-3 flex items-center gap-3 text-sm cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
          @click="emit('click-member', entry.member.id)"
      >
        <span class="w-8 text-gray-500 dark:text-gray-400 shrink-0">#{{ entry.rank }}</span>
        <span
            class="font-bold w-12 shrink-0"
            :class="accentColor === 'purple'
              ? 'text-purple-600 dark:text-purple-400'
              : 'text-indigo-600 dark:text-indigo-400'"
        >{{ entry.value }}</span>
        <MemberDisplay :member="entry.member" class="flex-1 min-w-0" />
      </li>
      <li v-if="entries.length === 0" class="px-6 py-6 text-center text-gray-500 dark:text-gray-400">
        {{ t('common.noData') }}
      </li>
    </ul>
    <div v-if="hasPagination()" class="flex items-center justify-between px-6 py-3 border-t border-gray-200 dark:border-gray-700">
      <button :disabled="(page ?? 0) === 0" class="px-3 py-1 text-xs rounded border border-gray-300 dark:border-gray-600 disabled:opacity-40" @click="emit('prev')">{{ t('common.previous') }}</button>
      <span class="text-xs text-gray-500 dark:text-gray-400">{{ t('common.pageOf', {current: (page ?? 0) + 1, total: totalPages}) }}</span>
      <button :disabled="(page ?? 0) >= (totalPages ?? 1) - 1" class="px-3 py-1 text-xs rounded border border-gray-300 dark:border-gray-600 disabled:opacity-40" @click="emit('next')">{{ t('common.next') }}</button>
    </div>
  </div>
</template>

<style scoped>
</style>

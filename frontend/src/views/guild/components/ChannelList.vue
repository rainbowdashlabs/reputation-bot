/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import type {ChannelStatsPOJO} from '@/api/types'

defineProps<{
  title: string
  entries: ChannelStatsPOJO[]
  accentColor?: 'indigo' | 'purple'
  channelName: (channelId: string) => string
}>()

const {t} = useI18n()
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
    <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
      <h2 class="text-lg font-semibold text-gray-900 dark:text-white">{{ title }}</h2>
    </div>
    <ul class="divide-y divide-gray-200 dark:divide-gray-700">
      <li
          v-for="entry in entries"
          :key="entry.channelId"
          class="px-6 py-3 flex items-center justify-between text-sm"
      >
        <span class="font-medium text-gray-900 dark:text-white">#{{ channelName(entry.channelId) }}</span>
        <span
            class="font-bold"
            :class="accentColor === 'indigo'
              ? 'text-indigo-600 dark:text-indigo-400'
              : 'text-purple-600 dark:text-purple-400'"
        >{{ entry.count }}</span>
      </li>
      <li v-if="entries.length === 0" class="px-6 py-6 text-center text-gray-500 dark:text-gray-400">
        {{ t('common.noData') }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
</style>

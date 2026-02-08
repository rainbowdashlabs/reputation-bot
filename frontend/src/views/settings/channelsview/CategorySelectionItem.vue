/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import type { CategoryPOJO } from '@/api/types'
import ChannelSelectionItem from './ChannelSelectionItem.vue'

const { t } = useI18n()

const props = defineProps<{
  category: CategoryPOJO
  selected: boolean
  disabled: boolean
  isChannelSelected: (channelId: string) => boolean
  isChannelLimitReached: boolean
}>()

const emit = defineEmits<{
  (e: 'toggleCategory', categoryId: string): void
  (e: 'toggleChannel', channelId: string): void
}>()
</script>

<template>
  <div class="bg-white dark:bg-gray-900">
    <div
      class="flex items-center justify-between p-3 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
      @click="emit('toggleCategory', category.id)"
    >
      <div class="flex items-center gap-3">
        <input
          :checked="selected"
          :disabled="!selected && disabled"
          class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 disabled:opacity-50"
          type="checkbox"
          @click.stop="emit('toggleCategory', category.id)"
        />
        <span class="font-medium text-gray-900 dark:text-gray-100 uppercase text-xs tracking-wider">
          {{ category.name }}
        </span>
      </div>
      <span class="text-xs text-gray-500 uppercase">{{ t('general.channels.list.categories') }}</span>
    </div>

    <!-- Channels in Category -->
    <div class="divide-y divide-gray-100 dark:divide-gray-800 bg-gray-50/50 dark:bg-gray-800/50">
      <ChannelSelectionItem
        v-for="channel in category.channels"
        :key="channel.id"
        :channel="channel"
        :selected="isChannelSelected(channel.id) || selected"
        :disabled="selected || (!isChannelSelected(channel.id) && isChannelLimitReached)"
        class="pl-10"
        @toggle="emit('toggleChannel', channel.id)"
      />
    </div>
  </div>
</template>

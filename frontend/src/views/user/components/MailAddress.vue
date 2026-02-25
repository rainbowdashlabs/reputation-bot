/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { computed } from 'vue'
import type { MailEntryPOJO } from '@/api/types'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  entry: MailEntryPOJO
}>()

const emit = defineEmits<{
  (e: 'delete', hash: string): void
}>()

const { t } = useI18n()

const isDeletable = computed(() => props.entry.source === 'USER')
const sourceLabel = computed(() => t(`user.settings.mails.source.${props.entry.source.toLowerCase() as 'user'|'discord'|'kofi'}`))
</script>

<template>
  <div class="flex items-center justify-between p-3 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800">
    <div class="flex items-center gap-3">
      <font-awesome-icon :icon="['fas','check-circle']" class="text-green-500" v-if="entry.verified"/>
      <font-awesome-icon :icon="['fas','circle-exclamation']" class="text-yellow-500" v-else/>
      <div class="flex flex-col">
        <span class="font-medium text-gray-900 dark:text-gray-100">{{ entry.mailShort }}</span>
        <div class="text-xs text-gray-500 dark:text-gray-400 flex items-center gap-2">
          <span class="px-2 py-0.5 rounded bg-gray-100 dark:bg-gray-700">{{ sourceLabel }}</span>
          <span v-if="entry.verified">{{ t('user.settings.mails.verified') }}</span>
          <span v-else>{{ t('user.settings.mails.pending') }}</span>
        </div>
      </div>
    </div>
    <button
      class="inline-flex items-center gap-2 px-3 py-1.5 text-sm rounded-md border border-red-300 dark:border-red-700 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 disabled:opacity-50"
      :disabled="!isDeletable"
      @click="emit('delete', entry.hash)"
    >
      <font-awesome-icon :icon="['fas','trash']"/>
      {{ t('common.delete') }}
    </button>
  </div>
</template>

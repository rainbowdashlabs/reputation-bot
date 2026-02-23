/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import type { VoteLog } from '@/api/types.ts'
import { useSession } from '@/composables/useSession.ts'

interface Props {
  log: VoteLog
}

defineProps<Props>()

const { t } = useI18n()
const { userSession } = useSession()

const formattedDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}

const getGuildName = (guildId: string) => {
  if (guildId === '0') return t('voting.log.personal')
  return userSession.value?.guilds[guildId]?.name || t('voting.log.unknownGuild')
}

const getReasonLabel = (reason: string) => {
  return t(`voting.log.reason.${reason}`) || t('voting.log.reason.unknown')
}

const getReasonDescription = (reason: string) => {
  return t(`voting.log.reason.${reason}_DESCRIPTION`) || t('voting.log.reason.unknown_DESCRIPTION')
}

const getReasonColor = (reason: string) => {
  switch (reason) {
    case 'STANDARD':
    case 'STREAK':
    case 'BONUS':
      return 'text-green-600 dark:text-green-400'
    case 'TRANSFER':
    case 'USE':
      return 'text-red-600 dark:text-red-400'
    default:
      return 'text-gray-600 dark:text-gray-400'
  }
}

const isNegativeReason = (reason: string) => {
  return reason === 'TRANSFER' || reason === 'USE'
}

const getTokensDisplay = (log: VoteLog) => {
  const amount = isNegativeReason(log.reason) ? -Math.abs(log.tokens) : log.tokens
  const sign = amount > 0 ? '+' : ''
  return `${sign}${amount}`
}

const getTokensColor = (log: VoteLog) => {
  const amount = isNegativeReason(log.reason) ? -Math.abs(log.tokens) : log.tokens
  if (amount > 0) return 'text-green-600 dark:text-green-400'
  if (amount < 0) return 'text-red-600 dark:text-red-400'
  return 'text-gray-600 dark:text-gray-400'
}
</script>

<template>
  <tr class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors group">
    <td class="py-2 px-3 text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">
      {{ formattedDate(log.created) }}
    </td>
    <td class="py-2 px-3 text-sm text-gray-900 dark:text-white font-medium">
      {{ getGuildName(log.guildId) }}
    </td>
    <td class="py-2 px-3 text-sm relative">
      <div class="flex items-center gap-1.5 cursor-help" :class="getReasonColor(log.reason)">
        <span>{{ getReasonLabel(log.reason) }}</span>
        <font-awesome-icon :icon="['fas', 'question-circle']" class="w-3 h-3 opacity-50 group-hover:opacity-100 transition-opacity" />

        <!-- Simple Tooltip -->
        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 hidden group-hover:block z-10">
          <div class="bg-gray-900 text-white text-xs rounded py-1 px-2 whitespace-nowrap shadow-lg">
            {{ getReasonDescription(log.reason) }}
            <div class="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-gray-900"></div>
          </div>
        </div>
      </div>
    </td>
    <td class="py-2 px-3 text-sm text-right font-bold" :class="getTokensColor(log)">
      {{ getTokensDisplay(log) }}
    </td>
  </tr>
</template>

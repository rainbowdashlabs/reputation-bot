/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'

interface Props {
  reactionIds: string[]
}

const props = defineProps<Props>()
const {t} = useI18n()
const {session} = useSession()

// Check if value is a unicode emoji (simple heuristic)
const isUnicodeEmoji = (str: string): boolean => {
  // Check if it's not a snowflake ID (numeric string)
  return !!(str && !/^\d+$/.test(str))
}

const findReaction = (reactionId: string) => {
  return session.value?.guild?.reactions?.find(r => r.id === reactionId)
}
</script>

<template>
  <template v-if="reactionIds.length === 0">
    <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
  </template>
  <template v-else>
    <span
        v-for="reactionId in reactionIds"
        :key="reactionId"
        class="inline-flex items-center"
    >
      <!-- Unicode emoji -->
      <span v-if="isUnicodeEmoji(reactionId)" class="text-xl">{{ reactionId }}</span>
      <!-- Custom emoji -->
      <template v-else>
        <img
            v-if="findReaction(reactionId)"
            :src="findReaction(reactionId)!.url"
            :alt="findReaction(reactionId)!.name"
            class="w-6 h-6"
        />
        <span v-else class="text-gray-500 dark:text-gray-400 italic">:{{ reactionId }}:</span>
      </template>
    </span>
  </template>
</template>

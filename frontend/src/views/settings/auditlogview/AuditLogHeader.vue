/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {MemberPOJO} from '@/api/types'
import MemberDisplay from './MemberDisplay.vue'

interface Props {
  member: MemberPOJO | undefined
  timestamp: string
}

const props = defineProps<Props>()
const {t} = useI18n()

// Format the timestamp
const formattedDate = computed(() => {
  const date = new Date(props.timestamp)
  return date.toLocaleString()
})
</script>

<template>
  <div class="flex items-center justify-between flex-wrap gap-2">
    <div class="flex items-center gap-2">
      <MemberDisplay v-if="member" :member="member" />
      <span v-else class="text-gray-500 dark:text-gray-400 italic">
        {{ t('auditLog.unknownMember') }}
      </span>
      <span class="text-gray-500 dark:text-gray-400">•</span>
      <span class="text-sm text-gray-500 dark:text-gray-400">{{ formattedDate }}</span>
    </div>
  </div>
</template>

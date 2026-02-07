/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useSession} from '@/composables/useSession.ts'

interface Props {
  channelId: string
}

const props = defineProps<Props>()
const {session} = useSession()

const channel = computed(() => {
  const channels = session.value?.guild?.channels
  if (!channels) return undefined

  // Search in direct channels
  const directChannel = channels.channels?.find(c => c.id === props.channelId)
  if (directChannel) return directChannel

  // Search in categories
  for (const category of channels.categories || []) {
    const channelInCategory = category.channels?.find(c => c.id === props.channelId)
    if (channelInCategory) return channelInCategory
  }

  return undefined
})
</script>

<template>
  <span v-if="channel" class="inline-flex items-center px-2 py-1 rounded text-sm font-medium text-gray-700 dark:text-gray-300">
    #{{ channel.name }}
  </span>
  <span v-else class="text-gray-500 italic">Unknown Channel</span>
</template>

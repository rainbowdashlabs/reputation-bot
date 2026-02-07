/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import ChannelDisplay from '@/components/display/ChannelDisplay.vue'

interface Props {
  channelId?: string
  oldChannelId?: string
  newChannelId?: string
}

const props = defineProps<Props>()

const isComparison = computed(() => {
  return props.oldChannelId !== undefined && props.newChannelId !== undefined && props.oldChannelId !== props.newChannelId
})

const effectiveChannelId = computed(() => {
  if (props.channelId !== undefined) return props.channelId
  return props.newChannelId !== undefined ? props.newChannelId : props.oldChannelId
})
</script>

<template>
  <template v-if="isComparison">
    <ChannelValue :channel-id="oldChannelId" />
    <span class="text-gray-400 dark:text-gray-500 mx-1">→</span>
    <ChannelValue :channel-id="newChannelId" />
  </template>
  <template v-else-if="effectiveChannelId">
    <ChannelDisplay :channel-id="effectiveChannelId" />
  </template>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { computed } from 'vue'
import type { ChannelPOJO } from '@/api/types'

const props = defineProps<{
  channel: ChannelPOJO
  selected: boolean
  disabled: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle', channelId: string): void
}>()

const getChannelIcon = (type: string) => {
  switch (type) {
    case 'TEXT':
      return 'hashtag'
    case 'VOICE':
      return 'volume-high'
    case 'NEWS':
      return 'bullhorn'
    case 'FORUM':
      return 'comments'
    default:
      return 'hashtag'
  }
}

const icon = computed(() => getChannelIcon(props.channel.type))
</script>

<template>
  <div
    :class="{ 'opacity-50 pointer-events-none': disabled }"
    class="flex items-center justify-between p-3 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer transition-colors"
    @click="emit('toggle', channel.id)"
  >
    <div class="flex items-center gap-3">
      <input
        :checked="selected"
        :disabled="disabled"
        class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 disabled:opacity-50"
        type="checkbox"
        @click.stop="emit('toggle', channel.id)"
      />
      <span class="text-gray-700 dark:text-gray-300 flex items-center gap-2">
        <font-awesome-icon :icon="icon" class="text-gray-400 dark:text-gray-500" />
        {{ channel.name }}
      </span>
    </div>
    <slot name="right"></slot>
  </div>
</template>

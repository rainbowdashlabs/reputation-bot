/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import ChannelDisplay from '@/components/display/ChannelDisplay.vue'

interface Props {
  channelIds?: string[]
  oldChannelIds?: string[]
  newChannelIds?: string[]
}

const props = defineProps<Props>()
const {t} = useI18n()

const addedItems = computed(() => {
  if (!props.oldChannelIds || !props.newChannelIds) return []
  const oldSet = new Set(props.oldChannelIds)
  return props.newChannelIds.filter(id => !oldSet.has(id))
})

const removedItems = computed(() => {
  if (!props.oldChannelIds || !props.newChannelIds) return []
  const newSet = new Set(props.newChannelIds)
  return props.oldChannelIds.filter(id => !newSet.has(id))
})

const isComparison = computed(() => {
  return props.oldChannelIds !== undefined && props.newChannelIds !== undefined
})

const effectiveChannelIds = computed(() => {
  if (props.channelIds !== undefined) return props.channelIds
  return props.newChannelIds !== undefined ? props.newChannelIds : props.oldChannelIds || []
})
</script>

<template>
  <template v-if="isComparison">
    <div class="flex flex-col gap-2">
      <div v-if="addedItems.length > 0" class="flex items-center gap-2">
        <span class="text-green-600 dark:text-green-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'plus']" class="h-5 w-5" />
        </span>
        <ChannelsListValue :channel-ids="addedItems" />
      </div>
      <div v-if="removedItems.length > 0" class="flex items-center gap-2">
        <span class="text-red-600 dark:text-red-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'minus']" class="h-5 w-5" />
        </span>
        <ChannelsListValue :channel-ids="removedItems" />
      </div>
    </div>
  </template>
  <template v-else>
    <template v-if="effectiveChannelIds.length === 0">
      <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
    </template>
    <template v-else>
      <div class="flex flex-col">
        <ChannelDisplay
            v-for="channelId in effectiveChannelIds"
            :key="channelId"
            :channel-id="channelId"
        />
      </div>
    </template>
  </template>
</template>

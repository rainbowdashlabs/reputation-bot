/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'

interface Props {
  reactionIds?: string[]
  oldReactionIds?: string[]
  newReactionIds?: string[]
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

const addedItems = computed(() => {
  if (!props.oldReactionIds || !props.newReactionIds) return []
  const oldSet = new Set(props.oldReactionIds)
  return props.newReactionIds.filter(id => !oldSet.has(id))
})

const removedItems = computed(() => {
  if (!props.oldReactionIds || !props.newReactionIds) return []
  const newSet = new Set(props.newReactionIds)
  return props.oldReactionIds.filter(id => !newSet.has(id))
})

const isComparison = computed(() => {
  return props.oldReactionIds !== undefined && props.newReactionIds !== undefined
})

const effectiveReactionIds = computed(() => {
  if (props.reactionIds !== undefined) return props.reactionIds
  return props.newReactionIds !== undefined ? props.newReactionIds : props.oldReactionIds || []
})
</script>

<template>
  <template v-if="isComparison">
    <div class="flex flex-col gap-2">
      <div v-if="addedItems.length > 0" class="flex items-center gap-2">
        <span class="text-green-600 dark:text-green-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'plus']" class="h-5 w-5" />
        </span>
        <ReactionsListValue :reaction-ids="addedItems" />
      </div>
      <div v-if="removedItems.length > 0" class="flex items-center gap-2">
        <span class="text-red-600 dark:text-red-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'minus']" class="h-5 w-5" />
        </span>
        <ReactionsListValue :reaction-ids="removedItems" />
      </div>
    </div>
  </template>
  <template v-else>
    <template v-if="effectiveReactionIds.length === 0">
      <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
    </template>
    <template v-else>
      <div class="flex flex-col">
        <span
            v-for="reactionId in effectiveReactionIds"
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
      </div>
    </template>
  </template>
</template>

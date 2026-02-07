/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'

interface Props {
  words?: string[]
  oldWords?: string[]
  newWords?: string[]
}

const props = defineProps<Props>()
const {t} = useI18n()

const addedItems = computed(() => {
  if (!props.oldWords || !props.newWords) return []
  const oldSet = new Set(props.oldWords)
  return props.newWords.filter(word => !oldSet.has(word))
})

const removedItems = computed(() => {
  if (!props.oldWords || !props.newWords) return []
  const newSet = new Set(props.newWords)
  return props.oldWords.filter(word => !newSet.has(word))
})

const isComparison = computed(() => {
  return props.oldWords !== undefined && props.newWords !== undefined
})

const effectiveWords = computed(() => {
  if (props.words !== undefined) return props.words
  return props.newWords !== undefined ? props.newWords : props.oldWords || []
})
</script>

<template>
  <template v-if="isComparison">
    <div class="flex flex-col gap-2">
      <div v-if="addedItems.length > 0" class="flex items-center gap-2">
        <span class="text-green-600 dark:text-green-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'plus']" class="h-5 w-5" />
        </span>
        <WordsListValue :words="addedItems" />
      </div>
      <div v-if="removedItems.length > 0" class="flex items-center gap-2">
        <span class="text-red-600 dark:text-red-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'minus']" class="h-5 w-5" />
        </span>
        <WordsListValue :words="removedItems" />
      </div>
    </div>
  </template>
  <template v-else>
    <template v-if="effectiveWords.length === 0">
      <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
    </template>
    <template v-else>
      <div class="flex flex-col">
        <span
            v-for="(word, index) in effectiveWords"
            :key="index"
            class="inline-flex items-center px-2 py-1 rounded text-sm bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300"
        >
          {{ word }}
        </span>
      </div>
    </template>
  </template>
</template>

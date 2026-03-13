/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
import {ref, computed} from 'vue'
import type {ScanProgress} from '@/api/types'
import {ScanTarget} from '@/api/types'
import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome'

const props = defineProps<{
  progress: ScanProgress
  depth: number
}>()

const isExpanded = ref(true)

const toggleExpand = () => {
  if (props.progress.childs && props.progress.childs.length > 0) {
    isExpanded.value = !isExpanded.value
  }
}

const getName = computed(() => props.progress.name || props.progress.id)

const getIcon = (target: ScanTarget) => {
  switch (target) {
    case ScanTarget.GUILD:
      return 'server'
    case ScanTarget.CATEGORY:
      return 'folder'
    case ScanTarget.TEXT:
      return 'hashtag'
    case ScanTarget.VOICE:
      return 'volume-high'
    case ScanTarget.FORUM:
      return 'comments'
    case ScanTarget.THREAD:
      return 'arrow-turn-up'
    default:
      return 'hashtag'
  }
}

const getPercentage = (scanned: number, max: number) => {
  if (max === 0) return 100
  return Math.min(Math.round((scanned / max) * 100), 100)
}
</script>

<template>
  <div class="flex flex-col">
    <div
        :style="{ paddingLeft: depth * 12 + 'px' }"
        class="group flex items-center py-1 px-2 rounded hover:bg-gray-200 dark:hover:bg-gray-800 cursor-pointer transition-colors"
        @click="toggleExpand"
    >
      <!-- Expand Arrow -->
      <div class="w-4 flex items-center justify-center mr-1">
        <font-awesome-icon
            v-if="progress.childs && progress.childs.length > 0"
            :icon="isExpanded ? 'chevron-down' : 'chevron-right'"
            class="text-xs text-gray-500 transition-transform"
        />
      </div>

      <!-- Icon -->
      <div class="w-5 flex items-center justify-center mr-2 text-gray-500 dark:text-gray-400 group-hover:text-gray-700 dark:group-hover:text-gray-200 transition-colors">
        <font-awesome-icon :icon="getIcon(progress.target)" class="text-sm"/>
      </div>

      <!-- Label and Progress -->
      <div class="flex-grow flex items-center justify-between min-w-0">
        <span class="truncate text-sm font-medium text-gray-700 dark:text-gray-300 mr-2">
          {{ getName }}
        </span>

        <div class="flex items-center gap-4 flex-shrink-0">
          <div class="hidden sm:flex items-center gap-2 text-xs text-gray-500">
            <span>{{ progress.scanned }} / {{ progress.maxMessages }}</span>
            <span class="text-indigo-500 font-semibold">{{ progress.hits }} {{ t('scan.reputationFound') }}</span>
          </div>

          <div class="w-16 h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div
                :style="{ width: getPercentage(progress.scanned, progress.maxMessages) + '%' }"
                class="bg-indigo-500 h-full transition-all duration-300"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Children -->
    <div v-if="isExpanded && progress.childs && progress.childs.length > 0">
      <ScanProgressItem
          v-for="child in progress.childs"
          :key="child.id"
          :depth="depth + 1"
          :progress="child"
      />
    </div>
  </div>
</template>

<style scoped>
</style>

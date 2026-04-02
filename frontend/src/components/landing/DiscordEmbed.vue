<!--
    SPDX-License-Identifier: AGPL-3.0-only

    Copyright (C) RainbowDashLabs and Contributor
-->
<script lang="ts" setup>
defineProps<{
  title: string
  description: string
  targets: string[]
}>()
</script>

<template>
  <div class="ml-11 mt-1">
    <div class="border-l-4 border-indigo-500 bg-gray-100 dark:bg-gray-700 rounded-r-lg p-3 max-w-xs">
      <div class="text-xs font-semibold text-indigo-600 dark:text-indigo-400 mb-1">{{ title }}</div>
      <div class="text-xs text-gray-700 dark:text-gray-300 mb-2">{{ description }}</div>
    </div>
    <div class="flex flex-wrap gap-2 mt-2">
      <button
        v-for="target in targets"
        :key="target"
        class="text-xs bg-[#5865F2] hover:bg-[#4752C4] text-white px-3 py-1.5 rounded font-medium transition-colors"
      >
        {{ target }}
      </button>
      <button class="flex items-center gap-1 bg-red-500 hover:bg-red-600 text-white text-xs px-2 py-1.5 rounded transition-colors">
        <font-awesome-icon :icon="['fas', 'trash']" class="text-[10px]" />
      </button>
    </div>
  </div>
</template>

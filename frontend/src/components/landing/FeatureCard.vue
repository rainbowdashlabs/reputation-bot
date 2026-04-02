/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
defineProps<{
  icon: string[]
  title: string
  description: string
}>()
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl shadow p-6 border border-gray-100 dark:border-gray-700 flex flex-col items-center text-center">
    <div class="w-12 h-12 bg-indigo-100 dark:bg-indigo-900/50 rounded-full flex items-center justify-center mb-4">
      <font-awesome-icon :icon="icon" class="text-2xl text-indigo-600 dark:text-indigo-400" />
    </div>
    <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-2">{{ title }}</h3>
    <p class="text-gray-600 dark:text-gray-400 text-sm">{{ description }}</p>
  </div>
</template>

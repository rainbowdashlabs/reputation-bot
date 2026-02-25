/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
defineProps<{
  title: string
  value?: string | number
  borderColor?: string
}>()
</script>

<template>
  <div
      :class="[borderColor || 'border-blue-500']"
      class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 border-l-4"
  >
    <div class="text-sm font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
      {{ title }}
    </div>
    <div class="mt-2 text-3xl font-bold text-gray-900 dark:text-white">
      <slot>
        {{ value }}
      </slot>
    </div>
  </div>
</template>

<style scoped>
</style>

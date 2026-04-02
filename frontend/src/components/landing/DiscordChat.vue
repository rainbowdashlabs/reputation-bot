<!--
    SPDX-License-Identifier: AGPL-3.0-only

    Copyright (C) RainbowDashLabs and Contributor
-->
<script lang="ts" setup>
defineProps<{
  channelName?: string
}>()
</script>

<template>
  <div class="bg-gray-50 dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden shadow-md w-full max-w-md mx-auto">
    <!-- Channel header -->
    <div class="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 px-4 py-2 flex items-center gap-2">
      <font-awesome-icon :icon="['fas', 'hashtag']" class="text-gray-400 dark:text-gray-500 text-sm" />
      <span class="text-sm font-semibold text-gray-700 dark:text-gray-300">{{ channelName ?? 'general' }}</span>
    </div>
    <!-- Messages -->
    <div class="p-3 flex flex-col gap-2">
      <slot />
    </div>
  </div>
</template>

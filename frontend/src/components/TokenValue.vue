/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
defineProps<{
  tokens: number | null
  iconClass?: string
}>()
</script>

<template>
  <span class="inline-flex items-center">
    <font-awesome-icon :icon="['fas', 'coins']" :class="['mr-1.5', iconClass || 'text-yellow-500']"/>
    <span v-if="tokens !== null" class="font-bold">{{ tokens }}</span>
    <span v-else class="animate-pulse bg-gray-200 dark:bg-gray-700 h-4 w-8 rounded"></span>
  </span>
</template>

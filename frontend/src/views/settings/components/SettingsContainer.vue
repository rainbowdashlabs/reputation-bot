/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
// Common container for settings subviews
import Header2 from "@/components/heading/Header2.vue";

defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="mb-6">
    <div class="flex items-center justify-between gap-4 mb-4">
      <Header2 class="!mb-0">{{ title }}</Header2>
      <slot name="header-actions"/>
    </div>
    <p v-if="description" class="text-gray-600 dark:text-gray-400 mb-4 -mt-1">
      {{ description }}
    </p>
    <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6 transition-colors">
      <slot/>
    </div>
  </div>
</template>

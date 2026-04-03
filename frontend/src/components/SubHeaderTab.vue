/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
const props = defineProps<{
  to?: string
  active: boolean
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <router-link
      v-if="props.to"
      :to="props.to"
      class="px-6 py-3 text-sm font-medium transition-colors border-b-2 flex items-center gap-2 flex-1 text-center justify-center"
      :class="[
        active
          ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
          : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
      ]"
  >
    <slot/>
  </router-link>
  <button
      v-else
      class="px-6 py-3 text-sm font-medium transition-colors border-b-2 flex items-center gap-2 flex-1 text-center justify-center"
      :class="[
        active
          ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
          : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
      ]"
      @click="emit('click')"
  >
    <slot/>
  </button>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
const props = defineProps<{
  placeholder: string
}>()

const emit = defineEmits<{ (e: 'submit', value: string): void }>()

const value = defineModel<string>({ default: '' })

const onSubmit = () => {
  if (!value.value) return
  emit('submit', value.value)
}
</script>

<template>
  <div class="flex gap-2">
    <input
      v-model="value"
      type="email"
      class="flex-1 block w-full rounded-lg border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm transition-colors"
      :placeholder="placeholder"
    />
    <button
      class="inline-flex items-center gap-2 px-3 py-2 text-sm rounded-md border border-indigo-300 dark:border-indigo-700 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-50 dark:hover:bg-indigo-900/30"
      @click="onSubmit"
    >
      <font-awesome-icon :icon="['fas','plus']" />
      <slot />
    </button>
  </div>
</template>

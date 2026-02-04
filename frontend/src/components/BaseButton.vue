/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
interface Props {
  color?: 'indigo' | 'gray' | 'red' | 'blue' | 'secondary' | 'primary' | 'danger'
  rounded?: boolean
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
  class?: any
}

const props = withDefaults(defineProps<Props>(), {
  color: 'indigo' as any,
  rounded: true,
  disabled: false,
  type: 'button',
  class: ''
})

const colorClasses = {
  indigo: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500',
  primary: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500',
  gray: 'bg-gray-600 text-white hover:bg-gray-700 focus:ring-gray-500',
  secondary: 'bg-gray-200 text-gray-700 hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600 focus:ring-gray-500',
  red: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
  blue: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
}
</script>

<template>
  <button
      :type="type"
      :disabled="disabled"
      class="text-sm font-medium shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      :class="[
      colorClasses[color as keyof typeof colorClasses],
      rounded ? 'rounded-md' : 'rounded-none',
      props.class
    ]"
  >
      <slot/>
  </button>
</template>

<style scoped>
button {
  border-radius: inherit;
  padding: inherit;
  font-size: inherit;
  font-weight: inherit;
}

button:hover {
  border-color: transparent;
}
</style>

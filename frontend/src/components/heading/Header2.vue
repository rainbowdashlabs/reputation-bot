<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  color?: 'default' | 'indigo' | 'green' | 'red' | 'blue';
}

const props = withDefaults(defineProps<Props>(), {
  color: 'default'
})

const colorClasses = computed(() => {
  switch (props.color) {
    case 'indigo':
      return 'text-indigo-600 dark:text-indigo-400'
    case 'green':
      return 'text-green-600 dark:text-green-400'
    case 'red':
      return 'text-red-600 dark:text-red-400'
    case 'blue':
      return 'text-blue-600 dark:text-blue-400'
    default:
      return 'text-gray-900 dark:text-white'
  }
})
</script>

<template>
  <h2 :class="['text-2xl font-bold mb-4 transition-colors', colorClasses]">
    <slot />
  </h2>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  type?: 'success' | 'warning' | 'error'
}>(), {
  type: 'success'
})

const icon = computed(() => {
  switch (props.type) {
    case 'success':
      return ['fas', 'check-circle']
    case 'warning':
      return ['fas', 'circle-exclamation']
    case 'error':
      return ['fas', 'circle-exclamation']
  }
})

const classes = computed(() => {
  switch (props.type) {
    case 'success':
      return 'p-3 rounded border border-green-300 bg-green-50 text-green-700 dark:bg-green-900/20 dark:border-green-700 dark:text-green-300'
    case 'warning':
      return 'p-3 rounded border border-yellow-300 bg-yellow-50 text-yellow-700 dark:bg-yellow-900/20 dark:border-yellow-700 dark:text-yellow-300'
    case 'error':
      return 'p-3 rounded border border-red-300 bg-red-50 text-red-700 dark:bg-red-900/20 dark:border-red-700 dark:text-red-300'
  }
})
</script>

<template>
  <div :class="classes">
    <font-awesome-icon :icon="icon as any" class="mr-2" />
    <slot />
  </div>
</template>

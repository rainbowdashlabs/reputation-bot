/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'

interface Props {
  value: string
}

const props = defineProps<Props>()

const displayValue = computed(() => {
  return new Date(props.value).toLocaleDateString()
})
</script>

<template>
  <span class="text-sm text-gray-900 dark:text-gray-100">
    {{ displayValue }}
  </span>
</template>

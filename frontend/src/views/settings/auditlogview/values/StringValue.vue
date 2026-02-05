/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
interface Props {
  value: string
}

defineProps<Props>()
</script>

<template>
  <span class="text-sm text-gray-900 dark:text-gray-100">
    {{ value }}
  </span>
</template>

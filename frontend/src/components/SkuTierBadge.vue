/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import type { SkuInfo } from '@/api/types'

interface Props {
  sku: SkuInfo
  active: boolean
}

defineProps<Props>()
</script>

<template>
  <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-100 dark:bg-indigo-900/40 text-indigo-800 dark:text-indigo-300">
    {{ sku.name }}
    <font-awesome-icon
      v-if="active"
      icon="check-circle"
      class="text-green-500"
    />
  </span>
</template>

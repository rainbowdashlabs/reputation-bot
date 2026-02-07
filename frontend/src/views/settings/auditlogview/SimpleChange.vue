/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AuditLogValue from './AuditLogValue.vue'

interface Props {
  settingsKey: string
  oldValue: any
  newValue: any
}

defineProps<Props>()
</script>

<template>
  <div class="flex items-start gap-2 flex-wrap">
    <div class="flex items-center gap-2">
      <AuditLogValue :settings-key="settingsKey" :value="oldValue" :is-old-value="true" />
    </div>
    <span class="text-gray-400 dark:text-gray-500">→</span>
    <div class="flex items-center gap-2">
      <AuditLogValue :settings-key="settingsKey" :value="newValue" />
    </div>
  </div>
</template>

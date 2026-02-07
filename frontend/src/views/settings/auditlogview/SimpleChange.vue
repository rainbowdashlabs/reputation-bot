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
    <AuditLogValue :settings-key="settingsKey" :old-value="oldValue" :new-value="newValue" />
  </div>
</template>

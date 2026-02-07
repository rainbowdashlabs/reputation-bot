/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import AuditLogValue from './AuditLogValue.vue'

interface Props {
  settingsKey: string
  oldValue: any
  newValue: any
}

const props = defineProps<Props>()
const {t} = useI18n()

// Get a human-readable setting name
const settingName = computed(() => {
  if (props.settingsKey.startsWith('integration_bypass.')) {
    return t('settings.integrationBypass')
  }
  // Try to get translation, fallback to the key itself
  const key = `auditLog.settings.${props.settingsKey.toLowerCase().replace(/\./g, '_')}`
  const translated = t(key)
  return translated !== key ? translated : props.settingsKey
})
</script>

<template>
  <div class="text-sm">
    <span class="font-semibold text-gray-700 dark:text-gray-300">{{ settingName }}</span>
    <span class="text-gray-700 dark:text-gray-300">: </span>
    <AuditLogValue :settings-key="settingsKey" :old-value="oldValue" :new-value="newValue" />
  </div>
</template>

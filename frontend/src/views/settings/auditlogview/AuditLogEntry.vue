/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {SettingsAuditLogPOJO, MemberPOJO} from '@/api/types'
import AuditLogHeader from './AuditLogHeader.vue'
import AuditLogSimpleEntry from './AuditLogSimpleEntry.vue'
import AuditLogChange from './AuditLogChange.vue'

interface Props {
  log: SettingsAuditLogPOJO
  member: MemberPOJO | undefined
}

const props = defineProps<Props>()
const {t} = useI18n()

// Get a human-readable setting name
const settingName = computed(() => {
  if (props.log.settingsKey.startsWith('integration_bypass.')) {
    return t('settings.integrationBypass')
  }
  // Try to get translation, fallback to the key itself
  const key = `auditLog.settings.${props.log.settingsKey.toLowerCase().replace(/\./g, '_')}`
  const translated = t(key)
  return translated !== key ? translated : props.log.settingsKey
})

// Check if this is a simple value change (not a list)
const isSimpleValue = computed(() => {
  if (props.log.settingsKey.startsWith('integration_bypass.')) {
    return true
  }
  return !Array.isArray(props.log.oldValue) && !Array.isArray(props.log.newValue)
})
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 space-y-3 transition-colors">
    <!-- Header: Member and timestamp -->
    <AuditLogHeader :member="member" :timestamp="log.changed" />

    <!-- Setting name with inline simple values -->
    <template v-if="isSimpleValue">
      <AuditLogSimpleEntry
          :settings-key="log.settingsKey"
          :old-value="log.oldValue"
          :new-value="log.newValue"
      />
    </template>

    <!-- For list values, show setting name and then changes -->
    <template v-else>
      <div class="text-sm">
        <span class="font-semibold text-gray-700 dark:text-gray-300">{{ settingName }}</span>
      </div>

      <div class="pl-4 border-l-2 border-gray-200 dark:border-gray-700">
        <AuditLogChange
            :settings-key="log.settingsKey"
            :old-value="log.oldValue"
            :new-value="log.newValue"
        />
      </div>
    </template>
  </div>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {SettingsAuditLogPOJO, MemberPOJO} from '@/api/types'
import MemberDisplay from './MemberDisplay.vue'
import AuditLogChange from './AuditLogChange.vue'
import AuditLogValue from './AuditLogValue.vue'

interface Props {
  log: SettingsAuditLogPOJO
  member: MemberPOJO | undefined
}

const props = defineProps<Props>()
const {t} = useI18n()

// Format the timestamp
const formattedDate = computed(() => {
  const date = new Date(props.log.changed)
  return date.toLocaleString()
})

// Get a human-readable setting name
const settingName = computed(() => {
  // Try to get translation, fallback to the key itself
  const key = `auditLog.settings.${props.log.settingsKey.toLowerCase().replace(/\./g, '_')}`
  const translated = t(key)
  return translated !== key ? translated : props.log.settingsKey
})

// Check if this is a simple value change (not a list)
const isSimpleValue = computed(() => {
  return !Array.isArray(props.log.oldValue) && !Array.isArray(props.log.newValue)
})

// Check if this is a boolean value
const isBooleanValue = computed(() => {
  return typeof props.log.newValue === 'boolean'
})
</script>

<template>
  <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-4 space-y-3 transition-colors">
    <!-- Header: Member and timestamp -->
    <div class="flex items-center justify-between flex-wrap gap-2">
      <div class="flex items-center gap-2">
        <MemberDisplay v-if="member" :member="member" />
        <span v-else class="text-gray-500 dark:text-gray-400 italic">
          {{ t('auditLog.unknownMember') }}
        </span>
        <span class="text-gray-500 dark:text-gray-400">•</span>
        <span class="text-sm text-gray-500 dark:text-gray-400">{{ formattedDate }}</span>
      </div>
    </div>

    <!-- Setting name with inline simple values -->
    <div class="text-sm">
      <span class="font-semibold text-gray-700 dark:text-gray-300">{{ settingName }}</span>
      <!-- Inline simple values -->
      <template v-if="isSimpleValue">
        <span class="text-gray-700 dark:text-gray-300">: </span>
        <!-- For booleans, show only new value -->
        <template v-if="isBooleanValue">
          <AuditLogValue :settings-key="log.settingsKey" :value="log.newValue" />
        </template>
        <!-- For other simple values, show old → new -->
        <template v-else>
          <AuditLogValue :settings-key="log.settingsKey" :value="log.oldValue" :is-old-value="true" />
          <span class="text-gray-400 dark:text-gray-500"> → </span>
          <AuditLogValue :settings-key="log.settingsKey" :value="log.newValue" />
        </template>
      </template>
    </div>

    <!-- Changes for list values only -->
    <div v-if="!isSimpleValue" class="pl-4 border-l-2 border-gray-200 dark:border-gray-700">
      <AuditLogChange
          :settings-key="log.settingsKey"
          :old-value="log.oldValue"
          :new-value="log.newValue"
      />
    </div>
  </div>
</template>

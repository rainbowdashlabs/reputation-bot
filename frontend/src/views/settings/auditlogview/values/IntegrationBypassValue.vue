/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {Bypass} from '@/api/types'
import {useSession} from '@/composables/useSession'
import MemberDisplay from '../MemberDisplay.vue'

interface Props {
  value: Bypass | null | undefined
}

const props = defineProps<Props>()
const {t} = useI18n()
const {session} = useSession()

const integration = computed(() => {
  if (!props.value?.integrationId) return undefined
  return session.value?.guild?.integrations?.find(i => i.id === props.value?.integrationId)
})

const items = computed(() => {
  if (!props.value) return []
  return [
    {label: t('integrationBypass.allowReactions'), value: props.value.allowReactions},
    {label: t('integrationBypass.allowAnswer'), value: props.value.allowAnswer},
    {label: t('integrationBypass.allowMention'), value: props.value.allowMention},
    {label: t('integrationBypass.allowFuzzy'), value: props.value.allowFuzzy},
    {label: t('integrationBypass.ignoreCooldown'), value: props.value.ignoreCooldown},
    {label: t('integrationBypass.ignoreLimit'), value: props.value.ignoreLimit},
  ]
})
</script>

<template>
  <div v-if="value" class="flex flex-col gap-1 p-2 bg-gray-50 dark:bg-gray-900/50 rounded border border-gray-200 dark:border-gray-700">
    <div class="flex items-center gap-2 mb-1 border-b border-gray-200 dark:border-gray-700 pb-1">
      <MemberDisplay v-if="integration" :member="integration" />
      <span v-else class="text-gray-500 italic">{{ value.integrationId }}</span>
    </div>
    <div class="grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
      <div v-for="item in items" :key="item.label" class="flex items-center justify-between gap-2">
        <span class="text-gray-600 dark:text-gray-400">{{ item.label }}:</span>
        <span :class="item.value ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'" class="font-medium">
          {{ item.value ? t('auditLog.values.enabled') : t('auditLog.values.disabled') }}
        </span>
      </div>
    </div>
  </div>
  <span v-else class="text-gray-500 italic">{{ t('auditLog.values.null') }}</span>
</template>

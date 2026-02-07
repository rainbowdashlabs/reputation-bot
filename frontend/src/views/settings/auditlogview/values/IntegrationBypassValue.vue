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
import BooleanValue from './BooleanValue.vue'
import IntegrationHeader from './IntegrationHeader.vue'

interface Props {
  value?: Bypass | null | undefined
  oldValue?: Bypass | null | undefined
  newValue?: Bypass | null | undefined
}

const props = defineProps<Props>()
const {t} = useI18n()
const {session} = useSession()

const effectiveValue = computed(() => {
  if (props.value !== undefined) return props.value
  return (props.newValue !== undefined && props.newValue !== null) ? props.newValue : props.oldValue
})

const isComparison = computed(() => {
  return !!props.oldValue && !!props.newValue
})

const integration = computed(() => {
  const val = effectiveValue.value
  if (!val?.integrationId) return undefined
  return session.value?.guild?.integrations?.find(i => i.id === val?.integrationId)
})

const items = computed(() => {
  const val = effectiveValue.value
  if (!val) return []
  return [
    {label: t('integrationBypass.allowReactions'), value: val.allowReactions, key: 'allowReactions'},
    {label: t('integrationBypass.allowAnswer'), value: val.allowAnswer, key: 'allowAnswer'},
    {label: t('integrationBypass.allowMention'), value: val.allowMention, key: 'allowMention'},
    {label: t('integrationBypass.allowFuzzy'), value: val.allowFuzzy, key: 'allowFuzzy'},
    {label: t('integrationBypass.allowDirect'), value: val.allowDirect, key: 'allowDirect'},
    {label: t('integrationBypass.ignoreCooldown'), value: val.ignoreCooldown, key: 'ignoreCooldown'},
    {label: t('integrationBypass.ignoreLimit'), value: val.ignoreLimit, key: 'ignoreLimit'},
    {label: t('integrationBypass.ignoreContext'), value: val.ignoreContext, key: 'ignoreContext'},
  ]
})

const diffItems = computed(() => {
  if (!props.oldValue || !props.newValue) return []
  const diffs: Array<{label: string, old: boolean, new: boolean}> = []
  const keys: Array<{label: string, key: keyof Bypass}> = [
    {label: t('integrationBypass.allowReactions'), key: 'allowReactions'},
    {label: t('integrationBypass.allowAnswer'), key: 'allowAnswer'},
    {label: t('integrationBypass.allowMention'), key: 'allowMention'},
    {label: t('integrationBypass.allowFuzzy'), key: 'allowFuzzy'},
    {label: t('integrationBypass.allowDirect'), key: 'allowDirect'},
    {label: t('integrationBypass.ignoreCooldown'), key: 'ignoreCooldown'},
    {label: t('integrationBypass.ignoreLimit'), key: 'ignoreLimit'},
    {label: t('integrationBypass.ignoreContext'), key: 'ignoreContext'},
  ]

  for (const item of keys) {
    if (props.oldValue[item.key] !== props.newValue[item.key]) {
      console.log(`${props.oldValue[item.key]} - ${props.newValue[item.key]}`)
      diffs.push({
        label: item.label,
        old: props.oldValue[item.key] as boolean,
        new: props.newValue[item.key] as boolean
      })
    }
  }
  return diffs
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <!-- Values / Comparison -->
      <!-- Case 1: Addition -->
      <template v-if="!oldValue && newValue">
        <div class="flex items-center gap-2">
          <span class="text-green-600 dark:text-green-400 shrink-0">
            <font-awesome-icon :icon="['fas', 'plus']" class="h-4 w-4" />
          </span>
          <div class="flex flex-col gap-2">
            <IntegrationHeader :integration="integration" :integration-id="effectiveValue?.integrationId" />

            <div class="flex flex-col gap-1">
              <div v-for="item in items" :key="item.key" class="text-xs flex items-center gap-1">
                <span class="text-gray-500">{{ item.label }}:</span>
                <BooleanValue :value="item.value" />
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Case 2: Comparison (both present) -->
      <template v-else-if="isComparison">
        <div class="flex flex-col gap-2">
          <IntegrationHeader :integration="integration" :integration-id="effectiveValue?.integrationId" />

          <div>
            <div v-if="diffItems.length === 0" class="text-xs text-gray-500 italic">
              {{ t('auditLog.values.noChanges') }}
            </div>
            <div v-for="diff in diffItems" :key="diff.label" class="text-xs flex items-center gap-1">
              <span class="text-gray-700 dark:text-gray-300 font-medium">{{ diff.label }}:</span>
              <BooleanValue :value="diff.old" />
              <span class="text-gray-400">→</span>
              <BooleanValue :value="diff.new" />
            </div>
          </div>
        </div>
      </template>

      <!-- Case 3: Removal -->
      <template v-else-if="oldValue && !newValue">
        <div class="flex items-center gap-2">
          <span class="text-red-600 dark:text-red-400 shrink-0">
            <font-awesome-icon :icon="['fas', 'minus']" class="h-4 w-4" />
          </span>
          <div class="flex flex-col gap-2">
            <IntegrationHeader :integration="integration" :integration-id="effectiveValue?.integrationId" />

            <div class="flex flex-col gap-1 opacity-70">
              <div v-for="item in items" :key="item.key" class="text-xs flex items-center gap-1">
                <span class="text-gray-500">{{ item.label }}:</span>
                <BooleanValue :value="item.value" />
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Case 4: Single value display (neither oldValue nor newValue, e.g. from AuditLogValue directly) -->
      <template v-else-if="effectiveValue">
        <div class="flex flex-col gap-2">
          <IntegrationHeader :integration="integration" :integration-id="effectiveValue?.integrationId" />

          <div class="flex flex-col gap-1">
            <div v-for="item in items" :key="item.key" class="text-xs flex items-center gap-1">
              <span class="text-gray-500">{{ item.label }}:</span>
              <BooleanValue :value="item.value" />
            </div>
          </div>
        </div>
      </template>
    </div>
</template>

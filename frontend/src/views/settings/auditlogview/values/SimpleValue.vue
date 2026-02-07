/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import BooleanValue from './BooleanValue.vue'
import NumberValue from './NumberValue.vue'
import EnumValue from './EnumValue.vue'
import DateValue from './DateValue.vue'
import StringValue from './StringValue.vue'

interface Props {
  settingsKey: string
  value?: boolean | number | string | null | undefined
  oldValue?: boolean | number | string | null | undefined
  newValue?: boolean | number | string | null | undefined
}

const props = defineProps<Props>()
const {t} = useI18n()

const effectiveValue = computed(() => {
  if (props.value !== undefined) return props.value
  return props.newValue !== undefined ? props.newValue : props.oldValue
})

const valueType = computed(() => {
  const val = effectiveValue.value
  if (val === null || val === undefined) {
    return 'null'
  }

  const key = props.settingsKey.toLowerCase()

  if (typeof val === 'boolean') {
    return 'boolean'
  }

  if (typeof val === 'number') {
    return 'number'
  }

  if (typeof val === 'string') {
    // Enum values
    if (key.includes('cooldowndirection') || key.includes('reputationmode') ||
        key.includes('refreshtype') || key.includes('refreshinterval')) {
      return 'enum'
    }

    // ISO date
    if (key.includes('resetdate')) {
      return 'date'
    }

    // Language and regular strings
    return 'string'
  }

  return 'unknown'
})

const isComparison = computed(() => {
  return props.oldValue !== undefined && props.newValue !== undefined && props.oldValue !== props.newValue && valueType.value !== 'boolean'
})
</script>

<template>
  <template v-if="isComparison">
    <SimpleValue :settings-key="settingsKey" :value="oldValue" />
    <span class="text-gray-400 dark:text-gray-500 mx-1">→</span>
    <SimpleValue :settings-key="settingsKey" :value="newValue" />
  </template>
  <template v-else>
    <span v-if="valueType === 'null'" class="text-sm text-gray-900 dark:text-gray-100">
      {{ t('auditLog.values.null') }}
    </span>
    <BooleanValue v-else-if="valueType === 'boolean'" :value="effectiveValue as boolean" />
    <NumberValue v-else-if="valueType === 'number'" :settings-key="settingsKey" :value="effectiveValue as number" />
    <EnumValue v-else-if="valueType === 'enum'" :value="effectiveValue as string" />
    <DateValue v-else-if="valueType === 'date'" :value="effectiveValue as string" />
    <StringValue v-else-if="valueType === 'string'" :value="effectiveValue as string" />
    <span v-else class="text-sm text-gray-900 dark:text-gray-100">
      {{ String(effectiveValue) }}
    </span>
  </template>
</template>

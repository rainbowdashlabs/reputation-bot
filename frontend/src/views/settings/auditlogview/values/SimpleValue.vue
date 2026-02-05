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
  value: boolean | number | string | null | undefined
}

const props = defineProps<Props>()
const {t} = useI18n()

const valueType = computed(() => {
  if (props.value === null || props.value === undefined) {
    return 'null'
  }

  const key = props.settingsKey.toLowerCase()

  if (typeof props.value === 'boolean') {
    return 'boolean'
  }

  if (typeof props.value === 'number') {
    return 'number'
  }

  if (typeof props.value === 'string') {
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
</script>

<template>
  <span v-if="valueType === 'null'" class="text-sm text-gray-900 dark:text-gray-100">
    {{ t('auditLog.values.null') }}
  </span>
  <BooleanValue v-else-if="valueType === 'boolean'" :value="value as boolean" />
  <NumberValue v-else-if="valueType === 'number'" :settings-key="settingsKey" :value="value as number" />
  <EnumValue v-else-if="valueType === 'enum'" :value="value as string" />
  <DateValue v-else-if="valueType === 'date'" :value="value as string" />
  <StringValue v-else-if="valueType === 'string'" :value="value as string" />
  <span v-else class="text-sm text-gray-900 dark:text-gray-100">
    {{ String(value) }}
  </span>
</template>

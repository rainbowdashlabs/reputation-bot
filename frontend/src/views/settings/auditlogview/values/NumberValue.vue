/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'

interface Props {
  settingsKey: string
  value: number
}

const props = defineProps<Props>()
const {t} = useI18n()

const displayValue = computed(() => {
  const key = props.settingsKey.toLowerCase()

  // Special case for cooldown -1
  if (key.includes('cooldown') && props.value === -1) {
    return t('auditLog.values.once')
  }

  return props.value.toString()
})
</script>

<template>
  <span class="text-sm text-gray-900 dark:text-gray-100">
    {{ displayValue }}
  </span>
</template>

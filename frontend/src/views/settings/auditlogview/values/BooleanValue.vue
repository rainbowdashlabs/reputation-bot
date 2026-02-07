/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'

interface Props {
  value: boolean
}

const props = defineProps<Props>()
const {t} = useI18n()

const displayValue = computed(() => {
  return props.value ? t('auditLog.values.enabled') : t('auditLog.values.disabled')
})

const colorClass = computed(() => {
  return props.value ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
})
</script>

<template>
  <span class="text-sm" :class="colorClass">
    {{ displayValue }}
  </span>
</template>

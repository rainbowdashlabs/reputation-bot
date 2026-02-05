/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'

interface Props {
  value: string
}

const props = defineProps<Props>()
const {t} = useI18n()

const displayValue = computed(() => {
  return t(`auditLog.values.${props.value.toLowerCase()}`, props.value)
})
</script>

<template>
  <span class="text-sm text-gray-900 dark:text-gray-100">
    {{ displayValue }}
  </span>
</template>

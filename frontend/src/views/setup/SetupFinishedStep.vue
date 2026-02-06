/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onMounted} from 'vue'
import {useI18n} from 'vue-i18n'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const {t} = useI18n()

onMounted(() => {
  emit('canProceed', true)
})
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400 whitespace-pre-line">
      {{ t('setup.steps.finished.description') }}
    </p>
  </div>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ReputationTypeSettings from '@/views/settings/reputationview/ReputationTypeSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()

// Always allow proceeding (reputation types are optional)
watch(() => true, () => {
  emit('canProceed', true)
}, { immediate: true })
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.reputationTypes.description') }}
    </p>
    
    <ReputationTypeSettings />
  </div>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ThankwordInput from '@/views/settings/thankwordsview/ThankwordInput.vue'
import ThankwordList from '@/views/settings/thankwordsview/ThankwordList.vue'
import DefaultThankwords from '@/views/settings/thankwordsview/DefaultThankwords.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()

const isUpdating = ref(false)

// Always allow proceeding (thankwords are optional)
watch(() => true, () => {
  emit('canProceed', true)
}, { immediate: true })
</script>

<template>
  <div class="space-y-6">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.thankwords.description') }}
    </p>
    
    <div class="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
      <p class="text-sm text-blue-800 dark:text-blue-200">
        {{ t('setup.steps.thankwords.recommendation') }}
      </p>
    </div>
    
    <ThankwordInput :is-updating="isUpdating" @update:is-updating="isUpdating = $event" />
    <ThankwordList :is-updating="isUpdating" @update:is-updating="isUpdating = $event" />
    <DefaultThankwords :is-updating="isUpdating" @update:is-updating="isUpdating = $event" />
  </div>
</template>

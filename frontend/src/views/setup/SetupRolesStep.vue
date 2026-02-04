/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DonorRolesSettings from '@/views/settings/rolesview/DonorRolesSettings.vue'
import ReceiverRolesSettings from '@/views/settings/rolesview/ReceiverRolesSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()

// Always allow proceeding (roles are optional)
watch(() => true, () => {
  emit('canProceed', true)
}, { immediate: true })
</script>

<template>
  <div class="space-y-6">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.roles.description') }}
    </p>
    
    <div class="space-y-6">
      <div>
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4">
          {{ t('roles.donor.label') }}
        </h3>
        <DonorRolesSettings />
      </div>
      
      <hr class="border-gray-200 dark:border-gray-700" />
      
      <div>
        <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4">
          {{ t('roles.receiver.label') }}
        </h3>
        <ReceiverRolesSettings />
      </div>
    </div>
  </div>
</template>

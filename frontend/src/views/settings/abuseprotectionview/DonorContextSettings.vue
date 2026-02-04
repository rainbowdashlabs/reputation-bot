/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { session, updateAbuseProtectionSettings } = useSession()

const donorContext = computed({
  get: () => session.value?.settings.abuseProtection.donorContext ?? false,
  set: async (value) => {
    try {
      await api.updateAbuseProtectionDonorContext(value)
      updateAbuseProtectionSettings({ donorContext: value })
    } catch (error) {
      console.error('Failed to update donor context:', error)
    }
  }
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <Toggle
      v-model="donorContext"
      :label="t('abuseProtection.donorContext.label')"
    />
    <p class="description">{{ t('abuseProtection.donorContext.description') }}</p>
  </div>
</template>

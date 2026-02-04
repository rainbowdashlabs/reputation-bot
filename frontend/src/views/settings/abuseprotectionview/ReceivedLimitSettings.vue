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

const isEnabled = computed({
  get: () => (session.value?.settings.abuseProtection.maxReceived ?? 0) !== 0,
  set: async (value) => {
    const maxReceived = value ? 1 : 0
    const maxReceivedHours = value ? 1 : 0
    try {
      await api.updateAbuseProtectionMaxReceived(maxReceived)
      await api.updateAbuseProtectionMaxReceivedHours(maxReceivedHours)
      updateAbuseProtectionSettings({ maxReceived, maxReceivedHours })
    } catch (error) {
      console.error('Failed to update received limit status:', error)
    }
  }
})

const maxReceived = computed({
  get: () => session.value?.settings.abuseProtection.maxReceived ?? 1,
  set: async (value) => {
    if (value < 1) return
    try {
      await api.updateAbuseProtectionMaxReceived(value)
      updateAbuseProtectionSettings({ maxReceived: value })
    } catch (error) {
      console.error('Failed to update max received:', error)
    }
  }
})

const maxReceivedHours = computed({
  get: () => session.value?.settings.abuseProtection.maxReceivedHours ?? 1,
  set: async (value) => {
    if (value < 1) return
    try {
      await api.updateAbuseProtectionMaxReceivedHours(value)
      updateAbuseProtectionSettings({ maxReceivedHours: value })
    } catch (error) {
      console.error('Failed to update max received hours:', error)
    }
  }
})
</script>

<template>
  <div class="space-y-4">
    <Toggle
      v-model="isEnabled"
      :label="t('abuseProtection.receivedLimit.enabled.label')"
    />
    
    <div v-if="isEnabled" class="grid grid-cols-1 md:grid-cols-2 gap-4 ml-6">
      <div class="flex flex-col gap-2">
        <label class="label">{{ t('abuseProtection.receivedLimit.maxReceived.label') }}</label>
        <input
          v-model.number="maxReceived"
          type="number"
          class="input"
          min="1"
        />
      </div>
      <div class="flex flex-col gap-2">
        <label class="label">{{ t('abuseProtection.receivedLimit.maxReceivedHours.label') }}</label>
        <input
          v-model.number="maxReceivedHours"
          type="number"
          class="input"
          min="1"
        />
      </div>
    </div>
    <p class="description">{{ t('abuseProtection.receivedLimit.description') }}</p>
  </div>
</template>

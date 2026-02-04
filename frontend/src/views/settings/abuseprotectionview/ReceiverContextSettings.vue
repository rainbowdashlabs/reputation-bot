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

const receiverContext = computed({
  get: () => session.value?.settings.abuseProtection.receiverContext ?? false,
  set: async (value) => {
    try {
      await api.updateAbuseProtectionReceiverContext(value)
      updateAbuseProtectionSettings({ receiverContext: value })
    } catch (error) {
      console.error('Failed to update receiver context:', error)
    }
  }
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <Toggle
      v-model="receiverContext"
      :label="t('abuseProtection.receiverContext.label')"
    />
    <p class="description">{{ t('abuseProtection.receiverContext.description') }}</p>
  </div>
</template>

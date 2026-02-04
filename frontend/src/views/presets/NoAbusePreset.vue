/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import PresetCard from './PresetCard.vue'

const { t } = useI18n()
const { updateAbuseProtectionSettings } = useSession()

const applyPreset = async () => {
  // Disable donor and receiver context
  const abuseProtectionUpdates = {
    donorContext: false,
    receiverContext: false
  }

  // Update local state
  updateAbuseProtectionSettings(abuseProtectionUpdates)

  // Apply settings via API
  await Promise.all([
    api.updateAbuseProtectionDonorContext(false),
    api.updateAbuseProtectionReceiverContext(false)
  ])
}
</script>

<template>
  <PresetCard
    :title="t('presets.noAbuse.title')"
    :description="t('presets.noAbuse.description')"
    :settings="t('presets.noAbuse.settings')"
    @apply="applyPreset"
  />
</template>

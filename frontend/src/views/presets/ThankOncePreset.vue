/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import {CooldownDirection} from '@/api/types'
import PresetCard from './PresetCard.vue'

const {t} = useI18n()
const {updateAbuseProtectionSettings} = useSession()

const applyPreset = async () => {
  // Set cooldown to -1 (only once) and direction to unidirectional
  const abuseProtectionUpdates = {
    cooldown: -1,
    cooldownDirection: CooldownDirection.UNIDIRECTIONAL
  }

  // Update local state
  updateAbuseProtectionSettings(abuseProtectionUpdates)

  // Apply settings via API
  await Promise.all([
    api.updateAbuseProtectionCooldown(-1),
    api.updateAbuseProtectionCooldownDirection(CooldownDirection.UNIDIRECTIONAL)
  ])
}
</script>

<template>
  <PresetCard
      :description="t('presets.thankOnce.description')"
      :settings="t('presets.thankOnce.settings')"
      :title="t('presets.thankOnce.title')"
      @apply="applyPreset"
  />
</template>

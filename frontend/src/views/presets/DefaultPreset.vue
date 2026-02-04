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
const {updateReputationSettings, updateAbuseProtectionSettings, updateMessagesSettings} = useSession()

const applyPreset = async () => {
  // Set cooldown to 30 minutes with bidirectional direction
  // Enable donor and receiver context checks
  const abuseProtectionUpdates = {
    cooldown: 30,
    cooldownDirection: CooldownDirection.BIDIRECTIONAL,
    donorContext: true,
    receiverContext: true
  }

  // Disable command reputation, enable all other modes
  const reputationUpdates = {
    reactionActive: true,
    answerActive: true,
    mentionActive: true,
    fuzzyActive: true,
    embedActive: true,
    directActive: false, // Disable directEmbed as specified
    commandActive: false // Disable command reputation
  }

  // Enable reputation confirmation
  const messagesUpdates = {
    reactionConfirmation: true
  }

  // Update local state
  updateReputationSettings(reputationUpdates)
  updateAbuseProtectionSettings(abuseProtectionUpdates)
  updateMessagesSettings(messagesUpdates)

  // Apply settings via API
  await Promise.all([
    // Reputation settings
    api.updateReputationReactionActive(true),
    api.updateReputationAnswerActive(true),
    api.updateReputationMentionActive(true),
    api.updateReputationFuzzyActive(true),
    api.updateReputationEmbedActive(true),
    api.updateReputationDirectActive(false),
    api.updateReputationCommandActive(false),
    // Abuse protection settings
    api.updateAbuseProtectionCooldown(30),
    api.updateAbuseProtectionCooldownDirection(CooldownDirection.BIDIRECTIONAL),
    api.updateAbuseProtectionDonorContext(true),
    api.updateAbuseProtectionReceiverContext(true),
    // Message settings
    api.updateMessagesReactionConfirmation(true)
  ])
}
</script>

<template>
  <PresetCard
      :description="t('presets.default.description')"
      :settings="t('presets.default.settings')"
      :title="t('presets.default.title')"
      @apply="applyPreset"
  />
</template>

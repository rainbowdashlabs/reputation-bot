<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import PresetCard from './PresetCard.vue'

const { t } = useI18n()
const { updateReputationSettings, updateAbuseProtectionSettings } = useSession()

const applyPreset = async () => {
  // Disable all reputation types except command
  const reputationUpdates = {
    reactionActive: false,
    answerActive: false,
    mentionActive: false,
    fuzzyActive: false,
    embedActive: false,
    directActive: false,
    commandActive: true
  }

  // Disable donor and receiver context
  const abuseProtectionUpdates = {
    donorContext: false,
    receiverContext: false
  }

  // Update local state
  updateReputationSettings(reputationUpdates)
  updateAbuseProtectionSettings(abuseProtectionUpdates)

  // Apply settings via API
  await Promise.all([
    api.updateReputationReactionActive(false),
    api.updateReputationAnswerActive(false),
    api.updateReputationMentionActive(false),
    api.updateReputationFuzzyActive(false),
    api.updateReputationEmbedActive(false),
    api.updateReputationDirectActive(false),
    api.updateReputationCommandActive(true),
    api.updateAbuseProtectionDonorContext(false),
    api.updateAbuseProtectionReceiverContext(false)
  ])
}
</script>

<template>
  <PresetCard
    :title="t('presets.simpleReputation.title')"
    :description="t('presets.simpleReputation.description')"
    :settings="t('presets.simpleReputation.settings')"
    @apply="applyPreset"
  />
</template>

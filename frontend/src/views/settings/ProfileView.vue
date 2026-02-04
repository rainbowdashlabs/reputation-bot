/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'
import SettingsContainer from './components/SettingsContainer.vue'
import NicknameSettings from './profileview/NicknameSettings.vue'
import ProfilePictureSettings from './profileview/ProfilePictureSettings.vue'
import ReputationNameSettings from './profileview/ReputationNameSettings.vue'

const {t} = useI18n()
const {session} = useSession()

// Local state for initial values
const initialNickname = ref<string>('')
const initialProfilePictureUrl = ref<string>('')
const initialReputationName = ref<string>('')

// Check if profile feature is unlocked
const isProfileUnlocked = computed(() => {
  return session.value?.premiumFeatures?.profile?.unlocked ?? false
})

const profileRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.profile?.requiredSkus ?? []
})

// Initialize from session data
watch(session, (newSession) => {
  if (newSession?.settings?.profile) {
    initialNickname.value = newSession.settings.profile.nickname || ''
    initialProfilePictureUrl.value = newSession.settings.profile.profilePictureUrl || ''
    initialReputationName.value = newSession.settings.profile.reputationName || ''
  }
}, {immediate: true})
</script>

<template>
  <SettingsContainer :description="t('profile.description')" :title="t('settings.profile')">
    <!-- Premium Feature Warning for Profile -->
    <PremiumFeatureWarning
        v-if="!isProfileUnlocked"
        :feature-name="t('profile.premiumRequired')"
        :required-skus="profileRequiredSkus"
        variant="large"
    />
    <div class="space-y-6">
      <NicknameSettings
          :disabled="!isProfileUnlocked"
          :initial-nickname="initialNickname"
      />

      <ProfilePictureSettings
          :disabled="!isProfileUnlocked"
          :initial-profile-picture-url="initialProfilePictureUrl"
      />

      <ReputationNameSettings
          :initial-reputation-name="initialReputationName"
      />
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import SettingsContainer from './components/SettingsContainer.vue'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'
import LogChannelActiveSettings from './logchannelview/LogChannelActiveSettings.vue'
import LogChannelIdSettings from './logchannelview/LogChannelIdSettings.vue'

const {t} = useI18n()
const {session} = useSession()

const isLogChannelUnlocked = computed(() => {
  return session.value?.premiumFeatures?.logChannel?.unlocked ?? false
})

const logChannelRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.logChannel?.requiredSkus ?? []
})

const isLogChannelActive = computed(() => {
  return session.value?.settings?.logChannel?.active ?? false
})
</script>

<template>
  <SettingsContainer :description="t('logChannel.description')" :title="t('settings.logChannel')">
    <PremiumFeatureWarning
        v-if="!isLogChannelUnlocked"
        :feature-name="t('logChannel.premiumRequired')"
        :required-skus="logChannelRequiredSkus"
        variant="large"
    />

    <div v-else class="space-y-8">
      <LogChannelActiveSettings/>

      <div v-if="isLogChannelActive" class="space-y-8 pt-4 border-t border-gray-200 dark:border-gray-700">
        <LogChannelIdSettings/>
      </div>
    </div>
  </SettingsContainer>
</template>

<style scoped>
</style>

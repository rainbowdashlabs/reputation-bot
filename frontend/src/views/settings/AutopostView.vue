/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import SettingsContainer from './components/SettingsContainer.vue'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'
import AutopostActiveSettings from './autopostview/AutopostActiveSettings.vue'
import AutopostChannelSettings from './autopostview/AutopostChannelSettings.vue'
import AutopostIntervalSettings from './autopostview/AutopostIntervalSettings.vue'
import AutopostTypeSettings from './autopostview/AutopostTypeSettings.vue'
import AutopostSendSettings from './autopostview/AutopostSendSettings.vue'

const { t } = useI18n()
const { session } = useSession()

const isAutopostUnlocked = computed(() => {
  return session.value?.premiumFeatures?.autopost?.unlocked ?? false
})

const autopostRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.autopost?.requiredSkus ?? []
})

const isAutopostActive = computed(() => {
  return session.value?.settings?.autopost?.active ?? false
})
</script>

<template>
  <SettingsContainer :title="t('settings.autopost')" :description="t('autopost.description')">
    <PremiumFeatureWarning
      v-if="!isAutopostUnlocked"
      :feature-name="t('autopost.premiumRequired')"
      :required-skus="autopostRequiredSkus"
      variant="large"
    />

    <div v-else class="space-y-8">
      <AutopostActiveSettings />

      <div v-if="isAutopostActive" class="space-y-8 pt-4 border-t border-gray-200 dark:border-gray-700">
        <AutopostChannelSettings />
        <AutopostIntervalSettings />
        <AutopostTypeSettings />
        <AutopostSendSettings />
      </div>
    </div>
  </SettingsContainer>
</template>

<style scoped>
</style>

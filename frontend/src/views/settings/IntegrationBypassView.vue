/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import SettingsContainer from './components/SettingsContainer.vue'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'
import AddBypass from './integrationbypassview/AddBypass.vue'
import BypassList from './integrationbypassview/BypassList.vue'

const {t} = useI18n()
const {session} = useSession()

const isIntegrationBypassUnlocked = computed(() => {
  return session.value?.premiumFeatures?.integrationBypass?.unlocked ?? false
})

const integrationBypassRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.integrationBypass?.requiredSkus ?? []
})

const expandedBypasses = ref<Set<string>>(new Set())

const toggleExpand = (id: string) => {
  if (expandedBypasses.value.has(id)) {
    expandedBypasses.value.delete(id)
  } else {
    expandedBypasses.value.add(id)
  }
}

const onBypassAdded = (integrationId: string) => {
  expandedBypasses.value.add(integrationId)
}
</script>

<template>
  <SettingsContainer :description="t('integrationBypass.description')" :title="t('settings.integrationBypass')">
    <PremiumFeatureWarning
        v-if="!isIntegrationBypassUnlocked"
        :feature-name="t('integrationBypass.premiumRequired')"
        :required-skus="integrationBypassRequiredSkus"
        variant="large"
    />

    <div v-else class="space-y-4">
      <AddBypass @added="onBypassAdded" />
      <BypassList :expanded-bypasses="expandedBypasses" @toggle="toggleExpand" />
    </div>
  </SettingsContainer>
</template>

<style scoped>
</style>

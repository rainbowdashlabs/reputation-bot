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
import CommandAliasInput from './commandaliasesview/CommandAliasInput.vue'

const {t} = useI18n()
const {session} = useSession()

const isCommandAliasesUnlocked = computed(() => {
  return session.value?.premiumFeatures?.commandAliases?.unlocked ?? true
})

const commandAliasesRequiredSkus = computed(() => {
  return session.value?.premiumFeatures?.commandAliases?.requiredSkus ?? []
})

const aliases = computed(() => session.value?.settings?.commandAliases?.aliases ?? {})
</script>

<template>
  <SettingsContainer :description="t('commandAliases.description')" :title="t('settings.commandAliases')">
    <PremiumFeatureWarning
        v-if="!isCommandAliasesUnlocked"
        :feature-name="t('settings.commandAliases')"
        :required-skus="commandAliasesRequiredSkus"
        variant="large"
    />

    <div v-else class="space-y-6">
      <CommandAliasInput
          :description="t('commandAliases.reputation.description')"
          :initial-alias="aliases.reputation ?? ''"
          command="reputation"
      />
      <CommandAliasInput
          :description="t('commandAliases.profile.description')"
          :initial-alias="aliases.profile ?? ''"
          command="profile"
      />
    </div>
  </SettingsContainer>
</template>

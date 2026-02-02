<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'
import ReputationModeSettings from './reputationview/ReputationModeSettings.vue'
import ResetDateSettings from './reputationview/ResetDateSettings.vue'
import ReputationTypeSettings from './reputationview/ReputationTypeSettings.vue'

import { useSession } from '@/composables/useSession'
import { ReputationMode } from '@/api/types'

const { t } = useI18n()
const { session } = useSession()
</script>

<template>
  <SettingsContainer :title="t('settings.reputation')" :description="t('general.reputation.description')">
    <div class="space-y-8">
      <ReputationModeSettings />
      <template v-if="session?.settings?.general?.reputationMode === ReputationMode.TOTAL">
        <hr class="border-gray-200 dark:border-gray-700" />
        <ResetDateSettings />
      </template>
      <hr class="border-gray-200 dark:border-gray-700" />
      <ReputationTypeSettings />
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

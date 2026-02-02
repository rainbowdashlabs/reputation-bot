<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'
import CooldownSettings from './abuseprotectionview/CooldownSettings.vue'
import MaxMessageAgeSettings from './abuseprotectionview/MaxMessageAgeSettings.vue'
import MinMessagesSettings from './abuseprotectionview/MinMessagesSettings.vue'
import DonorContextSettings from './abuseprotectionview/DonorContextSettings.vue'
import ReceiverContextSettings from './abuseprotectionview/ReceiverContextSettings.vue'
import GivenLimitSettings from './abuseprotectionview/GivenLimitSettings.vue'
import ReceivedLimitSettings from './abuseprotectionview/ReceivedLimitSettings.vue'
import MaxMessageReputationSettings from './abuseprotectionview/MaxMessageReputationSettings.vue'

const { t } = useI18n()
</script>

<template>
  <SettingsContainer :title="t('settings.abuseProtection')" :description="t('abuseProtection.description')">
    <div class="space-y-8">
      <CooldownSettings />
      <MaxMessageAgeSettings />
      <MinMessagesSettings />
      <DonorContextSettings />
      <ReceiverContextSettings />
      <GivenLimitSettings />
      <ReceivedLimitSettings />
      <MaxMessageReputationSettings />
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

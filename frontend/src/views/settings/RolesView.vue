<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'
import DonorRolesSettings from './rolesview/DonorRolesSettings.vue'
import ReceiverRolesSettings from './rolesview/ReceiverRolesSettings.vue'

const { t } = useI18n()
</script>

<template>
  <SettingsContainer :title="t('settings.roles')" :description="t('general.roles.description')">
    <div class="space-y-8">
      <DonorRolesSettings />
      <hr class="border-gray-200 dark:border-gray-700" />
      <ReceiverRolesSettings />
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

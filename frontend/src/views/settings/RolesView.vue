/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import DenyReceiverRolesSettings from "@/views/settings/rolesview/DenyReceiverRolesSettings.vue";
import DenyDonorRolesSettings from "@/views/settings/rolesview/DenyDonorRolesSettings.vue";
import SettingsContainer from "@/views/settings/components/SettingsContainer.vue";
import DonorRolesSettings from "@/views/settings/rolesview/DonorRolesSettings.vue";
import ReceiverRolesSettings from "@/views/settings/rolesview/ReceiverRolesSettings.vue";

const {t} = useI18n()
</script>

<template>
  <SettingsContainer :description="t('general.roles.description')" :title="t('settings.roles')">
    <div class="space-y-8">
      <DonorRolesSettings/>
      <hr class="border-gray-200 dark:border-gray-700"/>
      <ReceiverRolesSettings/>
      <hr class="border-gray-200 dark:border-gray-700"/>
      <DenyReceiverRolesSettings/>
      <hr class="border-gray-200 dark:border-gray-700"/>
      <DenyDonorRolesSettings/>
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

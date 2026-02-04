/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'
import StackRolesSettings from './ranksview/StackRolesSettings.vue'
import RankManagement from './ranksview/RankManagement.vue'

const {t} = useI18n()
</script>

<template>
  <SettingsContainer :description="t('general.ranks.description')" :title="t('settings.ranks')">
    <div class="space-y-8">
      <StackRolesSettings/>
      <hr class="border-gray-200 dark:border-gray-700"/>
      <RankManagement/>
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

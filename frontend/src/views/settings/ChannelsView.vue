/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import SettingsContainer from './components/SettingsContainer.vue'
import ChannelListTypeSettings from './channelsview/ChannelListTypeSettings.vue'
import ChannelSelectionSettings from './channelsview/ChannelSelectionSettings.vue'

const {t} = useI18n()
const {session} = useSession()
</script>

<template>
  <SettingsContainer :description="t('general.channels.description')" :title="t('settings.channels')">
    <div v-if="session?.settings?.thanking?.channels" class="space-y-8">
      <ChannelListTypeSettings/>
      <hr class="border-gray-200 dark:border-gray-700"/>
      <ChannelSelectionSettings/>
    </div>
  </SettingsContainer>
</template>

<style scoped>
</style>

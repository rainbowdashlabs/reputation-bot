/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import Toggle from '@/components/Toggle.vue'

const {t} = useI18n()
const {session, updateLogChannelSettings} = useSession()

const updateActive = async (active: boolean) => {
  if (!session.value?.settings?.logChannel) return

  try {
    await api.updateLogChannelActive(active);
    updateLogChannelSettings({active});
  } catch (error) {
    console.error('Failed to update log channel active status:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.logChannel" class="flex flex-col gap-1">
    <Toggle
        :label="t('logChannel.active.label')"
        :model-value="session.settings.logChannel.active"
        @update:model-value="updateActive"
    />
    <p class="description">
      {{ t('logChannel.active.description') }}
    </p>
  </div>
</template>

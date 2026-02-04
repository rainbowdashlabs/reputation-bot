/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import ChannelSelect from '@/components/ChannelSelect.vue'

const {t} = useI18n()
const {session, updateAutopostSettings} = useSession()

const updateChannel = async (channelId: string | null) => {
  if (!session.value?.settings?.autopost) return

  const idStr = channelId || '0'
  try {
    await api.updateAutopostChannel(idStr);
    updateAutopostSettings({channelId: idStr});
  } catch (error) {
    console.error('Failed to update autopost channel:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.autopost" class="flex flex-col gap-1">
    <ChannelSelect
        :label="t('autopost.channel.label')"
        :model-value="session.settings.autopost.channelId === '0' ? null : session.settings.autopost.channelId"
        allow-clear
        @update:model-value="updateChannel"
    />
    <p class="description">
      {{ t('autopost.channel.description') }}
    </p>
  </div>
</template>

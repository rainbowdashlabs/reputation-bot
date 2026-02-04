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
import ChannelSelect from '@/components/ChannelSelect.vue'

const {t} = useI18n()
const {session, updateAnnouncementsSettings} = useSession()

const updateSameChannel = async (sameChannel: boolean) => {
  if (!session.value?.settings?.announcements) return

  try {
    await api.updateAnnouncementsSameChannel(sameChannel);
    updateAnnouncementsSettings({sameChannel});
  } catch (error) {
    console.error('Failed to update same channel setting:', error)
  }
}

const updateChannel = async (channelId: string | null) => {
  if (!session.value?.settings?.announcements) return

  const idStr = channelId || '0'
  try {
    await api.updateAnnouncementsChannel(idStr);
    updateAnnouncementsSettings({channelId: idStr});
  } catch (error) {
    console.error('Failed to update announcement channel:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.announcements" class="flex flex-col gap-6">
    <div class="flex flex-col gap-1">
      <Toggle
          :label="t('announcements.sameChannel.label')"
          :model-value="session.settings.announcements.sameChannel"
          @update:model-value="updateSameChannel"
      />
      <p class="description">
        {{ t('announcements.sameChannel.description') }}
      </p>
    </div>

    <div v-if="!session.settings.announcements.sameChannel" class="flex flex-col gap-4">
      <div class="flex flex-col gap-1">
        <ChannelSelect
            :label="t('announcements.channel.label')"
            :model-value="session.settings.announcements.channelId === '0' ? null : session.settings.announcements.channelId"
            allow-clear
            @update:model-value="updateChannel"
        />
        <p class="description">
          {{ t('announcements.channel.description') }}
        </p>
      </div>
    </div>
  </div>
</template>

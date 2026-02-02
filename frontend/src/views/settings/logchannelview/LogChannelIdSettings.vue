<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import ChannelSelect from '@/components/ChannelSelect.vue'

const { t } = useI18n()
const { session, updateLogChannelSettings } = useSession()

const updateChannel = async (channelId: number | null) => {
  if (!session.value?.settings?.logChannel) return
  
  const idStr = channelId ? channelId.toString() : '0'
  try {
    await api.updateLogChannelId(idStr);
    updateLogChannelSettings({ channelId: idStr });
  } catch (error) {
    console.error('Failed to update log channel:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.logChannel" class="flex flex-col gap-1">
    <ChannelSelect
      :model-value="session.settings.logChannel.channelId === '0' ? null : parseInt(session.settings.logChannel.channelId)"
      :label="t('logChannel.channel.label')"
      allow-clear
      @update:model-value="updateChannel"
    />
    <p class="description">
      {{ t('logChannel.channel.description') }}
    </p>
  </div>
</template>

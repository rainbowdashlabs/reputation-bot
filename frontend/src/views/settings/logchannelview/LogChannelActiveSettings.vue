<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { session, updateLogChannelSettings } = useSession()

const updateActive = async (active: boolean) => {
  if (!session.value?.settings?.logChannel) return
  
  try {
    await api.updateLogChannelActive(active);
    updateLogChannelSettings({ active });
  } catch (error) {
    console.error('Failed to update log channel active status:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.logChannel" class="flex flex-col gap-1">
    <Toggle
      :model-value="session.settings.logChannel.active"
      :label="t('logChannel.active.label')"
      @update:model-value="updateActive"
    />
    <p class="description">
      {{ t('logChannel.active.description') }}
    </p>
  </div>
</template>

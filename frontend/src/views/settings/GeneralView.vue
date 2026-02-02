<script lang="ts" setup>
import {ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import ChannelSelect from '@/components/ChannelSelect.vue'
import SettingsContainer from './components/SettingsContainer.vue'

const {t} = useI18n()
const {session} = useSession()

const systemChannel = ref<number | null>(null)

watch(session, (newSession) => {
  if (newSession?.settings?.general) {
    systemChannel.value = newSession.settings.general.systemChannel || null
  }
}, {immediate: true})

const updateSystemChannel = async () => {
  try {
    await api.updateGeneralSystemChannel(systemChannel.value ? systemChannel.value.toString() : '0')
  } catch (error) {
    console.error('Failed to update system channel:', error)
  }
}
</script>

<template>
  <SettingsContainer :title="t('settings.general')">
    <div class="grid grid-cols-1 gap-6">
      <ChannelSelect
          v-model="systemChannel"
          :label="t('general.systemChannel.label')"
          allow-clear
          @update:model-value="updateSystemChannel"
      />
      <p class="description">{{ t('general.systemChannel.note') }}</p>
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

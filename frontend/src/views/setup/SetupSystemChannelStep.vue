<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import ChannelSelect from '@/components/ChannelSelect.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()
const { session, updateGeneralSettings } = useSession()

const systemChannel = ref<string | null>(null)

watch(session, (newSession) => {
  if (newSession?.settings?.general) {
    systemChannel.value = newSession.settings.general.systemChannel || null
  }
}, { immediate: true })

// Only allow proceeding if a system channel is set
watch(systemChannel, (newValue) => {
  emit('canProceed', newValue !== null && newValue !== "0")
}, { immediate: true })

const updateSystemChannel = async () => {
  try {
    await api.updateGeneralSystemChannel(systemChannel.value ? systemChannel.value.toString() : '0')
    updateGeneralSettings({ systemChannel: systemChannel.value || "0" })
  } catch (error) {
    console.error('Failed to update system channel:', error)
  }
}
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.systemChannel.description') }}
    </p>
    
    <div class="flex flex-col gap-1.5">
      <ChannelSelect
        v-model="systemChannel"
        :label="t('general.systemChannel.label')"
        @update:model-value="updateSystemChannel"
      />
      <p class="text-sm text-gray-500 dark:text-gray-400">
        {{ t('general.systemChannel.note') }}
      </p>
    </div>
    
    <div v-if="!systemChannel || systemChannel === '0'" class="p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
      <p class="text-sm text-yellow-800 dark:text-yellow-200">
        {{ t('setup.steps.systemChannel.required') }}
      </p>
    </div>
  </div>
</template>

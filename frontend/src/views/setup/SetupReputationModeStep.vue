<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ReputationModeSettings from '@/views/settings/reputationview/ReputationModeSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()

// Always allow proceeding (reputation mode is optional)
watch(() => true, () => {
  emit('canProceed', true)
}, { immediate: true })
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.reputationMode.description') }}
    </p>
    
    <ReputationModeSettings />
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'

const { t } = useI18n()
const { session, updateAbuseProtectionSettings } = useSession()

const minMessages = computed({
  get: () => session.value?.settings.abuseProtection.minMessages ?? 0,
  set: async (value) => {
    try {
      await api.updateAbuseProtectionMinMessages(value)
      updateAbuseProtectionSettings({ minMessages: value })
    } catch (error) {
      console.error('Failed to update min messages:', error)
    }
  }
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <label class="label">{{ t('abuseProtection.minMessages.label') }}</label>
    <input
      v-model.number="minMessages"
      type="number"
      class="input"
      min="0"
    />
    <p class="description">{{ t('abuseProtection.minMessages.description') }}</p>
  </div>
</template>

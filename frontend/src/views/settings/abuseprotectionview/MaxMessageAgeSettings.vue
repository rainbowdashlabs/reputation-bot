<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'

const { t } = useI18n()
const { session, updateAbuseProtectionSettings } = useSession()

const maxMessageAge = computed({
  get: () => session.value?.settings.abuseProtection.maxMessageAge ?? 0,
  set: async (value) => {
    try {
      await api.updateAbuseProtectionMaxMessageAge(value)
      updateAbuseProtectionSettings({ maxMessageAge: value })
    } catch (error) {
      console.error('Failed to update max message age:', error)
    }
  }
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <label class="label">{{ t('abuseProtection.maxMessageAge.label') }}</label>
    <input
      v-model.number="maxMessageAge"
      type="number"
      class="input"
      min="0"
    />
    <p class="description">{{ t('abuseProtection.maxMessageAge.description') }}</p>
  </div>
</template>

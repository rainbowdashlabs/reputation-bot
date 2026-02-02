<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { session, updateAbuseProtectionSettings } = useSession()

const isEnabled = computed({
  get: () => (session.value?.settings.abuseProtection.maxGiven ?? 0) !== 0,
  set: async (value) => {
    const maxGiven = value ? 1 : 0
    const maxGivenHours = value ? 1 : 0
    try {
      await api.updateAbuseProtectionMaxGiven(maxGiven)
      await api.updateAbuseProtectionMaxGivenHours(maxGivenHours)
      updateAbuseProtectionSettings({ maxGiven, maxGivenHours })
    } catch (error) {
      console.error('Failed to update given limit status:', error)
    }
  }
})

const maxGiven = computed({
  get: () => session.value?.settings.abuseProtection.maxGiven ?? 1,
  set: async (value) => {
    if (value < 1) return
    try {
      await api.updateAbuseProtectionMaxGiven(value)
      updateAbuseProtectionSettings({ maxGiven: value })
    } catch (error) {
      console.error('Failed to update max given:', error)
    }
  }
})

const maxGivenHours = computed({
  get: () => session.value?.settings.abuseProtection.maxGivenHours ?? 1,
  set: async (value) => {
    if (value < 1) return
    try {
      await api.updateAbuseProtectionMaxGivenHours(value)
      updateAbuseProtectionSettings({ maxGivenHours: value })
    } catch (error) {
      console.error('Failed to update max given hours:', error)
    }
  }
})
</script>

<template>
  <div class="space-y-4">
    <Toggle
      v-model="isEnabled"
      :label="t('abuseProtection.givenLimit.enabled.label')"
    />
    
    <div v-if="isEnabled" class="grid grid-cols-1 md:grid-cols-2 gap-4 ml-6">
      <div class="flex flex-col gap-2">
        <label class="label">{{ t('abuseProtection.givenLimit.maxGiven.label') }}</label>
        <input
          v-model.number="maxGiven"
          type="number"
          class="input"
          min="1"
        />
      </div>
      <div class="flex flex-col gap-2">
        <label class="label">{{ t('abuseProtection.givenLimit.maxGivenHours.label') }}</label>
        <input
          v-model.number="maxGivenHours"
          type="number"
          class="input"
          min="1"
        />
      </div>
    </div>
    <p class="description">{{ t('abuseProtection.givenLimit.description') }}</p>
  </div>
</template>

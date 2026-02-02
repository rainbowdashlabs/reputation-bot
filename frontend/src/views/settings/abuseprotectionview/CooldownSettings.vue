<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import { CooldownDirection } from '@/api/types'

const { t } = useI18n()
const { session, updateAbuseProtectionSettings } = useSession()

const cooldown = computed({
  get: () => session.value?.settings.abuseProtection.cooldown ?? 0,
  set: async (value) => {
    try {
      await api.updateAbuseProtectionCooldown(value)
      updateAbuseProtectionSettings({ cooldown: value })
    } catch (error) {
      console.error('Failed to update cooldown:', error)
    }
  }
})

const cooldownDirection = computed({
  get: () => session.value?.settings.abuseProtection.cooldownDirection ?? CooldownDirection.UNIDIRECTIONAL,
  set: async (value) => {
    try {
      await api.updateAbuseProtectionCooldownDirection(value)
      updateAbuseProtectionSettings({ cooldownDirection: value })
    } catch (error) {
      console.error('Failed to update cooldown direction:', error)
    }
  }
})
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-col gap-2">
      <label class="label">{{ t('abuseProtection.cooldown.label') }}</label>
      <input
        v-model.number="cooldown"
        type="number"
        class="input"
        min="-1"
      />
      <p class="description">{{ t('abuseProtection.cooldown.description') }}</p>
    </div>

    <div class="flex flex-col gap-2">
      <label class="label">{{ t('abuseProtection.cooldownDirection.label') }}</label>
      <select v-model="cooldownDirection" class="input">
        <option :value="CooldownDirection.UNIDIRECTIONAL">{{ t('abuseProtection.cooldownDirection.unidirectional') }}</option>
        <option :value="CooldownDirection.BIDIRECTIONAL">{{ t('abuseProtection.cooldownDirection.bidirectional') }}</option>
      </select>
      <p class="description">{{ t('abuseProtection.cooldownDirection.description') }}</p>
    </div>
  </div>
</template>

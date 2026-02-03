<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import { CooldownDirection } from '@/api/types'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { session, updateAbuseProtectionSettings } = useSession()

const cooldown = computed({
  get: () => session.value?.settings.abuseProtection.cooldown ?? 0,
  set: async (value) => {
    if (value < 0) return
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

const isCooldownOnce = computed({
  get: () => cooldown.value < 0,
  set: async (value) => {
    try {
      if (value) {
        await Promise.all([
          api.updateAbuseProtectionCooldown(-1),
          api.updateAbuseProtectionCooldownDirection(CooldownDirection.UNIDIRECTIONAL)
        ])
        updateAbuseProtectionSettings({ cooldown: -1, cooldownDirection: CooldownDirection.UNIDIRECTIONAL })
      } else {
        await api.updateAbuseProtectionCooldown(30)
        updateAbuseProtectionSettings({ cooldown: 30 })
      }
    } catch (error) {
      console.error('Failed to update cooldown once:', error)
    }
  }
})
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-col gap-1">
      <Toggle v-model="isCooldownOnce" :label="t('abuseProtection.cooldown.once.label')" />
      <p class="description">{{ t('abuseProtection.cooldown.once.description') }}</p>
    </div>

    <div v-if="isCooldownOnce" class="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
      <p class="text-sm text-blue-800 dark:text-blue-300">
        {{ t('abuseProtection.cooldown.once.activeDescription') }}
      </p>
    </div>

    <template v-else>
      <div class="flex flex-col gap-2">
        <label class="label">{{ t('abuseProtection.cooldown.label') }}</label>
        <input
          v-model.number="cooldown"
          type="number"
          class="input"
          min="0"
        />
        <p class="description">{{ t('abuseProtection.cooldown.description') }}</p>
      </div>

      <div class="flex flex-col gap-2">
        <label class="label">{{ t('abuseProtection.cooldownDirection.label') }}</label>
        <select v-model="cooldownDirection" class="select">
          <option :value="CooldownDirection.UNIDIRECTIONAL">{{ t('abuseProtection.cooldownDirection.unidirectional') }}</option>
          <option :value="CooldownDirection.BIDIRECTIONAL">{{ t('abuseProtection.cooldownDirection.bidirectional') }}</option>
        </select>
        <p class="description">{{ t('abuseProtection.cooldownDirection.description') }}</p>
      </div>
    </template>
  </div>
</template>

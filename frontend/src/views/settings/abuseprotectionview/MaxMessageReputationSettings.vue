/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'

const {t} = useI18n()
const {session, updateAbuseProtectionSettings} = useSession()

const maxMessageReputation = computed({
  get: () => session.value?.settings.abuseProtection.maxMessageReputation ?? 0,
  set: async (value) => {
    if (value < 0) return
    try {
      await api.updateAbuseProtectionMaxMessageReputation(value)
      updateAbuseProtectionSettings({maxMessageReputation: value})
    } catch (error) {
      console.error('Failed to update max message reputation:', error)
    }
  }
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <label class="label">{{ t('abuseProtection.maxMessageReputation.label') }}</label>
    <input
        v-model.number="maxMessageReputation"
        class="input"
        min="0"
        type="number"
    />
    <p class="description">{{ t('abuseProtection.maxMessageReputation.description') }}</p>
  </div>
</template>

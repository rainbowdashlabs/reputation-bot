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

const minMessages = computed({
  get: () => session.value?.settings.abuseProtection.minMessages ?? 0,
  set: async (value) => {
    if (value < 0) return
    try {
      await api.updateAbuseProtectionMinMessages(value)
      updateAbuseProtectionSettings({minMessages: value})
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
        class="input"
        min="0"
        type="number"
    />
    <p class="description">{{ t('abuseProtection.minMessages.description') }}</p>
  </div>
</template>

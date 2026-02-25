/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import TimeQuickPresets from '@/components/TimeQuickPresets.vue'
import {api} from '@/api'
import NumberInput from '@/components/NumberInput.vue'

const {t} = useI18n()
const {session, updateAbuseProtectionSettings} = useSession()

const maxMessageAge = computed({
  get: () => session.value?.settings?.abuseProtection.maxMessageAge ?? 0,
  set: async (value) => {
    if (value < 0) return
    try {
      await api.updateAbuseProtectionMaxMessageAge(value)
      updateAbuseProtectionSettings({maxMessageAge: value})
    } catch (error) {
      console.error('Failed to update max message age:', error)
    }
  }
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <NumberInput
        v-model="maxMessageAge"
        :label="t('abuseProtection.maxMessageAge.label')"
        :min="0"
        :max="86400"
    />
    <TimeQuickPresets v-model="maxMessageAge"/>
    <p class="description">{{ t('abuseProtection.maxMessageAge.description') }}</p>
  </div>
</template>

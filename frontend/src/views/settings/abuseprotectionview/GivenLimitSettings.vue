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
import TimeQuickPresets from '@/components/TimeQuickPresets.vue'
import Toggle from '@/components/Toggle.vue'
import NumberInput from '@/components/NumberInput.vue'

const {t} = useI18n()
const {session, updateAbuseProtectionSettings} = useSession()

const isEnabled = computed({
  get: () => (session.value?.settings.abuseProtection.maxGiven ?? 0) !== 0,
  set: async (value) => {
    const maxGiven = value ? 1 : 0
    const maxGivenHours = value ? 1 : 0
    try {
      await api.updateAbuseProtectionMaxGiven(maxGiven)
      await api.updateAbuseProtectionMaxGivenHours(maxGivenHours)
      updateAbuseProtectionSettings({maxGiven, maxGivenHours})
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
      updateAbuseProtectionSettings({maxGiven: value})
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
      updateAbuseProtectionSettings({maxGivenHours: value})
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
      <NumberInput
          v-model="maxGiven"
          :label="t('abuseProtection.givenLimit.maxGiven.label')"
          :min="1"
          :max="1000"
      />
      <div class="flex flex-col gap-2">
        <NumberInput
            v-model="maxGivenHours"
            :label="t('abuseProtection.givenLimit.maxGivenHours.label')"
            :min="1"
            :max="8760"
        />
        <TimeQuickPresets v-model="maxGivenHours" use-hours/>
      </div>
    </div>
    <p class="description">{{ t('abuseProtection.givenLimit.description') }}</p>
  </div>
</template>

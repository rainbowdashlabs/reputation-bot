/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import BaseButton from '@/components/BaseButton.vue'

const {t} = useI18n()
const {session, updateGeneralSettings} = useSession()

const dateInput = ref('')

watch(() => session.value?.settings?.general?.resetDate, (newDate) => {
  if (newDate) {
    // Format for datetime-local input: YYYY-MM-DDThh:mm
    const date = new Date(newDate)
    const pad = (n: number) => n.toString().padStart(2, '0')
    dateInput.value = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
  } else {
    dateInput.value = ''
  }
}, {immediate: true})

const updateResetDate = async () => {
  if (!session.value?.settings?.general) return

  let isoString: string | null = null
  if (dateInput.value) {
    // Input is in local time, but we should send it as UTC
    const localDate = new Date(dateInput.value)
    isoString = localDate.toISOString()
  }

  try {
    await api.updateGeneralResetDate(isoString)
    updateGeneralSettings({resetDate: isoString || ''});
  } catch (error) {
    // No easy way to revert without keeping previous value
    console.error('Failed to update reset date:', error)
  }
}

const clearResetDate = async () => {
  if (!session.value?.settings?.general) return

  try {
    await api.updateGeneralResetDate(null)
    dateInput.value = ''
    updateGeneralSettings({resetDate: ''});
  } catch (error) {
    console.error('Failed to clear reset date:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.general" class="flex flex-col gap-4">
    <div class="flex flex-col gap-1">
      <label class="label">{{ t('general.reputation.resetDate.label') }}</label>
      <p class="description">{{ t('general.reputation.resetDate.description') }}</p>
    </div>

    <div class="flex flex-wrap items-center gap-3">
      <input
          v-model="dateInput"
          class="input max-w-xs"
          type="datetime-local"
          @change="updateResetDate"
      />
      <BaseButton
          v-if="session.settings.general.resetDate"
          color="secondary"
          @click="clearResetDate"
      >
        {{ t('general.reputation.resetDate.clear') }}
      </BaseButton>
    </div>
  </div>
</template>

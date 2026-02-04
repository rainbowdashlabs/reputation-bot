/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import {RefreshInterval} from '@/api/types'

const {t} = useI18n()
const {session, updateAutopostSettings} = useSession()

const intervals = Object.values(RefreshInterval)

const updateInterval = async (event: Event) => {
  if (!session.value?.settings?.autopost) return
  const interval = (event.target as HTMLSelectElement).value as RefreshInterval

  try {
    await api.updateAutopostRefreshInterval(interval);
    updateAutopostSettings({refreshInterval: interval});
  } catch (error) {
    console.error('Failed to update autopost refresh interval:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.autopost" class="flex flex-col gap-1">
    <label class="label">{{ t('autopost.refreshInterval.label') }}</label>
    <select
        :value="session.settings.autopost.refreshInterval"
        class="select"
        @change="updateInterval"
    >
      <option v-for="interval in intervals" :key="interval" :value="interval">
        {{ t(`autopost.refreshInterval.${interval}`) }}
      </option>
    </select>
    <p class="description">
      {{ t('autopost.refreshInterval.description') }}
    </p>
  </div>
</template>

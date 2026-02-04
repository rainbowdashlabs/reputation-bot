/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import {ReputationMode} from '@/api/types'

const {t} = useI18n()
const {session, updateGeneralSettings} = useSession()

const modes = Object.values(ReputationMode)

const updateMode = async (mode: ReputationMode) => {
  if (!session.value?.settings?.general) return

  try {
    await api.updateGeneralReputationMode(mode);
    updateGeneralSettings({reputationMode: mode});
  } catch (error) {
    console.error('Failed to update reputation mode:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.general" class="flex flex-col gap-4">
    <div class="flex flex-col gap-1">
      <label class="label">{{ t('general.reputation.mode.label') }}</label>
      <p class="description">{{ t('general.reputation.mode.description') }}</p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
      <div
          v-for="mode in modes"
          :key="mode"
          :class="[
          session.settings.general.reputationMode === mode
            ? 'bg-indigo-600 border-indigo-600 text-white'
            : 'bg-white dark:bg-gray-800 border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:border-indigo-400'
        ]"
          class="flex flex-col border rounded-md p-3 transition-colors cursor-pointer"
          @click="updateMode(mode)"
      >
        <span class="font-medium mb-1">{{ t(`general.reputation.mode.modes.${mode}`) }}</span>
        <span
            :class="[
            session.settings.general.reputationMode === mode
              ? 'text-indigo-100'
              : 'text-gray-500 dark:text-gray-400'
          ]"
            class="text-xs"
        >
          {{ t(`general.reputation.mode.descriptions.${mode}`) }}
        </span>
      </div>
    </div>
  </div>
</template>

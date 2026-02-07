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
import type {Bypass} from '@/api/types'

const {t} = useI18n()
const {session, updateIntegrationBypass} = useSession()

const bypasses = computed(() => {
  return Object.values(session.value?.settings?.integrationBypass?.bypasses || {}) as Bypass[]
})

const availableIntegrations = computed(() => {
  const existingIds = new Set(bypasses.value.map(b => b.integrationId))
  return session.value?.guild?.integrations?.filter(i => !existingIds.has(i.id)) || []
})

const addBypass = async (integrationId: string) => {
  const newBypass: Bypass = {
    integrationId,
    allowReactions: false,
    allowAnswer: false,
    allowMention: false,
    allowFuzzy: false,
    ignoreCooldown: false,
    ignoreLimit: false
  }
  try {
    await api.updateIntegrationBypass(newBypass)
    updateIntegrationBypass(newBypass)
    emit('added', integrationId)
  } catch (error) {
    console.error('Failed to add bypass:', error)
  }
}

const emit = defineEmits<{
  (e: 'added', integrationId: string): void
}>()
</script>

<template>
  <div>
    <div v-if="availableIntegrations.length > 0" class="flex items-center gap-4 p-4 bg-gray-50 dark:bg-gray-900/50 rounded-lg border border-gray-200 dark:border-gray-700">
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          {{ t('integrationBypass.add') }}
        </label>
        <select
            class="w-full bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm transition-colors"
            @change="(e) => addBypass((e.target as HTMLSelectElement).value)"
        >
          <option disabled selected value="">{{ t('integrationBypass.selectIntegration') }}</option>
          <option v-for="integration in availableIntegrations" :key="integration.id" :value="integration.id">
            {{ integration.displayName }}
          </option>
        </select>
      </div>
    </div>
    <p v-else class="text-sm text-gray-500 italic">
      {{ t('integrationBypass.noIntegrations') }}
    </p>
  </div>
</template>

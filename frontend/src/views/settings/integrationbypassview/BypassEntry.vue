/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import Toggle from '@/components/Toggle.vue'
import MemberDisplay from '../auditlogview/MemberDisplay.vue'
import type {Bypass} from '@/api/types'

interface Props {
  bypass: Bypass
  expanded: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'toggle'): void
}>()

const {t} = useI18n()
const {session, updateIntegrationBypass, removeIntegrationBypass} = useSession()

const getIntegration = (id: string) => {
  return session.value?.guild?.integrations?.find(i => i.id === id)
}

const updateBypassValue = async (key: keyof Bypass, value: boolean) => {
  const newBypass = {...props.bypass, [key]: value}
  try {
    await api.updateIntegrationBypass(newBypass)
    updateIntegrationBypass(newBypass)
  } catch (error) {
    console.error('Failed to update bypass:', error)
  }
}

const deleteBypass = async () => {
  if (!confirm(t('settings.integrationBypass.deleteConfirm'))) return
  try {
    await api.deleteIntegrationBypass(props.bypass.integrationId)
    removeIntegrationBypass(props.bypass.integrationId)
  } catch (error) {
    console.error('Failed to delete bypass:', error)
  }
}

const bypassFields = [
  {key: 'allowReactions', label: 'allowReactions', description: 'allowReactionsDescription'},
  {key: 'allowAnswer', label: 'allowAnswer', description: 'allowAnswerDescription'},
  {key: 'allowMention', label: 'allowMention', description: 'allowMentionDescription'},
  {key: 'allowFuzzy', label: 'allowFuzzy', description: 'allowFuzzyDescription'},
  {key: 'ignoreCooldown', label: 'ignoreCooldown', description: 'ignoreCooldownDescription'},
  {key: 'ignoreLimit', label: 'ignoreLimit', description: 'ignoreLimitDescription'}
] as const
</script>

<template>
  <div class="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden">
    <div
        class="flex items-center justify-between p-4 bg-white dark:bg-gray-800 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors"
        @click="emit('toggle')"
    >
      <div class="flex items-center gap-3">
        <MemberDisplay v-if="getIntegration(bypass.integrationId)" :member="getIntegration(bypass.integrationId)!" />
        <span v-else class="text-gray-500 italic">{{ bypass.integrationId }}</span>
      </div>
      <div class="flex items-center gap-4">
        <button
            class="text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300 transition-colors p-1"
            @click.stop="deleteBypass"
        >
          <font-awesome-icon :icon="['fas', 'trash']" />
        </button>
        <font-awesome-icon
            :class="{'rotate-180': expanded}"
            :icon="['fas', 'chevron-down']"
            class="text-gray-400 transition-transform duration-200"
        />
      </div>
    </div>

    <div v-if="expanded" class="p-4 bg-gray-50 dark:bg-gray-900/30 border-t border-gray-200 dark:border-gray-700 space-y-6">
      <div v-for="field in bypassFields" :key="field.key" class="flex flex-col gap-1">
        <Toggle
            :label="t(`settings.integrationBypass.${field.label}`)"
            :model-value="bypass[field.key]"
            @update:model-value="(val) => updateBypassValue(field.key, val)"
        />
        <p class="text-xs text-gray-500 dark:text-gray-400 ml-15">
          {{ t(`settings.integrationBypass.${field.description}`) }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ml-15 {
  margin-left: 3.75rem;
}
</style>

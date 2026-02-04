/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import ReputationTypeToggle from './ReputationTypeToggle.vue'

const { t } = useI18n()
const { session, updateReputationSettings, updateMessagesSettings } = useSession()

const updateSetting = async (key: string, value: boolean, type: 'reputation' | 'messages' = 'reputation') => {
  if (!session.value?.settings?.[type]) return
  
  // Update local state first for better UX
  if (type === 'reputation') {
    updateReputationSettings({ [key]: value })
  } else {
    updateMessagesSettings({ [key]: value })
  }
  
  try {
    const prefix = type.charAt(0).toUpperCase() + type.slice(1)
    const methodName = `update${prefix}${key.charAt(0).toUpperCase()}${key.slice(1)}` as keyof typeof api
    if (typeof api[methodName] === 'function') {
      await (api[methodName] as Function)(value)
    } else {
      console.error(`Method ${methodName} not found in API client`)
    }
  } catch (error) {
    // Revert local state on error
    if (type === 'reputation') {
      updateReputationSettings({ [key]: !value })
    } else {
      updateMessagesSettings({ [key]: !value })
    }
    console.error(`Failed to update ${type} setting ${key}:`, error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.reputation" class="space-y-6">
    <ReputationTypeToggle
      :model-value="session.settings.reputation.reactionActive"
      :label="t('general.reputation.types.reaction.label')"
      :description="t('general.reputation.types.reaction.description')"
      @update:model-value="updateSetting('reactionActive', $event)"
    />

    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0"
    >
      <div v-if="session.settings.reputation.reactionActive" class="pl-8">
        <ReputationTypeToggle
          :model-value="session.settings.messages.reactionConfirmation"
          :label="t('general.reputation.types.reaction.confirmation.label')"
          :description="t('general.reputation.types.reaction.confirmation.description')"
          @update:model-value="updateSetting('reactionConfirmation', $event, 'messages')"
        />
      </div>
    </transition>
    
    <ReputationTypeToggle
      :model-value="session.settings.reputation.answerActive"
      :label="t('general.reputation.types.answer.label')"
      :description="t('general.reputation.types.answer.description')"
      @update:model-value="updateSetting('answerActive', $event)"
    />
    
    <ReputationTypeToggle
      :model-value="session.settings.reputation.mentionActive"
      :label="t('general.reputation.types.mention.label')"
      :description="t('general.reputation.types.mention.description')"
      @update:model-value="updateSetting('mentionActive', $event)"
    />
    
    <ReputationTypeToggle
      :model-value="session.settings.reputation.fuzzyActive"
      :label="t('general.reputation.types.fuzzy.label')"
      :description="t('general.reputation.types.fuzzy.description')"
      @update:model-value="updateSetting('fuzzyActive', $event)"
    />
    
    <ReputationTypeToggle
      :model-value="session.settings.reputation.embedActive"
      :label="t('general.reputation.types.embed.label')"
      :description="t('general.reputation.types.embed.description')"
      @update:model-value="updateSetting('embedActive', $event)"
    />
    
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0"
    >
      <div v-if="session.settings.reputation.embedActive" class="pl-8">
        <ReputationTypeToggle
          :model-value="session.settings.reputation.directActive"
          :label="t('general.reputation.types.direct.label')"
          :description="t('general.reputation.types.direct.description')"
          @update:model-value="updateSetting('directActive', $event)"
        />
      </div>
    </transition>
    
    <ReputationTypeToggle
      :model-value="session.settings.reputation.commandActive"
      :label="t('general.reputation.types.command.label')"
      :description="t('general.reputation.types.command.description')"
      @update:model-value="updateSetting('commandActive', $event)"
    />

    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0"
    >
      <div v-if="session.settings.reputation.commandActive" class="pl-8">
        <ReputationTypeToggle
          :model-value="session.settings.messages.commandReputationEphemeral"
          :label="t('general.reputation.types.command.ephemeral.label')"
          :description="t('general.reputation.types.command.ephemeral.description')"
          @update:model-value="updateSetting('commandReputationEphemeral', $event, 'messages')"
        />
      </div>
    </transition>
  </div>
</template>

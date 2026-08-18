/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import ReputationTypeToggle from './ReputationTypeToggle.vue'
import {useSettingUpdate} from './useSettingUpdate'

const {t} = useI18n()
const {session, updateSetting} = useSettingUpdate()
</script>

<template>
  <div v-if="session?.settings?.reputation" class="space-y-6">
    <ReputationTypeToggle
        :description="t('general.reputation.types.reaction.description')"
        :label="t('general.reputation.types.reaction.label')"
        :model-value="session.settings.reputation.reactionActive"
        @update:model-value="updateSetting('reactionActive', $event)"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.types.answer.description')"
        :label="t('general.reputation.types.answer.label')"
        :model-value="session.settings.reputation.answerActive"
        @update:model-value="updateSetting('answerActive', $event)"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.types.mention.description')"
        :label="t('general.reputation.types.mention.label')"
        :model-value="session.settings.reputation.mentionActive"
        @update:model-value="updateSetting('mentionActive', $event)"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.types.fuzzy.description')"
        :label="t('general.reputation.types.fuzzy.label')"
        :model-value="session.settings.reputation.fuzzyActive"
        @update:model-value="updateSetting('fuzzyActive', $event)"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.types.embed.description')"
        :label="t('general.reputation.types.embed.label')"
        :model-value="session.settings.reputation.embedActive"
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
            :description="t('general.reputation.types.direct.description')"
            :label="t('general.reputation.types.direct.label')"
            :model-value="session.settings.reputation.directActive"
            @update:model-value="updateSetting('directActive', $event)"
        />
      </div>
    </transition>

    <ReputationTypeToggle
        :description="t('general.reputation.types.command.description')"
        :label="t('general.reputation.types.command.label')"
        :model-value="session.settings.reputation.commandActive"
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
            :description="t('general.reputation.types.command.ephemeral.description')"
            :label="t('general.reputation.types.command.ephemeral.label')"
            :model-value="session.settings.messages.commandReputationEphemeral"
            @update:model-value="updateSetting('commandReputationEphemeral', $event, 'messages')"
        />
      </div>
    </transition>
  </div>
</template>

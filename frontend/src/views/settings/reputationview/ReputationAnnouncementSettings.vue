/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import ReputationTypeToggle from './ReputationTypeToggle.vue'
import {useSettingUpdate} from './useSettingUpdate'

const {t} = useI18n()
const {session, updateSetting} = useSettingUpdate()

// A command reply everyone can see is the announcement itself. In that case the toggle adds the reputation count to
// that reply instead of sending an additional message.
const commandDescription = computed(() =>
    session.value?.settings?.messages?.commandReputationEphemeral
        ? t('general.reputation.announcement.types.command.ephemeral')
        : t('general.reputation.announcement.types.command.public')
)
</script>

<template>
  <div v-if="session?.settings?.messages && session?.settings?.reputation" class="space-y-6">
    <div class="flex flex-col gap-1">
      <label class="label">{{ t('general.reputation.announcement.label') }}</label>
      <p class="description">{{ t('general.reputation.announcement.description') }}</p>
    </div>

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.types.reaction')"
        :label="t('general.reputation.types.reaction.label')"
        :model-value="session.settings.messages.announceReaction"
        @update:model-value="updateSetting('announceReaction', $event, 'messages')"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.types.answer')"
        :label="t('general.reputation.types.answer.label')"
        :model-value="session.settings.messages.announceAnswer"
        @update:model-value="updateSetting('announceAnswer', $event, 'messages')"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.types.mention')"
        :label="t('general.reputation.types.mention.label')"
        :model-value="session.settings.messages.announceMention"
        @update:model-value="updateSetting('announceMention', $event, 'messages')"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.types.fuzzy')"
        :label="t('general.reputation.types.fuzzy.label')"
        :model-value="session.settings.messages.announceFuzzy"
        @update:model-value="updateSetting('announceFuzzy', $event, 'messages')"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.types.embed')"
        :label="t('general.reputation.types.embed.label')"
        :model-value="session.settings.messages.announceEmbed"
        @update:model-value="updateSetting('announceEmbed', $event, 'messages')"
    />

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.types.direct')"
        :label="t('general.reputation.types.direct.label')"
        :model-value="session.settings.messages.announceDirect"
        @update:model-value="updateSetting('announceDirect', $event, 'messages')"
    />

    <ReputationTypeToggle
        v-if="session.settings.reputation.commandActive"
        :description="commandDescription"
        :label="t('general.reputation.types.command.label')"
        :model-value="session.settings.messages.announceCommand"
        @update:model-value="updateSetting('announceCommand', $event, 'messages')"
    />

    <hr class="border-gray-200 dark:border-gray-700"/>

    <ReputationTypeToggle
        :description="t('general.reputation.announcement.delete.description')"
        :label="t('general.reputation.announcement.delete.label')"
        :model-value="session.settings.messages.announceDelete"
        @update:model-value="updateSetting('announceDelete', $event, 'messages')"
    />
  </div>
</template>

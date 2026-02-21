/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import SettingsContainer from './components/SettingsContainer.vue'
import MainEmojiSettings from './reactionsview/MainEmojiSettings.vue'
import AdditionalEmojiSettings from './reactionsview/AdditionalEmojiSettings.vue'

const {t} = useI18n()
const {session} = useSession()

const mainReaction = computed(() => session.value?.settings?.thanking.reactions.mainReaction || '🏅')
const additionalReactions = computed(() => [...(session.value?.settings?.thanking.reactions.reactions || [])])
</script>

<template>
  <SettingsContainer :description="t('reactions.description')" :title="t('reactions.title')">
    <MainEmojiSettings :initial-main-reaction="mainReaction"/>
    <hr class="my-8 border-gray-200 dark:border-gray-700"/>
    <AdditionalEmojiSettings :initial-reactions="additionalReactions"/>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

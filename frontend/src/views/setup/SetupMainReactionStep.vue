/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import MainEmojiSettings from '@/views/settings/reactionsview/MainEmojiSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const {t} = useI18n()
const {session} = useSession()

const mainReaction = computed(() => session.value?.settings.thanking.reactions.mainReaction || '🏅')

// Always allow proceeding (main reaction is optional)
watch(() => true, () => {
  emit('canProceed', true)
}, {immediate: true})
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.mainReaction.description') }}
    </p>

    <MainEmojiSettings :initial-main-reaction="mainReaction"/>
  </div>
</template>

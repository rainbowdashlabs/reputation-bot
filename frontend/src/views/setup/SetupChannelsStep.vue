/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import ChannelSelectionSettings from '@/views/settings/channelsview/ChannelSelectionSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const {t} = useI18n()
const {session} = useSession()

const canProceed = ref(false)

const isBlacklist = computed(() => {
  return session.value?.settings?.thanking?.channels?.whitelist === false
})

watch(session, (newSession) => {
  if (newSession?.settings?.thanking?.channels) {
    const channels = newSession.settings.thanking.channels.channels || []
    const categories = newSession.settings.thanking.channels.categories || []
    const hasSelection = channels.length > 0 || categories.length > 0

    // Allow proceeding if:
    // - At least one channel/category is selected, OR
    // - List type is BLACKLIST (can proceed without selection)
    canProceed.value = hasSelection || isBlacklist.value
    emit('canProceed', canProceed.value)
  }
}, {deep: true, immediate: true})
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.channels.description') }}
    </p>

    <ChannelSelectionSettings/>

    <div v-if="!canProceed"
         class="p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
      <p class="text-sm text-yellow-800 dark:text-yellow-200">
        {{ t('setup.steps.channels.required') }}
      </p>
    </div>
  </div>
</template>

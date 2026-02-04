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
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'

const {t} = useI18n()
const {session, updateThankingChannelsSettings} = useSession()

const channelsSettings = computed(() => session.value?.settings?.thanking?.channels)
const premiumFeatures = computed(() => session.value?.premiumFeatures)

const isChannelBlacklistUnlocked = computed(() => premiumFeatures.value?.channelBlacklist?.unlocked ?? true)

const updateWhitelist = async (value: boolean) => {
  if (!channelsSettings.value) return
  if (!value && !isChannelBlacklistUnlocked.value) return
  try {
    await api.updateThankingChannelsWhitelist(value)
    updateThankingChannelsSettings({whitelist: value})
  } catch (error) {
    console.error('Failed to update whitelist:', error)
  }
}
</script>

<template>
  <div v-if="channelsSettings" class="flex flex-col gap-4">
    <div class="flex flex-col gap-1">
      <label class="block text-sm font-medium text-gray-900 dark:text-gray-100">{{
          t('general.channels.listType.label')
        }}</label>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ t('general.channels.listType.description') }}</p>
    </div>

    <PremiumFeatureWarning
        v-if="!isChannelBlacklistUnlocked"
        :feature-name="t('general.channels.listType.premiumRequired')"
        :required-skus="premiumFeatures?.channelBlacklist?.requiredSkus"
        variant="small"
    />

    <div class="flex bg-gray-100 dark:bg-gray-800 p-1 rounded-lg w-fit">
      <button
          :class="channelsSettings.whitelist
          ? 'bg-white dark:bg-gray-700 shadow-sm text-indigo-600 dark:text-indigo-400'
          : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300'"
          class="px-4 py-2 rounded-md text-sm font-medium transition-colors"
          @click="updateWhitelist(true)"
      >
        {{ t('general.channels.listType.allow') }}
      </button>
      <button
          :class="[
          !channelsSettings.whitelist
            ? 'bg-white dark:bg-gray-700 shadow-sm text-indigo-600 dark:text-indigo-400'
            : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300',
          !isChannelBlacklistUnlocked ? 'opacity-50 cursor-not-allowed' : ''
        ]"
          :disabled="!isChannelBlacklistUnlocked"
          class="px-4 py-2 rounded-md text-sm font-medium transition-colors"
          @click="updateWhitelist(false)"
      >
        {{ t('general.channels.listType.deny') }}
      </button>
    </div>
  </div>
</template>

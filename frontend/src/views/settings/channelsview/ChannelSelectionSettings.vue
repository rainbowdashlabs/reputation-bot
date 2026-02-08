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
import ChannelSelectionItem from './ChannelSelectionItem.vue'
import CategorySelectionItem from './CategorySelectionItem.vue'

const {t} = useI18n()
const {session, updateThankingChannelsSettings} = useSession()

const channelsSettings = computed(() => session.value?.settings?.thanking?.channels)
const guildChannels = computed(() => session.value?.guild?.channels)
const premiumFeatures = computed(() => session.value?.premiumFeatures)

const visibleGuildChannels = computed(() => {
  if (!guildChannels.value) return null

  const categories = guildChannels.value.categories
    .map(category => {
      const visibleChannels = category.channels.filter(c => c.visible)
      return {
        ...category,
        channels: visibleChannels,
        originallyEmpty: category.channels.length === 0
      }
    })
    .filter(category => category.channels.length > 0 || category.originallyEmpty)

  const uncategorized = guildChannels.value.channels.filter(c => c.visible)

  return {
    categories,
    channels: uncategorized
  }
})

const toggleChannel = async (channelId: string) => {
  if (!channelsSettings.value) return
  let newChannels = channelsSettings.value.channels.slice()

  if (newChannels.some(c => c === channelId)) {
    newChannels = newChannels.filter(c => c !== channelId)
  } else {
    if (isChannelLimitReached.value) return
    newChannels.push(channelId)
  }

  try {
    await api.updateThankingChannelsList(newChannels)
    updateThankingChannelsSettings({channels: newChannels})
  } catch (error) {
    console.error('Failed to update channels:', error)
  }
}

const toggleCategory = async (categoryId: string) => {
  if (!channelsSettings.value) return
  let newCategories = channelsSettings.value.categories.slice()
  let newChannels = channelsSettings.value.channels.slice()

  const category = guildChannels.value?.categories.find(c => c.id === categoryId)
  const categoryChannelIds = category?.channels.map(c => c.id) || []

  if (newCategories.some(c => c === categoryId)) {
    newCategories = newCategories.filter(c => c !== categoryId)
  } else {
    if (isCategoryLimitReached.value) return
    newCategories.push(categoryId)
    // Automatically deselect all channels in this category
    newChannels = newChannels.filter(cId => !categoryChannelIds.includes(String(cId)))
  }

  try {
    await api.updateThankingCategoriesList(newCategories)
    updateThankingChannelsSettings({categories: newCategories})
    if (newChannels.length !== channelsSettings.value.channels.length) {
      await api.updateThankingChannelsList(newChannels)
      updateThankingChannelsSettings({channels: newChannels})
    }
  } catch (error) {
    console.error('Failed to update categories:', error)
  }
}

const isCategorySelected = (categoryId: string): boolean => {
  const id = String(categoryId)
  return !!channelsSettings.value?.categories.some(c => c === id)
}

const isChannelSelected = (channelId: string): boolean => {
  const id = String(channelId)
  return !!channelsSettings.value?.channels.some(c => c === id)
}

const isChannelLimitReached = computed(() => {
  if (!premiumFeatures.value?.reputationChannel || !channelsSettings.value) return false
  return !premiumFeatures.value.reputationChannel.unlocked &&
      channelsSettings.value.channels.length >= premiumFeatures.value.reputationChannel.max
})

const isCategoryLimitReached = computed(() => {
  if (!premiumFeatures.value?.reputationCategories || !channelsSettings.value) return false
  return !premiumFeatures.value.reputationCategories.unlocked &&
      channelsSettings.value.categories.length >= premiumFeatures.value.reputationCategories.max
})
</script>

<template>
  <div v-if="channelsSettings" class="space-y-8">
    <!-- Premium Warnings -->
    <div class="space-y-4">
      <PremiumFeatureWarning
          v-if="!premiumFeatures?.reputationChannel.unlocked"
          :message="t('general.channels.premium.channelLimitWarning', {
          limit: premiumFeatures?.reputationChannel.max
        })"
          :required-skus="premiumFeatures?.reputationChannel.requiredSkus"
          variant="small"
      />
      <PremiumFeatureWarning
          v-if="!premiumFeatures?.reputationCategories.unlocked"
          :message="t('general.channels.premium.categoryLimitWarning', {
          limit: premiumFeatures?.reputationCategories.max
        })"
          :required-skus="premiumFeatures?.reputationCategories.requiredSkus"
          variant="small"
      />
    </div>

    <!-- Channels and Categories List -->
    <div class="space-y-4">
      <div class="flex flex-col gap-1">
        <label class="block text-sm font-medium text-gray-900 dark:text-gray-100">{{
            t('general.channels.list.label')
          }}</label>
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ t('general.channels.list.description') }}</p>
      </div>

      <div
          class="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden divide-y divide-gray-200 dark:divide-gray-700">
        <!-- Categories -->
        <CategorySelectionItem
          v-for="category in visibleGuildChannels?.categories"
          :key="category.id"
          :category="category"
          :selected="isCategorySelected(category.id)"
          :disabled="isCategoryLimitReached"
          :is-channel-selected="isChannelSelected"
          :is-channel-limit-reached="isChannelLimitReached"
          @toggle-category="toggleCategory"
          @toggle-channel="toggleChannel"
        />

        <!-- Uncategorized Channels -->
        <ChannelSelectionItem
          v-for="channel in visibleGuildChannels?.channels"
          :key="channel.id"
          :channel="channel"
          :selected="isChannelSelected(channel.id)"
          :disabled="!isChannelSelected(channel.id) && isChannelLimitReached"
          class="bg-white dark:bg-gray-900"
          @toggle="toggleChannel"
        >
          <template #right>
            <span class="text-xs text-gray-500 uppercase">{{ t('general.channels.list.channels') }}</span>
          </template>
        </ChannelSelectionItem>
      </div>
    </div>
  </div>
</template>

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

// Helper function to get icon for channel type
const getChannelIcon = (type: string) => {
  switch (type) {
    case 'TEXT':
      return 'hashtag'
    case 'VOICE':
      return 'volume-high'
    case 'NEWS':
      return 'bullhorn'
    case 'FORUM':
      return 'comments'
    default:
      return 'hashtag'
  }
}

const channelsSettings = computed(() => session.value?.settings?.thanking?.channels)
const guildChannels = computed(() => session.value?.guild?.channels)
const premiumFeatures = computed(() => session.value?.premiumFeatures)

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

const isCategorySelected = (categoryId: string) => {
  const id = String(categoryId)
  return channelsSettings.value?.categories.some(c => c === id)
}

const isChannelSelected = (channelId: string) => {
  const id = String(channelId)
  return channelsSettings.value?.channels.some(c => c === id)
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
        <div v-for="category in guildChannels?.categories" :key="category.id" class="bg-white dark:bg-gray-900">
          <div
              class="flex items-center justify-between p-3 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors"
              @click="toggleCategory(category.id)"
          >
            <div class="flex items-center gap-3">
              <input
                  :checked="isCategorySelected(category.id)"
                  :disabled="!isCategorySelected(category.id) && isCategoryLimitReached"
                  class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 disabled:opacity-50"
                  type="checkbox"
                  @click.stop="toggleCategory(category.id)"
              />
              <span class="font-medium text-gray-900 dark:text-gray-100 uppercase text-xs tracking-wider">{{
                  category.name
                }}</span>
            </div>
            <span class="text-xs text-gray-500 uppercase">{{ t('general.channels.list.categories') }}</span>
          </div>

          <!-- Channels in Category -->
          <div class="divide-y divide-gray-100 dark:divide-gray-800 bg-gray-50/50 dark:bg-gray-800/50">
            <div
                v-for="channel in category.channels"
                :key="channel.id"
                :class="{ 'opacity-50 pointer-events-none': isCategorySelected(category.id) }"
                class="flex items-center justify-between p-3 pl-10 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer transition-colors"
                @click="toggleChannel(channel.id)"
            >
              <div class="flex items-center gap-3">
                <input
                    :checked="isChannelSelected(channel.id) || isCategorySelected(category.id)"
                    :disabled="isCategorySelected(category.id) || (!isChannelSelected(channel.id) && isChannelLimitReached)"
                    class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 disabled:opacity-50"
                    type="checkbox"
                    @click.stop="toggleChannel(channel.id)"
                />
                <span class="text-gray-700 dark:text-gray-300 flex items-center gap-2">
                  <font-awesome-icon :icon="getChannelIcon(channel.type)" class="text-gray-400 dark:text-gray-500"/>
                  {{ channel.name }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Uncategorized Channels -->
        <div
            v-for="channel in guildChannels?.channels"
            :key="channel.id"
            class="flex items-center justify-between p-3 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors bg-white dark:bg-gray-900"
            @click="toggleChannel(channel.id)"
        >
          <div class="flex items-center gap-3">
            <input
                :checked="isChannelSelected(channel.id)"
                :disabled="!isChannelSelected(channel.id) && isChannelLimitReached"
                class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 disabled:opacity-50"
                type="checkbox"
                @click.stop="toggleChannel(channel.id)"
            />
            <span class="text-gray-700 dark:text-gray-300 flex items-center gap-2">
              <font-awesome-icon :icon="getChannelIcon(channel.type)" class="text-gray-400 dark:text-gray-500"/>
              {{ channel.name }}
            </span>
          </div>
          <span class="text-xs text-gray-500 uppercase">{{ t('general.channels.list.channels') }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

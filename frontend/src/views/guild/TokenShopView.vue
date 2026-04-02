/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import type {ActiveFeaturePOJO, Feature} from '@/api/types'
import {useSession} from '@/composables/useSession'
import TokenFeatureCard from '@/components/TokenFeatureCard.vue'
import ViewContainer from '@/components/ViewContainer.vue'
import Toggle from '@/components/Toggle.vue'
import Header2 from "@/components/heading/Header2.vue";
import BaseButton from '@/components/BaseButton.vue'
import TokenValue from '@/components/TokenValue.vue'

const {t} = useI18n()
const {userSession, currentGuildId, userTokens, guildTokens, refreshUserTokens, refreshGuildTokens, refreshGuildPremium, premiumFeatures: sessionPremiumFeatures} = useSession()

const features = ref<Feature[]>([])
const activeFeatures = ref<ActiveFeaturePOJO[]>([])
const loading = ref(true)
const actionLoading = ref(false)
const useGuildTokens = ref(false)
const everyoneTokenPurchase = ref(true)
const transferAmount = ref(1)
const transferLoading = ref(false)

const isGuildAdmin = computed(() => {
  if (!currentGuildId.value || !userSession.value) return false
  return userSession.value.guilds[currentGuildId.value]?.accessLevel === 'GUILD_ADMIN' || userSession.value.isBotOwner
})

const fetchData = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const [featuresRes, activeRes, everyoneTokenPurchaseRes] = await Promise.all([
      api.getTokenFeatures(),
      api.getActiveFeatures(),
      api.getEveryoneTokenPurchase(),
      refreshGuildPremium(),
      refreshUserTokens(),
      refreshGuildTokens()
    ])

    features.value = Object.values(featuresRes)
    activeFeatures.value = activeRes
    everyoneTokenPurchase.value = everyoneTokenPurchaseRes
  } catch (error) {
    console.error('Failed to fetch token shop data', error)
  } finally {
    loading.value = false
  }
}

const handlePurchase = async (feature: Feature) => {
  actionLoading.value = true
  try {
    const result = await api.purchaseFeature(feature.id, useGuildTokens.value)
    if (result.success) {
      await fetchData(true)
    }
  } catch (error) {
    console.error('Purchase failed', error)
  } finally {
    actionLoading.value = false
  }
}

const handleSubscribe = async (feature: Feature) => {
  actionLoading.value = true
  try {
    const result = await api.subscribeFeature(feature.id)
    if (result.success) {
      await fetchData(true)
    }
  } catch (error) {
    console.error('Subscription failed', error)
  } finally {
    actionLoading.value = false
  }
}

const handleUnsubscribe = async (feature: Feature) => {
  actionLoading.value = true
  try {
    const result = await api.unsubscribeFeature(feature.id)
    if (result.success) {
      await fetchData(true)
    }
  } catch (error) {
    console.error('Unsubscription failed', error)
  } finally {
    actionLoading.value = false
  }
}

const handleTransfer = async () => {
  if (!currentGuildId.value || transferAmount.value <= 0) return
  transferLoading.value = true
  try {
    await api.transferTokensToGuild(currentGuildId.value, transferAmount.value)
    await fetchData(true)
    transferAmount.value = 1
  } catch (error) {
    console.error('Transfer failed', error)
  } finally {
    transferLoading.value = false
  }
}

onMounted(fetchData)

const getActiveFeature = (featureId: number) => {
  return activeFeatures.value.find(f => f.featureId === featureId)
}

const getRequiredSkus = (localeKey: string) => {
  if (!sessionPremiumFeatures.value) return []
  // localeKey is like "sku.reputationlog" -> strip "sku." -> camelCase key in premiumFeatures
  const key = localeKey.replace('sku.', '').toLowerCase()
  const map: Record<string, string> = {
    reputationlog: 'reputationLog',
    analyzerlog: 'analyzerLog',
    channelblacklist: 'channelBlacklist',
    localeoverrides: 'localeOverrides',
    autopost: 'autopost',
    advancedrankings: 'advancedRankings',
    detailedprofile: 'detailedProfile',
    logchannel: 'logChannel',
    additionalemojis: 'additionalEmojis',
    profile: 'profile',
    integrationbypass: 'integrationBypass',
    reputationchannel: 'reputationChannel',
    reputationcategories: 'reputationCategories',
  }
  const field = map[key]
  if (!field) return []
  const feature = (sessionPremiumFeatures.value as any)[field]
  return feature?.requiredSkus ?? []
}

const canAfford = (tokens: number) => {
  if (!everyoneTokenPurchase.value && !useGuildTokens.value && !isGuildAdmin.value) return false
  return (useGuildTokens.value ? guildTokens.value : userTokens.value) >= tokens
}

const canPurchase = computed(() => {
  return everyoneTokenPurchase.value || isGuildAdmin.value
})

</script>

<template>
  <ViewContainer>
    <div class="pb-4">
    <Header2 class="mb-4">{{ t('token_shop.title') }}</Header2>
    <span class="text-gray-500 dark:text-gray-400">{{ t('token_shop.description') }}</span>
    </div>

    <div v-if="!canPurchase" class="mb-6 p-4 bg-red-100 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-lg flex items-center gap-3 text-red-700 dark:text-red-400">
      <font-awesome-icon icon="exclamation-triangle" />
      <span class="text-sm font-medium">{{ t('token_shop.adminOnly') }}</span>
    </div>

    <div class="mb-8 p-6 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center gap-8">
          <div class="flex flex-col">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('token_shop.userTokens') }}</span>
            <div class="flex items-center gap-3">
              <span class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
                <TokenValue :tokens="userTokens" />
              </span>
              <router-link
                  to="/user/vote"
                  class="inline-flex items-center text-xs font-semibold text-indigo-600 dark:text-indigo-400 hover:text-indigo-500 dark:hover:text-indigo-300 transition-colors"
              >
                <font-awesome-icon icon="plus" class="mr-1" />
                {{ t('token_shop.getMoreTokens') }}
              </router-link>
            </div>
          </div>
          <div class="flex flex-col">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('token_shop.guildTokens') }}</span>
            <span class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
              <TokenValue :tokens="guildTokens" icon-class="text-yellow-600" />
            </span>
          </div>
          <div v-if="userTokens > 0" class="flex flex-col border-l border-gray-200 dark:border-gray-700 pl-8">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('voting.transfer.title') }}</span>
            <div class="flex items-center gap-2 mt-1">
              <input
                  type="number"
                  v-model.number="transferAmount"
                  min="1"
                  :max="userTokens"
                  class="w-20 px-2 py-1 text-sm rounded border border-gray-300 dark:border-gray-600 bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-gray-100"
              />
              <BaseButton
                  color="primary"
                  size="sm"
                  :disabled="transferLoading || transferAmount <= 0 || transferAmount > userTokens"
                  @click="handleTransfer"
              >
                <font-awesome-icon v-if="transferLoading" icon="spinner" spin class="mr-1"/>
                <font-awesome-icon v-else icon="exchange-alt" class="mr-1"/>
                {{ t('voting.transfer.submit') }}
              </BaseButton>
            </div>
          </div>
        </div>

        <div v-if="isGuildAdmin" class="flex items-center gap-3">
          <span
              :class="['text-sm transition-colors', !useGuildTokens ? 'text-indigo-600 dark:text-indigo-400 font-bold' : 'text-gray-500 dark:text-gray-400']">
            {{ t('token_shop.userTokens') }}
          </span>
          <Toggle
              v-model="useGuildTokens"
              active-class="bg-indigo-600"
              inactive-class="bg-indigo-600"
              :active-icon="['fas', 'coins']"
              :inactive-icon="['fas', 'coins']"
          />
          <span
              :class="['text-sm transition-colors', useGuildTokens ? 'text-indigo-600 dark:text-indigo-400 font-bold' : 'text-gray-500 dark:text-gray-400']">
            {{ t('token_shop.guildTokens') }}
          </span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-12">
      <font-awesome-icon icon="spinner" spin class="text-4xl text-indigo-600"/>
    </div>

    <div v-else-if="features.length === 0" class="text-center py-12 text-gray-500 dark:text-gray-400">
      {{ t('token_shop.noFeatures') }}
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <TokenFeatureCard
          v-for="feature in features"
          :key="feature.id"
          :feature="feature"
          :active-feature="getActiveFeature(feature.id)"
          :active-skus="[...(sessionPremiumFeatures?.activeSkus ?? [])]"
          :required-skus="getRequiredSkus(feature.localeKey)"
          :is-guild-admin="isGuildAdmin"
          :can-afford="canAfford(feature.tokens) && (canPurchase || useGuildTokens)"
          :loading="actionLoading"
          @purchase="handlePurchase(feature)"
          @subscribe="handleSubscribe(feature)"
          @unsubscribe="handleUnsubscribe(feature)"
      />
    </div>
  </ViewContainer>
</template>

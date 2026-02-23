/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import type {ActiveFeaturePOJO, Feature, PremiumFeaturesPOJO} from '@/api/types'
import {useSession} from '@/composables/useSession'
import TokenFeatureCard from '@/components/TokenFeatureCard.vue'
import ViewContainer from '@/components/ViewContainer.vue'
import Toggle from '@/components/Toggle.vue'
import Header2 from "@/components/heading/Header2.vue";

const {t} = useI18n()
const {userSession, currentGuildId} = useSession()

const features = ref<Feature[]>([])
const activeFeatures = ref<ActiveFeaturePOJO[]>([])
const loading = ref(true)
const actionLoading = ref(false)
const useGuildTokens = ref(false)
const userTokens = ref(0)
const guildTokens = ref(0)
const premiumFeatures = ref<PremiumFeaturesPOJO | null>(null)

const isGuildAdmin = computed(() => {
  if (!currentGuildId.value || !userSession.value) return false
  return userSession.value.guilds[currentGuildId.value]?.accessLevel === 'GUILD_ADMIN' || userSession.value.isBotOwner
})

const fetchData = async () => {
  loading.value = true
  try {
    const [featuresRes, activeRes, userTokensRes, guildTokensRes, premiumRes] = await Promise.all([
      api.getTokenFeatures(),
      api.getActiveFeatures(),
      api.getUserTokens(),
      api.getGuildTokens(),
      api.getGuildPremium()
    ])

    // api.getTokenFeatures() returns a Map-like structure or Array depending on how it was implemented/cached
    // In our case, the response from backend is an array of features
    features.value = Object.values(featuresRes)
    activeFeatures.value = activeRes
    userTokens.value = userTokensRes.tokens
    guildTokens.value = guildTokensRes.tokens
    premiumFeatures.value = premiumRes
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
      await fetchData()
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
      await fetchData()
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
      await fetchData()
    }
  } catch (error) {
    console.error('Unsubscription failed', error)
  } finally {
    actionLoading.value = false
  }
}

onMounted(fetchData)

const getActiveFeature = (featureId: number) => {
  return activeFeatures.value.find(f => f.featureId === featureId)
}

const canAfford = (tokens: number) => {
  return (useGuildTokens.value ? guildTokens.value : userTokens.value) >= tokens
}

</script>

<template>
  <ViewContainer>
    <div class="pb-4">
    <Header2 class="mb-4">{{ t('token_shop.title') }}</Header2>
    <span class="text-gray-500 dark:text-gray-400">{{ t('token_shop.description') }}</span>
    </div>
    <div class="mb-8 p-6 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center gap-8">
          <div class="flex flex-col">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('token_shop.userTokens') }}</span>
            <div class="flex items-center gap-3">
              <span class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
                <font-awesome-icon icon="coins" class="mr-2 text-yellow-500"/>
                {{ userTokens }}
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
          <div v-if="isGuildAdmin" class="flex flex-col">
            <span class="text-sm text-gray-500 dark:text-gray-400">{{ t('token_shop.guildTokens') }}</span>
            <span class="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
              <font-awesome-icon icon="coins" class="mr-2 text-yellow-600"/>
              {{ guildTokens }}
            </span>
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
          :active-skus="premiumFeatures?.activeSkus ?? []"
          :is-guild-admin="isGuildAdmin"
          :can-afford="canAfford(feature.tokens)"
          :loading="actionLoading"
          @purchase="handlePurchase(feature)"
          @subscribe="handleSubscribe(feature)"
          @unsubscribe="handleUnsubscribe(feature)"
      />
    </div>
  </ViewContainer>
</template>

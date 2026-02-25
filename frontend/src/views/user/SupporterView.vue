/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { api } from '@/api'
import type { KofiPurchasePOJO, SKU, Links } from '@/api/types'
import { useSession } from '@/composables/useSession'
import ViewContainer from '@/components/ViewContainer.vue'
import LoginPanel from '@/views/settings/components/LoginPanel.vue'
import SettingsContainer from "@/views/settings/components/SettingsContainer.vue";
import SupporterPurchaseItem from "@/views/user/components/SupporterPurchaseItem.vue";

const { t } = useI18n()
const { userSession, refreshGuildPremium } = useSession()

const purchases = ref<KofiPurchasePOJO[]>([])
const skus = ref<SKU[]>([])
const links = ref<Links | null>(null)
const loading = ref(false)
const error = ref<string>('')

const guilds = computed(() => {
  if (!userSession.value) return []
  return Object.values(userSession.value.guilds).sort((a, b) => a.name.localeCompare(b.name))
})

const subscriptions = computed(() =>
  purchases.value
    .filter(p => p.type === 'Subscription' && p.expiresAt && new Date(p.expiresAt) > new Date())
    .sort((a, b) => a.id - b.id)
)

const lifetimes = computed(() =>
  purchases.value
    .filter(p => p.type === 'Shop Order')
    .sort((a, b) => a.id - b.id)
)

async function loadPurchases() {
  try {
    loading.value = true
    const [purchasesData, skusData, linksData] = await Promise.all([
      api.getUserPurchases(),
      api.getAvailableSKUs(),
      api.getLinks()
    ])
    purchases.value = purchasesData
    skus.value = skusData
    links.value = linksData
  } catch (e) {
    console.error('Failed to load purchases', e)
    error.value = t('user.supporter.errors.load') as string
  } finally {
    loading.value = false
  }
}

function mapSubscriptionError(err: any): string {
  const data = err?.response?.data
  const title = typeof data === 'string' ? data : (data && typeof data.title === 'string' ? data.title : '')
  if (!title) return t('user.supporter.errors.assign') as string
  // Localize known subscription results
  const key = `user.supporter.results.${title}`
  const localized = t(key)
  // If the key resolves to the same path, fallback to generic error
  if (localized === key) {
    return t('user.supporter.errors.assign') as string
  }
  return localized as string
}

async function assign(purchaseId: number, guildId: string) {
  try {
    await api.assignPurchaseToGuild(purchaseId, guildId)
    await loadPurchases()
    await refreshGuildPremium()
  } catch (e: any) {
    console.error('Failed to assign purchase', e)
    error.value = mapSubscriptionError(e)
  }
}

async function unassign(purchaseId: number) {
  try {
    await api.unassignPurchaseFromGuild(purchaseId)
    await loadPurchases()
    await refreshGuildPremium()
  } catch (e: any) {
    console.error('Failed to unassign purchase', e)
    // Backend generally does not return SubscriptionResult on unassign, keep generic
    error.value = (t('user.supporter.errors.unassign') as string)
  }
}

onMounted(loadPurchases)
</script>

<template>
  <ViewContainer class="pt-8">
    <div v-if="!userSession" class="max-w-4xl mx-auto px-4">
      <LoginPanel />
    </div>

    <SettingsContainer v-else class="max-w-4xl mx-auto px-4 space-y-4" :title="t('user.supporter.title')" :description="t('user.supporter.description')">
      <template #header-actions v-if="links?.kofi">
        <a :href="links.kofi" target="_blank" rel="noopener noreferrer" class="px-3 py-1.5 text-sm rounded-md bg-indigo-600 text-white hover:bg-indigo-700 flex items-center gap-2 transition-colors">
          <font-awesome-icon :icon="['fas', 'shopping-cart']" />
          {{ t('user.supporter.kofiLink') }}
        </a>
      </template>

      <div v-if="loading" class="flex justify-center py-8">
        <font-awesome-icon :icon="['fas','spinner']" class="animate-spin text-gray-500" />
      </div>

      <div v-else class="space-y-10">
        <div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100 flex items-center gap-2">
            <font-awesome-icon :icon="['fas','shopping-cart']" />
            {{ t('user.supporter.sections.subscriptions') }}
          </h2>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ t('user.supporter.sections.subscriptions_desc') }}</p>
          <div v-if="subscriptions.length === 0" class="text-sm text-gray-500 dark:text-gray-400">{{ t('common.noData') }}</div>
          <div v-else class="space-y-2">
            <SupporterPurchaseItem
              v-for="p in subscriptions"
              :key="p.id"
              :purchase="p"
              :guilds="guilds"
              :skus="skus"
              @assign="assign"
              @unassign="unassign"
            />
          </div>
        </div>

        <div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-gray-100 flex items-center gap-2">
            <font-awesome-icon :icon="['fas','shopping-cart']" />
            {{ t('user.supporter.sections.lifetime') }}
          </h2>
          <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ t('user.supporter.sections.lifetime_desc') }}</p>
          <div v-if="lifetimes.length === 0" class="text-sm text-gray-500 dark:text-gray-400">{{ t('common.noData') }}</div>
          <div v-else class="space-y-2">
            <SupporterPurchaseItem
              v-for="p in lifetimes"
              :key="p.id"
              :purchase="p"
              :guilds="guilds"
              :skus="skus"
              @assign="assign"
              @unassign="unassign"
            />
          </div>
        </div>

        <div v-if="purchases.length === 0" class="rounded-md bg-indigo-50 dark:bg-indigo-900/30 p-4 border border-indigo-100 dark:border-indigo-800">
          <div class="flex">
            <div class="flex-shrink-0">
              <font-awesome-icon :icon="['fas', 'info-circle']" class="text-indigo-400" />
            </div>
            <div class="ml-3">
              <p class="text-sm text-indigo-700 dark:text-indigo-300">
                <i18n-t keypath="user.supporter.missingPurchasesNotice" scope="global">
                  <template #link>
                    <router-link to="/user/settings" class="font-medium underline hover:text-indigo-600 dark:hover:text-indigo-200">
                      {{ t('user.supporter.settingsLink') }}
                    </router-link>
                  </template>
                </i18n-t>
              </p>
            </div>
          </div>
        </div>

        <div v-if="error" class="rounded-md bg-red-50 dark:bg-red-900/30 p-3 text-sm text-red-700 dark:text-red-300">
          {{ error }}
        </div>
      </div>
    </SettingsContainer>
  </ViewContainer>
</template>

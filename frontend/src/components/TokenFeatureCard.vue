/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {ActiveFeaturePOJO, Feature} from '@/api/types'
import ConfirmButton from '@/components/ConfirmButton.vue'

const {t, d} = useI18n()

interface Props {
  feature: Feature
  activeFeature?: ActiveFeaturePOJO
  activeSkus: string[]
  isGuildAdmin: boolean
  canAfford: boolean
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'purchase'): void
  (e: 'subscribe'): void
  (e: 'unsubscribe'): void
}>()

const featureLocaleKey = computed(() => {
  const key = props.feature.localeKey.replace('sku.', '')
  return `token_shop.feature.${key}`
})

const expiresDate = computed(() => {
  if (!props.activeFeature?.expires) return null
  const date = new Date(props.activeFeature.expires)
  if (isNaN(date.getTime())) {
    console.error('Invalid date format:', props.activeFeature.expires)
    return props.activeFeature.expires
  }
  try {
    return d(date, 'short')
  } catch (e) {
    console.error('i18n date formatting failed:', e)
    return date.toLocaleDateString()
  }
})

const isActive = computed(() => !!props.activeFeature)
const isAutoRenewal = computed(() => props.activeFeature?.autoRenewal ?? false)

const isEnabledBySubscription = computed(() => {
  if (!props.feature.skuEntry?.skus) return false
  return props.feature.skuEntry.skus.some(sku => props.activeSkus.includes(sku.skuId))
})
</script>

<template>
  <div :class="['bg-white dark:bg-gray-800 rounded-lg shadow-md overflow-hidden border flex flex-col', isEnabledBySubscription ? 'border-indigo-500 ring-1 ring-indigo-500 opacity-90' : 'border-gray-200 dark:border-gray-700']">
    <div class="p-6 flex-grow">
      <div class="flex justify-between items-start mb-4 gap-2">
        <h3 class="text-xl font-bold text-gray-900 dark:text-white flex-grow">
          {{ t(`${featureLocaleKey}.name`) }}
        </h3>
        <div class="shrink-0 pt-1">
          <div v-if="isEnabledBySubscription" class="px-2 py-1 bg-indigo-100 text-indigo-800 text-[10px] leading-tight font-bold rounded-full uppercase whitespace-nowrap">
            {{ t('token_shop.enabledBySubscription') }}
          </div>
          <div v-else-if="isActive" class="px-2 py-1 bg-green-100 text-green-800 text-[10px] leading-tight font-bold rounded-full uppercase whitespace-nowrap">
            {{ t('token_shop.active') }}
          </div>
        </div>
      </div>

      <p class="text-gray-600 dark:text-gray-400 mb-6">
        {{ t(`${featureLocaleKey}.description`) }}
      </p>

      <div class="space-y-2 mb-6">
        <div class="flex items-center text-sm text-gray-500 dark:text-gray-400">
          <font-awesome-icon icon="coins" class="mr-2 text-yellow-500" />
          <span class="font-semibold text-gray-700 dark:text-gray-300 mr-1">{{ feature.tokens }}</span>
          {{ t('voting.log.tokens') }}
        </div>

        <div v-if="isActive && expiresDate" class="flex items-center text-sm text-gray-500 dark:text-gray-400">
          <font-awesome-icon icon="calendar-alt" class="mr-2 text-blue-500" />
          {{ isAutoRenewal ? t('token_shop.nextRenewal', { date: expiresDate }) : t('token_shop.expires', { date: expiresDate }) }}
        </div>

        <div v-if="isActive" class="flex items-center text-sm text-gray-500 dark:text-gray-400">
          <font-awesome-icon :icon="isAutoRenewal ? 'sync-alt' : 'times-circle'" :class="['mr-2', isAutoRenewal ? 'text-green-500' : 'text-red-500']" />
          {{ t('token_shop.autoRenewal') }}: {{ isAutoRenewal ? t('auditLog.values.enabled') : t('auditLog.values.disabled') }}
        </div>
      </div>
    </div>

    <div class="p-4 bg-gray-50 dark:bg-gray-700/50 border-t border-gray-200 dark:border-gray-700 flex flex-wrap gap-2">
      <template v-if="isEnabledBySubscription">
        <div class="w-full text-center py-2 text-sm font-medium text-indigo-600 dark:text-indigo-400 italic">
          {{ t('token_shop.subscriptionActiveNote') }}
        </div>
      </template>
      <template v-else>
        <ConfirmButton
          @confirm="emit('purchase')"
          :disabled="loading || !canAfford"
          :label="t('token_shop.purchase')"
          icon="shopping-cart"
          class="flex-grow"
        />

        <template v-if="isGuildAdmin">
          <ConfirmButton
            v-if="!isAutoRenewal"
            @confirm="emit('subscribe')"
            :disabled="loading || (!isActive && !canAfford)"
            :label="t('token_shop.subscribe')"
            icon="bell"
            class="flex-grow"
            base-class="border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:ring-indigo-500"
          />

          <button
            v-else-if="isActive"
            @click="emit('unsubscribe')"
            :disabled="loading"
            class="flex-grow inline-flex justify-center items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-md text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <font-awesome-icon icon="bell-slash" class="mr-2" />
            {{ t('token_shop.unsubscribe') }}
          </button>
        </template>
      </template>
    </div>
  </div>
</template>

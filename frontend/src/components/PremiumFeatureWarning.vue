/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {SkuInfo} from '@/api/types'

const {t} = useI18n()

interface Props {
  featureName?: string
  message?: string
  requiredSkus?: readonly SkuInfo[]
  variant?: 'large' | 'small'
}

const props = withDefaults(defineProps<Props>(), {
  requiredSkus: () => [],
  variant: 'large'
})

const displayMessage = computed(() => {
  // If message is provided directly, use it
  if (props.message) {
    return props.message
  }
  // Otherwise, construct from featureName
  if (props.featureName) {
    return t('premiumFeature.message', {name: props.featureName})
  }
  return ''
})
</script>

<template>
  <div
      v-if="variant === 'large'"
      class="mb-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4"
  >
    <div class="flex items-start">
      <svg class="h-6 w-6 text-yellow-600 mr-3 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" stroke-linecap="round" stroke-linejoin="round"
              stroke-width="2"/>
      </svg>
      <div>
        <h3 class="text-lg font-semibold text-yellow-800 mb-2">{{ t('premiumFeature.title') }}</h3>
        <p class="text-yellow-700 mb-2">{{ displayMessage }}</p>
        <div v-if="requiredSkus.length > 0" class="mt-2">
          <p class="text-sm font-medium text-yellow-800 mb-1">
            {{ requiredSkus.length > 1 ? t('premiumFeature.requiredTierPlural') : t('premiumFeature.requiredTierSingular') }}
          </p>
          <ul class="list-disc list-inside text-sm text-yellow-700">
            <li v-for="sku in requiredSkus" :key="sku.id">{{ sku.name }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>

  <div
      v-else
      class="mb-3 p-3 bg-yellow-50 border border-yellow-200 rounded text-sm"
  >
    <p class="text-yellow-800 font-medium mb-1">{{ t('premiumFeature.title') }}</p>
    <p class="text-yellow-700">{{ displayMessage }}</p>
    <p v-if="requiredSkus.length > 0" class="text-yellow-800 font-medium mt-2 mb-1">
      {{ requiredSkus.length > 1 ? t('premiumFeature.requiredTierPlural') : t('premiumFeature.requiredTierSingular') }}
    </p>
    <ul v-if="requiredSkus.length > 0" class="list-disc list-inside text-yellow-700 ml-2">
      <li v-for="sku in requiredSkus" :key="sku.id">{{ sku.name }}</li>
    </ul>
  </div>
</template>

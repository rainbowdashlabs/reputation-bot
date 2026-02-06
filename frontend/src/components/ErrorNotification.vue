/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useErrorStore} from '../stores/errorStore'
import type {ApiErrorResponse} from '../api/types'

const errorStore = useErrorStore()
const {t} = useI18n()

const errors = computed(() => errorStore.errors)

const closeError = (id: string) => {
  errorStore.removeError(id)
}

const getErrorTitle = (error: ApiErrorResponse): string => {
  return error.error || 'Error'
}

const getErrorMessage = (error: ApiErrorResponse): string => {
  return error.message || 'An unexpected error occurred'
}

const getErrorDetails = (error: ApiErrorResponse): string | null => {
  if (!error.details) return null

  // Handle PremiumFeatureErrorDetails
  if (error.details.feature) {
    const details = error.details
    let message = `Feature: ${details.feature}`

    if (details.requiredSkus && details.requiredSkus.length > 0) {
      const skuNames = details.requiredSkus.map((sku: any) => sku.name).join(', ')
      message += `\nRequired: ${skuNames}`
    }

    if (details.currentValue !== undefined && details.maxValue !== undefined) {
      message += `\nLimit: ${details.currentValue}/${details.maxValue}`
    }

    return message
  }

  // Handle other details as JSON string
  if (typeof error.details === 'object') {
    return JSON.stringify(error.details, null, 2)
  }

  return String(error.details)
}
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-md">
    <div
        v-for="errorItem in errors"
        :key="errorItem.id"
        class="bg-red-500 text-white rounded-lg shadow-lg p-4 cursor-pointer hover:bg-red-700 transition-colors duration-200 animate-slide-in"
        @click="closeError(errorItem.id)"
    >
      <div class="flex items-start justify-between gap-2">
        <div class="flex-1">
          <h3 class="font-bold text-lg mb-1">{{ getErrorTitle(errorItem.error) }}</h3>
          <p class="text-sm mb-2">{{ getErrorMessage(errorItem.error) }}</p>
          <pre
              v-if="getErrorDetails(errorItem.error)"
              class="text-xs bg-red-700 bg-opacity-50 rounded p-2 mt-2 whitespace-pre-wrap break-words"
          >{{ getErrorDetails(errorItem.error) }}</pre>
          <p class="text-xs mt-2 italic">{{ t('error.notice.workingOnIt') }}</p>
        </div>
      </div>
      <p class="text-xs text-red-200 mt-2">Click to dismiss</p>
    </div>
  </div>
</template>

<style scoped>
@keyframes slide-in {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-slide-in {
  animation: slide-in 0.3s ease-out;
}
</style>

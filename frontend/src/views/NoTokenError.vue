/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <div class="max-w-2xl mx-auto">
      <div class="bg-red-50 border border-red-200 shadow rounded-lg p-8 text-center">
        <div class="mb-4">
          <svg class="mx-auto h-16 w-16 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" stroke-linecap="round" stroke-linejoin="round"
                  stroke-width="2"/>
          </svg>
        </div>
        <h1 class="text-3xl font-bold text-red-800 mb-4">{{ t('error.noToken.title') }}</h1>
        <p class="text-lg text-red-700 mb-6">{{ t('error.noToken.message') }}</p>
        <p class="text-gray-600">{{ t('error.noToken.instruction') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>

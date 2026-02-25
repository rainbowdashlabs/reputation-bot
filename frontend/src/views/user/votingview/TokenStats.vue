/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'

defineProps<{
  tokens: number | null
}>()

const { t } = useI18n()
</script>

<template>
  <section>
    <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">{{ t('voting.tokens.title') }}</h3>
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">{{ t('voting.tokens.description') }}</p>
    <div class="bg-indigo-50 dark:bg-indigo-900/20 rounded-lg p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
      <div class="flex items-center justify-between w-full sm:w-auto sm:gap-4">
        <span class="text-indigo-700 dark:text-indigo-300 font-medium">{{ t('voting.tokens.label') }}</span>
        <span class="text-2xl font-bold text-indigo-600 dark:text-indigo-400">{{ tokens }}</span>
      </div>
      <router-link
        to="/guild/token-shop"
        class="w-full sm:w-auto inline-flex justify-center items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
      >
        <font-awesome-icon icon="shopping-cart" class="mr-2" />
        {{ t('voting.tokens.shop') }}
      </router-link>
    </div>
  </section>
</template>

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
    <div class="bg-indigo-50 dark:bg-indigo-900/20 rounded-lg p-4 flex items-center justify-between">
      <span class="text-indigo-700 dark:text-indigo-300 font-medium">{{ t('voting.tokens.label') }}</span>
      <span class="text-2xl font-bold text-indigo-600 dark:text-indigo-400">{{ tokens }}</span>
    </div>
  </section>
</template>

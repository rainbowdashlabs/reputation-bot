/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import BaseButton from '@/components/BaseButton.vue'

const {t} = useI18n()
const {login} = useSession()
</script>

<template>
  <div class="flex flex-col items-center justify-center p-12 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
    <div class="w-16 h-16 bg-indigo-100 dark:bg-indigo-900/50 rounded-full flex items-center justify-center mb-6">
      <font-awesome-icon :icon="['fab', 'discord']" class="text-3xl text-indigo-600 dark:text-indigo-400" />
    </div>
    <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-2">
      {{ t('login.title') }}
    </h2>
    <p class="text-gray-600 dark:text-gray-400 text-center mb-8 max-w-sm">
      {{ t('login.description') }}
    </p>
    <BaseButton
        class="flex items-center gap-3 px-4 py-2"
        @click="login()"
    >
      <font-awesome-icon :icon="['fab', 'discord']" />
      {{ t('login.button') }}
    </BaseButton>
  </div>
</template>

<style scoped>
</style>

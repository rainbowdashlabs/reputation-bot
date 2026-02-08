/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {onMounted, ref} from 'vue'
import {api} from '@/api'

const {t} = useI18n()

const hasProblems = ref(false)

onMounted(async () => {
  try {
    const result = await api.getDebug()
    hasProblems.value = result.missingGlobalPermissions.length > 0 ||
        result.simpleProblems.length > 0 ||
        result.missingPermissions.length > 0 ||
        result.rankProblems.length > 0 ||
        result.reputationChannelProblems.length > 0 ||
        result.simpleWarnings.length > 0
  } catch (e) {
    console.error('Failed to fetch debug info', e)
  }
})
</script>

<template>
  <div class="bg-gray-50 dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 transition-colors">
    <div class="mx-auto px-4" style="max-width: 1600px;">
      <nav class="flex">
        <router-link
            to="/settings/edit"
            class="px-6 py-3 text-sm font-medium transition-colors border-b-2"
            :class="[
              $route.path.startsWith('/settings/edit') && !$route.path.endsWith('/problems') && !$route.path.endsWith('/audit-log')
                ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            ]"
        >
          {{ t('navigation.settings') }}
        </router-link>
        <router-link
            to="/settings/preset"
            class="px-6 py-3 text-sm font-medium transition-colors border-b-2"
            :class="[
              $route.path.startsWith('/settings/preset')
                ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            ]"
        >
          {{ t('navigation.presets') }}
        </router-link>
        <router-link
            to="/settings/edit/audit-log"
            class="px-6 py-3 text-sm font-medium transition-colors border-b-2"
            :class="[
              $route.path.endsWith('/audit-log')
                ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            ]"
        >
          {{ t('settings.auditLog') }}
        </router-link>
        <router-link
            to="/settings/edit/problems"
            class="px-6 py-3 text-sm font-medium transition-colors border-b-2 flex items-center gap-2"
            :class="[
              $route.path.endsWith('/problems')
                ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400'
                : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'
            ]"
        >
          <span>{{ t('settings.problems') }}</span>
          <font-awesome-icon
              v-if="hasProblems"
              icon="circle-exclamation"
              class="text-red-500"
          />
        </router-link>
      </nav>
    </div>
  </div>
</template>

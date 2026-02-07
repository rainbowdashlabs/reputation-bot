/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import Header1 from '@/components/heading/Header1.vue'

const {t} = useI18n()

const settingsSections = [
  {key: 'general', path: '/settings/general'},
  {key: 'profile', path: '/settings/profile'},
  {key: 'ranks', path: '/settings/ranks'},
  {key: 'reputation', path: '/settings/reputation'},
  {key: 'channels', path: '/settings/channels'},
  {key: 'roles', path: '/settings/roles'},
  {key: 'reactions', path: '/settings/reactions'},
  {key: 'thankwords', path: '/settings/thankwords'},
  {key: 'abuseProtection', path: '/settings/abuse-protection'},
  {key: 'announcements', path: '/settings/announcements'},
  {key: 'autopost', path: '/settings/autopost'},
  {key: 'logChannel', path: '/settings/log-channel'},
  {key: 'integrationBypass', path: '/settings/integration-bypass'},
  {key: 'auditLog', path: '/settings/audit-log'}
]
</script>

<template>
  <div class="mx-auto" style="max-width: 1600px;">
    <div class="transition-colors">
      <!-- Heading above sidebar -->
      <Header1 class="mb-2 px-4">{{ t('navigation.settings') }}</Header1>

      <div class="flex">
        <!-- Sidebar - Full height, stretches from top to bottom -->
        <aside class="w-64 shrink-0">
          <nav class="bg-white dark:bg-gray-800 shadow rounded-lg p-4 top-24 ml-4 transition-colors">
            <ul class="space-y-1">
              <li v-for="section in settingsSections" :key="section.key">
                <router-link
                    :to="section.path"
                    active-class="bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 font-medium"
                    class="block px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 hover:text-indigo-600 dark:hover:text-indigo-400 rounded transition-colors"
                >
                  {{ t(`settings.${section.key}`) }}
                </router-link>
              </li>
            </ul>
          </nav>
        </aside>

        <!-- Content Area - Max width 1200px -->
        <div class="flex-1 px-4 pb-8" style="max-width: 1200px;">
          <main>
            <router-view/>
          </main>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'

const {t} = useI18n()
const {userSession} = useSession()

const settingsSections = [
  {key: 'general', path: '/settings/edit/general'},
  {key: 'profile', path: '/settings/edit/profile'},
  {key: 'ranks', path: '/settings/edit/ranks'},
  {key: 'reputation', path: '/settings/edit/reputation'},
  {key: 'channels', path: '/settings/edit/channels'},
  {key: 'roles', path: '/settings/edit/roles'},
  {key: 'reactions', path: '/settings/edit/reactions'},
  {key: 'thankwords', path: '/settings/edit/thankwords'},
  {key: 'abuseProtection', path: '/settings/edit/abuse-protection'},
  {key: 'announcements', path: '/settings/edit/announcements'},
  {key: 'autopost', path: '/settings/edit/autopost'},
  {key: 'logChannel', path: '/settings/edit/log-channel'},
  {key: 'integrationBypass', path: '/settings/edit/integration-bypass'}
]

</script>

<template>
  <div class="mx-auto" style="max-width: 1600px;">
    <div class="transition-colors">
      <div class="flex justify-center">
        <!-- Sidebar - Full height, stretches from top to bottom -->
        <aside v-if="userSession && $route.path.startsWith('/settings/edit') && !$route.path.endsWith('/audit-log') && !$route.path.endsWith('/problems')" class="w-64 shrink-0">
          <nav class="bg-white dark:bg-gray-800 shadow rounded-lg p-4 top-24 ml-4 transition-colors">
            <ul class="space-y-1">
              <li v-for="section in settingsSections" :key="section.key">
                <router-link
                    :to="section.path"
                    active-class="bg-indigo-100 dark:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 font-medium"
                    class="flex items-center justify-between px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 hover:text-indigo-600 dark:hover:text-indigo-400 rounded transition-colors"
                >
                  <span>{{ t(`settings.${section.key}`) }}</span>
                </router-link>
              </li>
            </ul>
          </nav>
        </aside>

        <!-- Content Area - Max width 1200px -->
        <div class="flex-1 px-4 pb-8 mx-auto" style="max-width: 1200px;">
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

<script setup lang="ts">
import {useI18n} from 'vue-i18n'

const {t} = useI18n()

const settingsSections = [
  {key: 'profile', path: '/settings/profile'},
  {key: 'general', path: '/settings/general'},
  {key: 'ranks', path: '/settings/ranks'},
  {key: 'reputation', path: '/settings/reputation'},
  {key: 'channels', path: '/settings/channels'},
  {key: 'roles', path: '/settings/roles'},
  {key: 'reactions', path: '/settings/reactions'},
  {key: 'thankwords', path: '/settings/thankwords'},
  {key: 'abuseProtection', path: '/settings/abuse-protection'},
  {key: 'announcements', path: '/settings/announcements'},
  {key: 'autopost', path: '/settings/autopost'},
  {key: 'logChannel', path: '/settings/log-channel'}
]
</script>

<template>
  <div class="mx-auto" style="max-width: 1600px;">
    <div>
      <!-- Heading above sidebar -->
      <h1 class="text-3xl font-bold mb-6 px-4 pt-8">{{ t('navigation.settings') }}</h1>

      <div class="flex min-h-screen">
        <!-- Sidebar - Full height, stretches from top to bottom -->
        <aside class="w-64 flex-shrink-0">
          <nav class="bg-white shadow rounded-lg p-4 top-24 ml-4 min-h-[calc(100vh-8rem)]">
            <ul class="space-y-1">
              <li v-for="section in settingsSections" :key="section.key">
                <router-link
                    :to="section.path"
                    class="block px-4 py-2 text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 rounded transition-colors"
                    active-class="bg-indigo-100 text-indigo-700 font-medium"
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

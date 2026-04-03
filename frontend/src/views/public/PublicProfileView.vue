/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import type {PublicProfilePOJO, GuildPOJO} from '@/api/types'

const route = useRoute()
const {t} = useI18n()

const guildId = route.params.guildId as string
const userId = route.params.userId as string

const profile = ref<PublicProfilePOJO | null>(null)
const guild = ref<GuildPOJO | null>(null)
const error = ref(false)
const privateProfile = ref(false)
onMounted(async () => {
  try {
    profile.value = await api.getPublicProfile(guildId, userId)
    guild.value = await api.getGuildMeta(guildId)
  } catch (e: any) {
    if (e?.response?.status === 401) {
      privateProfile.value = true
    } else {
      error.value = true
    }
  }
})
</script>

<template>
  <div class="min-h-auto bg-gray-100 dark:bg-gray-900 flex items-center justify-center p-4">
    <div class="w-full max-w-lg">
      <div v-if="privateProfile" class="bg-white dark:bg-gray-800 rounded-lg shadow p-8 text-center text-yellow-500">
        <font-awesome-icon icon="lock" class="text-3xl mb-3" />
        <div>{{ t('dashboard.publicProfileView.privateProfile') }}</div>
      </div>
      <div v-else-if="error" class="bg-white dark:bg-gray-800 rounded-lg shadow p-8 text-center text-red-500">
        {{ t('dashboard.publicProfileView.error') }}
      </div>

      <div v-else-if="profile && guild" class="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
        <!-- Guild header -->
        <div class="bg-indigo-600 dark:bg-indigo-700 px-6 py-4 flex items-center gap-3">
          <img v-if="guild.iconUrl" :src="guild.iconUrl" :alt="guild.name" class="w-8 h-8 rounded-full" />
          <span class="text-white font-semibold text-lg">{{ guild.name }}</span>
        </div>

        <!-- Member info -->
        <div class="px-6 py-3 flex items-center gap-4 border-b border-gray-200 dark:border-gray-700">
          <img
              :src="profile.member.profilePictureUrl"
              :alt="profile.member.displayName"
              class="w-16 h-16 rounded-full shrink-0"
          />
          <span
              class="text-2xl font-bold"
              :style="profile.member.color ? { color: profile.member.color } : {}"
          >{{ profile.member.displayName }}</span>
        </div>

        <!-- Stats -->
        <div class="grid grid-cols-2 divide-x divide-gray-200 dark:divide-gray-700">
          <div class="px-6 py-3 text-center">
            <div class="text-2xl font-bold text-indigo-600 dark:text-indigo-400">{{ profile.reputation }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ t('dashboard.publicProfileView.reputation') }}</div>
          </div>
          <div class="px-6 py-3 text-center">
            <div class="text-2xl font-bold text-purple-600 dark:text-purple-400">#{{ profile.rank }}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 mt-1">{{ t('dashboard.publicProfileView.rank') }}</div>
          </div>
        </div>
        <!-- Guild footer -->
        <div class="px-6 py-3 bg-gray-50 dark:bg-gray-700 text-center text-sm text-gray-500 dark:text-gray-400">
          {{ t('dashboard.publicProfileView.guildFooter', { guild: guild.name }) }}
        </div>
      </div>
      <div v-else class="flex justify-center py-16">
        <div class="w-8 h-8 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>

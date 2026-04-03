/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { onMounted, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import ViewContainer from '@/components/ViewContainer.vue'
import SettingsContainer from '../settings/components/SettingsContainer.vue'
import LoginPanel from '../settings/components/LoginPanel.vue'
import UserMailsSection from './components/UserMailsSection.vue'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { userSession } = useSession()

const voteGuild = ref<string>('0')
const publicProfile = ref<boolean>(false)
const loading = ref(true)

const guilds = computed(() => {
  if (!userSession.value) return []
  return Object.values(userSession.value.guilds).sort((a, b) => a.name.localeCompare(b.name))
})

onMounted(async () => {
  try {
    const settings = await api.getUserSettings()
    voteGuild.value = settings.voteGuild
    publicProfile.value = settings.publicProfile
  } catch (error) {
    console.error('Failed to fetch user settings:', error)
  } finally {
    loading.value = false
  }
})

const updateVoteGuild = async () => {
  try {
    await api.updateUserVoteGuild(voteGuild.value)
  } catch (error) {
    console.error('Failed to update vote guild:', error)
  }
}

const updatePublicProfile = async () => {
  try {
    await api.updateUserPublicProfile(publicProfile.value)
  } catch (error) {
    console.error('Failed to update public profile:', error)
  }
}
</script>

<template>
  <ViewContainer class="pt-8">
    <div v-if="!userSession" class="max-w-4xl mx-auto px-4">
      <LoginPanel />
    </div>
    <SettingsContainer v-else :description="t('user.settings.description')" :title="t('user.settings.title')">
      <div v-if="loading" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
      <div v-else class="p-4 space-y-10">
        <div class="flex flex-col gap-1.5">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-200">
            {{ t('user.settings.voteGuild.label') }}
          </label>
          <select
              v-model="voteGuild"
              class="block w-full rounded-lg border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm transition-colors"
              @change="updateVoteGuild"
          >
            <option value="0">{{ t('user.settings.voteGuild.none') }}</option>
            <option v-for="guild in guilds" :key="guild.id" :value="guild.id">
              {{ guild.name }}
            </option>
          </select>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            {{ t('user.settings.voteGuild.description') }}
          </p>
        </div>

        <div class="flex flex-col gap-1.5">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-200">
            {{ t('user.settings.publicProfile.label') }}
          </label>
          <Toggle v-model="publicProfile" @update:modelValue="updatePublicProfile" />
          <p class="text-sm text-gray-500 dark:text-gray-400">
            {{ t('user.settings.publicProfile.description') }}
          </p>
        </div>

        <!-- Mails Section extracted component -->
        <UserMailsSection />
      </div>
    </SettingsContainer>
  </ViewContainer>
</template>

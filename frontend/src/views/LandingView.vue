<!--
    SPDX-License-Identifier: AGPL-3.0-only

    Copyright (C) RainbowDashLabs and Contributor
-->
<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useSession } from '@/composables/useSession'
import { onMounted } from 'vue'
import { useLinks } from '@/composables/useLinks'
import VerticalFeatureReel from '@/components/landing/VerticalFeatureReel.vue'
import ReputationMethodReel from '@/components/landing/ReputationMethodReel.vue'
import SupporterFeatureReel from '@/components/landing/SupporterFeatureReel.vue'

const { t } = useI18n()
const router = useRouter()
const { userSession } = useSession()
const { links, loadLinks } = useLinks()

onMounted(loadLinks)

function goToDashboard() {
  router.push('/guild/dashboard')
}

function goToLogin() {
  router.push('/guild/dashboard')
}

const features = [
  { icon: ['fas', 'comments'], key: 'reputation' },
  { icon: ['fas', 'shield-halved'], key: 'protection' },
  { icon: ['fas', 'layer-group'], key: 'channels' },
  { icon: ['fas', 'user-gear'], key: 'profiles' },
  { icon: ['fas', 'trophy'], key: 'roles' },
  { icon: ['fas', 'list-ol'], key: 'thankwords' },
  { icon: ['fas', 'sliders'], key: 'customizable' },
  { icon: ['fas', 'hashtag'], key: 'channel_categories' },
  { icon: ['fas', 'arrow-up'], key: 'level_up' },
  { icon: ['fas', 'user-shield'], key: 'role_restrictions' },
  { icon: ['fas', 'magnifying-glass'], key: 'scan' },
  { icon: ['fas', 'gauge'], key: 'dashboard' },
  { icon: ['fas', 'lock'], key: 'privacy' },
  { icon: ['fab', 'github'], key: 'open_source' },
  { icon: ['fas', 'clipboard-list'], key: 'audit_log' },
]
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-indigo-50 to-white dark:from-gray-900 dark:to-gray-800">
    <!-- Hero Section -->
    <div class="container mx-auto px-4 py-20 max-w-5xl text-center">
      <div class="flex justify-center mb-6">
        <img src="/favicon.ico" alt="Reputation Bot Logo" class="w-20 h-20 rounded-2xl shadow-lg" />
      </div>
      <h1 class="text-5xl font-extrabold text-gray-900 dark:text-white mb-4">
        {{ t('landing.hero.title') }}
      </h1>
      <p class="text-xl text-gray-600 dark:text-gray-300 mb-10 max-w-2xl mx-auto">
        {{ t('landing.hero.subtitle') }}
      </p>
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          v-if="userSession"
          @click="goToDashboard"
          class="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg shadow transition-colors text-lg"
        >
          <font-awesome-icon :icon="['fas', 'chart-line']" class="mr-2" />
          {{ t('landing.hero.go_to_dashboard') }}
        </button>
        <button
          v-else
          @click="goToLogin"
          class="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-lg shadow transition-colors text-lg"
        >
          <font-awesome-icon :icon="['fab', 'discord']" class="mr-2" />
          {{ t('landing.hero.login') }}
        </button>
        <a
          :href="links.invite"
          target="_blank"
          rel="noopener noreferrer"
          class="inline-flex items-center justify-center px-8 py-3 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 text-indigo-600 dark:text-indigo-400 font-semibold rounded-lg shadow border border-indigo-200 dark:border-gray-600 transition-colors text-lg"
        >
          {{ t('landing.hero.add_to_server') }}
        </a>
      </div>
    </div>

    <!-- Features Section -->
    <div class="container mx-auto px-4 pb-20 max-w-2xl">
      <h2 class="text-3xl font-bold text-center text-gray-900 dark:text-white mb-4">
        {{ t('landing.features.title') }}
      </h2>
      <p class="text-center text-gray-600 dark:text-gray-400 mb-12 max-w-2xl mx-auto">
        {{ t('landing.features.subtitle') }}
      </p>
      <VerticalFeatureReel :features="features.map(f => ({ icon: f.icon, title: t(`landing.features.${f.key}.title`), description: t(`landing.features.${f.key}.description`) }))" />
    </div>

    <!-- Reputation Methods Section -->
    <div class="bg-white dark:bg-gray-900 py-20">
      <div class="container mx-auto px-4 max-w-3xl">
        <h2 class="text-3xl font-bold text-center text-gray-900 dark:text-white mb-4">
          {{ t('landing.reputation_methods.title') }}
        </h2>
        <p class="text-center text-gray-600 dark:text-gray-400 mb-12 max-w-2xl mx-auto">
          {{ t('landing.reputation_methods.subtitle') }}
        </p>
        <ReputationMethodReel />
      </div>
    </div>

    <!-- Supporter Features Section -->
    <div class="bg-gradient-to-b from-yellow-50 to-white dark:from-gray-900 dark:to-gray-800 py-20">
      <SupporterFeatureReel />
    </div>
  </div>
</template>

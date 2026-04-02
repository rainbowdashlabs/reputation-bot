/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLinks } from '@/composables/useLinks'
import FeatureCard from './FeatureCard.vue'

const { t } = useI18n()
const { links, loadLinks } = useLinks()
onMounted(loadLinks)

const supporterFeatures = [
  { icon: ['fas', 'palette'], key: 'branding' },
  { icon: ['fas', 'scroll'], key: 'log_channel' },
  { icon: ['fas', 'chart-bar'], key: 'autopost' },
  { icon: ['fas', 'hashtag'], key: 'unlimited_channels' },
  { icon: ['fas', 'chart-line'], key: 'advanced_rankings' },
  { icon: ['fas', 'face-smile'], key: 'additional_emojis' },
  { icon: ['fas', 'id-card'], key: 'detailed_profile' },
  { icon: ['fas', 'book-open'], key: 'full_rep_log' },
  { icon: ['fas', 'clock-rotate-left'], key: 'longer_analyzer_log' },
]
</script>

<template>
  <div class="container mx-auto px-4 max-w-5xl">
    <div class="flex items-center justify-center gap-3 mb-4">
      <font-awesome-icon :icon="['fas', 'star']" class="text-yellow-500 text-2xl" />
      <h2 class="text-3xl font-bold text-center text-gray-900 dark:text-white">
        {{ t('landing.supporter_features.title') }}
      </h2>
      <font-awesome-icon :icon="['fas', 'star']" class="text-yellow-500 text-2xl" />
    </div>
    <p class="text-center text-gray-600 dark:text-gray-400 mb-8 max-w-2xl mx-auto">
      {{ t('landing.supporter_features.subtitle') }}
    </p>
    <div class="flex flex-col sm:flex-row gap-4 justify-center mb-12">
      <a
        :href="links.kofi"
        target="_blank"
        rel="noopener noreferrer"
        class="inline-flex items-center justify-center gap-2 px-6 py-3 bg-yellow-400 hover:bg-yellow-500 text-gray-900 font-semibold rounded-lg shadow transition-colors"
      >
        <font-awesome-icon :icon="['fas', 'mug-hot']" />
        {{ t('landing.supporter_features.unlock_kofi') }}
      </a>
      <router-link
        to="/user/vote"
        class="inline-flex items-center justify-center gap-2 px-6 py-3 bg-white dark:bg-gray-700 hover:bg-gray-50 dark:hover:bg-gray-600 text-indigo-600 dark:text-indigo-400 font-semibold rounded-lg shadow border border-indigo-200 dark:border-gray-600 transition-colors"
      >
        <font-awesome-icon :icon="['fas', 'coins']" />
        {{ t('landing.supporter_features.unlock_vote') }}
      </router-link>
    </div>
    <p class="text-center text-xs text-gray-500 dark:text-gray-500 mb-10 max-w-xl mx-auto">
      {{ t('landing.supporter_features.per_server_note') }}
    </p>
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-8">
      <FeatureCard
        v-for="f in supporterFeatures"
        :key="f.key"
        :icon="f.icon"
        :title="t(`landing.supporter_features.${f.key}.title`)"
        :description="t(`landing.supporter_features.${f.key}.description`)"
      />
    </div>
  </div>
</template>

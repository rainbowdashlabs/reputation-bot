/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import type {DashboardStatsPOJO} from '@/api/types'
import ChannelDisplay from '@/components/display/ChannelDisplay.vue'
import StatsCard from "@/views/guild/components/StatsCard.vue";
import TopUsersTable from "@/views/guild/components/TopUsersTable.vue";
import Header2 from "@/components/heading/Header2.vue";

const {t} = useI18n()
const stats = ref<DashboardStatsPOJO | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    stats.value = await api.getDashboardStats()
  } catch (e) {
    console.error('Failed to load dashboard stats:', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="mx-auto space-y-8 py-8" style="max-width: 1200px;">
    <div class="flex items-center justify-between">
      <Header2 class="text-3xl font-bold text-gray-900 dark:text-white">{{ t('dashboard.title') }}</Header2>
    </div>

    <div v-if="loading" class="flex justify-center items-center h-64">
      <div class="text-xl text-gray-500 dark:text-gray-400">{{ t('common.loading') }}</div>
    </div>

    <div v-else-if="stats" class="space-y-8">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard :title="t('dashboard.totalReputation')" :value="stats.totalReputation"
                   border-color="border-blue-500"/>
        <StatsCard :title="t('dashboard.weekReputation')" :value="stats.weekReputation"
                   border-color="border-indigo-500"/>
        <StatsCard :title="t('dashboard.todayReputation')" :value="stats.todayReputation"
                   border-color="border-purple-500"/>

        <StatsCard :title="t('dashboard.topChannel')" border-color="border-pink-500">
          <span v-if="stats.topChannelId !== '0'">
              <ChannelDisplay :channel-id="stats.topChannelId"/>
            </span>
          <span v-else class="text-gray-400 italic">None</span>
        </StatsCard>
      </div>

      <!-- Top Users Table -->
      <TopUsersTable :title="t('dashboard.topUsers')" :entries="stats.topUsers"/>
    </div>
  </div>
</template>

<style scoped>
</style>

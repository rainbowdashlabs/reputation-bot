/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import {ReputationMode} from '@/api/types'
import type {RankingPagePOJO} from '@/api/types'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'
import TopUsersTable from '@/views/guild/components/TopUsersTable.vue'

const {t} = useI18n()
const router = useRouter()
const {session} = useSession()

const navigateToProfile = (memberId: string) => {
  router.push({name: 'GuildDashboardUserProfile', params: {userId: memberId}})
}

type RankingType = 'received' | 'given'

const rankingType = ref<RankingType>('received')
const selectedMode = ref<ReputationMode>(ReputationMode.TOTAL)
const pageSize = ref<number>(10)
const currentPage = ref<number>(0)
const rankingData = ref<RankingPagePOJO | null>(null)
const loading = ref(false)

const isAdvancedRankingsUnlocked = computed(() => session.value?.premiumFeatures?.advancedRankings?.unlocked ?? true)
const advancedRankingsRequiredSkus = computed(() => session.value?.premiumFeatures?.advancedRankings?.requiredSkus ?? [])
const isReputationLogUnlocked = computed(() => session.value?.premiumFeatures?.reputationLog?.unlocked ?? true)

const maxPageSize = computed(() => isReputationLogUnlocked.value ? 50 : 20)
const pageSizeOptions = computed(() => [5, 10, 20, 50].filter(s => s <= maxPageSize.value))

const reputationModes = Object.values(ReputationMode)

const fetchRanking = async () => {
  if (rankingType.value === 'given' && !isAdvancedRankingsUnlocked.value) {
    rankingData.value = null
    return
  }
  loading.value = true
  try {
    const size = Math.min(pageSize.value, maxPageSize.value)
    if (rankingType.value === 'received') {
      rankingData.value = await api.getGuildRankingReceived(currentPage.value, size, selectedMode.value)
    } else {
      rankingData.value = await api.getGuildRankingGiven(currentPage.value, size, selectedMode.value)
    }
  } catch (e) {
    console.error('Failed to load ranking:', e)
  } finally {
    loading.value = false
  }
}

const setRankingType = (type: RankingType) => {
  rankingType.value = type
  currentPage.value = 0
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
  }
}

const nextPage = () => {
  if (rankingData.value && currentPage.value < rankingData.value.pages - 1) {
    currentPage.value++
  }
}

watch([rankingType, selectedMode, pageSize, currentPage], fetchRanking)
watch(maxPageSize, (max) => {
  if (pageSize.value > max) {
    pageSize.value = max
  }
})

onMounted(fetchRanking)
</script>

<template>
  <div>
    <div class="space-y-6 py-8">
    <!-- Controls -->
    <div class="flex flex-wrap gap-4 items-center">
      <!-- Mode selector -->
      <div class="flex items-center gap-2">
        <label class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('dashboard.rankingView.mode') }}</label>
        <select
            v-model="selectedMode"
            class="text-sm border border-gray-300 dark:border-gray-600 rounded-md px-3 py-1.5 bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option v-for="mode in reputationModes" :key="mode" :value="mode">
            {{ t(`general.reputation.mode.modes.${mode}`) }}
          </option>
        </select>
      </div>

      <!-- Page size selector -->
      <div class="flex items-center gap-2">
        <label class="text-sm font-medium text-gray-700 dark:text-gray-300">{{ t('dashboard.rankingView.pageSize') }}</label>
        <select
            v-model.number="pageSize"
            class="text-sm border border-gray-300 dark:border-gray-600 rounded-md px-3 py-1.5 bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
        </select>
      </div>
    </div>

    <!-- Tab switcher header (always visible) -->
    <div class="flex items-center justify-between mb-2">
      <div class="inline-flex rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700">
        <div
            :class="rankingType === 'received'
              ? 'text-indigo-600 dark:text-indigo-400 font-semibold'
              : 'text-gray-500 dark:text-gray-400 cursor-pointer hover:text-gray-700 dark:hover:text-gray-200'"
            class="px-4 py-1.5 text-sm transition-colors"
            @click="setRankingType('received')"
        >
          {{ t('dashboard.rankingView.received') }}
        </div>
        <div
            :class="rankingType === 'given'
              ? 'text-indigo-600 dark:text-indigo-400 font-semibold'
              : 'text-gray-500 dark:text-gray-400 cursor-pointer hover:text-gray-700 dark:hover:text-gray-200'"
            class="px-4 py-1.5 text-sm transition-colors border-l border-gray-200 dark:border-gray-700"
            @click="setRankingType('given')"
        >
          {{ t('dashboard.rankingView.given') }}
        </div>
      </div>
    </div>

    <!-- Advanced Rankings Warning -->
    <PremiumFeatureWarning
        v-if="!isAdvancedRankingsUnlocked && rankingType === 'given'"
        :feature-name="t('dashboard.rankingView.advancedRankings')"
        :required-skus="advancedRankingsRequiredSkus"
        variant="large"
    />

    <!-- Loading -->
    <div v-else-if="loading" class="flex justify-center items-center h-40">
      <div class="text-xl text-gray-500 dark:text-gray-400">{{ t('common.loading') }}</div>
    </div>

    <!-- Ranking Table -->
    <template v-else-if="rankingData">
      <TopUsersTable
          :title="rankingType === 'received' ? t('dashboard.rankingView.received') : t('dashboard.rankingView.given')"
          :entries="rankingData.entries"
          @click-member="navigateToProfile"
      />

      <!-- Pagination -->
      <div v-if="rankingData.pages > 1" class="flex items-center justify-between">
        <button
            :disabled="currentPage === 0"
            class="px-4 py-2 text-sm font-medium rounded-md border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            @click="prevPage"
        >
          {{ t('common.previous') }}
        </button>
        <span class="text-sm text-gray-500 dark:text-gray-400">
          {{ t('common.pageOf', {current: currentPage + 1, total: rankingData.pages}) }}
        </span>
        <button
            :disabled="currentPage >= rankingData.pages - 1"
            class="px-4 py-2 text-sm font-medium rounded-md border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            @click="nextPage"
        >
          {{ t('common.next') }}
        </button>
      </div>
    </template>
  </div>
  </div>
</template>

<style scoped>
</style>

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
import type {RankingEntryStatPOJO, RankingPagePOJO, UserProfilePOJO} from '@/api/types'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'
import RankingList from '@/views/guild/components/RankingList.vue'
import ChannelList from '@/views/guild/components/ChannelList.vue'

const props = defineProps<{
  userId?: string
}>()

const {t} = useI18n()
const router = useRouter()

const navigateToProfile = (memberId: string) => {
  router.push({name: 'GuildDashboardUserProfile', params: {userId: memberId}})
}
const {session, currentGuildId} = useSession()
const copied = ref(false)
const shareProfile = () => {
  const guildId = currentGuildId.value
  const memberId = profile.value?.member?.id
  if (!guildId || !memberId) return
  const url = `${window.location.origin}/public/profile/${guildId}/${memberId}`
  navigator.clipboard.writeText(url)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

const profile = ref<UserProfilePOJO | null>(null)
const loading = ref(false)
const error = ref(false)

const isAdvancedRankingsUnlocked = computed(() => session.value?.premiumFeatures?.advancedRankings?.unlocked ?? true)
const isDetailedProfileUnlocked = computed(() => session.value?.premiumFeatures?.detailedProfile?.unlocked ?? true)
const detailedProfileRequiredSkus = computed(() => session.value?.premiumFeatures?.detailedProfile?.requiredSkus ?? [])

const PAGE_SIZE = 5
const donorsPage = ref(0)
const receiversPage = ref(0)
const donorsData = ref<RankingPagePOJO | null>(null)
const receiversData = ref<RankingPagePOJO | null>(null)

const donorEntries = computed<RankingEntryStatPOJO[]>(() => {
  if (isAdvancedRankingsUnlocked.value && donorsData.value) return donorsData.value.entries as unknown as RankingEntryStatPOJO[]
  return profile.value?.detailedProfile?.topDonors ?? []
})
const receiverEntries = computed<RankingEntryStatPOJO[]>(() => {
  if (isAdvancedRankingsUnlocked.value && receiversData.value) return receiversData.value.entries as unknown as RankingEntryStatPOJO[]
  return profile.value?.detailedProfile?.topReceivers ?? []
})

const fetchDonors = async () => {
  if (!isAdvancedRankingsUnlocked.value || !profile.value) return
  const targetId = props.userId ?? profile.value.member.id
  donorsData.value = await api.getUserRankingGiven(donorsPage.value, PAGE_SIZE, undefined, targetId)
}
const fetchReceivers = async () => {
  if (!isAdvancedRankingsUnlocked.value || !profile.value) return
  const targetId = props.userId ?? profile.value.member.id
  receiversData.value = await api.getUserRankingReceived(receiversPage.value, PAGE_SIZE, undefined, targetId)
}

const rankRole = computed(() => {
  if (!profile.value?.level) return null
  return session.value?.roles?.find(r => r.id === String(profile.value!.level)) ?? null
})

const memberColor = computed(() => {
  const color = profile.value?.member?.color
  if (!color || color === '#ffffff' || color === '#FFFFFF') return undefined
  return color
})

const rankRoleColor = computed(() => {
  const role = rankRole.value
  if (!role) return undefined
  const color = role.color
  if (!color || color === '#ffffff' || color === '#FFFFFF') return undefined
  return color
})

const allChannels = computed(() => {
  const view = session.value?.channels
  if (!view) return []
  const flat = [...(view.channels ?? [])]
  for (const cat of view.categories ?? []) {
    flat.push(...(cat.channels ?? []))
  }
  return flat
})

const channelName = (channelId: string) => {
  return allChannels.value.find(c => c.id === channelId)?.name ?? `${channelId}`
}

const fetchProfile = async () => {
  loading.value = true
  error.value = false
  try {
    if (props.userId) {
      profile.value = await api.getUserProfile(props.userId)
    } else {
      profile.value = await api.getUserProfileMe()
    }
  } catch (e) {
    console.error('Failed to load profile:', e)
    error.value = true
  } finally {
    loading.value = false
  }
}

const progressPercent = computed(() => {
  if (!profile.value || profile.value.nextLevelReputation === null) return 100
  const next = profile.value.nextLevelReputation
  if (next === 0) return 100
  return Math.min(100, Math.round((profile.value.currentProgress / next) * 100))
})

onMounted(async () => {
  await fetchProfile()
  await Promise.all([fetchDonors(), fetchReceivers()])
})
watch(() => props.userId, async () => {
  donorsPage.value = 0
  receiversPage.value = 0
  donorsData.value = null
  receiversData.value = null
  await fetchProfile()
  await Promise.all([fetchDonors(), fetchReceivers()])
})
watch(donorsPage, fetchDonors)
watch(receiversPage, fetchReceivers)
</script>

<template>
  <div class="space-y-6">
    <!-- Loading -->
    <div v-if="loading" class="flex justify-center items-center h-40">
      <div class="text-xl text-gray-500 dark:text-gray-400">{{ t('common.loading') }}</div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="text-center text-red-500 dark:text-red-400 py-10">
      {{ t('dashboard.profileView.error') }}
    </div>

    <!-- Profile -->
    <template v-else-if="profile">
      <!-- Header card -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6 flex flex-col sm:flex-row items-center sm:items-start gap-6">
        <img
            :src="profile.member.profilePictureUrl"
            :alt="profile.member.displayName"
            class="w-20 h-20 rounded-full border-2 border-indigo-400"
        />
        <div class="flex-1 space-y-2 text-center sm:text-left">
          <div class="flex items-center justify-between gap-2">
            <span class="font-medium" :style="{ color: memberColor }">{{ profile.member.displayName }}</span>
            <button
                @click="shareProfile"
                class="ml-auto text-xs px-2 py-1 rounded bg-indigo-100 dark:bg-indigo-900 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-200 dark:hover:bg-indigo-800 transition"
                :title="t('dashboard.profileView.shareProfile')"
            >
              <font-awesome-icon :icon="copied ? 'check' : 'share-nodes'" />
              {{ copied ? t('dashboard.profileView.shareProfileCopied') : t('dashboard.profileView.shareProfile') }}
            </button>
          </div>
          <div v-if="rankRole" class="text-sm font-medium" :style="{ color: rankRoleColor }">
            {{ t('dashboard.profileView.rank', { rank: rankRole.name }) }}
          </div>

          <!-- Stats row -->
          <div class="flex flex-wrap gap-6 mt-2 justify-center sm:justify-start">
            <div class="text-center">
              <div class="text-2xl font-bold text-indigo-600 dark:text-indigo-400">{{ profile.reputation }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('dashboard.profileView.received') }}</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-indigo-600 dark:text-indigo-400">#{{ profile.rank }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('dashboard.profileView.rankReceived') }}</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-purple-600 dark:text-purple-400">{{ profile.donated }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('dashboard.profileView.given') }}</div>
            </div>
            <div class="text-center">
              <div class="text-2xl font-bold text-purple-600 dark:text-purple-400">#{{ profile.rankDonated }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('dashboard.profileView.rankGiven') }}</div>
            </div>
            <template v-if="profile.adminProfile">
              <div class="text-center">
                <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ profile.adminProfile.rawReputation }}</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('dashboard.profileView.rawReputation') }}</div>
              </div>
              <div class="text-center">
                <div class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ profile.adminProfile.repOffset }}</div>
                <div class="text-xs text-gray-500 dark:text-gray-400">{{ t('dashboard.profileView.repOffset') }}</div>
              </div>
            </template>
          </div>

          <!-- Progress bar -->
          <div v-if="profile.level !== null" class="mt-3">
            <div class="flex justify-between text-xs text-gray-500 dark:text-gray-400 mb-1">
              <span>{{ t('dashboard.profileView.progress') }}</span>
              <span v-if="profile.nextLevelReputation !== null">
                {{ profile.currentProgress }} / {{ profile.nextLevelReputation }}
              </span>
              <span v-else>{{ t('dashboard.profileView.maxLevel') }}</span>
            </div>
            <div class="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
              <div
                  class="bg-indigo-500 h-2 rounded-full transition-all"
                  :style="{ width: progressPercent + '%' }"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Detailed Profile: rankings + channels -->
      <template v-if="profile.detailedProfile">
        <!-- Top Donors + Top Receivers -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <RankingList
              :title="t('dashboard.profileView.topDonors')"
              :entries="donorEntries"
              accent-color="indigo"
              :page="donorsPage"
              :total-pages="donorsData?.pages"
              @prev="donorsPage--"
              @next="donorsPage++"
              @click-member="navigateToProfile"
          />
          <RankingList
              :title="t('dashboard.profileView.topReceivers')"
              :entries="receiverEntries"
              accent-color="purple"
              :page="receiversPage"
              :total-pages="receiversData?.pages"
              @prev="receiversPage--"
              @next="receiversPage++"
              @click-member="navigateToProfile"
          />
        </div>

        <!-- Most Given + Most Received Channels -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <ChannelList
              :title="t('dashboard.profileView.mostGivenChannels')"
              :entries="profile.detailedProfile.mostGivenChannels"
              accent-color="purple"
              :channel-name="channelName"
          />
          <ChannelList
              :title="t('dashboard.profileView.mostReceivedChannels')"
              :entries="profile.detailedProfile.mostReceivedChannels"
              accent-color="indigo"
              :channel-name="channelName"
          />
        </div>
      </template>

      <!-- Detailed Profile locked -->
      <PremiumFeatureWarning
          v-else-if="!isDetailedProfileUnlocked"
          :feature-name="t('dashboard.profileView.detailedProfile')"
          :required-skus="detailedProfileRequiredSkus"
          variant="small"
      />
    </template>
  </div>
</template>

<style scoped>
</style>

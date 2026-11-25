/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import type {SupporterIssuePOJO} from '@/api/types'

const {t, locale} = useI18n()
const {premiumFeatures} = useSession()

const status = computed(() => premiumFeatures.value?.supporterStatus)
const issues = computed(() => status.value?.issues ?? [])
const blockingIssues = computed(() => issues.value.filter(issue => issue.blocking))
const pausedIssues = computed(() => issues.value.filter(issue => !issue.blocking))

const headline = computed(() => {
  if (status.value?.blocked) return t('supporterStatus.blocked')
  if (status.value?.blocksAt) return t('supporterStatus.blocksAt', {date: new Date(status.value.blocksAt).toLocaleString(locale.value)})
  if (blockingIssues.value.length > 0) return t('supporterStatus.blocksSoon')
  return t('supporterStatus.paused')
})

const groups = computed(() => [
  {key: 'blocking', intro: '', issues: blockingIssues.value},
  {key: 'paused', intro: blockingIssues.value.length > 0 ? t('supporterStatus.paused') : '', issues: pausedIssues.value}
].filter(group => group.issues.length > 0))

const sectionKey = (path: string) =>
  (path.split('/').pop() ?? '').replace(/-([a-z])/g, (_, letter: string) => letter.toUpperCase())

const sectionName = (issue: SupporterIssuePOJO) => issue.path ? t(`settings.${sectionKey(issue.path)}`) : ''
</script>

<template>
  <div v-if="issues.length > 0"
       :class="status?.blocked
         ? 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-900/40'
         : 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-900/40'"
       class="border rounded-lg p-4">
    <div class="flex items-start gap-3">
      <font-awesome-icon :class="status?.blocked ? 'text-red-600 dark:text-red-400' : 'text-yellow-600 dark:text-yellow-400'"
                         :icon="['fas', 'circle-exclamation']"
                         class="mt-1 shrink-0"/>
      <div class="space-y-3 text-sm text-gray-800 dark:text-gray-200">
        <p class="font-semibold">{{ headline }}</p>
        <template v-for="group in groups" :key="group.key">
          <p v-if="group.intro">{{ group.intro }}</p>
          <ul class="list-disc pl-5 space-y-1">
            <li v-for="issue in group.issues" :key="issue.feature">
              {{ t(`supporterStatus.feature.${issue.feature}`) }}
              <router-link v-if="issue.path" :to="`/${issue.path}`" class="underline font-medium">
                {{ t('supporterStatus.open', {section: sectionName(issue)}) }}
              </router-link>
            </li>
          </ul>
        </template>
        <router-link class="inline-block underline" to="/user/supporter">{{ t('supporterStatus.support') }}</router-link>
      </div>
    </div>
  </div>
</template>

/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import Header2 from "@/components/heading/Header2.vue"
import RankItem from './RankItem.vue'
import type {RankEntry} from '@/api/types'

const props = defineProps<{
  ranks: RankEntry[]
}>()

const emit = defineEmits<{
  (e: 'update', ranks: RankEntry[]): void
  (e: 'delete', roleId: string): void
}>()

const {t} = useI18n()

const sortedRanks = computed(() => {
  return [...props.ranks].sort((a, b) => b.reputation - a.reputation)
})

const onUpdateRank = (updatedRank: RankEntry, index: number) => {
  const originalRank = sortedRanks.value[index]
  if (!originalRank) return

  const updatedRanks = [...props.ranks]
  const originalIndex = updatedRanks.findIndex(r => r.roleId.toString() === originalRank.roleId.toString())

  if (originalIndex !== -1) {
    updatedRanks[originalIndex] = updatedRank
    emit('update', updatedRanks)
  }
}

const onDeleteRank = (roleId: string) => {
  emit('delete', roleId)
}
</script>

<template>
  <div class="space-y-4">
    <Header2>{{ t('settings.ranks') }}</Header2>
    <div v-if="sortedRanks.length > 0" class="divide-y divide-gray-200 dark:divide-gray-700">
      <RankItem
          v-for="(rank, index) in sortedRanks"
          :key="rank.roleId"
          :other-ranks="ranks"
          :rank="rank"
          @delete="() => onDeleteRank(rank.roleId.toString())"
          @update="(updatedRank) => onUpdateRank(updatedRank, index)"
      />
    </div>
    <p v-else class="text-gray-500 dark:text-gray-400 italic">
      {{ t('general.ranks.noRanks') }}
    </p>
  </div>
</template>

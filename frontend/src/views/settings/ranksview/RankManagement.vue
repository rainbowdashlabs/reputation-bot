<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import type { RankEntry } from '@/api/types'
import AddRankForm from './AddRankForm.vue'
import RankList from './RankList.vue'

const { session } = useSession()

const ranks = ref<RankEntry[]>([])

watch(session, (newSession) => {
  if (newSession?.settings?.ranks) {
    ranks.value = JSON.parse(JSON.stringify(newSession.settings.ranks.ranks))
  }
}, { immediate: true })

const saveRanks = async () => {
  try {
    await api.updateRanks({ ranks: ranks.value })
  } catch (error) {
    console.error('Failed to update ranks:', error)
  }
}

const onAddRank = async (newRank: RankEntry) => {
  ranks.value.push(newRank)
  await saveRanks()
}

const onUpdateRanks = async (updatedRanks: RankEntry[]) => {
  ranks.value = updatedRanks
  await saveRanks()
}

const onDeleteRank = async (roleId: string) => {
  ranks.value = ranks.value.filter(r => r.roleId !== roleId)
  await saveRanks()
}
</script>

<template>
  <div class="space-y-6">
    <AddRankForm :existing-ranks="ranks" @add="onAddRank" />
    <RankList :ranks="ranks" @update="onUpdateRanks" @delete="onDeleteRank" />
  </div>
</template>

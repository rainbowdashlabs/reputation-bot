/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import RoleDisplay from '@/components/display/RoleDisplay.vue'

interface Rank {
  roleId: string
  reputation: number
}

interface Props {
  ranks?: Rank[]
  oldRanks?: Rank[]
  newRanks?: Rank[]
}

const props = defineProps<Props>()
const {t} = useI18n()

// Helper function to get roleId from rank object
const getRoleId = (item: Rank): string => {
  return item.roleId
}

// Calculate added and removed items for list changes
const addedItems = computed(() => {
  if (!props.oldRanks || !props.newRanks) return []
  const oldRoleIds = new Set(props.oldRanks.map(getRoleId))
  return props.newRanks.filter((item: Rank) => !oldRoleIds.has(getRoleId(item)))
})

const removedItems = computed(() => {
  if (!props.oldRanks || !props.newRanks) return []
  const newRoleIds = new Set(props.newRanks.map(getRoleId))
  return props.oldRanks.filter((item: Rank) => !newRoleIds.has(getRoleId(item)))
})

// Calculate changed items for ranks (same roleId, different reputation)
const changedItems = computed(() => {
  if (!props.oldRanks || !props.newRanks) return []

  const changes: Array<{roleId: string, oldReputation: number, newReputation: number}> = []
  const newRanksMap = new Map(props.newRanks.map((rank: Rank) => [rank.roleId, rank.reputation]))

  for (const oldRank of props.oldRanks) {
    const newReputation = newRanksMap.get(oldRank.roleId)
    if (newReputation !== undefined && newReputation !== null && newReputation !== oldRank.reputation) {
      changes.push({
        roleId: oldRank.roleId,
        oldReputation: oldRank.reputation,
        newReputation: newReputation as number
      })
    }
  }

  return changes
})

const isComparison = computed(() => {
  return props.oldRanks !== undefined && props.newRanks !== undefined
})

const effectiveRanks = computed(() => {
  if (props.ranks !== undefined) return props.ranks
  return props.newRanks !== undefined ? props.newRanks : props.oldRanks || []
})
</script>

<template>
  <template v-if="isComparison">
    <div class="flex flex-col gap-2">
      <!-- Added -->
      <div v-if="addedItems.length > 0" class="flex items-center gap-2">
        <span class="text-green-600 dark:text-green-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'plus']" class="h-5 w-5" />
        </span>
        <RanksListValue :ranks="addedItems" />
      </div>

      <!-- Changed -->
      <div v-if="changedItems.length > 0" class="flex items-center gap-2">
        <span class="text-blue-600 dark:text-blue-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'pen']" class="h-5 w-5" />
        </span>
        <div class="flex flex-col gap-2">
          <div
              v-for="change in changedItems"
              :key="change.roleId"
              class="inline-flex items-center gap-2"
          >
            <RoleDisplay :role-id="change.roleId" />
            <span class="text-gray-600 dark:text-gray-400">→</span>
            <span class="font-medium text-gray-900 dark:text-gray-100">{{ change.oldReputation }}</span>
            <span class="text-gray-400 dark:text-gray-500">→</span>
            <span class="font-medium text-gray-900 dark:text-gray-100">{{ change.newReputation }}</span>
          </div>
        </div>
      </div>

      <!-- Removed -->
      <div v-if="removedItems.length > 0" class="flex items-center gap-2">
        <span class="text-red-600 dark:text-red-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'minus']" class="h-5 w-5" />
        </span>
        <RanksListValue :ranks="removedItems" />
      </div>
    </div>
  </template>

  <template v-else>
    <template v-if="effectiveRanks.length === 0">
      <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
    </template>
    <template v-else>
      <div class="flex flex-col gap-2">
        <div
            v-for="rank in effectiveRanks"
            :key="rank.roleId"
            class="inline-flex items-center gap-2"
        >
          <RoleDisplay :role-id="rank.roleId" />
          <span class="text-gray-600 dark:text-gray-400">→</span>
          <span class="font-medium text-gray-900 dark:text-gray-100">{{ rank.reputation }}</span>
        </div>
      </div>
    </template>
  </template>
</template>

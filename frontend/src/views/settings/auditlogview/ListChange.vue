/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import AuditLogValue from './AuditLogValue.vue'
import RoleDisplay from './RoleDisplay.vue'

interface Props {
  settingsKey: string
  oldValue: any[]
  newValue: any[]
}

const props = defineProps<Props>()

// Check if this is a ranks list
const isRanksList = computed(() => {
  return props.settingsKey.toLowerCase() === 'ranks' &&
         props.oldValue.length > 0 &&
         typeof props.oldValue[0] === 'object' &&
         'roleId' in props.oldValue[0]
})

// Helper function to create a comparable key for items (handles objects and primitives)
const getItemKey = (item: any): string => {
  if (typeof item === 'object' && item !== null) {
    return JSON.stringify(item)
  }
  return String(item)
}

// Helper function to get roleId from rank object
const getRoleId = (item: any): string => {
  return item.roleId
}

// Calculate added and removed items for list changes
const addedItems = computed(() => {
  if (isRanksList.value) {
    // For ranks, only consider truly new roleIds as additions
    const oldRoleIds = new Set(props.oldValue.map(getRoleId))
    return props.newValue.filter((item: any) => !oldRoleIds.has(getRoleId(item)))
  }

  const oldKeys = new Set(props.oldValue.map(getItemKey))
  return props.newValue.filter((item: any) => !oldKeys.has(getItemKey(item)))
})

const removedItems = computed(() => {
  if (isRanksList.value) {
    // For ranks, only consider truly removed roleIds as removals
    const newRoleIds = new Set(props.newValue.map(getRoleId))
    return props.oldValue.filter((item: any) => !newRoleIds.has(getRoleId(item)))
  }

  const newKeys = new Set(props.newValue.map(getItemKey))
  return props.oldValue.filter((item: any) => !newKeys.has(getItemKey(item)))
})

// Calculate changed items for ranks (same roleId, different reputation)
const changedItems = computed(() => {
  if (!isRanksList.value) return []

  const changes: Array<{roleId: string, oldReputation: number, newReputation: number}> = []
  const newRanksMap = new Map(props.newValue.map((rank: any) => [rank.roleId, rank.reputation]))

  for (const oldRank of props.oldValue) {
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

const hasChanges = computed(() => {
  if (isRanksList.value) {
    return addedItems.value.length > 0 || removedItems.value.length > 0 || changedItems.value.length > 0
  }
  return addedItems.value.length > 0 || removedItems.value.length > 0
})
</script>

<template>
  <div v-if="hasChanges" class="space-y-2">
    <div v-if="addedItems.length > 0" class="flex items-center gap-2">
      <span class="text-green-600 dark:text-green-400 shrink-0">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z" clip-rule="evenodd" />
        </svg>
      </span>
      <AuditLogValue :settings-key="settingsKey" :value="addedItems" />
    </div>
    <div v-if="changedItems.length > 0" class="flex items-center gap-2">
      <span class="text-blue-600 dark:text-blue-400 shrink-0">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
          <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
        </svg>
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
    <div v-if="removedItems.length > 0" class="flex items-center gap-2">
      <span class="text-red-600 dark:text-red-400 shrink-0">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M5 10a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1z" clip-rule="evenodd" />
        </svg>
      </span>
      <AuditLogValue :settings-key="settingsKey" :value="removedItems" />
    </div>
  </div>
</template>

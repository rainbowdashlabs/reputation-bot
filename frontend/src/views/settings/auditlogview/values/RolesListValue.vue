/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import RoleDisplay from '@/components/display/RoleDisplay.vue'

interface Props {
  roleIds?: string[]
  oldRoleIds?: string[]
  newRoleIds?: string[]
}

const props = defineProps<Props>()
const {t} = useI18n()

const addedItems = computed(() => {
  if (!props.oldRoleIds || !props.newRoleIds) return []
  const oldSet = new Set(props.oldRoleIds)
  return props.newRoleIds.filter(id => !oldSet.has(id))
})

const removedItems = computed(() => {
  if (!props.oldRoleIds || !props.newRoleIds) return []
  const newSet = new Set(props.newRoleIds)
  return props.oldRoleIds.filter(id => !newSet.has(id))
})

const isComparison = computed(() => {
  return props.oldRoleIds !== undefined && props.newRoleIds !== undefined
})

const effectiveRoleIds = computed(() => {
  if (props.roleIds !== undefined) return props.roleIds
  return props.newRoleIds !== undefined ? props.newRoleIds : props.oldRoleIds || []
})
</script>

<template>
  <template v-if="isComparison">
    <div class="flex flex-col">
      <div v-if="addedItems.length > 0" class="flex items-center gap-2">
        <span class="text-green-600 dark:text-green-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'plus']" class="h-5 w-5" />
        </span>
        <RolesListValue :role-ids="addedItems" />
      </div>
      <div v-if="removedItems.length > 0" class="flex items-center gap-2">
        <span class="text-red-600 dark:text-red-400 shrink-0">
          <font-awesome-icon :icon="['fas', 'minus']" class="h-5 w-5" />
        </span>
        <RolesListValue :role-ids="removedItems" />
      </div>
    </div>
  </template>
  <template v-else>
    <template v-if="effectiveRoleIds.length === 0">
      <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
    </template>
    <template v-else>
      <div class="flex flex-col gap-2">
        <RoleDisplay
            v-for="roleId in effectiveRoleIds"
            :key="roleId"
            :role-id="roleId"
        />
      </div>
    </template>
  </template>
</template>

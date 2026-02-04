/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import type {RolePOJO} from '@/api/types'

const props = defineProps<{
  selectedRoleIds: readonly string[]
  availableRoles: readonly RolePOJO[]
}>()

const emit = defineEmits<{
  (e: 'update:selectedRoleIds', value: string[]): void
}>()

const {t} = useI18n()

const isRoleSelected = (roleId: string) => {
  return props.selectedRoleIds.some(selectedId => selectedId === roleId)
}

const toggleRole = (roleId: string) => {
  const id = String(roleId)
  let newSelected = props.selectedRoleIds.slice()
  if (newSelected.includes(id)) {
    newSelected = newSelected.filter(r => r !== id)
  } else {
    newSelected.push(id)
  }
  emit('update:selectedRoleIds', newSelected)
}

const displayedRoles = computed(() => {
  return props.availableRoles
})

const getRoleColor = (color: string) => {
  const colorStr = String(color)
  if (!color || colorStr === '#000000' || colorStr.toLowerCase() === '#ffffff' || colorStr === '0') return 'currentColor'
  return colorStr
}
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-col gap-1">
      <label class="block text-sm font-medium text-gray-900 dark:text-gray-100">{{
          t('general.roles.list.label')
        }}</label>
      <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ t('general.roles.list.description') }}</p>
    </div>

    <div
        class="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden divide-y divide-gray-200 dark:divide-gray-700 max-h-96 overflow-y-auto">
      <div
          v-for="role in displayedRoles"
          :key="role.id"
          class="flex items-center justify-between p-3 hover:bg-gray-50 dark:hover:bg-gray-800 cursor-pointer transition-colors bg-white dark:bg-gray-900"
          @click="toggleRole(role.id)"
      >
        <div class="flex items-center gap-3">
          <input
              :checked="isRoleSelected(role.id)"
              class="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
              type="checkbox"
              @click.stop="toggleRole(role.id)"
          />
          <span
              :style="{ color: getRoleColor(role.color) }"
              class="font-medium"
          >
            {{ role.name }}
          </span>
        </div>
      </div>
      <div v-if="displayedRoles.length === 0" class="p-4 text-center text-gray-500">
        {{ t('general.roleSelect.noRolesFound') }}
      </div>
    </div>
  </div>
</template>

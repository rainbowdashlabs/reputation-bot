/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import BaseButton from '@/components/BaseButton.vue'
import type { RankEntry } from '@/api/types'

const props = defineProps<{
  rank: RankEntry
  otherRanks: RankEntry[]
}>()

const emit = defineEmits<{
  (e: 'update', rank: RankEntry): void
  (e: 'delete'): void
}>()

const { t } = useI18n()
const { session } = useSession()

const isEditing = ref(false)
const editReputation = ref<number | null>(null)
const errorMessage = ref('')

const getRole = (roleId: string) => {
  return session.value?.guild?.roles.find(r => r.id === roleId)
}

const getRoleColor = (roleId: string) => {
  const role = getRole(roleId)
  if (!role || !role.color || role.color === '#000000' || role.color === '#ffffff' || role.color === '0') return 'inherit'
  if (typeof role.color === 'number') {
    if (role.color === 0xFFFFFF) return 'inherit'
    return '#' + (role.color as number).toString(16).padStart(6, '0')
  }
  const colorStr = role.color.startsWith('#') ? role.color : `#${role.color}`
  return colorStr.toLowerCase() === '#ffffff' ? 'inherit' : colorStr
}

const validate = () => {
  if (!isEditing.value) {
    errorMessage.value = ''
    return
  }

  if (editReputation.value !== null && !isNaN(editReputation.value)) {
    // Check if the reputation value is already used by another rank (excluding the current one)
    if (editReputation.value !== props.rank.reputation && props.otherRanks.some(r => r.reputation === editReputation.value)) {
      errorMessage.value = t('general.ranks.reputationAlreadyUsed')
      return
    }
  }

  errorMessage.value = ''
}

watch(editReputation, validate)

const startEdit = () => {
  editReputation.value = props.rank.reputation
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  errorMessage.value = ''
}

const saveEdit = () => {
  if (editReputation.value === null || editReputation.value < 0 || !!errorMessage.value) return
  
  emit('update', {
    roleId: props.rank.roleId,
    reputation: editReputation.value
  })
  isEditing.value = false
}
</script>

<template>
  <div class="py-4 first:pt-0 last:pb-0">
    <div v-if="isEditing" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div class="flex items-center gap-3 min-h-9.5">
        <div
            class="w-3 h-3 rounded-full"
            :style="{ backgroundColor: getRoleColor(rank.roleId) }"
        ></div>
        <span class="font-medium" :style="{ color: getRoleColor(rank.roleId) }">
          {{ getRole(rank.roleId)?.name || rank.roleId }}
        </span>
      </div>
      <div class="flex gap-2 items-start">
        <div class="flex flex-col gap-2">
          <input
              v-model.number="editReputation"
              type="number"
              class="input"
              min="0"
              @keyup.enter="saveEdit"
          />
        </div>
        <BaseButton color="primary" :disabled="editReputation === null || !!errorMessage" @click="saveEdit">
          {{ t('common.save') }}
        </BaseButton>
        <BaseButton color="secondary" @click="cancelEdit">
          {{ t('common.cancel') }}
        </BaseButton>
      </div>
      <div v-if="errorMessage" class="text-red-500 text-sm md:col-span-2">
        {{ errorMessage }}
      </div>
    </div>
    <div v-else class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div
            class="w-3 h-3 rounded-full"
            :style="{ backgroundColor: getRoleColor(rank.roleId) }"
        ></div>
        <span class="font-medium" :style="{ color: getRoleColor(rank.roleId) }">
          {{ getRole(rank.roleId)?.name || rank.roleId }}
        </span>
        <span class="text-gray-500 dark:text-gray-400">
          ({{ rank.reputation }} {{ t('settings.reputation') }})
        </span>
      </div>
      <div class="flex gap-2">
        <BaseButton color="secondary" @click="startEdit">
          {{ t('common.edit') }}
        </BaseButton>
        <BaseButton color="danger" @click="emit('delete')">
          {{ t('common.delete') }}
        </BaseButton>
      </div>
    </div>
  </div>
</template>

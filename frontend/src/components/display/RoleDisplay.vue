/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useSession} from '@/composables/useSession.ts'

interface Props {
  roleId: string
}

const props = defineProps<Props>()
const {session} = useSession()

const role = computed(() => {
  return session.value?.guild?.roles?.find(r => r.id === props.roleId)
})

const roleColor = computed(() => {
  if (!role.value?.color) return 'inherit'
  const color = role.value.color

  // Handle white color - use default text color
  if (color === '#ffffff' || color === '#FFFFFF' || color === 'ffffff' || color === 'FFFFFF') {
    return 'inherit'
  }
  if (typeof color === 'number' && color === 0xFFFFFF) {
    return 'inherit'
  }

  return color
})
</script>

<template>
  <span
      v-if="role"
      :style="{ color: roleColor }"
      class="inline-flex items-center px-2 py-1 rounded text-sm font-medium"
  >
    @{{ role.name }}
  </span>
  <span v-else class="text-gray-500 italic">Unknown Role</span>
</template>

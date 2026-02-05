/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import type {MemberPOJO} from '@/api/types'

interface Props {
  member: MemberPOJO
}

const props = defineProps<Props>()

const memberColor = computed(() => {
  if (!props.member?.color) return 'inherit'
  const color = props.member.color

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
  <div class="inline-flex items-center gap-2">
    <img
        :src="member.profilePictureUrl"
        :alt="member.displayName"
        class="w-6 h-6 rounded-full"
    />
    <span :style="{ color: memberColor }" class="font-medium">
      {{ member.displayName }}
    </span>
  </div>
</template>

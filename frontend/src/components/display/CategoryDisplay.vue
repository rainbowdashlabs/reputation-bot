/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useSession} from '@/composables/useSession.ts'

interface Props {
  categoryId: string
}

const props = defineProps<Props>()
const {session} = useSession()

const category = computed(() => {
  return session.value?.guild?.channels?.categories?.find(c => c.id === props.categoryId)
})
</script>

<template>
  <span v-if="category" class="inline-flex items-center px-2 py-1 rounded text-sm font-medium text-gray-700 dark:text-gray-300">
    📁 {{ category.name }}
  </span>
  <span v-else class="text-gray-500 italic">Unknown Category</span>
</template>

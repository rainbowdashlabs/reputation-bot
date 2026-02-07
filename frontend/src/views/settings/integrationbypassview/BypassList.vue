/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useSession} from '@/composables/useSession'
import BypassEntry from './BypassEntry.vue'
import type {Bypass} from '@/api/types'

const {session} = useSession()

const bypasses = computed(() => {
  return Object.values(session.value?.settings?.integrationBypass?.bypasses || {}) as Bypass[]
})

interface Props {
  expandedBypasses: Set<string>
}

defineProps<Props>()
const emit = defineEmits<{
  (e: 'toggle', integrationId: string): void
}>()
</script>

<template>
  <div class="space-y-4">
    <BypassEntry
        v-for="bypass in bypasses"
        :key="bypass.integrationId"
        :bypass="bypass"
        :expanded="expandedBypasses.has(bypass.integrationId)"
        @toggle="emit('toggle', bypass.integrationId)"
    />
  </div>
</template>

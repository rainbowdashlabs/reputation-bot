/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import type {GuildSessionData} from '@/api/types'

const props = defineProps<{
  guild: GuildSessionData
}>()

defineEmits<{
  (e: 'switch', guildId: string): void
}>()

const iconUrl = computed(() => {
  if (!props.guild.icon) return null
  return `https://cdn.discordapp.com/icons/${props.guild.id}/${props.guild.icon}.png`
})
</script>

<template>
  <button
      @click="$emit('switch', guild.id)"
      class="w-full flex items-center gap-3 px-4 py-3 text-sm text-gray-700 dark:text-gray-200 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 transition-colors group"
  >
    <img
        v-if="iconUrl"
        :src="iconUrl"
        :alt="guild.name"
        class="w-10 h-10 rounded-full border border-gray-100 dark:border-gray-700 shadow-sm group-hover:scale-105 transition-transform"
    />
    <div v-else class="w-10 h-10 rounded-full bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center text-indigo-600 dark:text-indigo-400 font-bold border border-indigo-200 dark:border-indigo-800">
      {{ guild.name.charAt(0) }}
    </div>
    <span class="flex-1 text-left font-medium truncate">{{ guild.name }}</span>
  </button>
</template>

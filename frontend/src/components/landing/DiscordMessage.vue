/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
const props = defineProps<{
  username: string
  avatarColor: string
  message?: string
  isBot?: boolean
  replyTo?: string
  replyText?: string
  reaction?: string
  medal?: boolean
}>()

function discordAvatarUrl(username: string): string {
  let hash = 0
  for (let i = 0; i < username.length; i++) {
    hash = (hash * 31 + username.charCodeAt(i)) >>> 0
  }
  return `https://cdn.discordapp.com/embed/avatars/${hash % 5}.png`
}
</script>

<template>
  <div class="flex flex-col gap-0.5">
    <!-- Reply reference -->
    <div v-if="replyTo" class="flex items-center gap-2 ml-10 text-xs text-gray-400 dark:text-gray-500 mb-0.5">
      <div class="w-4 h-4 border-l-2 border-t-2 border-gray-400 dark:border-gray-500 rounded-tl ml-1 flex-shrink-0"></div>
      <span class="font-medium" :style="{ color: avatarColor }">{{ replyTo }}</span>
      <span class="truncate max-w-[200px]">{{ replyText }}</span>
    </div>

    <!-- Message row -->
    <div class="flex items-start gap-3 group px-2 py-0.5 rounded hover:bg-black/5 dark:hover:bg-white/5">
      <!-- Avatar -->
      <img
        v-if="isBot"
        src="/favicon.ico"
        alt="Bot avatar"
        class="w-8 h-8 rounded-full flex-shrink-0 mt-0.5 object-cover"
      />
      <img
        v-else
        :src="discordAvatarUrl(props.username)"
        :alt="props.username"
        class="w-8 h-8 rounded-full flex-shrink-0 mt-0.5 object-cover"
      />

      <div class="flex-1 min-w-0">
        <!-- Username + BOT badge -->
        <div class="flex items-center gap-2 mb-0.5">
          <span class="font-semibold text-sm" :style="{ color: avatarColor }">{{ username }}</span>
          <span v-if="isBot" class="text-[10px] bg-indigo-500 text-white px-1 py-0.5 rounded font-bold leading-none">BOT</span>
        </div>

        <!-- Message content -->
        <div v-if="message || $slots.default" class="text-sm text-gray-800 dark:text-gray-200 leading-relaxed">
          <slot>{{ message }}</slot>
        </div>

        <!-- Reaction -->
        <div v-if="medal" class="mt-1 inline-flex items-center gap-1 bg-indigo-100 dark:bg-indigo-900/40 border border-indigo-300 dark:border-indigo-700 rounded px-2 py-0.5 text-sm">
          🏅 <span class="text-xs text-indigo-700 dark:text-indigo-300">1</span>
        </div>
      </div>
    </div>
  </div>
</template>

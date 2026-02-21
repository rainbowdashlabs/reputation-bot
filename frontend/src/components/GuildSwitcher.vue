/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <div v-if="guildMeta" class="relative inline-block text-left" v-click-outside="closeSwitcher">
    <button
        @click="toggleSwitcher"
        type="button"
        class="inline-flex items-center gap-3 px-3 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all shadow-sm"
    >
      <div class="flex flex-col items-end">
        <span class="leading-none mb-0.5">{{ guildMeta.name }}</span>
        <span class="text-[10px] text-gray-500 dark:text-gray-400 uppercase tracking-wider font-bold">{{ $t('guildSwitcher.current') }}</span>
      </div>
      <img
          v-if="guildMeta.iconUrl"
          :src="guildMeta.iconUrl"
          :alt="guildMeta.name"
          class="w-8 h-8 rounded-full border border-gray-200 dark:border-gray-700 shadow-sm"
      />
      <font-awesome-icon :icon="['fas', isSwitcherOpen ? 'chevron-up' : 'chevron-down']" class="text-gray-400 w-3"/>
    </button>

    <transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
    >
      <div
          v-if="isSwitcherOpen"
          class="absolute right-0 bottom-full mb-2 w-72 rounded-xl bg-white dark:bg-gray-800 shadow-2xl ring-1 ring-black ring-opacity-5 focus:outline-none z-50 overflow-hidden border border-gray-100 dark:border-gray-700"
      >
        <div class="p-3 border-b border-gray-100 dark:border-gray-700 bg-gray-50/50 dark:bg-gray-800/50">
          <h3 class="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-widest">{{ $t('guildSwitcher.title') }}</h3>
        </div>
        <div class="py-1 max-h-64 overflow-y-auto custom-scrollbar">
          <template v-if="otherSessions.length > 0">
            <GuildSwitcherItem
                v-for="guild in otherSessions"
                :key="guild.id"
                :guild="guild"
                @switch="handleSwitch"
            />
          </template>
          <div v-else class="px-4 py-6 text-center">
            <p class="text-sm text-gray-500 dark:text-gray-400">{{ $t('guildSwitcher.none') }}</p>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useSession} from '@/composables/useSession'
import GuildSwitcherItem from './GuildSwitcherItem.vue'

const {userSession, guildMeta, switchSession} = useSession()

const isSwitcherOpen = ref(false)
const toggleSwitcher = () => isSwitcherOpen.value = !isSwitcherOpen.value
const closeSwitcher = () => isSwitcherOpen.value = false

const otherSessions = computed(() => {
  if (!userSession.value) return []
  const currentGuildId = guildMeta.value?.id
  return Object.values(userSession.value.guilds)
      .filter((guild) => guild.id !== currentGuildId)
})

const handleSwitch = (guildId: string) => {
  switchSession(guildId)
  closeSwitcher()
}

// Custom directive for clicking outside
const vClickOutside = {
  mounted(el: any, binding: any) {
    el.clickOutsideEvent = (event: MouseEvent) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event);
      }
    };
    document.body.addEventListener('click', el.clickOutsideEvent);
  },
  unmounted(el: any) {
    document.body.removeEventListener('click', el.clickOutsideEvent);
  },
};
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #4a5568;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #cbd5e0;
}

.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #718096;
}
</style>

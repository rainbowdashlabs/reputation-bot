/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'

const {t} = useI18n()
const {logout, userSession} = useSession()

const open = ref(false)
const toggle = () => open.value = !open.value
const close = () => open.value = false

const userName = computed(() => userSession.value?.member?.displayName || 'User')
const userAvatar = computed(() => userSession.value?.member?.profilePictureUrl || '/favicon.ico')

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

<template>
  <div class="relative" v-click-outside="close">
    <button @click.stop="toggle" class="user-button flex items-center gap-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
      <img :src="userAvatar" :alt="userName" class="w-7 h-7 rounded-full border border-gray-200 dark:border-gray-700"/>
      <span class="hidden sm:block text-sm font-medium text-gray-700 dark:text-gray-200">{{ userName }}</span>
      <font-awesome-icon :icon="['fas', open ? 'chevron-up' : 'chevron-down']" class="text-gray-400 w-3"/>
    </button>
    <transition
        enter-active-class="transition ease-out duration-100"
        enter-from-class="transform opacity-0 scale-95"
        enter-to-class="transform opacity-100 scale-100"
        leave-active-class="transition ease-in duration-75"
        leave-from-class="transform opacity-100 scale-100"
        leave-to-class="transform opacity-0 scale-95"
    >
      <div v-if="open" class="absolute right-0 mt-2 w-48 rounded-lg shadow-xl bg-white dark:bg-gray-800 ring-1 ring-black ring-opacity-5 overflow-hidden z-50">
        <button @click="logout" class="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-indigo-50 dark:hover:bg-indigo-900/30 flex items-center gap-2">
          <font-awesome-icon :icon="['fas', 'right-from-bracket']" class="w-4"/>
          {{ t('navigation.logout') }}
        </button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.user-button {
  padding: 0 !important;
  border: none !important;
  min-height: 0 !important;
}
</style>

<!--
  -     SPDX-License-Identifier: AGPL-3.0-only
  -
  -     Copyright (C) RainbowDashLabs and Contributor
  -->
<template>
  <transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="transform -translate-y-full opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-full opacity-0"
  >
    <div v-if="isExpired" class="fixed top-4 left-0 right-0 z-[100] flex justify-center px-4 pointer-events-none">
      <div class="bg-red-600 dark:bg-red-700 text-white px-8 py-6 rounded-2xl shadow-2xl flex items-center gap-6 max-w-2xl border-4 border-white dark:border-gray-800 pointer-events-auto ring-8 ring-red-600/20">
        <div class="bg-white/20 p-4 rounded-xl">
          <font-awesome-icon :icon="['fas', 'triangle-exclamation']" class="text-3xl"/>
        </div>
        <div class="flex-1">
          <h3 class="text-xl font-bold mb-1">{{ $t('error.expired.title') }}</h3>
          <p class="text-red-50 opacity-90 leading-relaxed font-medium">
            {{ $t('error.expired.message') }} {{ $t('error.expired.instruction') }}
          </p>
        </div>
      </div>
    </div>
  </transition>
</template>

<script lang="ts" setup>
import {useSession} from '@/composables/useSession'

const {isExpired} = useSession()
</script>

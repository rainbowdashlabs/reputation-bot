/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onMounted} from 'vue'
import {useLinks} from '@/composables/useLinks'

const {links, loadLinks} = useLinks()

onMounted(async () => {
  await loadLinks()
  if (links.value.faq) {
    window.location.replace(links.value.faq)
  }
})
</script>

<template>
  <div class="container mx-auto px-4 py-8 max-w-4xl text-center text-gray-700 dark:text-gray-300">
    <a v-if="links.faq" :href="links.faq" class="hover:text-indigo-600 dark:hover:text-indigo-400">{{ links.faq }}</a>
  </div>
</template>

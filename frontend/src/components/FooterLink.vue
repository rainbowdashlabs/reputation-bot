/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <router-link
      v-if="to"
      :to="to"
      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
  >
    <font-awesome-icon :icon="icon"/>
    {{ label }}
  </router-link>
  <a
      v-else
      :href="href"
      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors inline-flex items-center gap-1"
      rel="noopener noreferrer"
      target="_blank"
  >
    <font-awesome-icon :icon="icon"/>
    {{ label }}
  </a>
</template>

<script lang="ts" setup>
defineProps<{
  to?: string
  href?: string
  icon: string[]
  label: string
}>()
</script>

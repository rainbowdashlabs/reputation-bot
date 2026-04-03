/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import {useRoute} from 'vue-router'
import UserProfile from '@/views/guild/components/UserProfile.vue'

const route = useRoute()
const userId = computed(() => route.params.userId as string | undefined)
</script>

<template>
  <div class="space-y-8 py-8">
    <UserProfile :user-id="userId" />
  </div>
</template>

<style scoped>
</style>

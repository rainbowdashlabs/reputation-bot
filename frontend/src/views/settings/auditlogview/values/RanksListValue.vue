/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import RoleDisplay from '../RoleDisplay.vue'

interface Rank {
  roleId: string
  reputation: number
}

interface Props {
  ranks: Rank[]
}

defineProps<Props>()
const {t} = useI18n()
</script>

<template>
  <template v-if="ranks.length === 0">
    <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
  </template>
  <template v-else>
    <div
        v-for="rank in ranks"
        :key="rank.roleId"
        class="inline-flex items-center gap-2"
    >
      <RoleDisplay :role-id="rank.roleId" />
      <span class="text-gray-600 dark:text-gray-400">→</span>
      <span class="font-medium text-gray-900 dark:text-gray-100">{{ rank.reputation }}</span>
    </div>
  </template>
</template>

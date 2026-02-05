/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import RoleDisplay from '../RoleDisplay.vue'

interface Props {
  roleIds: string[]
}

defineProps<Props>()
const {t} = useI18n()
</script>

<template>
  <template v-if="roleIds.length === 0">
    <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
  </template>
  <template v-else>
    <RoleDisplay
        v-for="roleId in roleIds"
        :key="roleId"
        :role-id="roleId"
    />
  </template>
</template>

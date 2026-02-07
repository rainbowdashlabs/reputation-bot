/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import MemberDisplay from '@/components/display/MemberDisplay.vue'

interface Props {
  integration?: any | null | undefined
  integrationId?: string | number | null | undefined
}

defineProps<Props>()
const {t} = useI18n()
</script>

<template>
  <div v-if="integration" class="flex items-center gap-2">
    <MemberDisplay :member="integration" />
  </div>
  <div v-else-if="integrationId" class="flex items-center gap-2">
    <span class="text-sm font-medium text-gray-500 italic">
      {{ t('auditLog.values.unknownIntegration') }} ({{ integrationId }})
    </span>
  </div>
</template>

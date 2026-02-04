/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import RankManagement from '@/views/settings/ranksview/RankManagement.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()
const { session } = useSession()

const hasRanks = ref(false)

watch(session, (newSession) => {
  if (newSession?.settings?.ranks) {
    hasRanks.value = newSession.settings.ranks.ranks.length > 0
    emit('canProceed', hasRanks.value)
  }
}, { deep: true, immediate: true })
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.ranks.description') }}
    </p>
    
    <RankManagement />
    
    <div v-if="!hasRanks" class="p-4 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg">
      <p class="text-sm text-yellow-800 dark:text-yellow-200">
        {{ t('setup.steps.ranks.required') }}
      </p>
    </div>
  </div>
</template>

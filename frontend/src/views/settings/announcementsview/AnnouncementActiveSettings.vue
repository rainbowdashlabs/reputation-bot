/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { session, updateAnnouncementsSettings } = useSession()

const updateActive = async (active: boolean) => {
  if (!session.value?.settings?.announcements) return
  
  try {
    await api.updateAnnouncementsActive(active);
    updateAnnouncementsSettings({ active });
  } catch (error) {
    console.error('Failed to update announcements active status:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.announcements" class="flex flex-col gap-1">
    <Toggle
      :model-value="session.settings.announcements.active"
      :label="t('announcements.active.label')"
      @update:model-value="updateActive"
    />
    <p class="description">
      {{ t('announcements.active.description') }}
    </p>
  </div>
</template>

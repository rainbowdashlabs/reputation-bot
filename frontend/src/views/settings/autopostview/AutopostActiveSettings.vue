/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import Toggle from '@/components/Toggle.vue'

const {t} = useI18n()
const {session, updateAutopostSettings} = useSession()

const updateActive = async (active: boolean) => {
  if (!session.value?.settings?.autopost) return

  try {
    await api.updateAutopostActive(active);
    updateAutopostSettings({active});
  } catch (error) {
    console.error('Failed to update autopost active status:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.autopost" class="flex flex-col gap-1">
    <Toggle
        :label="t('autopost.active.label')"
        :model-value="session.settings.autopost.active"
        @update:model-value="updateActive"
    />
    <p class="description">
      {{ t('autopost.active.description') }}
    </p>
  </div>
</template>

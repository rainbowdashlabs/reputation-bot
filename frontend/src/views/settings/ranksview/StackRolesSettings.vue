/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import Toggle from '@/components/Toggle.vue'

const { t } = useI18n()
const { session, updateGeneralSettings } = useSession()

const stackRoles = ref(false)

watch(session, (newSession) => {
  if (newSession?.settings?.general) {
    stackRoles.value = newSession.settings.general.stackRoles
  }
}, { immediate: true })

const updateStackRoles = async () => {
  try {
    await api.updateGeneralStackRoles(stackRoles.value)
    updateGeneralSettings({ stackRoles: stackRoles.value })
  } catch (error) {
    console.error('Failed to update stack roles:', error)
  }
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <Toggle
        v-model="stackRoles"
        :label="t('general.ranks.stackRoles.label')"
        @update:model-value="updateStackRoles"
    />
    <p class="description">{{ t('general.ranks.stackRoles.description') }}</p>
  </div>
</template>

<style scoped>
</style>

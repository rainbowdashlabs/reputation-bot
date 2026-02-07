/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'
import AddBypass from './integrationbypassview/AddBypass.vue'
import BypassList from './integrationbypassview/BypassList.vue'

const {t} = useI18n()

const expandedBypasses = ref<Set<string>>(new Set())

const toggleExpand = (id: string) => {
  if (expandedBypasses.value.has(id)) {
    expandedBypasses.value.delete(id)
  } else {
    expandedBypasses.value.add(id)
  }
}

const onBypassAdded = (integrationId: string) => {
  expandedBypasses.value.add(integrationId)
}
</script>

<template>
  <SettingsContainer :description="t('integrationBypass.description')" :title="t('settings.integrationBypass')">
    <div class="space-y-4">
      <AddBypass @added="onBypassAdded" />
      <BypassList :expanded-bypasses="expandedBypasses" @toggle="toggleExpand" />
    </div>
  </SettingsContainer>
</template>

<style scoped>
</style>

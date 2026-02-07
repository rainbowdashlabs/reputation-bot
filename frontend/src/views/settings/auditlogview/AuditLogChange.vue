/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import ListChange from './ListChange.vue'
import SimpleChange from './SimpleChange.vue'

interface Props {
  settingsKey: string
  oldValue: any
  newValue: any
}

const props = defineProps<Props>()

// Check if values are arrays (for list changes)
const isListChange = computed(() => {
  return Array.isArray(props.oldValue) && Array.isArray(props.newValue)
})
</script>

<template>
  <div class="space-y-2">
    <!-- List changes: show added and removed -->
    <ListChange
        v-if="isListChange"
        :settings-key="settingsKey"
        :old-value="oldValue"
        :new-value="newValue"
    />

    <!-- Non-list changes: show old -> new -->
    <SimpleChange
        v-else
        :settings-key="settingsKey"
        :old-value="oldValue"
        :new-value="newValue"
    />
  </div>
</template>

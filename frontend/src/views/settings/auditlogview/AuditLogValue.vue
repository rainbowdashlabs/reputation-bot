/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed} from 'vue'
import SimpleValue from './values/SimpleValue.vue'
import ChannelValue from './values/ChannelValue.vue'
import RolesListValue from './values/RolesListValue.vue'
import RanksListValue from './values/RanksListValue.vue'
import ChannelsListValue from './values/ChannelsListValue.vue'
import CategoriesListValue from './values/CategoriesListValue.vue'
import ReactionsListValue from './values/ReactionsListValue.vue'
import WordsListValue from './values/WordsListValue.vue'

interface Props {
  settingsKey: string
  value: any
  isOldValue?: boolean
}

const props = defineProps<Props>()

// Determine the value type based on the settings key and value
const valueType = computed(() => {
  const key = props.settingsKey.toLowerCase()

  // Check for array types first
  if (Array.isArray(props.value)) {
    // Check for ranks (array of objects with roleId and reputation)
    if (key === 'ranks' && props.value.length > 0 &&
        typeof props.value[0] === 'object' &&
        'roleId' in props.value[0] &&
        'reputation' in props.value[0]) {
      return 'ranks-list'
    }
    if (key.includes('roles')) return 'roles-list'
    if (key.includes('channels') && !key.includes('categories')) return 'channels-list'
    if (key.includes('categories')) return 'categories-list'
    if (key.includes('reaction')) return 'reactions-list'
    if (key.includes('words')) return 'words-list'
    return 'unknown'
  }

  // Check for single channel reference
  if (typeof props.value === 'string' && key.includes('channel') && !key.includes('channels')) {
    return 'channel'
  }

  // Simple values (boolean, number, string, null, undefined)
  if (typeof props.value === 'boolean' ||
      typeof props.value === 'number' ||
      typeof props.value === 'string' ||
      props.value === null ||
      props.value === undefined) {
    return 'simple'
  }

  return 'unknown'
})
</script>

<template>
  <div class="inline-flex items-center gap-1 flex-wrap">
    <!-- Simple values -->
    <SimpleValue
        v-if="valueType === 'simple'"
        :settings-key="settingsKey"
        :value="value"
    />

    <!-- Single channel -->
    <ChannelValue
        v-else-if="valueType === 'channel'"
        :channel-id="value"
    />

    <!-- Roles list -->
    <RolesListValue
        v-else-if="valueType === 'roles-list'"
        :role-ids="value"
    />

    <!-- Ranks list -->
    <RanksListValue
        v-else-if="valueType === 'ranks-list'"
        :ranks="value"
    />

    <!-- Channels list -->
    <ChannelsListValue
        v-else-if="valueType === 'channels-list'"
        :channel-ids="value"
    />

    <!-- Categories list -->
    <CategoriesListValue
        v-else-if="valueType === 'categories-list'"
        :category-ids="value"
    />

    <!-- Reactions list -->
    <ReactionsListValue
        v-else-if="valueType === 'reactions-list'"
        :reaction-ids="value"
    />

    <!-- Words list -->
    <WordsListValue
        v-else-if="valueType === 'words-list'"
        :words="value"
    />

    <!-- Unknown type fallback -->
    <span v-else class="text-gray-500 dark:text-gray-400 italic">
      {{ JSON.stringify(value) }}
    </span>
  </div>
</template>

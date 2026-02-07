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
import IntegrationBypassValue from './values/IntegrationBypassValue.vue'

interface Props {
  settingsKey: string
  value?: any
  oldValue?: any
  newValue?: any
  isOldValue?: boolean
}

const props = defineProps<Props>()

const effectiveValue = computed(() => {
  if (props.value !== undefined) return props.value
  return props.newValue !== undefined ? props.newValue : props.oldValue
})

// Determine the value type based on the settings key and value
const valueType = computed(() => {
  const key = props.settingsKey.toLowerCase()
  const val = effectiveValue.value

  // Check for array types first
  if (Array.isArray(val)) {
    // Check for ranks (array of objects with roleId and reputation)
    if (key === 'ranks' && val.length > 0 &&
        typeof val[0] === 'object' &&
        'roleId' in val[0] &&
        'reputation' in val[0]) {
      return 'ranks-list'
    }
    if (key.includes('roles')) return 'roles-list'
    if (key.includes('channels') && !key.includes('categories')) return 'channels-list'
    if (key.includes('categories')) return 'categories-list'
    if (key.includes('reaction')) return 'reactions-list'
    if (key.includes('words')) return 'words-list'
    return 'unknown'
  }

  // Integration bypass
  if (key.startsWith('integration_bypass.')) {
    return 'integration-bypass'
  }

  // Check for single channel reference
  if (typeof val === 'string' && key.includes('channel') && !key.includes('channels')) {
    return 'channel'
  }

  // Simple values (boolean, number, string, null, undefined)
  if (typeof val === 'boolean' ||
      typeof val === 'number' ||
      typeof val === 'string' ||
      val === null ||
      val === undefined) {
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
        :old-value="oldValue"
        :new-value="newValue"
    />

    <!-- Single channel -->
    <ChannelValue
        v-else-if="valueType === 'channel'"
        :channel-id="effectiveValue"
        :old-channel-id="oldValue"
        :new-channel-id="newValue"
    />

    <!-- Roles list -->
    <RolesListValue
        v-else-if="valueType === 'roles-list'"
        :role-ids="value"
        :old-role-ids="oldValue"
        :new-role-ids="newValue"
    />

    <!-- Ranks list -->
    <RanksListValue
        v-else-if="valueType === 'ranks-list'"
        :ranks="value"
        :old-ranks="oldValue"
        :new-ranks="newValue"
    />

    <!-- Channels list -->
    <ChannelsListValue
        v-else-if="valueType === 'channels-list'"
        :channel-ids="value"
        :old-channel-ids="oldValue"
        :new-channel-ids="newValue"
    />

    <!-- Categories list -->
    <CategoriesListValue
        v-else-if="valueType === 'categories-list'"
        :category-ids="value"
        :old-category-ids="oldValue"
        :new-category-ids="newValue"
    />

    <!-- Reactions list -->
    <ReactionsListValue
        v-else-if="valueType === 'reactions-list'"
        :reaction-ids="value"
        :old-reaction-ids="oldValue"
        :new-reaction-ids="newValue"
    />

    <!-- Words list -->
    <WordsListValue
        v-else-if="valueType === 'words-list'"
        :words="value"
        :old-words="oldValue"
        :new-words="newValue"
    />

    <!-- Integration bypass -->
    <IntegrationBypassValue
        v-else-if="valueType === 'integration-bypass'"
        :value="value"
        :old-value="oldValue"
        :new-value="newValue"
    />

    <!-- Unknown type fallback -->
    <span v-else class="text-gray-500 dark:text-gray-400 italic">
      {{ JSON.stringify(effectiveValue) }}
    </span>
  </div>
</template>

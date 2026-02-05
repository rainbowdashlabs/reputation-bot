/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import ChannelDisplay from '../ChannelDisplay.vue'

interface Props {
  channelIds: string[]
}

defineProps<Props>()
const {t} = useI18n()
</script>

<template>
  <template v-if="channelIds.length === 0">
    <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
  </template>
  <template v-else>
    <ChannelDisplay
        v-for="channelId in channelIds"
        :key="channelId"
        :channel-id="channelId"
    />
  </template>
</template>

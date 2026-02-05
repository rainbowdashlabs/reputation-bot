/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import ChannelDisplay from '../ChannelDisplay.vue'

interface Props {
  channelId: string
}

defineProps<Props>()
</script>

<template>
  <ChannelDisplay :channel-id="channelId" />
</template>

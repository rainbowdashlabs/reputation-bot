/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'

interface Props {
  words: string[]
}

defineProps<Props>()
const {t} = useI18n()
</script>

<template>
  <template v-if="words.length === 0">
    <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
  </template>
  <template v-else>
    <span
        v-for="(word, index) in words"
        :key="index"
        class="inline-flex items-center px-2 py-1 rounded text-sm bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300"
    >
      {{ word }}
    </span>
  </template>
</template>

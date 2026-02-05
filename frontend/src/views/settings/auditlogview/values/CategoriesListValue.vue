/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import CategoryDisplay from '../CategoryDisplay.vue'

interface Props {
  categoryIds: string[]
}

defineProps<Props>()
const {t} = useI18n()
</script>

<template>
  <template v-if="categoryIds.length === 0">
    <span class="text-gray-500 dark:text-gray-400 italic">{{ t('auditLog.values.none') }}</span>
  </template>
  <template v-else>
    <CategoryDisplay
        v-for="categoryId in categoryIds"
        :key="categoryId"
        :category-id="categoryId"
    />
  </template>
</template>

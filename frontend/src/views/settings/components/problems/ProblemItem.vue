/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
defineProps<{
    isWarning?: boolean;
}>();
</script>

<template>
    <div class="flex flex-col sm:flex-row sm:items-center justify-between p-3 rounded-lg border transition-colors"
         :class="isWarning
            ? 'bg-yellow-50 border-yellow-200 dark:bg-yellow-900/20 dark:border-yellow-900/30'
            : 'bg-red-50 border-red-200 dark:bg-red-900/20 dark:border-red-900/30'">
        <div class="flex-grow">
            <div class="text-sm font-medium" :class="isWarning ? 'text-yellow-800 dark:text-yellow-300' : 'text-red-800 dark:text-red-300'">
                <slot name="title" />
            </div>
            <div v-if="$slots.description" class="text-xs mt-1" :class="isWarning ? 'text-yellow-700 dark:text-yellow-400' : 'text-red-700 dark:text-red-400'">
                <slot name="description" />
            </div>
        </div>
        <div v-if="$slots.action" class="mt-2 sm:mt-0 sm:ml-4 flex-shrink-0">
            <slot name="action" />
        </div>
    </div>
</template>

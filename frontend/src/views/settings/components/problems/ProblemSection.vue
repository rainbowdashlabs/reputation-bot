/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
defineProps<{
    title: string;
    description?: string;
    isWarning?: boolean;
}>();
</script>

<template>
    <div class="mb-6 last:mb-0">
        <h3 class="text-lg font-medium mb-2 flex items-center" :class="isWarning ? 'text-yellow-600 dark:text-yellow-500' : 'text-red-600 dark:text-red-500'">
            <font-awesome-icon :icon="isWarning ? 'exclamation-triangle' : 'exclamation-circle'" class="mr-2" />
            {{ title }}
        </h3>
        <p v-if="description" class="text-sm text-gray-600 dark:text-gray-400 mb-3">
            {{ description }}
        </p>
        <div class="space-y-2">
            <slot />
        </div>
    </div>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import type {MissingPermissions} from '@/api/types';
import {PermissionNames} from '@/api/types';
import ProblemSection from './ProblemSection.vue';
import ProblemItem from './ProblemItem.vue';
import ChannelDisplay from '@/components/display/ChannelDisplay.vue';
import CategoryDisplay from '@/components/display/CategoryDisplay.vue';

defineProps<{
    missingPermissions: MissingPermissions[];
}>();

const {t} = useI18n();
</script>

<template>
    <ProblemSection :title="t('settings.debug.missingPermissions.title')">
        <ProblemItem v-for="item in missingPermissions" :key="item.id + item.scope">
            <template #title>
                <div class="flex items-center space-x-2">
                    <span class="text-xs font-semibold px-2 py-0.5 rounded bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300">
                        {{ t(`settings.debug.missingPermissions.${item.scope}`) }}
                    </span>
                    <ChannelDisplay v-if="item.scope === 'CHANNEL'" :channel-id="item.id" />
                    <CategoryDisplay v-else-if="item.scope === 'CATEGORY'" :category-id="item.id" />
                    <span v-else>{{ item.id }}</span>
                </div>
            </template>
            <template #description>
                <div class="flex flex-wrap gap-1 mt-1">
                    <span v-for="perm in item.permissions" :key="perm" class="text-[10px] bg-red-100 dark:bg-red-900/40 text-red-600 dark:text-red-400 px-1.5 py-0.5 rounded border border-red-200 dark:border-red-800">
                        {{ PermissionNames[perm] }}
                    </span>
                </div>
            </template>
        </ProblemItem>
    </ProblemSection>
</template>

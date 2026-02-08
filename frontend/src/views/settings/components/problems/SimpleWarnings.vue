/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import type {SimpleWarning} from '@/api/types';
import ProblemSection from './ProblemSection.vue';
import ProblemItem from './ProblemItem.vue';
import BaseButton from '@/components/BaseButton.vue';

defineProps<{
    warnings: SimpleWarning[];
}>();

const {t} = useI18n();

const getSettingsRoute = (warning: SimpleWarning): { name: string } | string => {
    switch (warning) {
        case 'MAX_MESSAGE_AGE_LOW':
            return { name: 'SettingsAbuseProtection' };
        default:
            return '';
    }
};
</script>

<template>
    <ProblemSection :title="t('settings.debug.simpleWarnings.title')" is-warning>
        <ProblemItem v-for="warning in warnings" :key="warning" is-warning>
            <template #title>{{ t(`settings.debug.simpleWarnings.${warning}`) }}</template>
            <template #action v-if="getSettingsRoute(warning)">
                <router-link :to="getSettingsRoute(warning)">
                    <BaseButton color="secondary" class="px-3 py-1">
                        {{ t('settings.debug.goToSettings') }}
                    </BaseButton>
                </router-link>
            </template>
        </ProblemItem>
    </ProblemSection>
</template>

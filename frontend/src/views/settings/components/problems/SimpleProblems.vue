/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import type {SimpleProblems} from '@/api/types';
import ProblemSection from './ProblemSection.vue';
import ProblemItem from './ProblemItem.vue';
import BaseButton from '@/components/BaseButton.vue';

defineProps<{
    problems: SimpleProblems[];
}>();

const {t} = useI18n();

const getSettingsRoute = (problem: SimpleProblems): { name: string } | string => {
    switch (problem) {
        case 'SYSTEM_CHANNEL_NOT_DEFINED':
        case 'SYSTEM_CHANNEL_NOT_FOUND':
            return { name: 'SettingsGeneral' };
        case 'NO_ANNOUNCEMENT_CHANNEL_DEFINED':
        case 'ANNOUNCEMENT_CHANNEL_NOT_FOUND':
            return { name: 'SettingsAnnouncements' };
        case 'NO_AUTOPOST_CHANNEL_DEFINED':
        case 'AUTOPOST_CANNEL_NOT_FOUND':
            return { name: 'SettingsAutopost' };
        case 'NO_LOG_CHANNEL_DEFINED':
        case 'LOG_CHANNEL_NOT_FOUND':
            return { name: 'SettingsLogChannel' };
        case 'NO_THANKWORDS_DEFINED':
            return { name: 'SettingsThankwords' };
        case 'NO_REPUTATION_CHANNEL_DEFINED':
            return { name: 'SettingsChannels' };
        default:
            return '';
    }
};
</script>

<template>
    <ProblemSection :title="t('settings.debug.simpleProblems.title')">
        <ProblemItem v-for="problem in problems" :key="problem">
            <template #title>{{ t(`settings.debug.simpleProblems.${problem}`) }}</template>
            <template #action v-if="getSettingsRoute(problem)">
                <router-link :to="getSettingsRoute(problem)">
                    <BaseButton color="secondary" class="px-3 py-1">
                       {{ t('settings.debug.goToSettings') }}
                    </BaseButton>
                </router-link>
            </template>
        </ProblemItem>
    </ProblemSection>
</template>

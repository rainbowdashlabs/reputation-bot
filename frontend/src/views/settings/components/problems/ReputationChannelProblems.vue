/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import type {ReputationChannelProblem} from '@/api/types';
import ProblemSection from './ProblemSection.vue';
import ProblemItem from './ProblemItem.vue';
import ChannelDisplay from '@/components/display/ChannelDisplay.vue';
import CategoryDisplay from '@/components/display/CategoryDisplay.vue';
import BaseButton from '@/components/BaseButton.vue';

defineProps<{
    reputationChannelProblems: ReputationChannelProblem[];
}>();

const {t} = useI18n();
</script>

<template>
    <ProblemSection :title="t('settings.debug.reputationChannelProblems.title')">
        <ProblemItem v-for="problem in reputationChannelProblems" :key="problem.id + problem.type">
            <template #title>
                <div class="flex items-center space-x-2">
                    <ChannelDisplay v-if="problem.type !== 'MISSING_CATEGORY'" :channel-id="problem.id" />
                    <CategoryDisplay v-else :category-id="problem.id" />
                </div>
            </template>
            <template #description>{{ t(`settings.debug.reputationChannelProblems.${problem.type}`) }}</template>
            <template #action>
                <router-link :to="{ name: 'SettingsChannels' }">
                    <BaseButton color="secondary" class="px-3 py-1">
                        {{ t('settings.debug.goToSettings') }}
                    </BaseButton>
                </router-link>
            </template>
        </ProblemItem>
    </ProblemSection>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import type {RankProblem} from '@/api/types';
import ProblemSection from './ProblemSection.vue';
import ProblemItem from './ProblemItem.vue';
import RoleDisplay from '@/components/display/RoleDisplay.vue';
import BaseButton from '@/components/BaseButton.vue';

defineProps<{
    rankProblems: RankProblem[];
}>();

const {t} = useI18n();
</script>

<template>
    <ProblemSection :title="t('settings.debug.rankProblems.title')">
        <div v-for="problem in rankProblems" :key="problem.id" class="space-y-1">
            <ProblemItem v-for="type in problem.types" :key="type">
                <template #title>
                    <div class="flex items-center space-x-2">
                        <RoleDisplay :role-id="problem.id" />
                    </div>
                </template>
                <template #description>{{ t(`settings.debug.rankProblems.${type}`) }}</template>
                <template #action>
                    <router-link :to="{ name: 'SettingsRanks' }">
                        <BaseButton color="secondary" class="px-3 py-1">
                            {{ t('settings.debug.goToSettings') }}
                        </BaseButton>
                    </router-link>
                </template>
            </ProblemItem>
        </div>
    </ProblemSection>
</template>

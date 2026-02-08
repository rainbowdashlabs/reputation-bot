/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {onMounted, ref, computed} from 'vue';
import {useI18n} from 'vue-i18n';
import {api} from '@/api';
import type {DebugResultPOJO} from '@/api/types';
import SettingsContainer from './components/SettingsContainer.vue';
import GlobalPermissions from './components/problems/GlobalPermissions.vue';
import SimpleProblems from './components/problems/SimpleProblems.vue';
import MissingPermissions from './components/problems/MissingPermissions.vue';
import RankProblems from './components/problems/RankProblems.vue';
import ReputationChannelProblems from './components/problems/ReputationChannelProblems.vue';
import SimpleWarnings from './components/problems/SimpleWarnings.vue';

const {t} = useI18n();
const debugResult = ref<DebugResultPOJO | null>(null);
const loading = ref(true);

const hasProblems = computed(() => {
    if (!debugResult.value) return false;
    return debugResult.value.missingGlobalPermissions.length > 0 ||
        debugResult.value.simpleProblems.length > 0 ||
        debugResult.value.missingPermissions.length > 0 ||
        debugResult.value.rankProblems.length > 0 ||
        debugResult.value.reputationChannelProblems.length > 0 ||
        debugResult.value.simpleWarnings.length > 0;
});

onMounted(async () => {
    try {
        debugResult.value = await api.getDebug();
    } catch (e) {
        console.error('Failed to fetch debug info', e);
    } finally {
        loading.value = false;
    }
});
</script>

<template>
    <SettingsContainer :title="t('settings.problems')">
        <div v-if="loading" class="text-gray-500 py-4 flex items-center justify-center">
            <font-awesome-icon icon="spinner" spin class="mr-2" />
            {{ t('common.loading') }}
        </div>
        <div v-else-if="debugResult">
            <div v-if="!hasProblems" class="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-900/30 rounded-lg p-6 text-center">
                <font-awesome-icon icon="check-circle" class="text-green-500 text-4xl mb-3" />
                <h3 class="text-lg font-medium text-green-800 dark:text-green-300">No problems found</h3>
                <p class="text-sm text-green-700 dark:text-green-400 mt-1">Everything seems to be configured correctly!</p>
            </div>
            <div v-else class="space-y-8">
                <GlobalPermissions v-if="debugResult.missingGlobalPermissions.length > 0"
                                   :permissions="debugResult.missingGlobalPermissions" />

                <SimpleProblems v-if="debugResult.simpleProblems.length > 0"
                                :problems="debugResult.simpleProblems" />

                <MissingPermissions v-if="debugResult.missingPermissions.length > 0"
                                    :missing-permissions="debugResult.missingPermissions" />

                <RankProblems v-if="debugResult.rankProblems.length > 0"
                              :rank-problems="debugResult.rankProblems" />

                <ReputationChannelProblems v-if="debugResult.reputationChannelProblems.length > 0"
                                           :reputation-channel-problems="debugResult.reputationChannelProblems" />

                <SimpleWarnings v-if="debugResult.simpleWarnings.length > 0"
                                :warnings="debugResult.simpleWarnings" />
            </div>
        </div>
        <div v-else class="text-red-500 py-4 text-center">
            <font-awesome-icon icon="exclamation-triangle" class="mr-2" />
            Failed to load debug information.
        </div>
    </SettingsContainer>
</template>

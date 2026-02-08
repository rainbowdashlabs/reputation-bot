/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {useI18n} from 'vue-i18n';
import type {Permission} from '@/api/types';
import {PermissionNames} from '@/api/types';
import ProblemSection from './ProblemSection.vue';
import ProblemItem from './ProblemItem.vue';

defineProps<{
    permissions: Permission[];
}>();

const {t} = useI18n();
</script>

<template>
    <ProblemSection :title="t('settings.debug.permissions.title')" :description="t('settings.debug.permissions.description')">
        <ProblemItem v-for="permission in permissions" :key="permission">
            <template #title>{{ PermissionNames[permission] }}</template>
            <template #description>{{ t(`settings.debug.permissions.${permission}`) }}</template>
        </ProblemItem>
    </ProblemSection>
</template>

/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <div class="flex flex-col gap-2">
    <FooterLink
        :icon="['fas', 'file-contract']"
        :label="t('navigation.tos')"
        to="/tos"
    />
    <FooterLink
        :icon="['fas', 'shield-halved']"
        :label="t('navigation.privacy')"
        to="/privacy"
    />
    <FooterLink
        :icon="['fas', 'chart-line']"
        :label="t('navigation.metrics')"
        to="/metrics"
    />
  </div>
</template>

<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import FooterLink from '@/components/FooterLink.vue'

const {t} = useI18n()
</script>

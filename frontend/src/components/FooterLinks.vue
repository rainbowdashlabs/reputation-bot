/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<template>
  <div class="flex flex-wrap items-center gap-x-4 gap-y-2 justify-center">
    <FooterLink
        :icon="['fab', 'github']"
        href="https://github.com/RainbowDashLabs/reputation-bot"
        label="GitHub"
    />
    <template v-if="links">
      <template v-if="links.website">
        <span> • </span>
        <FooterLink
            :href="links.website"
            :icon="['fas', 'globe']"
            label="Website"
        />
      </template>
      <template v-if="links.faq">
        <span> • </span>
        <FooterLink
            :icon="['fas', 'question-circle']"
            :label="t('navigation.faq')"
            to="/faq"
        />
      </template>
      <template v-if="links.support">
        <span> • </span>
        <FooterLink
            :href="links.support"
            :icon="['fas', 'life-ring']"
            label="Support"
        />
      </template>
      <template v-if="links.kofi">
        <span> • </span>
        <FooterLink
            :href="links.kofi"
            :icon="['fas', 'mug-hot']"
            label="Ko-fi"
        />
      </template>
    </template>
  </div>
</template>

<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import type {Links} from '@/api/types'
import FooterLink from '@/components/FooterLink.vue'

const {t} = useI18n()

defineProps<{
  links: Links
}>()
</script>

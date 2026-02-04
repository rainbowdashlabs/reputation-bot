/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import DefaultPreset from './presets/DefaultPreset.vue'
import SimpleReputationPreset from './presets/SimpleReputationPreset.vue'
import ThankOncePreset from './presets/ThankOncePreset.vue'
import NoAbusePreset from './presets/NoAbusePreset.vue'

const {t} = useI18n()
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6 text-gray-900 dark:text-white">{{ t('presets.title') }}</h1>
    <p class="text-gray-600 dark:text-gray-400 mb-6">{{ t('presets.description') }}</p>
    <div class="space-y-6">
      <DefaultPreset/>
      <SimpleReputationPreset/>
      <ThankOncePreset/>
      <NoAbusePreset/>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>

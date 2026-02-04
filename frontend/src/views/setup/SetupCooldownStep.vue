/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {watch} from 'vue'
import {useI18n} from 'vue-i18n'
import CooldownSettings from '@/views/settings/abuseprotectionview/CooldownSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const {t} = useI18n()

// Always allow proceeding (cooldown is optional)
watch(() => true, () => {
  emit('canProceed', true)
}, {immediate: true})
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.cooldown.description') }}
    </p>

    <CooldownSettings/>
  </div>
</template>

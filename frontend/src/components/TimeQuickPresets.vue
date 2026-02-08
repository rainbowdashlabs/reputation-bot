/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import BaseButton from '@/components/BaseButton.vue'

const {t} = useI18n()

const props = defineProps<{
  useHours?: boolean
}>()

const model = defineModel<number>({required: true})

const presets = [
  {label: 'timePresets.hour', value: 60},
  {label: 'timePresets.day', value: 1440},
  {label: 'timePresets.week', value: 10080},
  {label: 'timePresets.fourWeeks', value: 40320},
  {label: 'timePresets.year', value: 525600}
]

function setPreset(value: number) {
  model.value = props.useHours ? value / 60 : value
}
</script>

<template>
  <div class="flex flex-col gap-2">
    <div class="flex flex-wrap gap-2">
      <BaseButton
          v-for="preset in presets"
          :key="preset.value"
          class="px-3 py-1"
          color="secondary"
          @click="setPreset(preset.value)"
      >
        {{ t(preset.label) }}
      </BaseButton>
    </div>
  </div>
</template>

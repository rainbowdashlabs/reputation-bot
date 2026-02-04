/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import Toggle from '@/components/Toggle.vue'

interface Props {
  modelValue: boolean;
  label: string;
  description: string;
  disabled?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue']);

const updateValue = (value: boolean) => {
  emit('update:modelValue', value);
};
</script>

<template>
  <div class="flex flex-col gap-1">
    <Toggle
        :disabled="disabled"
        :label="label"
        :model-value="modelValue"
        @update:model-value="updateValue"
    />
    <p :class="{ 'opacity-50': disabled }" class="description ml-0">
      {{ description }}
    </p>
  </div>
</template>

<style scoped>
</style>

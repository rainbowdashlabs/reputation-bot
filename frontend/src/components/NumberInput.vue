/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

const modelValue = defineModel<number | null>({ required: true });

interface Props {
  label?: string;
  min?: number;
  max?: number;
  disabled?: boolean;
  step?: number;
  placeholder?: string;
}

const props = withDefaults(defineProps<Props>(), {
  min: -Infinity,
  max: Infinity,
  disabled: false,
  step: 1,
});

const { t } = useI18n();
const internalValue = ref<string | number | null>(modelValue.value);
const hasError = ref(false);

const validateAndCorrect = () => {
  if (internalValue.value === null || internalValue.value === '') {
    modelValue.value = null;
    hasError.value = false;
    return;
  }

  let val = typeof internalValue.value === 'string' ? parseFloat(internalValue.value) : internalValue.value;

  if (isNaN(val)) {
    val = props.min !== -Infinity ? props.min : 0;
  }

  if (val < props.min) {
    val = props.min;
    hasError.value = true;
  } else if (val > props.max) {
    val = props.max;
    hasError.value = true;
  } else {
    hasError.value = false;
  }

  internalValue.value = val;
  modelValue.value = val;

  if (hasError.value) {
    setTimeout(() => {
      hasError.value = false;
    }, 3000);
  }
};

watch(modelValue, (newVal) => {
  if (newVal !== internalValue.value) {
    internalValue.value = newVal;
  }
});

onMounted(() => {
  internalValue.value = modelValue.value;
});
</script>

<template>
  <div class="flex flex-col gap-1">
    <label v-if="label" class="label">{{ label }}</label>
    <div class="relative">
      <input
        v-model="internalValue"
        type="number"
        :min="min"
        :max="max"
        :step="step"
        :disabled="disabled"
        :placeholder="placeholder"
        class="input"
        :class="{ 'ring-red-500 focus:ring-red-500 ring-2': hasError }"
        @blur="validateAndCorrect"
        @keyup.enter="validateAndCorrect"
      />
      <div
        v-if="hasError"
        class="absolute -bottom-6 left-0 text-xs text-red-500 font-medium transition-opacity duration-300"
      >
        {{ t('common.error.outOfBounds', { min, max }) }}
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Chrome, Safari, Edge, Opera */
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox */
input[type=number] {
  -moz-appearance: textfield;
}
</style>

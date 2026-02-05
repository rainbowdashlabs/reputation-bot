/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
const modelValue = defineModel<boolean>({required: true});

interface Props {
  label?: string;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  disabled: false,
});

const toggle = () => {
  if (!props.disabled) {
    modelValue.value = !modelValue.value;
  }
};
</script>

<template>
  <div class="flex items-center gap-3 cursor-pointer select-none" @click="toggle">

    <div
        :class="[
        modelValue ? 'bg-green-500' : 'bg-red-500',
        disabled ? 'opacity-50 cursor-not-allowed' : ''
      ]"
        class="relative inline-flex h-6 w-12 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-600 focus:ring-offset-2"
    >
      <span
          :class="modelValue ? 'opacity-0' : 'opacity-100'"
          class="absolute inset-0 flex items-center justify-end pr-2 text-xs font-semibold text-white pointer-events-none"
      >
        <font-awesome-icon :icon="['fas', 'xmark']"/>
      </span>
      <span
          :class="modelValue ? 'opacity-100' : 'opacity-0'"
          class="absolute inset-0 flex items-center justify-start pl-2 text-xs font-semibold text-white pointer-events-none"
      >
        <font-awesome-icon :icon="['fas', 'check']"/>
      </span>
      <span
          :class="modelValue ? 'translate-x-6' : 'translate-x-0'"
          aria-hidden="true"
          class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
      />
    </div>

    <span v-if="label" class="label">
      {{ label }}
    </span>

  </div>
</template>

<style scoped>
/* Additional custom styles if needed, though Tailwind covers most */
</style>

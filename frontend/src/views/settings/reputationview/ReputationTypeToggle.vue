<script setup lang="ts">
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
      :model-value="modelValue"
      :label="label"
      :disabled="disabled"
      @update:model-value="updateValue"
    />
    <p class="description ml-0" :class="{ 'opacity-50': disabled }">
      {{ description }}
    </p>
  </div>
</template>

<style scoped>
</style>

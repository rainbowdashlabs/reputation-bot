<script setup lang="ts">
const modelValue = defineModel<boolean>({ required: true });

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
    <span v-if="label" class="text-sm font-medium text-gray-700 dark:text-gray-300">
      {{ label }}
    </span>
    
    <div 
      class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-600 focus:ring-offset-2"
      :class="[
        modelValue ? 'bg-green-500' : 'bg-red-500',
        disabled ? 'opacity-50 cursor-not-allowed' : ''
      ]"
    >
      <span
        aria-hidden="true"
        class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
        :class="modelValue ? 'translate-x-5' : 'translate-x-0'"
      />
    </div>
  </div>
</template>

<style scoped>
/* Additional custom styles if needed, though Tailwind covers most */
</style>

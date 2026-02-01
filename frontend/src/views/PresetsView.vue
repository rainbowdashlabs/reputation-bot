<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">{{ t('navigation.presets') }}</h1>
    <div class="bg-white shadow rounded-lg p-6">
      <p class="text-gray-600">Presets content will be displayed here.</p>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>

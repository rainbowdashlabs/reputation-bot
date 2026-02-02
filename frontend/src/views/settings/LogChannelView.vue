<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import SettingsContainer from './components/SettingsContainer.vue'

const {t} = useI18n()
</script>

<template>
  <SettingsContainer :title="t('settings.logChannel')">
    <p class="text-gray-600 dark:text-gray-400">Log Channel settings content will go here.</p>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

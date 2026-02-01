<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'

const { t } = useI18n()
const { session } = useSession()
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">{{ t('navigation.setup') }}</h1>
    <div class="bg-white shadow rounded-lg p-6">
      <p class="text-gray-600">Setup content will be displayed here.</p>
      <div v-if="session" class="mt-4 p-4 bg-blue-50 border border-blue-200 rounded">
        <p class="text-sm text-blue-800">
          <strong>Session loaded:</strong> Guild has {{ session.guild.roles.length }} roles, 
          {{ session.guild.channels.channels.length }} channels
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional styles if needed */
</style>

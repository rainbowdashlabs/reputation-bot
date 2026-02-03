<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import AnnouncementActiveSettings from '@/views/settings/announcementsview/AnnouncementActiveSettings.vue'
import AnnouncementChannelSettings from '@/views/settings/announcementsview/AnnouncementChannelSettings.vue'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t } = useI18n()
const { session } = useSession()

// Always allow proceeding (announcements are optional)
watch(() => true, () => {
  emit('canProceed', true)
}, { immediate: true })
</script>

<template>
  <div class="space-y-6">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.announcements.description') }}
    </p>
    
    <AnnouncementActiveSettings />
    
    <AnnouncementChannelSettings v-if="session?.settings?.announcements?.active" />
  </div>
</template>

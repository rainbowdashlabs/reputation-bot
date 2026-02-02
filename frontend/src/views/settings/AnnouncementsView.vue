<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import SettingsContainer from './components/SettingsContainer.vue'
import AnnouncementActiveSettings from './announcementsview/AnnouncementActiveSettings.vue'
import AnnouncementChannelSettings from './announcementsview/AnnouncementChannelSettings.vue'

const { t } = useI18n()
const { session } = useSession()
</script>

<template>
  <SettingsContainer :title="t('settings.announcements')" :description="t('announcements.description')">
    <div class="flex flex-col gap-8">
      <AnnouncementActiveSettings />
      
      <AnnouncementChannelSettings v-if="session?.settings?.announcements?.active" />
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

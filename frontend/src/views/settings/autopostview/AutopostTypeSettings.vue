<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import { RefreshType } from '@/api/types'

const { t } = useI18n()
const { session, updateAutopostSettings } = useSession()

const types = Object.values(RefreshType)

const updateType = async (event: Event) => {
  if (!session.value?.settings?.autopost) return
  const type = (event.target as HTMLSelectElement).value as RefreshType
  
  try {
    await api.updateAutopostRefreshType(type);
    updateAutopostSettings({ refreshType: type });
  } catch (error) {
    console.error('Failed to update autopost refresh type:', error)
  }
}
</script>

<template>
  <div v-if="session?.settings?.autopost" class="flex flex-col gap-1">
    <label class="label">{{ t('autopost.refreshType.label') }}</label>
    <select
      :value="session.settings.autopost.refreshType"
      class="select"
      @change="updateType"
    >
      <option v-for="type in types" :key="type" :value="type">
        {{ t(`autopost.refreshType.${type}`) }}
      </option>
    </select>
    <p class="description">
      {{ t('autopost.refreshType.description') }}
    </p>
  </div>
</template>

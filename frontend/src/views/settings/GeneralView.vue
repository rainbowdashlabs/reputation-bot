/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {onMounted, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useSession} from '@/composables/useSession'
import {api} from '@/api'
import type {LanguageInfo} from '@/api/types'
import ChannelSelect from '@/components/ChannelSelect.vue'
import SettingsContainer from './components/SettingsContainer.vue'

const {t} = useI18n()
const {session, updateGeneralSettings} = useSession()

const systemChannel = ref<string | null>(null)
const language = ref<string>('')
const languages = ref<LanguageInfo[]>([])

onMounted(async () => {
  try {
    languages.value = await api.getLanguages()
  } catch (error) {
    console.error('Failed to fetch languages:', error)
  }
})

watch(session, (newSession) => {
  if (newSession?.settings?.general) {
    systemChannel.value = newSession.settings.general.systemChannel || null
    language.value = newSession.settings.general.language || ''
  }
}, {immediate: true})

const updateSystemChannel = async () => {
  const previous = session.value?.settings?.general?.systemChannel || null
  try {
    await api.updateGeneralSystemChannel(systemChannel.value ? systemChannel.value.toString() : '0')
    updateGeneralSettings({systemChannel: systemChannel.value || "0"})
  } catch (error) {
    console.error('Failed to update system channel:', error)
    systemChannel.value = previous
  }
}

const updateLanguage = async () => {
  const previous = session.value?.settings?.general?.language || ''
  try {
    await api.updateGeneralLanguage(language.value)
    updateGeneralSettings({language: language.value})
  } catch (error) {
    console.error('Failed to update language:', error)
    language.value = previous
  }
}
</script>

<template>
  <SettingsContainer :description="t('general.description')" :title="t('settings.general')">
    <div class="grid grid-cols-1 gap-6">
      <div class="flex flex-col gap-1.5">
        <label class="label">{{ t('general.language.label') }}</label>
        <select
            v-model="language"
            class="select"
            @change="updateLanguage"
        >
          <option disabled value="">{{ t('general.language.selectPlaceholder') }}</option>
          <option v-for="lang in languages" :key="lang.internalName" :value="lang.internalName">
            {{ lang.nativeName }} ({{ lang.name }})
          </option>
        </select>
        <p class="description">{{ t('general.language.description') }}</p>
      </div>

      <div class="flex flex-col gap-1.5">
        <ChannelSelect
            v-model="systemChannel"
            :label="t('general.systemChannel.label')"
            @update:model-value="updateSystemChannel"
        />
        <p class="description">{{ t('general.systemChannel.note') }}</p>
      </div>
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

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
import Toggle from '@/components/Toggle.vue'

const {t} = useI18n()
const {session, updateGeneralSettings} = useSession()

const systemChannel = ref<string | null>(null)
const language = ref<string>('')
const everyoneTokenPurchase = ref<boolean>(true)
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
    everyoneTokenPurchase.value = newSession.settings.general.everyoneTokenPurchase
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

const updateEveryoneTokenPurchase = async () => {
  const previous = session.value?.settings?.general?.everyoneTokenPurchase ?? true
  try {
    await api.updateGeneralEveryoneTokenPurchase(everyoneTokenPurchase.value)
    updateGeneralSettings({everyoneTokenPurchase: everyoneTokenPurchase.value})
  } catch (error) {
    console.error('Failed to update everyone token purchase:', error)
    everyoneTokenPurchase.value = previous
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

      <div class="flex flex-col gap-1.5">
        <div class="flex gap-2">

          <Toggle v-model="everyoneTokenPurchase" @update:model-value="updateEveryoneTokenPurchase" />
        <label class="label">{{ t('general.everyoneTokenPurchase.label') }}</label>
        </div>
        <div class="flex items-center gap-2">
        </div>
        <p class="description">{{ t('general.everyoneTokenPurchase.description') }}</p>
      </div>
    </div>
  </SettingsContainer>
</template>

<style scoped>
/* Additional styles if needed */
</style>

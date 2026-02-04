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
import type {SupportedLocale} from '@/i18n'
import {setLocale} from '@/i18n'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const {t} = useI18n()
const {session, updateGeneralSettings} = useSession()

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
    language.value = newSession.settings.general.language || ''
  }
}, {immediate: true})

// Always allow proceeding (language is optional)
watch(language, () => {
  emit('canProceed', true)
}, {immediate: true})

const updateLanguage = async () => {
  try {
    await api.updateGeneralLanguage(language.value)
    updateGeneralSettings({language: language.value})

    // Change frontend language
    const selectedLang = languages.value.find(l => l.internalName === language.value)
    if (selectedLang) {
      // Map DiscordLocale enum names to frontend locale codes
      const localeMap: Record<string, string> = {
        'ENGLISH_US': 'en-US',
        'GERMAN': 'de',
        'SPANISH': 'es-ES',
        'FRENCH': 'fr',
        'PORTUGUESE_BRAZILIAN': 'pt-BR',
        'RUSSIAN': 'ru',
        'UKRAINIAN': 'uk',
        'DUTCH': 'nl',
        'ITALIAN': 'it',
        'GREEK': 'el',
        'TURKISH': 'tr',
        'CHINESE_CHINA': 'zh-CN',
        'CZECH': 'cs',
        'POLISH': 'pl',
        'KOREAN': 'ko',
        'NORWEGIAN': 'no',
        'FINNISH': 'fi',
        'SWEDISH': 'sv-SE',
        'JAPANESE': 'ja'
      }

      const frontendLocale = localeMap[language.value] || 'en-US'
      await setLocale(frontendLocale as SupportedLocale)
    }
  } catch (error) {
    console.error('Failed to update language:', error)
  }
}
</script>

<template>
  <div class="space-y-4">
    <p class="text-gray-600 dark:text-gray-400">
      {{ t('setup.steps.language.description') }}
    </p>

    <div class="flex flex-col gap-1.5">
      <label class="block text-sm font-medium text-gray-900 dark:text-gray-100">
        {{ t('general.language.label') }}
      </label>
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
      <p class="text-sm text-gray-500 dark:text-gray-400">
        {{ t('general.language.description') }}
      </p>
    </div>
  </div>
</template>

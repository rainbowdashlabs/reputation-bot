<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'
import type { LanguageInfo } from '@/api/types'

const emit = defineEmits<{
  canProceed: [value: boolean]
}>()

const { t, locale } = useI18n()
const { session, updateGeneralSettings } = useSession()

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
}, { immediate: true })

// Always allow proceeding (language is optional)
watch(language, () => {
  emit('canProceed', true)
}, { immediate: true })

const updateLanguage = async () => {
  try {
    await api.updateGeneralLanguage(language.value)
    updateGeneralSettings({ language: language.value })
    
    // Change frontend language
    const selectedLang = languages.value.find(l => l.internalName === language.value)
    if (selectedLang) {
      // Map backend language codes to frontend locale codes
      const localeMap: Record<string, string> = {
        'en_US': 'en-US',
        'de_DE': 'de-DE',
        'es_ES': 'es-ES',
        'fr_FR': 'fr-FR',
        'it_IT': 'it-IT',
        'nl_NL': 'nl-NL',
        'pl_PL': 'pl-PL',
        'pt_BR': 'pt-BR',
        'ru_RU': 'ru-RU',
        'tr_TR': 'tr-TR',
        'zh_CN': 'zh-CN',
        'ja_JP': 'ja-JP',
        'ko_KR': 'ko-KR'
      }
      
      const frontendLocale = localeMap[language.value] || 'en-US'
      locale.value = frontendLocale
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
        <option value="" disabled>{{ t('general.language.selectPlaceholder') }}</option>
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

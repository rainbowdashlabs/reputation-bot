/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import type {LanguageInfo, ThankwordsContainer} from '@/api/types'
import {api} from '@/api'
import {useSession} from '@/composables/useSession'

const props = defineProps<{
  isUpdating: boolean
}>()

const emit = defineEmits<{
  (e: 'update:isUpdating', isUpdating: boolean): void
}>()

const {t} = useI18n()
const {session, updateThankingThankwordsSettings} = useSession()

const defaultThankwords = ref<ThankwordsContainer | null>(null)
const languages = ref<LanguageInfo[]>([])

onMounted(async () => {
  try {
    const [words, langs] = await Promise.all([
      api.getThankwords(),
      api.getLanguages()
    ])
    defaultThankwords.value = words
    languages.value = langs
  } catch (error) {
    console.error('Failed to fetch data:', error)
  }
})

const addLanguageDefaults = async (lang: string) => {
  if (!defaultThankwords.value) return

  const defaults = defaultThankwords.value.defaults[lang]
  if (!defaults || defaults.length === 0) return

  const currentWords = session.value?.settings.thanking.thankwords.thankwords || []
  const newList = [...currentWords, ...defaults]

  emit('update:isUpdating', true)
  const previousWords = [...currentWords]
  updateThankingThankwordsSettings({thankwords: newList})

  try {
    await api.updateThankingThankwordsList(newList)
  } catch (error) {
    console.error('Failed to update thankwords:', error)
    updateThankingThankwordsSettings({thankwords: previousWords})
  } finally {
    emit('update:isUpdating', false)
  }
}

const getLanguageName = (internalName: string, languages: LanguageInfo[]) => {
  const lang = languages.find(l => l.internalName === internalName)
  return lang ? lang.nativeName || lang.name : internalName
}
</script>

<template>
  <div v-if="defaultThankwords && Object.keys(defaultThankwords.defaults).length > 0"
       class="pt-6 border-t border-gray-200 dark:border-gray-700">
    <div class="mb-4">
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
        {{ t('thankwords.defaults.title') }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        {{ t('thankwords.defaults.description') }}
      </p>
    </div>

    <div class="flex flex-wrap gap-3">
      <button
          v-for="(words, lang) in defaultThankwords.defaults"
          :key="lang"
          :disabled="isUpdating"
          class="flex items-center gap-2 px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-750 transition-colors disabled:opacity-50 shadow-sm"
          @click="addLanguageDefaults(lang as string)"
      >
        <span class="font-medium">{{ getLanguageName(lang as string, languages) }}</span>
        <span class="text-xs text-gray-500 dark:text-gray-400">({{ words.length }})</span>
      </button>
    </div>
  </div>
</template>

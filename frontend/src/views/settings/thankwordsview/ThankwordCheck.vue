/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import type {ThankwordCheckPOJO} from '@/api/types'

const {t} = useI18n()

const message = ref('')
const result = ref<ThankwordCheckPOJO | null>(null)
const isChecking = ref(false)

const check = async () => {
  const text = message.value.trim()
  if (!text) return

  isChecking.value = true
  try {
    result.value = await api.checkThankwords(text)
  } catch (error) {
    console.error('Failed to check message:', error)
    result.value = null
  } finally {
    isChecking.value = false
  }
}
</script>

<template>
  <div class="pt-6 border-t border-gray-200 dark:border-gray-700">
    <div class="mb-4">
      <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
        {{ t('thankwords.check.title') }}
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        {{ t('thankwords.check.description') }}
      </p>
    </div>

    <textarea
        v-model="message"
        :placeholder="t('thankwords.check.placeholder')"
        class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 dark:text-gray-100"
        rows="3"
    ></textarea>

    <div class="flex justify-end mt-2">
      <button
          :disabled="isChecking || !message.trim()"
          class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          @click="check"
      >
        {{ t('thankwords.check.button') }}
      </button>
    </div>

    <div v-if="result" class="mt-4 p-4 rounded-lg border"
         :class="result.matched
           ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800'
           : 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800'">
      <template v-if="result.matched">
        <p class="text-sm text-green-800 dark:text-green-200">
          {{ t('thankwords.check.match', {match: result.match}) }}
        </p>
        <p v-if="result.thankwords.length > 0" class="text-sm text-green-800 dark:text-green-200 mt-2">
          {{ t('thankwords.check.matchingWords') }}
          <code v-for="word in result.thankwords" :key="word"
                class="ml-1 px-1.5 py-0.5 rounded bg-green-100 dark:bg-green-800/50">{{ word }}</code>
        </p>
      </template>
      <p v-else class="text-sm text-yellow-800 dark:text-yellow-200">
        {{ t('thankwords.check.noMatch') }}
      </p>
    </div>
  </div>
</template>

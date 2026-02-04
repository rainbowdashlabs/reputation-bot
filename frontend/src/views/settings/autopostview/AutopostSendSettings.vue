/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { api } from '@/api'
import { ref } from 'vue'

const { t } = useI18n()
const isSending = ref(false)

const sendAutopost = async () => {
  isSending.value = true
  try {
    await api.sendAutopost()
  } catch (error) {
    console.error('Failed to send autopost:', error)
  } finally {
    isSending.value = false
  }
}
</script>

<template>
  <div class="flex flex-col gap-1">
    <button
      @click="sendAutopost"
      :disabled="isSending"
      class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
    >
      {{ isSending ? t('autopost.send.sending') : t('autopost.send.label') }}
    </button>
    <p class="description">
      {{ t('autopost.send.description') }}
    </p>
  </div>
</template>

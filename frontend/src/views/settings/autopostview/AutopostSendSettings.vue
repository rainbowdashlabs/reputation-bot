/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import {ref} from 'vue'
import BaseButton from "@/components/BaseButton.vue";

const {t} = useI18n()
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
    <BaseButton
        :disabled="isSending"
        class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        @click="sendAutopost"
    >
      {{ isSending ? t('autopost.send.sending') : t('autopost.send.label') }}
    </BaseButton>
    <p class="description">
      {{ t('autopost.send.description') }}
    </p>
  </div>
</template>

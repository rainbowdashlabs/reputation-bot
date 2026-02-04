/*
*     SPDX-License-Identifier: AGPL-3.0-only
*
*     Copyright (C) RainbowDashLabs and Contributor
*/
<script lang="ts" setup>
import {ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import {useSession} from '@/composables/useSession'
import EmojiPicker from './EmojiPicker.vue'

const props = defineProps<{
  initialMainReaction: string
}>()

const {t} = useI18n()
const {updateThankingReactionsSettings} = useSession()

const mainReaction = ref(props.initialMainReaction)
const isUpdating = ref(false)

watch(() => props.initialMainReaction, (newVal) => {
  mainReaction.value = newVal
})

const updateMainReaction = async (newEmoji: string) => {
  if (isUpdating.value || newEmoji === props.initialMainReaction) return

  isUpdating.value = true
  try {
    await api.updateThankingReactionsMain(newEmoji)
    updateThankingReactionsSettings({mainReaction: newEmoji})
    mainReaction.value = newEmoji
  } catch (error) {
    console.error('Failed to update main reaction:', error)
    // Revert on error
    mainReaction.value = props.initialMainReaction
  } finally {
    isUpdating.value = false
  }
}
</script>

<template>
  <div class="mb-8">
    <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
      {{ t('reactions.main.label') }}
    </h3>
    <div class="flex items-start gap-4">
      <EmojiPicker
          v-model="mainReaction"
          :disabled="isUpdating"
          @update:modelValue="updateMainReaction"
      />
      <div>
        <p class="text-sm text-gray-500 dark:text-gray-400">
          {{ t('reactions.main.description') }}
        </p>
      </div>
    </div>
  </div>
</template>

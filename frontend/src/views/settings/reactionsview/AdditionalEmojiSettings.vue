/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {api} from '@/api'
import {useSession} from '@/composables/useSession'
import EmojiPicker from './EmojiPicker.vue'
import PremiumFeatureWarning from '@/components/PremiumFeatureWarning.vue'

const props = defineProps<{
  initialReactions: string[]
}>()

const {t} = useI18n()
const {session, updateThankingReactionsSettings} = useSession()

const reactions = ref([...props.initialReactions])
const isUpdating = ref(false)
const newEmoji = ref('')

const isPremium = computed(() => session.value?.premiumFeatures.additionalEmojis.unlocked || false)
const requiredSkus = computed(() => session.value?.premiumFeatures.additionalEmojis.requiredSkus || [])
const guildEmojis = computed(() => session.value?.guild.reactions || [])

watch(() => props.initialReactions, (newVal) => {
  reactions.value = [...newVal]
})

const updateReactions = async (newList: string[]) => {
  if (!isPremium.value || isUpdating.value) return

  isUpdating.value = true
  try {
    await api.updateThankingReactionsList(newList)
    updateThankingReactionsSettings({reactions: newList})
    reactions.value = [...newList]
  } catch (error) {
    console.error('Failed to update additional reactions:', error)
    // Revert on error
    reactions.value = [...props.initialReactions]
  } finally {
    isUpdating.value = false
  }
}

const addReaction = (emoji: string) => {
  if (!emoji || reactions.value.includes(emoji)) return
  const newList = [...reactions.value, emoji]
  updateReactions(newList)
  newEmoji.value = ''
}

const removeReaction = (index: number) => {
  const newList = reactions.value.filter((_, i) => i !== index)
  updateReactions(newList)
}

const getEmojiDisplay = (val: string) => {
  const guildEmoji = guildEmojis.value.find(e => e.id.toString() === val)
  if (guildEmoji) return guildEmoji.url
  return val
}

const isGuildEmoji = (val: string) => {
  return guildEmojis.value.some(e => e.id.toString() === val)
}
</script>

<template>
  <div>
    <h3 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
      {{ t('reactions.additional.label') }}
    </h3>
    <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">
      {{ t('reactions.additional.description') }}
    </p>

    <div v-if="!isPremium" class="mb-4">
      <PremiumFeatureWarning
          :feature-name="t('reactions.additional.premiumRequired')"
          :requiredSkus="requiredSkus"
          variant="small"
      />
    </div>

    <div :class="{'opacity-50 pointer-events-none': !isPremium}" class="space-y-4">
      <div v-if="reactions.length > 0"
           class="flex flex-wrap gap-2 p-3 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
        <div
            v-for="(emoji, index) in reactions"
            :key="index"
            class="relative group"
        >
          <div
              class="w-12 h-12 flex items-center justify-center bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded">
            <img v-if="isGuildEmoji(emoji)" :src="getEmojiDisplay(emoji)" alt="emoji" class="w-10 h-10 object-contain"/>
            <span v-else class="text-3xl">{{ emoji }}</span>
          </div>
          <button
              class="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs opacity-0 group-hover:opacity-100 transition-opacity shadow-sm"
              type="button"
              @click="removeReaction(index)"
          >
            &times;
          </button>
        </div>
      </div>
      <div v-else
           class="p-4 text-center text-gray-500 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700 italic">
        {{ t('reactions.additional.none') }}
      </div>

      <div class="flex items-center gap-2">
        <EmojiPicker
            v-model="newEmoji"
            :disabled="!isPremium || isUpdating"
            @update:modelValue="addReaction"
        />
        <span class="text-sm text-gray-500 dark:text-gray-400">
          {{ t('reactions.additional.add') }}
        </span>
      </div>
    </div>
  </div>
</template>

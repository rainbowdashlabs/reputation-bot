<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useSession } from '@/composables/useSession'
import { api } from '@/api'

const props = defineProps<{
  isUpdating: boolean
}>()

const emit = defineEmits<{
  (e: 'update:isUpdating', isUpdating: boolean): void
}>()

const { t } = useI18n()
const { session, updateThankingThankwordsSettings } = useSession()

const newWord = ref('')
const errorMessage = ref('')

const currentWords = computed(() => session.value?.settings.thanking.thankwords.thankwords || [])
const invalidCharacters = ["(", ")", "{", "}", "*", "\\s", " ", ".", "#", "<", ">"]

const addWord = async () => {
  const word = newWord.value.trim()
  if (!word) return

  for (const char of invalidCharacters) {
    if (word.includes(char)) {
      errorMessage.value = t('thankwords.invalidCharacter', { char: char })
      return
    }
  }

  if (currentWords.value.includes(word)) {
    errorMessage.value = t('thankwords.alreadyExists')
    return
  }

  errorMessage.value = ''
  const previousWords = [...currentWords.value]
  const newList = [...currentWords.value, word]
  
  emit('update:isUpdating', true)
  updateThankingThankwordsSettings({ thankwords: newList })
  newWord.value = ''

  try {
    await api.updateThankingThankwordsList(newList)
  } catch (error) {
    console.error('Failed to update thankwords:', error)
    updateThankingThankwordsSettings({ thankwords: previousWords })
    newWord.value = word
  } finally {
    emit('update:isUpdating', false)
  }
}
</script>

<template>
  <div>
    <div class="flex gap-2 mb-4">
      <div class="relative flex-grow">
        <input
            v-model="newWord"
            type="text"
            :placeholder="t('thankwords.placeholder')"
            class="w-full px-4 py-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 dark:text-gray-100"
            @keyup.enter="addWord"
        />
      </div>
      <button
          @click="addWord"
          :disabled="isUpdating || !newWord.trim()"
          class="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md shadow-sm disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      >
        {{ t('thankwords.add') }}
      </button>
    </div>
    <p v-if="errorMessage" class="text-sm text-red-600 dark:text-red-400 mb-2">
      {{ errorMessage }}
    </p>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
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

const words = computed(() => session.value?.settings.thanking.thankwords.thankwords || [])

const removeWord = async (word: string) => {
  const newList = words.value.filter(w => w !== word)
  
  emit('update:isUpdating', true)
  const previousWords = [...words.value]
  updateThankingThankwordsSettings({ thankwords: newList })
  
  try {
    await api.updateThankingThankwordsList(newList)
  } catch (error) {
    console.error('Failed to update thankwords:', error)
    updateThankingThankwordsSettings({ thankwords: previousWords })
  } finally {
    emit('update:isUpdating', false)
  }
}
</script>

<template>
  <div v-if="words.length > 0" class="flex flex-wrap gap-1.5 p-3 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
    <div
        v-for="word in words"
        :key="word"
        class="flex items-center gap-1.5 px-2.5 py-0.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-full group shadow-sm"
    >
      <code class="text-xs font-medium text-indigo-600 dark:text-indigo-400">{{ word }}</code>
      <button
          @click="removeWord(word)"
          :disabled="isUpdating"
          class="text-gray-400 hover:text-red-500 transition-colors disabled:opacity-50 text-base leading-none"
          :title="t('common.delete')"
      >
        &times;
      </button>
    </div>
  </div>
  <div v-else class="p-8 text-center text-gray-500 bg-gray-50 dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700 italic">
    {{ t('thankwords.none') }}
  </div>
</template>
